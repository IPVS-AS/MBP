package org.citopt.connde.conndeapp.advertise;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Created by rosso on 27.08.17.
 */

class DeviceHelper {
  static AdvertiseDevice deviceFromJSON(JSONObject jsonDevice) throws JSONException {
    String localId = jsonDevice.getString(Const.LOCAL_ID);
    JSONObject jsonConf = jsonDevice.getJSONObject(Const.ADAPTER_CONF);
    Map<String, Object> adapterConf = new HashMap<>();
    Iterator<String> keyIterator = jsonConf.keys();
    while (keyIterator.hasNext()) {
      String key = keyIterator.next();
      adapterConf.put(key, jsonConf.get(key));
    }

    AdvertiseDevice device = new AdvertiseDevice(localId, adapterConf);
    keyIterator = jsonDevice.keys();
    while (keyIterator.hasNext()) {
      String key = keyIterator.next();
      switch (key) {
        case Const.TYPE:
          device.setType(jsonDevice.getString(Const.TYPE));
          break;
        default: // do nothing
          break;
      }
    }

    return device;
  }
}
