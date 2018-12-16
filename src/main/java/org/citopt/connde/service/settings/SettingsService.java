package org.citopt.connde.service.settings;

import org.citopt.connde.service.settings.model.BrokerLocation;
import org.citopt.connde.service.settings.model.Settings;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 *
 *
 * @author Jan
 */
@Service
public class SettingsService {
    private static final String SETTINGS_FILE_NAME = "config.properties";

    private static final String SETTINGS_KEY_BROKER_LOCATION = "broker_location";
    private static final String SETTINGS_KEY_BROKER_IP_ADDRESS = "broker_url";

    private File propertiesFile = null;
    private Properties properties = null;

    public SettingsService() {
        URL fileURL = getClass().getClassLoader().getResource(SETTINGS_FILE_NAME);
        try {
            propertiesFile = new File(fileURL.toURI());
        } catch (URISyntaxException e) {
            System.err.println("Error while reading the properties file.");
        }
    }

    public Settings getSettings() throws IOException {
        if (properties == null) {
            loadPropertiesFile();
        }

        Settings settings = new Settings();

        BrokerLocation brokerLocation = BrokerLocation.valueOf( properties.getProperty(SETTINGS_KEY_BROKER_LOCATION,
                settings.getBrokerLocation().toString()));
        String brokerIPAddress = properties.getProperty(SETTINGS_KEY_BROKER_IP_ADDRESS, settings.getBrokerIPAddress());

        settings.setBrokerLocation(brokerLocation);
        settings.setBrokerIPAddress(brokerIPAddress);

        return settings;
    }

    public void saveSettings(Settings settings) throws IOException {
        if (settings == null) {
            throw new IllegalArgumentException("Settings must not be null.");
        }

        if (properties == null) {
            loadPropertiesFile();
        }

        BrokerLocation brokerLocation = settings.getBrokerLocation();
        String brokerURL = settings.getBrokerIPAddress();

        properties.setProperty(SETTINGS_KEY_BROKER_LOCATION, brokerLocation.toString());
        properties.setProperty(SETTINGS_KEY_BROKER_IP_ADDRESS, brokerURL);

        writePropertiesFile();
    }

    private void writePropertiesFile() throws IOException {
        if (propertiesFile == null) {
            throw new IllegalStateException("Properties file has not been resolved yet.");
        }
        OutputStream outputStream = new FileOutputStream(propertiesFile);
        properties.store(outputStream, null);
        outputStream.close();
    }

    private void loadPropertiesFile() throws IOException {
        if (propertiesFile == null) {
            throw new IllegalStateException("Properties file has not been resolved yet.");
        }
        InputStream inputStream = new FileInputStream(propertiesFile);
        properties = new Properties();
        properties.load(inputStream);
        inputStream.close();
    }
}
