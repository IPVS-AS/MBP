package de.ipvs.as.mbp.util.testexecution;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import static de.ipvs.as.mbp.util.TestEnvironmentUtils.EnvironmentType;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

public class MBPTestExtension implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        EnvironmentType environmentType = EnvironmentType.getTestEnvironmentType();
        if (environmentType == EnvironmentType.DEVELOPMENT) {
            return enabled("Running in Development Environment");
        }
        AnnotatedElement element = extensionContext.getElement().orElse(null);
        if (element == null && environmentType == EnvironmentType.BACKEND_TESTS) {
            return enabled("Test element is null");
        } else if (element == null) {
            return disabled("Test element is null thus the test is disabled since we are not running in Backend Mode");
        }

        if(element instanceof Method) {
            Method method = (Method) element;
            element = method.getDeclaringClass();
        }

        BackendTest backendTestAnnotation = element.getAnnotation(BackendTest.class);
        IoTDeviceTest deviceTestAnnotation = element.getAnnotation(IoTDeviceTest.class);
        if (backendTestAnnotation != null && deviceTestAnnotation == null) {
            return environmentType == EnvironmentType.BACKEND_TESTS ?
                    enabled("test is executed in Backend") :
                    disabled("Not executing backend test in this environment");
        } else if (deviceTestAnnotation != null && backendTestAnnotation == null) {
            return environmentType == EnvironmentType.IOT_DEVICE_TESTS ?
                    enabled("Test is executed in Device test environment") :
                    disabled("Not executing this test outside device test environment");
        } else if (backendTestAnnotation != null && deviceTestAnnotation != null) {
            return environmentType == EnvironmentType.BACKEND_TESTS ?
                    enabled("Double Annotated test is executed in Backend") :
                    disabled("Not executing Double Annotated test outside the backend");
        } else {
            return environmentType == EnvironmentType.BACKEND_TESTS ?
                    enabled("Uannotated test is executed in Backend") :
                    disabled("Not executing Unannotated test outside the backend");
        }
    }
}
