package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.service.mqtt.MQTTService;
import org.citopt.connde.service.settings.SettingsService;
import org.citopt.connde.service.settings.model.Settings;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * REST Controller for settings related REST requests.
 *
 * @author Jan
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestSettingsController {

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private MQTTService mqttService;

    /**
     * Called when the client wants to retrieve the settings.
     *
     * @return The settings object
     */
    @RequestMapping(value = "/settings", method = RequestMethod.GET)
    public ResponseEntity<Settings> getSettings() {
        //Get settings from settings service and return them
        Settings settings;
        try {
            settings = settingsService.getSettings();
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(settings, HttpStatus.OK);
    }

    /**
     * Called when the client wants to change the settings.
     *
     * @param settings The settings to update
     * @return OK (200) in case everything was successful
     */
    @PostMapping("/settings")
    public ResponseEntity saveSettings(@RequestBody Settings settings) {
        //Save settings and re-initialize MQTT service, since it needs to use a different ip address now
        try {
            settingsService.saveSettings(settings);
            mqttService.initialize();
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (MqttException e) {
            System.err.print("MqttException: " + e.getMessage());
        }

        //Everything fine
        return new ResponseEntity(HttpStatus.OK);
    }
}
