package org.citopt.connde.service.deploy;

import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.Code;
import org.citopt.connde.domain.adapter.parameters.Parameter;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.service.NetworkService;
import org.citopt.connde.service.settings.SettingsService;
import org.citopt.connde.service.settings.model.BrokerLocation;
import org.citopt.connde.service.settings.model.Settings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This component provides features for deploying components onto a remote device. Furthermore it is possible
 * to check the current state of a deployed component and to undeploy the component again.
 *
 * @author rafaelkperes, Jan
 */
@org.springframework.stereotype.Component
public class SSHDeployer {
    //Names of the adapter scripts
    private static final String INSTALL_SCRIPT_NAME = "install.sh";
    private static final String START_SCRIPT_NAME = "start.sh";
    private static final String RUN_SCRIPT_NAME = "running.sh";
    private static final String STOP_SCRIPT_NAME = "stop.sh";

    //Timeout for availability checks (ms)
    private static final int AVAILABILITY_CHECK_TIMEOUT = 5000;

    @Autowired
    private NetworkService networkService;
    @Autowired
    private SettingsService settingsService;

    //Class internal logger
    private static final Logger LOGGER = Logger.getLogger(SSHDeployer.class.getName());

    //Deployment location on remote devices
    private static final String DEPLOY_DIR = "$HOME/scripts";
    private static final String DEPLOY_DIR_PREFIX = "connde";

    //Port of the remote devices to use for SSH connections
    private static final int SSH_PORT = 22;

    //Prefix for base64 encoded files
    private static final String REGEX_BASE64_PREFIX = "^data\\:[a-zA-Z0-9\\/,\\-]*\\;base64\\,";

