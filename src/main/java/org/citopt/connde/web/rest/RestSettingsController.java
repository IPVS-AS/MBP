package org.citopt.connde.web.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.error.MissingAdminPrivilegesException;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.mqtt.MQTTService;
import org.citopt.connde.service.settings.DefaultOperatorService;
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
@Api(tags = {"Settings"}, description = "Retrieval and modification of platform-wide settings")
public class RestSettingsController {

    @Autowired
    private DefaultOperatorService defaultOperatorService;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private MQTTService mqttService;

    @Autowired
    private UserEntityService userEntityService;

    /**
     * Called when the client wants to load default operators and make them available for usage
     * in actuators and sensors by all users.
     *
     * @return An action response containing the result of the request
     * @throws MissingAdminPrivilegesException
     */
    @PostMapping(value = "/settings/default-operators")
    @ApiOperation(value = "Loads default operators from the resource directory of the MBP and makes them available for usage in actuators and sensors by all users.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to perform this action"), @ApiResponse(code = 500, message = "Default operators could not be added")})
    public ResponseEntity<Void> addDefaultOperators() throws MissingAdminPrivilegesException {
        userEntityService.requireAdmin();

        // Call corresponding service function
        defaultOperatorService.addDefaultOperators();

        // Respond
        return ResponseEntity.ok().build();
    }

    /**
     * Called when the client wants to retrieve the settings.
     *
     * @return The settings object
     * @throws MissingAdminPrivilegesException
     */
    @RequestMapping(value = "/settings", method = RequestMethod.GET)
    @ApiOperation(value = "Retrieves the current settings of the platform", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access the settings")})
    public ResponseEntity<Settings> getSettings() throws MissingAdminPrivilegesException {
        userEntityService.requireAdmin();

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
     * @throws MissingAdminPrivilegesException
     */
    @PostMapping("/settings")
    @ApiOperation(value = "Modifies the current settings of the platform", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to modify the settings")})
    public ResponseEntity<Void> saveSettings(@RequestBody Settings settings) throws MissingAdminPrivilegesException {
        userEntityService.requireAdmin();

        // Save settings and re-initialize MQTT service, since it needs to use a different IP address now
        try {
            settingsService.saveSettings(settings);
            mqttService.initialize();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        //Everything fine
        return ResponseEntity.ok().build();
    }
}