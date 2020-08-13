package org.citopt.connde.service.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.citopt.connde.service.settings.model.BrokerLocation;
import org.citopt.connde.service.settings.model.Settings;
import org.springframework.stereotype.Service;

/**
 * This service provides features for the management of application-wide settings that may be changed by the users.
 * It implicitly stores the settings persistently in a properties file on disk and enables changes of the settings.
 */
@Service
public class SettingsService {

    //The name of the file in which the settings are supposed to be stored
    private static final String SETTINGS_FILE_NAME = "config.properties";

    //Keys that are used to store the settings in the file
    private static final String SETTINGS_KEY_BROKER_LOCATION = "broker_location";
    private static final String SETTINGS_KEY_BROKER_IP_ADDRESS = "broker_url";

    private File settingsFile = null;
    private Properties properties = null;

    /**
     * Creates and initializes the settings service.
     */
    public SettingsService() {
        //Create a file object from the properties file
        URL fileURL = getClass().getClassLoader().getResource(SETTINGS_FILE_NAME);
        System.err.println(fileURL);
        try {
            settingsFile = new File(fileURL.toURI());
        } catch (URISyntaxException e) {
            System.err.println("Error while reading the properties file.");
        } catch (Exception e) {
        	System.err.println("Error while reading the properties file: "  + e.getMessage() + ".");
        }
    }

    /**
     * Returns the application settings that are currently applied.
     *
     * @return The settings wrapped in a DTO
     * @throws IOException In case of an I/O issue while reading the settings file
     */
    public Settings getSettings() throws IOException {
        //Check if the settings have already been loaded from file
        if (properties == null) {
            loadSettingsFile();
        }

        //Create new settings object with default values
        Settings settings = new Settings();

        //Retrieve the settings from the settings file
        BrokerLocation brokerLocation = BrokerLocation.valueOf(properties.getProperty(SETTINGS_KEY_BROKER_LOCATION,
                settings.getBrokerLocation().toString()));
        String brokerIPAddress = properties.getProperty(SETTINGS_KEY_BROKER_IP_ADDRESS, settings.getBrokerIPAddress());

        //Adjust the settings object accordingly
        settings.setBrokerLocation(brokerLocation);
        settings.setBrokerIPAddress(brokerIPAddress);

        return settings;
    }

    /**
     * Saves all settings that are provided as part of the settings object persistently in the settings file. After
     * a call of this method, the new settings will be returned by the service.
     *
     * @param settings The settings to save
     * @throws IOException In case of an I/O issue while writing the settings file
     */
    public void saveSettings(Settings settings) throws IOException {
        //Sanity check
        if (settings == null) {
            throw new IllegalArgumentException("Settings must not be null.");
        }

        //Check if the settings have already been loaded from file
        if (properties == null) {
            loadSettingsFile();
        }

        //Retrieve settings properties from the provided object
        BrokerLocation brokerLocation = settings.getBrokerLocation();
        String brokerURL = settings.getBrokerIPAddress();

        //Take those setting and replace the old ones
        properties.setProperty(SETTINGS_KEY_BROKER_LOCATION, brokerLocation.toString());
        properties.setProperty(SETTINGS_KEY_BROKER_IP_ADDRESS, brokerURL);

        //Write the settings file with the new settings
        writeSettingsFile();
    }

    /**
     * Writes the settings file with the settings stored in the internal property object.
     *
     * @throws IOException In case of an I/O issue while writing the settings file
     */
    private void writeSettingsFile() throws IOException {
        //Has the settings file been already resolved?
        if (settingsFile == null) {
            throw new IllegalStateException("Properties file has not been resolved yet.");
        }

        //Write the file
        OutputStream outputStream = new FileOutputStream(settingsFile);
        properties.store(outputStream, null);
        outputStream.close();
    }

    /**
     * Loads the settings file and stores the settings in the internal property object.
     *
     * @throws IOException In case of an I/O issue while reading the settings file
     */
    private void loadSettingsFile() throws IOException {
        //Has the settings file been already resolved?
        if (settingsFile == null) {
            throw new IllegalStateException("Properties file has not been resolved yet.");
        }
        //Read and load the file
        InputStream inputStream = new FileInputStream(settingsFile);
        properties = new Properties();
        properties.load(inputStream);
        inputStream.close();
    }
}
