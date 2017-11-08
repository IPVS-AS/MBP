package org.citopt.connde.conndeapp;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.citopt.connde.conndeapp.advertise.Const;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;

/**
 * Created by rosso on 25.09.17.
 */

public class MqttService extends Service {
  private static final String TAG = "MqttService";
  public static final String HOST_EXTRA = "host";
  public static final String ID_EXTRA = "CONNDE_ID";
  public static final String CLIENT_ID_EXTRA = "CLIENT_ID";

  public static final String SENSOR_TOPIC = "sensor";
  public static final String ACTUATOR_TOPIC = "actuator";

  private MqttClient myClient;

  private SensorManager mySensorManager;

  private SensorEventListener accelerometerListener;

  private String conndeId;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);

    String idForConnde = intent.getStringExtra(ID_EXTRA);
    String host = intent.getStringExtra(HOST_EXTRA);
    String clientId = intent.getStringExtra(CLIENT_ID_EXTRA);

    Objects.requireNonNull(idForConnde, "Connde id may not be null");
    Objects.requireNonNull(host, "host may not be null");
    Objects.requireNonNull(clientId, "client id may not be null");

    Log.i(TAG, "Starting MQTT Service for host |" + host + "| and id |" + idForConnde + "|");

    this.conndeId = idForConnde;

    accelerometerListener = new SensorEventListener() {
      @Override
      public void onSensorChanged(SensorEvent sensorEvent) {
        String message = String.format(Locale.getDefault(), "X:%f,Y:%f,Z:%f", sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
        Log.d(TAG, "accelerometer says: " + message);
        JSONObject jsonMessage = new JSONObject();
        try {
          jsonMessage.put(Const.MQTT_COMPONENT, Const.CONNDE_SENSOR_CATEGORY);
          jsonMessage.put(Const.MQTT_ID, conndeId);
          jsonMessage.put(Const.MQTT_VALUE, message);
        } catch (JSONException e) {
          e.printStackTrace();
        }
        if(myClient!=null && myClient.isConnected()){
          try {
            String topic = SENSOR_TOPIC + "/" + conndeId;
            Log.d(TAG, "publishing on topic |" + topic + "|");
            myClient.publish(topic, new MqttMessage(jsonMessage.toString().getBytes()));
          } catch (MqttException e) {
            e.printStackTrace();
          }
        }
      }

      @Override
      public void onAccuracyChanged(Sensor sensor, int i) {

      }
    };


    try {
      myClient = new MqttClient(host, MqttClient.generateClientId(), null);
      MqttConnectOptions connectOptions = new MqttConnectOptions();
      connectOptions.setCleanSession(true);
      myClient.connect(connectOptions);
      myClient.publish("test", new MqttMessage(("Hello World from |" + clientId + "|").getBytes()));

      mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      Sensor mSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
      if(mSensor!= null){
        mySensorManager.registerListener(accelerometerListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
      }
    } catch (MqttException e) {
      e.printStackTrace();
    }

    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if(myClient != null){
      try {
        myClient.disconnect();
      } catch (MqttException e) {
        e.printStackTrace();
      }
      myClient=null;
      mySensorManager.unregisterListener(accelerometerListener);
    }
    Log.i(TAG, "Stopped MQTT Service");
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return new MqttBinder(this);
  }

  public static class MqttBinder extends Binder{
    private MqttService instance;

    public MqttBinder(MqttService instance) {
      this.instance = instance;
    }

    public MqttService getService() {
      return instance;
    }
  }
}
