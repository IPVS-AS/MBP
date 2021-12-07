package de.ipvs.as.mbp.repository;


import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.data_model.DataModel;
import de.ipvs.as.mbp.domain.data_model.DataTreeNode;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * <p>Manages access to stored {@link DataModelTree}s. To avoid many read operations on the
 * {@link DataModel} repository and to reduce the need of converting the saved date model structure
 * each time to a {@link DataModelTree}, it caches recently used {@link DataModelTree}s
 * with the respective {@link de.ipvs.as.mbp.domain.component.Component#getId() component id}
 * as key.
 */
@Component
public class DataModelTreeCache {

    /**
     * Specifies how old a cache date entry can be, being still part of the cache.
     */
    private static final int UNUSED_DAYS_REMOVAL_CRITERION = 1;

    /**
     * Map used as the cache data structure to store data models together with their components id.
     * The dates are part of the value to enable the removal of old cache entries by using
     * {@link DataModelTreeCache#removeOldCacheEntries()}.
     */
    private final Map<String, Map.Entry<DataModelTree, Date>> cachedDataModels;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;

    /**
     * Data model with one "value" double data field
     */
    DataModelTree monitoringOperatorDataModel;

    private DataModelTreeCache() {
        // Init the data models cache
        this.cachedDataModels = new HashMap<>();
        this.monitoringOperatorDataModel = createOperatorDataModel();
    }

    /**
     * Create a {@link DataModelTree} for monitoring operators who dont have a user definied
     * data model but a fixed data model with one double field.
     * @return The data model of operators.
     */
    private DataModelTree createOperatorDataModel() {
        // The root object of the data model
        DataTreeNode root  = new DataTreeNode();
        root.setType("object");
        root.setName("root");
        root.setChildren(Collections.singletonList("value"));
        root.setParent("");

        // The child double field of the data model
        DataTreeNode valueChild = new DataTreeNode();
        valueChild.setType("double");
        valueChild.setName("value");
        valueChild.setChildren(new ArrayList<>());
        valueChild.setParent("root");

        return new DataModelTree(Arrays.asList(root, valueChild));
    }

    /**
     * Returns the data model by a given component id.
     *
     * @param componentId   MongoDB ObjectID of the entity
     * @return the data model used by this entity. If the componentId is not known a default data model with
     * one value double field is returned which is also used by monitoring operators by default.
     */
    public DataModelTree getDataModelOfComponent(String componentId) {
        // Is the data model is already cached?
        if (this.cachedDataModels.containsKey(componentId)) {
            // Yes, the data model is already present in the application logic --> just return it (and update the date before)
            DataModelTree tree = this.cachedDataModels.get(componentId).getKey();
            this.cachedDataModels.put(componentId, new AbstractMap.SimpleEntry<>(tree, new Date()));
            return tree;
        } else {
            // No, the data model is not present in the application logic yet --> get it from the db and add it to the
            // data model cache to avoid further db accesses
            DataModel dataModel = getDataModelByComponentIdFromDB(componentId);

            if (dataModel == null) {
                // No data model of the component could be found, maybe it is a monitoring operator --> give the monitoring data model back
                return this.monitoringOperatorDataModel;
            }

            // Build the data model tree from the data model
            DataModelTree tree = new DataModelTree(dataModel.getTreeNodes());
            this.cachedDataModels.put(componentId, new AbstractMap.SimpleEntry<>(tree, new Date()));
            return tree;
        }
    }

    /**
     * Removes all cache entries of all {@link DataModelTree}s of which the last mqtt
     * message sent is older than one day.
     * For this, the system clock (and system time zone) is used internally.
     */
    public void removeOldCacheEntries() {
        // Get the current time
        ZonedDateTime now = ZonedDateTime.now();
        // Get the current time minus 1 day
        ZonedDateTime oneDayAgo = now.plusDays(-UNUSED_DAYS_REMOVAL_CRITERION);

        // Go through all cache entries and check if the entry is older than one day
        for (Map.Entry<String, Map.Entry<DataModelTree, Date>> e : this.cachedDataModels.entrySet()) {
            if (e.getValue().getValue().toInstant().isBefore(oneDayAgo.toInstant())) {
                // Entry is older than 1 day --> remove
                this.cachedDataModels.remove(e.getKey());
            }
        }
    }

    /**
     * Tries to find the component with the given id either in the sensor repository or in the actuator
     * repository and returns it data model.
     *
     * @param componentId The id of the component.
     */
    private DataModel getDataModelByComponentIdFromDB(String componentId) {
        // The component which should be retrieved
        de.ipvs.as.mbp.domain.component.Component componentToReturn = null;

        // First check if the sensor repository knows the id
        Optional<Sensor> componentSensorOpt = sensorRepository.findById(componentId);
        componentToReturn = componentSensorOpt.orElse(null);

        if (componentToReturn == null) {
            // Now check if the actuator repository knows the id
            Optional<Actuator> componentActuatorOpt = actuatorRepository.findById(componentId);
            componentToReturn = componentActuatorOpt.orElse(null);

            if (componentToReturn != null) {
                // Component id found in the actuator repository --> return the data model
                return componentToReturn.getOperator().getDataModel();
            } else {
                // Component also not found in the actuator repository --> return null
                return null;
            }

        } else {
            // Component already found in the sensor repository --> return the data model
            return componentToReturn.getOperator().getDataModel();
        }
    }
}

