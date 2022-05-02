package de.ipvs.as.mbp.service.deployment.ssh;
import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.key_pair.KeyPair;
import de.ipvs.as.mbp.service.deployment.ssh.SSHSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a managed pool of SSH session for devices. The sessions may be requested and used by other components
 * on demand.
 */
@org.springframework.stereotype.Component
public class SSHSessionPool {

    //Map (component id -> SSH session) of sessions
    private final Map<String, SSHSession> sessionsMap;

    /**
     * Initializes the session pool.
     */
    private SSHSessionPool() {
        //Initialize map of sessions
        sessionsMap = new HashMap<>();
    }

    /**
     * Returns an active SSH session for a certain component. If no session for this component exists,
     * a new session is created and returned.
     *
     * @param component The device to return a SSH session for
     * @return The active SSH session for the device
     * @throws IOException In case of an I/O issue
     */
    public SSHSession getSSHSession(Component component) throws IOException {
        //Sanity check
        if (component == null || component.getDevice() == null) {
            throw new IllegalArgumentException("Component and its device must not be null.");
        }

        //Get component id
        String componentId = component.getId();

        //Check if a session is already registered
        if (sessionsMap.containsKey(component.getId())) {
            //Get registered session
            SSHSession session = sessionsMap.get(componentId);

            //Check if session is active
            if (session.isActive()) {
                return session;
            }

            //Unregister old session from map
            sessionsMap.remove(componentId);
        }

        //A new SSH session needs to be created
        SSHSession session = establishSSHSession(component.getDevice());

        //Register session in sessions map
        sessionsMap.put(componentId, session);

        //Return new session
        return session;
    }

    /**
     * Returns an active SSH session for a certain device. If no session for this device exists,
     * a new session is created and returned.
     *
     * @param device The device to return a SSH session for
     * @return The active SSH session for the device
     * @throws IOException In case of an I/O issue
     */
    public SSHSession getSSHSession(Device device) throws IOException {
        //Sanity check
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null.");
        }

        //Get device id
        String deviceId = device.getId();

        //Check if a session is already registered
        if (sessionsMap.containsKey(deviceId)) {
            //Get registered session
            SSHSession session = sessionsMap.get(deviceId);

            //Check if session is active
            if (session.isActive()) {
                return session;
            }

            //Unregister old session from map
            sessionsMap.remove(deviceId);
        }

        //A new SSH session needs to be created
        SSHSession session = establishSSHSession(device);

        //Register session in sessions map
        sessionsMap.put(deviceId, session);

        //Return new session
        return session;
    }

    /**
     * Unregisters an existing SSH session of a certain device from the SSH session pool, creates
     * a new session for the device registers it at the session pool and returns it.
     *
     * @param device The device for which a new SSH session is supposed to be returned
     * @return The new SSH session for the device
     */
    public SSHSession getNewSSHSession(Device device) throws IOException {
        //Sanity check
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null.");
        }

        //Unregister session from map if it is already existing
        sessionsMap.remove(device.getId());

        //Create a new SSH session
        return getSSHSession(device);
    }

    /*
     * Establishes a new SSH connection to the device that is referenced in the component object.
     *
     * @param device To device to connect with
     * @return The established SSH session
     * @throws IOException In case of an I/O issue
     */
    private SSHSession establishSSHSession(Device device) throws IOException {
        //Validity check
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null.");
        }

        //Retrieve private RSA key if existing
        String privateKey = null;
        KeyPair keypair = device.getKeyPair();

        //Check if a key pair was provided for this device
        if (keypair != null) {
            privateKey = keypair.getPrivateKey();
        }

        //Retrieve password
        String password = device.getPassword();

        //Check key for validity
        if (((privateKey == null) || (privateKey.isEmpty())) && ((password == null) || (password.isEmpty()))) {
            throw new IllegalArgumentException("No private RSA key or Password for SSH connection provided.");
        }

        //Retrieve ssh connection parameter
        String hostAddress = device.getIpAddress();
        String username = device.getUsername();
        int port = device.getPort();

        //Create new ssh session and connect
        SSHSession sshSession = new SSHSession(hostAddress, port, username, password, privateKey);
        sshSession.connect();

        return sshSession;
    }
}
