package de.ipvs.as.mbp.domain.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.GeneratedValue;

import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import de.ipvs.as.mbp.domain.visualization.repo.ActiveVisualization;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Document super class for components (actuators, sensors, ...).
 */
@Document
public abstract class Component extends UserEntity {

    @Id
    @GeneratedValue
    private String id;

    @Indexed(unique = true)
    private String name;

    @Indexed
    private String componentType;

    @DBRef
    private Operator operator;

    @DBRef
    private Device device;

    private List<ActiveVisualization> activeVisualizations = new ArrayList<>();

    private String idOfLastAddedVisualization;

    public String getId() {
        return id;
    }

    public Component setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Component setName(String name) {
        this.name = name;
        return this;
    }

    public String getComponentType() {
        return componentType;
    }

    public Component setComponentType(String componentType) {
        this.componentType = componentType;
        return this;
    }

    public Operator getOperator() {
        return operator;
    }

    public Component setOperator(Operator operator) {
        this.operator = operator;
        return this;
    }

    public Device getDevice() {
        return device;
    }

    public Component setDevice(Device address) {
        this.device = address;
        return this;
    }

    public String getTopicName() {
        return getComponentTypeName() + "/" + id;
    }

    public abstract String getComponentTypeName();

    /**
     * Returns the list of all currently active visualizations.
     *
     * @return The list of currently active visualizations
     */
    public List<ActiveVisualization> getActiveVisualizations() {
        return activeVisualizations;
    }

    /**
     * Sets the list of all currently active visualizations.
     *
     * @param activeVisualizations The list to set
     */
    public void setActiveVisualizations(List<ActiveVisualization> activeVisualizations) {
        this.activeVisualizations = activeVisualizations;
    }

    /**
     * Adds a visualization to the list of currently active visualiazations.
     *
     * @param activeVisualization The visualization to add
     */
    public void addActiveVisualization(ActiveVisualization activeVisualization) {
        //Null check
        if (activeVisualization == null) {
            throw new IllegalArgumentException("The visualization must not be null.");
        }
        this.activeVisualizations.add(activeVisualization);
        this.idOfLastAddedVisualization = activeVisualization.getInstanceId();
    }

    /**
     * Removes a certain {@link ActiveVisualization}, given by ts ID, from the list of active visualizations.
     *
     * @param visualizationId The ID of the {@link ActiveVisualization} to remove
     */
    public void removeActiveVisualization(String visualizationId) {
        //Sanity check
        if ((visualizationId == null) || (visualizationId.isEmpty())) {
            throw new IllegalArgumentException("The given ID must not be null or empty.");
        }

        //Remove visualization by its ID
        this.activeVisualizations.removeIf(vis -> vis.getInstanceId().equals(visualizationId));
    }

    /**
     * Returns the ID of the most recently added visualization.
     *
     * @return The visualization ID
     */
    public String getIdOfLastAddedVisualization() {
        return idOfLastAddedVisualization;
    }

    /**
     * Sets the ID of the most recently added visualization.
     *
     * @param idOfLastAddedVisualization The ID to set
     */
    public void setIdOfLastAddedVisualization(String idOfLastAddedVisualization) {
        this.idOfLastAddedVisualization = idOfLastAddedVisualization;
    }

    @Override
    public String toString() {
        return "Component{" + "id=" + id + ", name=" + name + ", type=" + operator + '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Component component = (Component) o;

        //Check if IDs are available
        if ((this.id == null || component.id == null)) {
            return false;
        }

        //Compare IDs
        return this.id.equals(component.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
