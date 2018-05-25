package org.citopt.connde.conndeapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.citopt.connde.conndeapp.advertise.AdvertiseDevice;
import org.citopt.connde.conndeapp.advertise.Const;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

import static org.citopt.connde.conndeapp.helper.RMPHelper.getStringType;
import static org.citopt.connde.conndeapp.helper.RMPHelper.readJSONFile;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "ConndeMain";
  private SensorManager mSensorManager;

  private AdvertiserService advertiserService;

  private ServiceConnection advertiseConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
      advertiserService = ((AdvertiserService.AdvertiseBinder) iBinder).getService();
      Log.i(TAG, "Connected to running AdvertiserService |" + advertiserService.isStarted() + "|");
      String buttonText = isAdvertising() ? getString(R.string.btn_stopAdvertising) : getString(R.string.btn_startAdvertising);
      Button toggleButton = (Button) findViewById(R.id.btnToggleAdvertise);
      toggleButton.setText(buttonText);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
      advertiserService = null;
      Log.i(TAG, "Disconnected from AdvertiserService");
    }
  };


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    try {
      ensureAutodeployConf();
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    bindService(new Intent(this, AdvertiserService.class), advertiseConnection, Context.BIND_AUTO_CREATE);
  }

  @Override
  protected void onPause() {
    super.onPause();
    unbindFromService();
  }

  private void unbindFromService(){
    if(advertiserService!=null) {
      unbindService(advertiseConnection);
      advertiserService = null;
    }
  }

  private boolean isAdvertising(){
    return advertiserService!=null&&advertiserService.isStarted();
  }

  private void ensureAutodeployConf() throws JSONException {
    File filesDir = getApplicationContext().getFilesDir();

    File autodeployFile = new File(filesDir, Const.AUTODEPLOY_FILE);
    if (!autodeployFile.exists()) {
      Log.i(TAG, "Generating autodeploy file");
      mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

      JSONObject autodeployConf = new JSONObject();

      JSONObject host = new JSONObject();
      host.put(Const.LOCAL_ID, Build.DEVICE);
      host.put(Const.TYPE, Build.MODEL);

      JSONObject hostAdapterConf = new JSONObject();
      hostAdapterConf.put(Const.TIMEOUT, 15); // default 30 seconds
      host.put(Const.ADAPTER_CONF, hostAdapterConf);

      autodeployConf.put(Const.DEPLOY_SELF, host);

      JSONArray deployDevices = new JSONArray();
      for (Sensor sensor : sensors) {
        JSONObject jsonSensor = new JSONObject();
        jsonSensor.put(Const.LOCAL_ID, sensor.getName());
        jsonSensor.put(Const.TYPE, getStringType(sensor.getType()));

        JSONObject adapterConf = new JSONObject();
        adapterConf.put(Const.TIMEOUT, 30); // default 30 seconds
        jsonSensor.put(Const.ADAPTER_CONF, adapterConf);
        deployDevices.put(jsonSensor);
      }

      autodeployConf.put(Const.DEPLOY_DEVICES, deployDevices);

      String jsonString = autodeployConf.toString(2);
      try (OutputStream os = new FileOutputStream(autodeployFile)) {
        os.write(jsonString.getBytes(Charset.forName("UTF-8")));
        os.flush();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      Log.i(TAG, "Successfully generated autodeploy file |\n" + autodeployConf.toString(4) + "\n|");
    } else {
      Log.w(TAG, "Autodeploy file exists");
    }
  }

  public void toggleAdvertising(View view) {
    if (isAdvertising()) {
      unbindFromService();
      stopService(new Intent(this, AdvertiserService.class));
      ((Button) view).setText(getString(R.string.btn_startAdvertising));
    } else {
      startService(new Intent(this, AdvertiserService.class));
      bindService(new Intent(this, AdvertiserService.class), advertiseConnection, Context.BIND_AUTO_CREATE);
      ((Button) view).setText(getString(R.string.btn_stopAdvertising));
    }
  }

  public void showSensorOverview(View view) {
    StringBuilder overview = new StringBuilder();
    if (isAdvertising()) {
      AdvertiseDevice host = advertiserService.getHost();
      Collection<AdvertiseDevice> devices = advertiserService.getDevices().values();

      if (host != null && host.isConnected()) {
        overview.append("Connected to server\n");
        overview.append("LOCAL_ID | TYPE | GLOBAL_ID | HOST | CONNECTED");
        overview.append(host.toString());
        overview.append("\n");
        for (AdvertiseDevice curDevice : devices) {
          overview.append(curDevice.toString());
          overview.append("\n");
        }
      } else {
        overview.append("Not connected");
      }
    } else {
      overview.append("Not connected");
    }

    TextView lblSensorlist = (TextView) findViewById(R.id.lblSensorlist);
    lblSensorlist.setText(overview.toString());
  }

  public void showDeployConf(View view) {
    TextView lblSensorlist = (TextView) findViewById(R.id.lblSensorlist);
    lblSensorlist.setText(getString(R.string.msg_model, Build.MODEL));

    String appendText = "\n\n";

    File filesDir = getApplicationContext().getFilesDir();
    File autodeployFile = new File(filesDir, Const.AUTODEPLOY_FILE);
    JSONObject deployConf = readJSONFile(autodeployFile);

    if (deployConf != null) {
      try {
        appendText += deployConf.toString(4);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    File globalIdFile = new File(filesDir, Const.GLOBAL_ID_FILE);
    JSONObject globalIds = readJSONFile(globalIdFile);

    if (globalIds != null) {
      try {
        appendText += "\n-------------------------------------------------\n";
        appendText += globalIds.toString(4);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    lblSensorlist.append(appendText);


//    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//    List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
//    String strSensors = "\n";
//    for (Sensor sensor : sensors) {
//      strSensors += sensor.getType() + "\n";
//    }
//
//    lblSensorlist.append("n" + strSensors);
//        String sb = "Your manufacturer is:\t\t" + Build.MANUFACTURER + "\n" +
//                "Your model is: \t\t" + Build.MODEL + "\n" +
//                "Your device is: \t\t" + Build.DEVICE + "\n" +
//                "Your brand is: \t\t" + Build.BRAND + "\n" +
//                "Your fingerprint is: \t\t" + Build.FINGERPRINT + "\n" +
//                "Your id is: \t\t" + Build.ID + "\n" +
//                "Your type is: \t\t" + Build.TYPE + "\n" +
//                "Your product is: \t\t" + Build.PRODUCT + "\n" +
//                "Your host is: \t\t" + Build.HOST + "\n" +
//                "Your user is: \t\t" + Build.USER + "\n";
//        lblSensorlist.setText(sb);
  }
}
