package de.ipvs.as.mbp.util;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;

public class RequiresMQTTExtension implements ExecutionCondition {

    public boolean mqttAvailable = false;

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        AnnotatedElement element = extensionContext.getElement().orElse(null);
        if(element == null) {
            return ConditionEvaluationResult.enabled("Test element is null");
        }
        RequiresMQTT annotation = element.getAnnotation(RequiresMQTT.class);
        if(annotation == null) {
            return ConditionEvaluationResult.enabled("MQTT Not needed");
        }

        if(!mqttAvailable) {
            return ConditionEvaluationResult.disabled("MQTT Not available");
        }
        return ConditionEvaluationResult.enabled("MQTT available");
    }
}
