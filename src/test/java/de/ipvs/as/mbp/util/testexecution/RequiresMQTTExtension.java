package de.ipvs.as.mbp.util.testexecution;

import java.lang.reflect.AnnotatedElement;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.*;

public class RequiresMQTTExtension implements ExecutionCondition {

    public boolean mqttAvailable = false;
    public boolean mqttStatusDetermined = false;

    private void determineMQTTStatus() {
        IMqttClient publisher = null;
        try {
            String publisherId = UUID.randomUUID().toString();
            publisher = new MqttClient("tcp://127.0.0.1:1883", publisherId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(false);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            publisher.connect(options);
            mqttAvailable = true;
        } catch (Exception e) {
            mqttAvailable = false;
        } finally {
            if (publisher != null) {
                try {
                    publisher.disconnect();
                } catch (MqttException e) {
                }
            }
        }
        mqttStatusDetermined = true;
    }

    public boolean isMqttAvailable() {
        if (!mqttStatusDetermined) {
            this.determineMQTTStatus();
        }
        return mqttAvailable;
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        AnnotatedElement element = extensionContext.getElement().orElse(null);
        if (element == null) {
            return enabled("Test element is null");
        }
        RequiresMQTT annotation = element.getAnnotation(RequiresMQTT.class);
        if (annotation == null) {
            return enabled("MQTT Not needed");
        }

        // Determine MQTT Status if mandatory
        if (!mqttStatusDetermined) {
            determineMQTTStatus();
        }

        if (!mqttAvailable) {
            return disabled("MQTT Not available");
        }
        return enabled("MQTT available");
    }
}
