package de.ipvs.as.mbp.service.deployment.demo;

import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.domain.valueLog.ValueLog;
import de.ipvs.as.mbp.error.DeploymentException;
import de.ipvs.as.mbp.repository.DataModelTreeCache;
import de.ipvs.as.mbp.service.deployment.ComponentState;
import de.ipvs.as.mbp.service.deployment.DeviceState;
import de.ipvs.as.mbp.service.deployment.IDeployer;
import de.ipvs.as.mbp.service.receiver.ValueLogReceiveVerifier;
import de.ipvs.as.mbp.service.receiver.ValueLogReceiver;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.ParseException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Deployer for demonstration purposes that only simulates the actual deployment of the components. In addition,
 * value logs with random values are generated for the running components.
 */
@org.springframework.stereotype.Component
public class DemoDeployer implements IDeployer {

    //Interval and minimum and maximum value for generating value logs
    private static final int VALUE_LOG_INTERVAL = 15 * 1000;
    private static final double VALUE_LOG_MIN = 0;
    private static final double VALUE_LOG_MAX = 40;

    //Artificial delay (in ms) for deployment operations
    private static final long DEPLOYMENT_DELAY = 2000;

    //Map (component --> state) of deployed components and their deployment state
    private final Map<Component, ComponentState> deployedComponents = new HashMap<>();

    // Map (component --> ValueLog queue) of predefined value logs which should be sent by a component (rerun feature)
    private final Map<Component, Queue<ValueLog>> rerunValueLogs = new HashMap<>();

    @Autowired
    private ValueLogReceiver valueLogReceiver;

    @Autowired
    private DataModelTreeCache dataModelCache;

    /**
     * Retrieves the current deployment state of a given component.
     *
     * @param component The component to retrieve the deployment state for
     * @return The current deployment state of the component
     */
    @Override
    public ComponentState retrieveComponentState(Component component) {
        //Check if component is deployed
        if (deployedComponents.containsKey(component)) {
            return deployedComponents.get(component);
        }

        //Return that the component is currently not deployed
        return ComponentState.READY;
    }

    /**
     * Retrieves the current availability state of a given device.
     *
     * @param device The device to retrieve the availability state for
     * @return The current availability state of the device
     */
    @Override
    public DeviceState retrieveDeviceState(Device device) {
        //Devices are always available
        return DeviceState.SSH_AVAILABLE;
    }

    /**
     * Starts a component on its corresponding remote device and passes deployment
     * parameters to it.
     *
     * @param component             The component to start
     * @param parameterInstanceList List of parameter instances to pass
     */
    @Override
    public void startComponent(Component component, List<ParameterInstance> parameterInstanceList) {
        // Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        //Check if component is not deployed or already running
        if (!deployedComponents.containsKey(component)) {
            throw new DeploymentException("The component must first be deployed before it can be started.");
        } else if (deployedComponents.get(component).equals(ComponentState.RUNNING)) {
            throw new DeploymentException("The component is already running.");
        }

        //Artificial delay for demo purposes
        sleep();

        //"Start" component
        deployedComponents.put(component, ComponentState.RUNNING);
    }

    /**
     * Stops a component on its corresponding remote device.
     *
     * @param component The component to stop
     */
    @Override
    public void stopComponent(Component component) {
        // Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        //Check if component is not running
        if (!deployedComponents.containsKey(component) || (!deployedComponents.get(component).equals(ComponentState.RUNNING))) {
            throw new DeploymentException("The component is not running and thus cannot be stopped.");
        }

        //Artificial delay for demo purposes
        sleep();

        //"Stop" component
        deployedComponents.put(component, ComponentState.DEPLOYED);
    }

    /**
     * Deploys a component onto its corresponding remote device.
     *
     * @param component The component to deploy
     */
    @Override
    public void deployComponent(Component component) {
        // Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        //Check if component is already deployed
        if (deployedComponents.containsKey(component)) {
            throw new DeploymentException("The component is already deployed.");
        }

        //Artificial delay for demo purposes
        sleep();

        //"Deploy" component
        deployedComponents.put(component, ComponentState.DEPLOYED);
    }

    /**
     * Undeploys a component from its corresponding remote device.
     *
     * @param component The component to undeploy
     */
    @Override
    public void undeployComponent(Component component) {
        // Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        //Check if component is not already deployed
        if (!deployedComponents.containsKey(component)) {
            throw new DeploymentException("The component is not deployed.");
        }

        //Artificial delay for demo purposes
        sleep();

        //"Undeploy" component
        deployedComponents.remove(component);
        rerunValueLogs.remove(component);
    }

