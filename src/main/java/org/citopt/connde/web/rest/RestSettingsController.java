package org.citopt.connde.web.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.constants.Constants;
import org.citopt.connde.service.mqtt.MQTTService;
import org.citopt.connde.service.settings.SettingsService;
import org.citopt.connde.service.settings.model.Settings;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * REST Controller for settings related REST requests.
 *
 * @author Jan
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Settings"}, description = "Retrieval and modification of platform-wide settings")
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
    @Secured({Constants.ADMIN})
    @ApiOperation(value = "Retrieves the current settings of the platform", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access the settings")})
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
    @Secured({Constants.ADMIN})
    @ApiOperation(value = "Modifies the current settings of the platform", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to modify the settings")})
    public ResponseEntity saveSettings(@RequestBody Settings settings) {
        //Save settings and re-initialize MQTT service, since it needs to use a different IP address now
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