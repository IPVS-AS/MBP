package de.ipvs.as.mbp.service.settings;

import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.settings.BrokerLocation;
import de.ipvs.as.mbp.domain.settings.MBPInfo;
import de.ipvs.as.mbp.domain.settings.Settings;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.repository.SettingsRepository;
import de.ipvs.as.mbp.service.deployment.demo.DemoDeployer;
import de.ipvs.as.mbp.service.messaging.PubSubService;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * This service provides features for the management of application-wide settings that may be changed by the users.
 * The settings are implicitly stored within a MongoDB repository. implicitly stores the settings persistently in a properties file on disk and enables changes of the settings.
 */
@Service
@DependsOn({"applicationPropertiesConfigurer", "gitPropertiesConfigurer"})
public class SettingsService {
    @Autowired
    private SettingsRepository settingsRepository;

    //Auto-injected data
    @Value("${mqtt_broker.default.location}")
    private String defaultBrokerLocation;

    @Value("${mqtt_broker.default.host}")
    private String defaultBrokerHost;

    @Value("${mqtt_broker.default.port}")
    private String defaultBrokerPort;

    @Value("${git.branch}")
    private String branch;

    @Value("${git.build.time}")
    private String buildTime;

    @Value("${git.build.version}")
    private String buildVersion;

    @Value("${git.commit.id.abbrev}")
    private String commitID;

    @Value("${git.commit.time")
    private String commitTime;

    /**
     * Returns a MBOInfo object containing information about the running MBP app instance and the environment
     * in which it is operated.
     *
     * @return The populated MBPInfo object
     */
    public MBPInfo getMBPInfo() {
        // Create MBPInfo object
        MBPInfo mbpInfo = new MBPInfo();
        mbpInfo.setVersion(buildVersion);
        mbpInfo.setCommitID(commitID);
        mbpInfo.setCommitTime(commitTime);
        mbpInfo.setBuildTime(buildTime);
        mbpInfo.setBranch(branch);

        //Set broker location from stored settings
        mbpInfo.setBrokerLocation(getSettings().getBrokerLocation());

        return mbpInfo;
    }

    /**
     * Loads and returns the application-wide settings that are currently applied from the MongoDB repository.
     *
     * @return The retrieved settings
     */
    public Settings getSettings() {
        //Retrieve settings from repository
        Optional<Settings> settingsOptional = settingsRepository.findById(Settings.SETTINGS_DOC_ID);

        //Return retrieved settings or the default ones if not found
        return settingsOptional.orElse(createDefaultSettings());
    }

    /**
     * Saves all settings that are provided as part of the settings object persistently in the MongoDB repository
     * and updates affected components accordingly.
     *
     * @param settings The new settings
     */
    public void updateSettings(Settings settings) throws MqttException {
        //Get previous settings
        Settings previousSettings = getSettings();

        //Check whether the messaging broker settings changed
        if ((!previousSettings.getBrokerLocation().equals(settings.getBrokerLocation())) ||
                (!previousSettings.getBrokerIPAddress().equals(settings.getBrokerIPAddress())) ||
                (previousSettings.getBrokerPort() != settings.getBrokerPort())) {
            //Broker settings changed, so re-connect the messaging client with the new settings
            reconnectMessagingClient(settings.getBrokerLocation(), settings.getBrokerIPAddress(), settings.getBrokerPort());
        }

        //Check whether the demo mode setting changed
        if (!previousSettings.isDemoMode() == settings.isDemoMode()) {
            //Retrieve demo deployer component bean
            DemoDeployer demoDeployer = DynamicBeanProvider.get(DemoDeployer.class);
            demoDeployer.resetDeployedComponents();
        }

        //Everything worked, thus save settings to repository
        saveSettings(settings);
    }

    /**
     * Saves all settings that are provided as part of the settings object persistently in the MongoDB repository
     * without updating possibly affected components.
     *
     * @param settings The new settings
     */
    public void saveSettings(Settings settings) {
        //Sanity check
        if (settings == null) {
            throw new IllegalArgumentException("Settings must not be null.");
        }

        //Save settings into repository
        settingsRepository.save(settings);
    }

    /**
     * Uses the publish-subscribe-based messaging service to re-connect the messaging client with the
     * new broker settings. Finally, it is checked whether the new connection could be established successfully.
     * If this is not the case, an exception is thrown.
     *
     * @param brokerLocation The location of the broker
     * @param brokerAddress  The broker address to use
     * @param brokerPort     The broker port to use
     */
    private void reconnectMessagingClient(BrokerLocation brokerLocation, String brokerAddress, int brokerPort) {
        //Retrieve the publish-subscribe-based messaging service
        PubSubService pubSubService = DynamicBeanProvider.get(PubSubService.class);

        //Ask the service to re-connect the messaging client with the new broker settings
        pubSubService.reconnect(brokerLocation, brokerAddress, brokerPort);

        //Check if the connection was established successfully
        if (!pubSubService.isConnected()) {
            throw new MBPException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to connect to the messaging broker.");
        }
    }

    /**
     * Creates and returns a basic settings object with default settings.
     *
     * @return The default settings object
     */
    private Settings createDefaultSettings() {
        //Create new settings object
        Settings defaultSettings = new Settings();

        //Set fields to default values
        defaultSettings.setBrokerLocation(BrokerLocation.valueOf(defaultBrokerLocation));
        defaultSettings.setBrokerIPAddress(defaultBrokerHost);
        defaultSettings.setBrokerPort(Integer.parseInt(defaultBrokerPort));

        return defaultSettings;
    }
}
