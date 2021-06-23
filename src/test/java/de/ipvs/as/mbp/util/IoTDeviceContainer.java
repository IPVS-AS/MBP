package de.ipvs.as.mbp.util;

import com.jcabi.ssh.Shell;
import com.jcabi.ssh.SshByPassword;
import org.testcontainers.containers.GenericContainer;

public class IoTDeviceContainer extends GenericContainer<IoTDeviceContainer> {
    public static final String MOCKDEVICE_DOCKER_IMAGE = "ghcr.io/c-mueller/mbp/mockdevice:latest";
    public static final String DEFAULT_USERNAME = "mbp";
    public static final String DEFAULT_PASSWORD = "password";

    public IoTDeviceContainer() {
        super(MOCKDEVICE_DOCKER_IMAGE);
        this.withExposedPorts(22);
    }

    public Integer getSshPort() {
        return this.getMappedPort(22);
    }

    public Shell openSshShell() throws Exception {
        if (!this.isRunning()) {
            this.start();
        }
        return new SshByPassword("127.0.0.1", this.getMappedPort(22), DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }
}
