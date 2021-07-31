package de.ipvs.as.mbp.util;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.transport.DockerHttpClient;
import de.ipvs.as.mbp.util.testexecution.RequiresMQTTExtension;
import org.testcontainers.shaded.com.github.dockerjava.core.DefaultDockerClientConfig;
import org.testcontainers.shaded.com.github.dockerjava.core.DockerClientConfig;
import org.testcontainers.shaded.com.github.dockerjava.core.DockerClientImpl;
import org.testcontainers.shaded.com.github.dockerjava.okhttp.OkDockerHttpClient;

public class TestEnvironmentUtils {

    public static final String TEST_RUNTIME_ENVIRONMENT_VARIABLE_NAME = "TEST_RUNTIME";
    public static final String TEST_MODE_ENVIRONMENT_VARIABLE = "TEST_MODE";

    public static boolean isMQTTAvailable() {
        return new RequiresMQTTExtension().isMqttAvailable();
    }

    public static boolean isDockerAvailable() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new OkDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();

        DockerClient client = DockerClientImpl.getInstance(config, httpClient);
        try {
            client.pingCmd().exec();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isCI() {
        String envValue = System.getenv(TEST_RUNTIME_ENVIRONMENT_VARIABLE_NAME);
        return envValue != null && envValue.equalsIgnoreCase("ci");
    }

    public static enum EnvironmentType {
        BACKEND_TESTS("backend"),
        IOT_DEVICE_TESTS("iotdevice"),
        DEVELOPMENT(null);

        private final String envValue;

        EnvironmentType(String envValue) {
            this.envValue = envValue;
        }

        public String getEnvValue() {
            return envValue;
        }

        public static EnvironmentType getTestEnvironmentType() {
            String envValue = System.getenv(TEST_MODE_ENVIRONMENT_VARIABLE);
            for (EnvironmentType value : values()) {
                if (value.envValue.equalsIgnoreCase(envValue)) {
                    return value;
                }
            }
            return DEVELOPMENT;
        }
    }
}
