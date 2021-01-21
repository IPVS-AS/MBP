package de.ipvs.as.mbp.service.receiver;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTreeNode;
import de.ipvs.as.mbp.domain.valueLog.ValueLog;
import de.ipvs.as.mbp.domain.visualization.tmp.VisualsDataTreesCollections;
import de.ipvs.as.mbp.repository.DataModelTreeCache;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * then added to the value log repository.
 * Provides methods for handling incoming Mqtt events and parsing incoming value messages to value logs
 * which are then passed to the observers of the ValueLogReceiver.
 */
class ValueLogReceiverArrivalHandler implements MqttCallback {

    //JSON key names
    private static final String JSON_KEY_COMPONENT_TYPE = "component";
    private static final String JSON_COMPONENT_ID = "id";
    private static final String JSON_KEY_VALUE = "value";

    // Cache of data model trees to provide fast supply
    private DataModelTreeCache dataModelTreeCache;

    //Set of observers
    private Set<ValueLogReceiverObserver> observerSet;

    /**
     * Creates a new value logger event handler.
     *
     * @param observerSet The set of observers to notify about incoming value logs.
     */
    ValueLogReceiverArrivalHandler(Set<ValueLogReceiverObserver> observerSet) {
        this.observerSet = observerSet;
        // Get the bean of the data model tree cache
        this.dataModelTreeCache = DynamicBeanProvider.get(DataModelTreeCache.class);
    }

    /**
     * Handles the case that the mqtt client lost connection to the broker.
     *
     * @param throwable Throwable that indicates the issue
     */
    @Override
    public void connectionLost(Throwable throwable) {
        System.err.println("Mqtt client lost connection.");
        try {
            throw new MqttException(throwable);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles incoming m
     * qtt messages, i.e. parses the incoming value message to a value log which is then
     * passed to the observers of the ValueLogReceiver.
     *
     * @param topic       The topic under which the message was sent
     * @param mqttMessage The received value log message
     * @throws JSONException  In case the message could not be parsed
     * @throws ParseException In case a date value field could not be parsed
     */
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws JSONException, ParseException {
        //Record current time
        Instant time = ZonedDateTime.now().toInstant();

        //Extract QoS
        int qos = mqttMessage.getQos();

        //Extract message string from the message object
        String message = new String(mqttMessage.getPayload());

        //Create a json object from the message
        JSONObject json = new JSONObject(message);

        //Extract all required data from the message and add it to a new value log object
        ValueLog valueLog = new ValueLog();

        String componentType = json.getString(JSON_KEY_COMPONENT_TYPE);
        String componentID = json.getString(JSON_COMPONENT_ID);

        //Set value log fields
        valueLog.setTopic(topic);
        valueLog.setMessage(message);
        valueLog.setQos(qos);
        valueLog.setTime(time);
        valueLog.setIdref(componentID);
        valueLog.setValue(ValueLogReceiveVerifier.validateJsonValueAndGetDocument(
                // Retrieve the root node of the value json object which must be part of the mqtt message by convention
                json.getJSONObject("value"),
                // Get the data model tree of the component as this is needed to infer the right database types
                dataModelTreeCache.getDataModelOfSensor(componentID)
        ));

        //TODO ONLY TEST REMOVE LATER
        VisualsDataTreesCollections coll = new VisualsDataTreesCollections();
        DataModelTree tree = dataModelTreeCache.getDataModelOfSensor(componentID);
        Map.Entry<List<DataModelTreeNode>, List<List<Map<String, String>>>>
                // z or newRoot
                test = tree.findSubtreeByTypes(coll.getVisById("string1").getVisualisableDataModels().get(0));
        System.out.println("All sub tree roots:");
        for (DataModelTreeNode n : test.getKey()) {
            System.out.print(n.getName() + ", ");
        }
        System.out.println("");

        List<List<Map<String, String>>> stringMap = test.getValue();
        for (List<Map<String, String>> stringMapping : stringMap) {
            System.out.println("---START MAPPING Subtree---");
            for (Map<String, String> m : stringMapping) {
                for (Map.Entry<String, String> e : m.entrySet()) {
                    System.out.println(e.getKey() + ": " + e.getValue());
                }
                System.out.println("---END OF THIS MAPPING---");
            }
        }

        // TODO REMOVE END

        valueLog.setComponent(componentType);

        //Notify all observers
        notifyObservers(valueLog);
    }

    /**
     * Handle events that are triggered when the delivery of a message was completed.
     *
     * @param iMqttDeliveryToken Delivery token of the message
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }

    /**
     * Notifies all observers of the ValueLogReceiver about the received value log.
     *
     * @param valueLog The received value log
     */
    private void notifyObservers(ValueLog valueLog) {
        //Sanity check
        if (valueLog == null) {
            throw new IllegalArgumentException("Value log must not be null.");
        }

        //Iterate over all observers and notify them
        for (ValueLogReceiverObserver observer : observerSet) {
            observer.onValueReceived(valueLog);
        }
    }
}