    /**
     * Returns the path to the directory to which the component is deployed.
     *
     * @param component The component
     * @return The path to the deployment directory
     */
    public static String getDeploymentPath(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("The component must not be null.");
        }
        return DEPLOY_DIR + "/" + DEPLOY_DIR_PREFIX + component.getId();
    }


    /**
     * Determines the current availability state of a given device.
     *
     * @param device The device to check
     * @return The current state of the device
     */
    public DeviceState determineDeviceState(Device device){
        //Get ip address
        String ipAddress = device.getIpAddress();

        //Check if device is reachable
        boolean reachable = false;
        try {
            reachable = InetAddress.getByName(ipAddress).isReachable(AVAILABILITY_CHECK_TIMEOUT);
        } catch (IOException e) {
            return DeviceState.OFFLINE;
        }

        //Check if device was not reachable
        if(!reachable){
            return DeviceState.OFFLINE;
        }

        //Check if it is possible to establish a SSH connection
        SSHSession sshSession;
        try {
            sshSession = establishSSHConnection(device);
        } catch (UnknownHostException e) {
            return DeviceState.ONLINE;
        }

        if(sshSession == null){
            return DeviceState.ONLINE;
        }

        //Check if it is possible to execute a basic command
        if(sshSession.isCommandExecutable()){
            return DeviceState.SSH_AVAILABLE;
        }else{
            return DeviceState.ONLINE;
        }
    }

    /**
     * Deploys a component onto the dedicated remote device and passes deployment parameters to the starter script.
     *
     * @param component             The component to deploy
     * @param parameterInstanceList List of parameter instances to pass to the start script
     * @throws IOException In case of an I/O issue
     */
    public void deployComponent(Component component, List<ParameterInstance> parameterInstanceList) throws IOException {
        //Validity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        LOGGER.log(Level.FINE, "Deploy request for component: " + "{0} (Type: {1})",
                new Object[]{component.getId(), component.getComponentTypeName()});

        //Get dedicated device of the component
        Device device = component.getDevice();

        //Establish new SSH session
        SSHSession sshSession = establishSSHConnection(device);

        //Resolve deployment path
        String deploymentPath = getDeploymentPath(component);

        //Create deployment directory
        LOGGER.log(Level.FINE, "Deploying to directory {1} ...", new Object[]{deploymentPath});
        sshSession.createDir(deploymentPath);
        LOGGER.log(Level.FINE, "Created directory successfully");

        //Retrieve adapter
        Adapter adapter = component.getAdapter();

        //Validity check
        if (adapter == null) {
            throw new IllegalArgumentException("Adapter must not be null.");
        }

        LOGGER.log(Level.FINE, "Copying adapter files to target device....");

        //Iterate over all adapter files and copy them
        for (Code file : adapter.getRoutines()) {
            String fileContent = file.getContent();

            //Check whether content is encoded as base64
            if (fileContent.matches(REGEX_BASE64_PREFIX + ".+")) {
                //Remove base64 prefix
                fileContent = fileContent.replaceAll(REGEX_BASE64_PREFIX, "");

                //Create file from base64
                sshSession.createFileFromBase64(deploymentPath, file.getName(), fileContent);
            } else {
                // No base64 string, just copy file
                sshSession.createFile(deploymentPath, file.getName(), fileContent);
            }
        }
        LOGGER.log(Level.FINE, "Copying adapter files was successful");

        //Resolve own IP address that might be used as broker IP address
        String brokerIP = networkService.getOwnIPAddress();

        //Determine from settings if a remote broker should be used
        Settings settings = settingsService.getSettings();
        if (settings.getBrokerLocation().equals(BrokerLocation.REMOTE)) {
            //Retrieve IP address of external broker from settings
            brokerIP = settings.getBrokerIPAddress();
        }

        //Sanity check
        if(brokerIP == null){
            throw new RuntimeException("Unable to resolve IP address of the broker.");
        }

        //Get topic name for the component
        String topicName = component.getTopicName();

        //Execute install script
        sshSession.changeFilePermissions(deploymentPath + "/" + INSTALL_SCRIPT_NAME, "+x");
        sshSession.executeShellScript(deploymentPath + "/" + INSTALL_SCRIPT_NAME, topicName, brokerIP, deploymentPath);

        LOGGER.log(Level.FINE, "Installation was successful");

        //Create JSON string from parameters
        JSONArray parameterArray = convertParametersToJSON(adapter, parameterInstanceList);
        String jsonString = convertJSONToCmdLineString(parameterArray);

        //Execute start script with parameters
        sshSession.changeFilePermissions(deploymentPath + "/" + START_SCRIPT_NAME, "u+rwx");
        sshSession.executeShellScript(deploymentPath + "/" + START_SCRIPT_NAME, deploymentPath, jsonString);

        LOGGER.log(Level.FINE, "Start was successful");

        //Set permissions of other shell scripts
        sshSession.changeFilePermissions(deploymentPath + "/" + RUN_SCRIPT_NAME, "+x");
        sshSession.changeFilePermissions(deploymentPath + "/" + STOP_SCRIPT_NAME, "+x");

        LOGGER.log(Level.FINE, "Deployment was successful");
    }

    /**
     * Checks whether the component is currently running on the dedicated remote device.
     *
     * @param component The component to check
     * @return True, if the component is running; false otherwise
     * @throws IOException In case of an I/O issue
     */
    public boolean isComponentRunning(Component component) throws IOException {
        //Validity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        LOGGER.log(Level.FINE, "Checking status of component: " + "{0} (Type: {1})",
                new Object[]{component.getId(), component.getComponentTypeName()});

        //Resolve deployment path
        String deploymentPath = getDeploymentPath(component);

        //Get dedicated device of the component
        Device device = component.getDevice();

        //Establish new SSH session
        SSHSession sshSession = establishSSHConnection(device);

        //Get output stream of the session
        OutputStream stdOutStream = sshSession.getStdOutStream();

        //Execute run script to check whether the adapter is running
        try {
            sshSession.executeShellScript(deploymentPath + "/" + RUN_SCRIPT_NAME);

            //Get return value of script
            String returnValue = stdOutStream.toString().toLowerCase();
            return returnValue.contains("true");
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Adapter has not been deployed yet or is not running");
        }
        return false;
    }

    /**
     * Undeploys a component from the dedicated remote device.
     *
     * @param component The component to undeploy
     * @throws IOException In case of an I/O issue
     */
    public void undeployComponent(Component component) throws IOException {
        //Validity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        LOGGER.log(Level.FINE, "Undeploy request for component: " + "{0} (Type: {1})",
                new Object[]{component.getId(), component.getComponentTypeName()});

        //Get dedicated device of the component
        Device device = component.getDevice();

        //Establish new SSH session
        SSHSession sshSession = establishSSHConnection(device);

        //Resolve deployment path
        String deploymentPath = getDeploymentPath(component);

        //Execute stop script in order to termintate the execution of the component
        sshSession.executeShellScript(deploymentPath + "/" + STOP_SCRIPT_NAME);

        //Remove deployment directory from remote machine
        sshSession.removeDir(deploymentPath);
    }

    /**
     * Establishes a SSH connection to a given device.
     *
     * @param device To device to connect with
     * @return The established SSH session
     * @throws UnknownHostException In case the Host could not be found
     */
    private SSHSession establishSSHConnection(Device device) throws UnknownHostException {
        //Validity check
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null.");
        }

        //Validity check
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null.");
        }

        //Retrieve private rsa key
        String rsaKey = device.getRsaKey();

        //Check key for validity
        if ((rsaKey == null) || (rsaKey.isEmpty())) {
            throw new IllegalArgumentException("No private RSA key for SSH connection provided.");
        }

        //Retrieve ssh connection parameter
        String url = device.getIpAddress();
        String username = device.getUsername();

        LOGGER.log(Level.FINE, "Establishing SSH connection to {0} (user: {1})",
                new Object[]{url, username});

        //Create new ssh session and connect
        SSHSession sshSession = new SSHSession(url, SSH_PORT, username, rsaKey);
        sshSession.connect();

        return sshSession;
    }

    /**
     * Converts a JSON array into a string that might be passed as command line parameter.
     *
     * @param jsonArray The JSON array to convert
     * @return The corresponding JSON string (command line compatible)
     */
    private String convertJSONToCmdLineString(JSONArray jsonArray){
        String jsonString = jsonArray.toString();
        //Escape backslashes
        jsonString = jsonString.replace("\"","\\\"");;
        //Wrap string with double quotes
        jsonString = "\"" + jsonString + "\"";

        return jsonString;
    }

    /**
     * Converts a list of parameter instances into a JSON array.
     * @param adapter The adapter that specifies the parameters
     * @param parameterInstanceList A list of parameter instances that correspond to the adapter parameters
     * @return A JSON array that contains the parameter instances
     */
    private JSONArray convertParametersToJSON(Adapter adapter, List<ParameterInstance> parameterInstanceList) {
        JSONArray parameterArray = new JSONArray();

        //Sanity check
        if (adapter == null) {
            throw new IllegalArgumentException("Adapter must not be null.");
        } else if ((parameterInstanceList == null) || parameterInstanceList.isEmpty()) {
            //Return empty array
            return parameterArray;
        }

        //Get specified parameters from adapter
        List<Parameter> parameters = adapter.getParameters();

        //Iterate over all specified parameters
        for (Parameter parameter : parameters) {
            boolean matchingFound = false;
            //Iterate over all parameter instances
            for (ParameterInstance parameterInstance : parameterInstanceList) {
                //Find matching instance for this parameter
                if (!parameter.isInstanceValid(parameterInstance)) {
                    continue;
                }
                matchingFound = true;

                //Create JSON object for the parameter that can be added to the parameter array
                JSONObject parameterObject = new JSONObject();
                try {
                    //Add properties to object
                    parameterObject.put("name", parameter.getName());
                    parameterObject.put("value", parameterInstance.getValue());
                } catch (JSONException e) {}
                //Add object to parameter
                parameterArray.put(parameterObject);
            }

            //Throw exception if no valid instance was provided for a mandatory parameter
            if ((!matchingFound) && parameter.isMandatory()) {
                throw new IllegalArgumentException("No valid instance for parameter \"" + parameter.getName() +
                        "\" provided.");
            }
        }

        return parameterArray;
    }
}
