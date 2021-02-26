package de.ipvs.as.mbp.service.testing;

import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * This service provides features for the management of application-wide settings that may be changed by the users.
 * It implicitly stores the settings persistently in a properties file on disk and enables changes of the settings.
 */
@Service
@PropertySource(value = "classpath:config.properties")
public class PropertiesService {

    //The name of the file in which the settings are supposed to be stored
    private static final String SETTINGS_FILE_NAME = "config.properties";

    private File settingsFile = null;
    private Properties properties = null;


    /**
     * Creates and initializes the settings service.
     */
    public PropertiesService() {
        //Create a file object from the properties file
        URL fileURL = getClass().getClassLoader().getResource(SETTINGS_FILE_NAME);
        try {
            settingsFile = new File(fileURL.toURI());
        } catch (URISyntaxException e) {
            System.err.println("Error while reading the properties file.");
        }
    }

    /**
     * Returns the application settings that are currently applied.
     *
     * @return The settings wrapped in a DTO
     * @throws IOException In case of an I/O issue while reading the settings file
     */
    public String getPropertiesString(String property) throws IOException {
        //Check if the settings have already been loaded from file
        if (property != null) {
            loadSettingsFile();
        }

        return properties.getProperty(property);
    }


    public List<String> getPropertiesList() throws IOException {
        // Check if properties have already been loaded from the file
        if (properties == null) {
            loadSettingsFile();
        }

        return null;


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
