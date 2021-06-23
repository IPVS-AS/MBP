package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.settings.MBPInfo;
import de.ipvs.as.mbp.domain.settings.Settings;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.error.MissingAdminPrivilegesException;
import de.ipvs.as.mbp.service.user.UserEntityService;
import de.ipvs.as.mbp.service.settings.DefaultOperatorService;
import de.ipvs.as.mbp.service.settings.SettingsService;
import de.ipvs.as.mbp.service.testing.DefaultTestingComponents;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for settings related REST requests.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/settings")
@Api(tags = {"Settings"}, description = "Retrieval and modification of platform-wide settings")
public class RestSettingsController {

    @Autowired
    private DefaultOperatorService defaultOperatorService;

    @Autowired
    private DefaultTestingComponents defaultTestingComponents;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private UserEntityService userEntityService;


    /**
     * Returns information about the running MBP app instance and the environment in which it is operated.
     *
     * @return A response entity containing the resulting MBP info
     */
    @GetMapping(value = "/mbpinfo")
    @ApiOperation(value = "Returns information about the running MBP app instance and the environment in which it is operated.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<MBPInfo> getMBPInfo() {
        //Retrieve MBPInfo object
        MBPInfo mbpInfo = settingsService.getMBPInfo();

        // Respond
        return new ResponseEntity<>(mbpInfo, HttpStatus.OK);
    }


    /**
     * Called when the client wants to load default operators and make them available for usage
     * in actuators and sensors by all users.
     *
     * @return A response entity containing the result of the request
     * @throws MissingAdminPrivilegesException In case the current user misses admin privileges
     */
    @PostMapping(value = "/default-operators")
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
     * Called when the client wants to reinstall  the invisible default components for the Testing-Tool and
     * make them available for usage in the Testing-Tool by all users.
     *
     * @return A response entity containing the result of the request
     */
    @PostMapping(value = "/default-test-components")
    @ApiOperation(value = "Loads default components from the resource directory of the MBP and makes them available for usage in the Testing-Tool by all users.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to perform this action"), @ApiResponse(code = 500, message = "Default operators could not be added")})
    public ResponseEntity<Void> reinstallTestingComponents() {

        // Delete & reinstall all default testing components
        defaultTestingComponents.replaceTestDevice();
        defaultTestingComponents.replaceOperators();
        defaultTestingComponents.replaceTestingActuator();
        defaultTestingComponents.replaceSensorSimulators();

        // Respond
        return ResponseEntity.ok().build();
    }


    /**
     * Called when the client wants to redeploy the invisible default components for the Testing-Tool.
     *
     * @return A response entity containing the result of the request
     */
    @PostMapping(value = "/test-components-redeploy")
    @ApiOperation(value = "Redeploy the default sensors/actuator from the resource directory of the MBP for usage in the Testing-Tool by all users.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to perform this action"), @ApiResponse(code = 500, message = "Default operators could not be added")})
    public ResponseEntity<Void> redeployTestingComponents() {

        // First delete all default testing components
        defaultTestingComponents.redeployComponents();

        // Respond
        return ResponseEntity.ok().build();
    }


    /**
     * Called when the client wants to retrieve the settings.
     *
     * @return The settings object
     * @throws MissingAdminPrivilegesException In case the current user misses admin privileges
     */
    @GetMapping
    @ApiOperation(value = "Retrieves the current settings of the platform", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<Settings> getSettings() throws MissingAdminPrivilegesException {
        //Get settings from settings service and return them
        return new ResponseEntity<>(settingsService.getSettings(), HttpStatus.OK);
    }

    /**
     * Called when the client wants to change the settings.
     *
     * @param settings The settings to update
     * @return OK (200) in case everything was successful
     * @throws MissingAdminPrivilegesException In case the current user misses admin privileges
     */
    @PostMapping
    @ApiOperation(value = "Modifies the current settings of the platform", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to modify the settings")})
    public ResponseEntity<Void> saveSettings(@RequestBody Settings settings) throws MissingAdminPrivilegesException {
        //Require admin permissions
        userEntityService.requireAdmin();

        // Update settings and update MBP components if necessary
        try {
            settingsService.updateSettings(settings);
        } catch (MqttException e) {
            throw new MBPException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not establish connection to MQTT broker.");
        }

        //Everything fine
        return ResponseEntity.ok().build();
    }
}