    /**
     * Undeploys a component from its corresponding remote device if it is currently running.
     *
     * @param component The component to undeploy
     */
    @Override
    public void undeployIfRunning(Component component) {
        // Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        // Determine component state
        ComponentState componentState = retrieveComponentState(component);

        // Undeploy component if running
        if (ComponentState.RUNNING.equals(componentState)) {
            this.undeployComponent(component);
        }
    }

    /**
     * Checks whether a given component is currently running on its corresponding remote device.
     *
     * @param component The component to check
     * @return True, if the component is running; false otherwise
     */
    @Override
    public boolean isComponentRunning(Component component) {
        return deployedComponents.containsKey(component) &&
                ComponentState.RUNNING.equals(deployedComponents.get(component));
    }

    /**
     * Checks whether a given component is currently deployed on its corresponding remote device.
     *
     * @param component The component to check
     * @return True, if the component is deployed; false otherwise
     */
    @Override
    public boolean isComponentDeployed(Component component) {
        return deployedComponents.containsKey(component);
    }

    /**
     * Resets all components such that they do not displayed as deployed anymore.
     */
    public void resetDeployedComponents() {
        deployedComponents.clear();
    }

    /**
     * Adds {@link ValueLog}s to the sending queue of ValueLogs to enable a dynamic configuration of ValueLogs
     * which should be sent by a certain component if it is started. Overwrites old entries.
     *
     * @param component       The component of which the ValueLogs to sent should be defined
     * @param valueLogsToSend A FIFO queue of ValueLogs which should be sent to the ValueLogReceiver
     */
    public void addRerunValueLogsForComponent(Component component, Queue<ValueLog> valueLogsToSend) {
        rerunValueLogs.put(component, valueLogsToSend);
    }

    /**
     * Checks whether there are certain scheduled ValueLogs for a component which are not
     * randomly generated.
     *
     * @param component The component of which to check the scheduled ValueLog status.
     * @return True if there are certain value logs scheduled for this component.
     */
    private boolean checkIfRerunValueLogsAreScheduledForComponent(Component component) {
        if (!rerunValueLogs.containsKey(component)) {
            return false;
        } else {
            return !rerunValueLogs.get(component).isEmpty();
        }
    }

    /**
     * Generates value logs for the running components with a fixed time interval.
     */
    @Scheduled(fixedDelay = VALUE_LOG_INTERVAL)
    private void generateValueLogs() {
        //Stream the deployed components and filter for running ones
        deployedComponents.keySet().stream().filter(c -> ComponentState.RUNNING.equals(deployedComponents.get(c))).forEach(component -> {

            ValueLog valueLog = new ValueLog();

            if (checkIfRerunValueLogsAreScheduledForComponent(component)) {
                valueLog = rerunValueLogs.get(component).poll();
                valueLog.setTime(Instant.now());
            } else {
                //Create new value log for the current component
                valueLog.setTime(Instant.now());
                valueLog.setIdref(component.getId());
                valueLog.setComponent(component.getComponentTypeName());
                valueLog.setTopic(component.getTopicName());
                valueLog.setMessage("Randomly generated");

                // Get the data model of the component to generate a fitting value
                DataModelTree dataModel = dataModelCache.getDataModelOfComponent(component.getId());

                // Get example json payload from the data model, parse it to a document and set it as value log value
                try {
                    JSONObject jsonValue = new JSONObject(dataModel.getJSONExample());
                    valueLog.setValue(ValueLogReceiveVerifier.validateJsonValueAndGetDocument(
                            jsonValue.getJSONObject("value"),
                            dataModel
                    ));
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }
            }

            //Inject value log into the receiver component
            valueLogReceiver.injectValueLog(valueLog);
        });
    }

    /**
     * Generates a random double value to be used within a value log.
     *
     * @return The generated random value
     */
    private Document generateValueLogValue() {
        //Generate random double
        double value = ThreadLocalRandom.current().nextDouble(VALUE_LOG_MIN, VALUE_LOG_MAX);
        //Round the generated value to two decimals
        value = Math.round(value * 1.0e2) / 1.0e2;

        // Store the value in a document
        Document retDocument = new Document();
        retDocument.append("value", value);

        return retDocument;
    }

    /**
     * Makes the current thread sleep for a defined amount of milliseconds. This allows to introduce an artificial delay
     * into the deployment operations for demonstration purposes.
     */
    private void sleep() {
        try {
            Thread.sleep(DemoDeployer.DEPLOYMENT_DELAY);
        } catch (InterruptedException ignored) {
        }
    }
}
