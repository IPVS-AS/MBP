package de.ipvs.as.mbp.service.settings;

import de.ipvs.as.mbp.domain.settings.BrokerLocation;
import de.ipvs.as.mbp.domain.settings.MBPInfo;
import de.ipvs.as.mbp.domain.settings.Settings;
import de.ipvs.as.mbp.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
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

    @Value("${git.branch}")
    private String branch;

    @Value("${git.build.time}")
    private String buildTime;

    @Value("${git.build.version}")
    private String buildVersion;

    @Value("${git.commit.id.abbrev}")
    private String commitID;

    @Value("${git.commit.time}")
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
     * Saves all settings that are provided as part of the settings object persistently in the MongoDB repository.
     *
     * @param settings The settings to save
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

        return defaultSettings;
    }
}
