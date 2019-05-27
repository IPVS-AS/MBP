package org.citopt.connde.service.ssh;

import org.citopt.connde.domain.device.Device;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a managed pool of SSH session for devices. The sessions may be requested and used by other components
 * on demand.
 */
@Component
public class SSHSessionPool {
    //Port of the remote devices to use for SSH connections
    private static final int SSH_PORT = 22;

    //Map (device id -> SSH session) of sessions
    private Map<String, SSHSession> sessionsMap;

    /**
     * Initializes the session pool.
     */
    private SSHSessionPool() {
        //Initialize map of sessions
        sessionsMap = new HashMap<>();
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
     * Unregisters the SSH session of a certain device from the SSH session pool. As an outcome,
     * a new session will be created on the next request.
     *
     * @param deviceId The id of the device for which the SSH session is supposed to be unregistered from the pool.
     */
    public void unregisterSSHSession(String deviceId) throws IOException {
        //Sanity check
        if ((deviceId == null) || deviceId.isEmpty()) {
            throw new IllegalArgumentException("Device id must not be null or empty.");
        }

        //Check if such a session is not part of the map
        if(!sessionsMap.containsKey(deviceId)){
            return;
        }

        //Get SSH session object
        SSHSession session = sessionsMap.get(deviceId);

        //Remove entry from sessions map
        sessionsMap.remove(deviceId);

        //Close session
        session.close();
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

        //Retrieve private rsa key
        String rsaKey = device.getRsaKey();

        //Check key for validity
        if ((rsaKey == null) || (rsaKey.isEmpty())) {
            throw new IllegalArgumentException("No private RSA key for SSH connection provided.");
        }

        //Retrieve ssh connection parameter
        String url = device.getIpAddress();
        String username = device.getUsername();
        String password = device.getPassword();

        //Create new ssh session and connect
        SSHSession sshSession = new SSHSession(url, SSH_PORT, username, password, rsaKey);
        sshSession.connect();

        return sshSession;
    }
}
