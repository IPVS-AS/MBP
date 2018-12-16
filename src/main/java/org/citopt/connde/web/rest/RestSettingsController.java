package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.service.mqtt.ValueLogger;
import org.citopt.connde.service.settings.SettingsService;
import org.citopt.connde.service.settings.model.Settings;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    private ValueLogger valueLogger;

    @RequestMapping(value = "/settings", method = RequestMethod.GET)
    public ResponseEntity<Settings> getSettings() {
        Settings settings;
        try {
            settings = settingsService.getSettings();
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(settings, HttpStatus.OK);
    }

    @RequestMapping(value = "/settings", method = RequestMethod.POST)
    public ResponseEntity saveSettings(@RequestBody Settings settings) {
        try {
            settingsService.saveSettings(settings);
            valueLogger.setupAndStart();
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (MqttException e) {
            System.err.print("MqttException: " + e.getMessage());
        }
        return new ResponseEntity(HttpStatus.OK);
    }
}
