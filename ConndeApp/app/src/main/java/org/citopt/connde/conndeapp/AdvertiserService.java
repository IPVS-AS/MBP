package org.citopt.connde.conndeapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.citopt.connde.conndeapp.advertise.AdvertiseDevice;
import org.citopt.connde.conndeapp.advertise.AdvertiseService;
import org.citopt.connde.conndeapp.advertise.DiscoveredServer;
import org.citopt.connde.conndeapp.advertise.InetHelper;
import org.citopt.connde.conndeapp.advertise.ServerDiscoveredListener;
import org.json.JSONException;

import java.util.Map;

/**
 * Created by rosso on 23.09.17.
 */

public class AdvertiserService extends Service {
  private static final String TAG = "AdvService";
  private static final String NOT_CHANNEL = "advertiser_channel_status";
  private static final long[] VIBRATION_PATTERN = new long[]{100, 200, 100, 200};
  private static int notificationCount = 0;
  private static final int NOTIFICATION_ID = 1;

  private AdvertiseService advertiseService = null;

  private boolean started= false;

  private String curSsid;
  private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      wifiConnectionChanged(context, intent);
    }
  };

  private ServerDiscoveredListener serverDiscoveredListener = new ServerDiscoveredListener() {
    @Override
    public void onServerDiscovered(ServerDiscoveredEvent event) {
      if(event.getDiscoveredServer() != null){
        DiscoveredServer discoveredServer = event.getDiscoveredServer();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(AdvertiserService.this);
        builder.setSmallIcon(android.R.drawable.presence_online);
        builder.setContentTitle("AdvertiserService");
        builder.setContentText("Service connected to " + InetHelper.getStringFor(discoveredServer.getServerAddress()));
        builder.setVibrate(VIBRATION_PATTERN);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
          builder.setChannel(NOT_CHANNEL);
        }
        startForeground(NOTIFICATION_ID, builder.build());

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(mSensor != null){
          String sensorName = mSensor.getName();
          AdvertiseDevice sensorDevice = advertiseService.getDevices().get(sensorName);
          if(sensorDevice.getConndeId() != null && !sensorDevice.getConndeId().isEmpty()){
            Log.i(TAG, "Initializing MQTT Service for Sensor |" + sensorName + "|");
            Intent mqttIntent = new Intent(AdvertiserService.this, MqttService.class);
            String hostUri = "tcp://" + InetHelper.getStringFor(discoveredServer.getServerAddress()) + ":1883"; // hardcode hack
            mqttIntent.putExtra(MqttService.HOST_EXTRA, hostUri);
            mqttIntent.putExtra(MqttService.CLIENT_ID_EXTRA, Build.DEVICE);
            mqttIntent.putExtra(MqttService.ID_EXTRA, sensorDevice.getConndeId());
            startService(mqttIntent);
          }
        }
      }
    }
  };

  public AdvertiseDevice getHost() {
    return advertiseService != null ? advertiseService.getHost() : null;
  }

  public Map<String, AdvertiseDevice> getDevices() {
    return advertiseService != null ? advertiseService.getDevices() : null;
  }

  public boolean isStarted() {
    return started;
  }

  public void wifiConnectionChanged(Context context, Intent intent) {
    final String action = intent.getAction();
    if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
      NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
      if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String ssid = wifiManager.getConnectionInfo().getSSID();
        if (ssid != null && !ssid.equals(curSsid)) {
          Log.i(TAG, "Connected to |" + ssid + "|");
          curSsid = ssid;
          startAdvertising();
        }
      } else {
        Log.d(TAG, "No Connection");
        curSsid = null;
        stopAdvertising();
      }
    }
  }

  private void startAdvertising() {
    if (advertiseService != null) {
      return; // nothing to do
    }
    try {
      advertiseService = new AdvertiseService(getFilesDir());
      advertiseService.addDiscoveredListener(serverDiscoveredListener);
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            advertiseService.start("android");
          } catch (JSONException e) {
            Log.e(TAG, "Error starting advertising service", e);
          }
        }
      }).start();
    } catch (JSONException e) {
      Log.e(TAG, "Error constructing advertising service", e);
    }
  }

  private void stopAdvertising() {
    if (advertiseService == null) {
      return; // nothing to do
    }
    try {
      advertiseService.stop();
      advertiseService = null;
      Log.i(TAG, "Stopped advertise service");
      NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
      builder.setSmallIcon(android.R.drawable.presence_busy);
      builder.setContentTitle("AdvertiserService");
      builder.setContentText("Service disconnected");
      builder.setVibrate(VIBRATION_PATTERN);
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        builder.setChannel(NOT_CHANNEL);
      }
      startForeground(NOTIFICATION_ID, builder.build());
    } catch (JSONException e) {
      Log.e(TAG, "Error stopping advertise service");
    }
    stopService(new Intent(this, MqttService.class));
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);

    if(!started) {
      Log.i(TAG, "Starting advertise service");
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        CharSequence name = getString(R.string.msg_advertiserChannelStatusName);
        String description = getString(R.string.msg_advertiserChannelStatusDesc);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel mChannel = new NotificationChannel(NOT_CHANNEL, name, importance);
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(VIBRATION_PATTERN);
        mNotificationManager.createNotificationChannel(mChannel);
      }

      Log.i(TAG, "Registering Broadcast receiver");
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
      registerReceiver(wifiStateReceiver, intentFilter);

      Log.i(TAG, "Started advertise service");
      NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
      builder.setSmallIcon(android.R.drawable.presence_away);
      builder.setContentTitle("AdvertiserService");
      builder.setContentText("Service running");
      builder.setVibrate(VIBRATION_PATTERN);
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        builder.setChannel(NOT_CHANNEL);
      }
      startForeground(NOTIFICATION_ID, builder.build());
    }
    started= true;
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.i(TAG, "Stopping advertise service");
    stopAdvertising();
    if(started) {
      unregisterReceiver(wifiStateReceiver);
    }
    stopForeground(true);

  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return new AdvertiseBinder(this);
  }

  public static class AdvertiseBinder extends Binder {
    private AdvertiserService instance;

    public AdvertiseBinder(AdvertiserService instance) {
      this.instance = instance;
    }

    public AdvertiserService getService() {
      return instance;
    }
  }

}
