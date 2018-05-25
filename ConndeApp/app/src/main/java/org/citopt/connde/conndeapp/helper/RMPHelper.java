package org.citopt.connde.conndeapp.helper;

import android.hardware.Sensor;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by rosso on 25.09.17.
 */

public class RMPHelper {
  private static final String TAG = "RMPHelper";

  public static JSONObject readJSONFile(File file) {
    JSONObject readObject = null;
    if (file.exists()) {
      try (InputStream is = new FileInputStream(file)) {
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        String json = new String(buffer, "UTF-8");
        try {
          readObject = new JSONObject(json);
        } catch (JSONException e) {
          Log.e(TAG, "Could not parse JSON from file |" + file.getName() + "|");
        }
      } catch (FileNotFoundException e) {
        Log.e(TAG, "Could not find file |" + file.getName() + "|", e);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return readObject;
  }

  public static String getStringType(int sensorType) {
    switch (sensorType) {
      case Sensor.TYPE_ACCELEROMETER:
        return Sensor.STRING_TYPE_ACCELEROMETER;
      case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          return Sensor.STRING_TYPE_ACCELEROMETER_UNCALIBRATED;
        } else {
          return "android.sensor.accelerometer_uncalibrated";
        }
      case Sensor.TYPE_AMBIENT_TEMPERATURE:
        return Sensor.STRING_TYPE_AMBIENT_TEMPERATURE;
      case Sensor.TYPE_DEVICE_PRIVATE_BASE:
        return "Unknown Sensor type |Device_Private_Base|";
      case Sensor.TYPE_GAME_ROTATION_VECTOR:
        return Sensor.STRING_TYPE_GAME_ROTATION_VECTOR;
      case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
        return Sensor.STRING_TYPE_GEOMAGNETIC_ROTATION_VECTOR;
      case Sensor.TYPE_GRAVITY:
        return Sensor.STRING_TYPE_GRAVITY;
      case Sensor.TYPE_GYROSCOPE:
        return Sensor.STRING_TYPE_GYROSCOPE;
      case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
        return Sensor.STRING_TYPE_GYROSCOPE_UNCALIBRATED;
      case Sensor.TYPE_HEART_BEAT:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          return Sensor.STRING_TYPE_HEART_BEAT;
        } else {
          return "android.sensor.heart_beat";
        }
      case Sensor.TYPE_HEART_RATE:
        return Sensor.STRING_TYPE_HEART_RATE;
      case Sensor.TYPE_LIGHT:
        return Sensor.STRING_TYPE_LIGHT;
      case Sensor.TYPE_LINEAR_ACCELERATION:
        return Sensor.STRING_TYPE_LINEAR_ACCELERATION;
      case Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT:
        return Sensor.STRING_TYPE_LOW_LATENCY_OFFBODY_DETECT;
      case Sensor.TYPE_MAGNETIC_FIELD:
        return Sensor.STRING_TYPE_MAGNETIC_FIELD;
      case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
        return Sensor.STRING_TYPE_MAGNETIC_FIELD_UNCALIBRATED;
      case Sensor.TYPE_MOTION_DETECT:
        return Sensor.STRING_TYPE_MOTION_DETECT;
      case Sensor.TYPE_ORIENTATION:
        return Sensor.STRING_TYPE_ORIENTATION;
      case Sensor.TYPE_POSE_6DOF:
        return Sensor.STRING_TYPE_POSE_6DOF;
      case Sensor.TYPE_PRESSURE:
        return Sensor.STRING_TYPE_PRESSURE;
      case Sensor.TYPE_PROXIMITY:
        return Sensor.STRING_TYPE_PROXIMITY;
      case Sensor.TYPE_RELATIVE_HUMIDITY:
        return Sensor.STRING_TYPE_RELATIVE_HUMIDITY;
      case Sensor.TYPE_ROTATION_VECTOR:
        return Sensor.STRING_TYPE_ROTATION_VECTOR;
      case Sensor.TYPE_SIGNIFICANT_MOTION:
        return Sensor.STRING_TYPE_SIGNIFICANT_MOTION;
      case Sensor.TYPE_STATIONARY_DETECT:
        return Sensor.STRING_TYPE_STATIONARY_DETECT;
      case Sensor.TYPE_STEP_COUNTER:
        return Sensor.STRING_TYPE_STEP_COUNTER;
      case Sensor.TYPE_STEP_DETECTOR:
        return Sensor.STRING_TYPE_STEP_DETECTOR;
      case Sensor.TYPE_TEMPERATURE:
        return Sensor.STRING_TYPE_TEMPERATURE;
      default:
        Log.w(TAG, "Could not translate Sensor Type |" + sensorType + "|");
        return "Unknown Sensor Type";
    }
  }
}
