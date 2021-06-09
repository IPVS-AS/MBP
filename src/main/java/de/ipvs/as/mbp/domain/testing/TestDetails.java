package de.ipvs.as.mbp.domain.testing;

import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import org.springframework.data.annotation.Id;

import javax.persistence.GeneratedValue;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@MBPEntity(createValidator = TestDetailsCreateValidator.class, deleteValidator = TestDetailsDeleteValidator.class)
public class TestDetails extends UserEntity {

    public TestDetails() {

    }

    @Id
    @GeneratedValue
    private String id;

    @Indexed(unique = true)
    private String name;

    @DBRef
    private List<Sensor> sensor;

    @DBRef
    private List<Rule> rules;

    private List<String> ruleNames;

    private List<List<ParameterInstance>> config;

    private List<String> type;


    private Map<String, LinkedHashMap<Long, Double>> simulationList;

    private boolean triggerRules;

    private boolean useNewData;

    /**
     * Returns, if the selected rules of the test should be triggered or not
     *
     * @return triggerRules
     */
    public boolean isTriggerRules() {
        return triggerRules;
    }

    /**
     * Sets, if the selected rules of the test should be triggered or not
     *
     * @param triggerRules rules that should be triggered or not through the test
     */
    public void setTriggerRules(boolean triggerRules) {
        this.triggerRules = triggerRules;
    }

    /**
     * Returns a list of the last simulated values for the test.
     *
     * @return simulationList
     */
    public Map<String, LinkedHashMap<Long, Double>> getSimulationList() {
        return simulationList;
    }

    /**
     * Sets a list of the last simulated values for the test.
     *
     * @param simulationList list of simulated values of the sensors
     */
    public void setSimulationList(Map<String, LinkedHashMap<Long, Double>> simulationList) {
        this.simulationList = simulationList;
    }



    /**
     * Returns the name of the test.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the test.
     *
     * @param name of the test
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the id of the test.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the test.
     *
     * @param id of the test
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the type of the sensor of the  application to be tested.
     *
     * @return type
     */
    public List<String> getType() {
        return type;
    }

    /**
     * Sets the type of the sensor of the application to be tested.
     *
     * @param type of the sensor-simulator used in the test
     */
    public void setType(List<String> type) {
        this.type = type;
    }

    /**
     * Returns the sensors of the application to be tested.
     *
     * @return sensors
     */
    public List<Sensor> getSensor() {
        return sensor;
    }

    /**
     * Sets the sensors of the application to be tested.
     *
     * @param sensor used in the test
     */
    public void setSensor(List<Sensor> sensor) {
        this.sensor = sensor;
    }

    /**
     * Returns the rules of the application to be tested.
     *
     * @return rules
     */
    public List<Rule> getRules() {
        return rules;
    }

    /**
     * Sets the rules of the application to be tested.
     *
     * @param rules which should be observed though the test
     */
    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    /**
     * Returns the configurations of the user for the sensor-simulator.
     *
     * @return config
     */
    public List<List<ParameterInstance>> getConfig() {
        return config;
    }

    /**
     * Sets the configurations of the user for the sensor-simulator.
     *
     * @param config configurations of the sensor-simulator
     */
    public void setConfig(List<List<ParameterInstance>> config) {
        this.config = config;
    }
    /**
     * Returns a list with the names of the rules of the application to be tested.
     *
     * @return List of rule names to be tested
     */
    public List<String> getRuleNames() {
        return ruleNames;
    }

    /**
     * Sets a List of the rule names of the application to be tested.
     *
     * @param ruleNames to be tested
     */
    public void setRuleNames(List<String> ruleNames) {
        this.ruleNames = ruleNames;
    }

    /**
     * Returns the information whether data from the last test run should be used again or not.
     *
     * @return useNewData boolean whether data from the last test run should be used again or not.
     */
    public boolean isUseNewData() {
        return useNewData;
    }

    /**
     * Sets the information whether data from the last test run should be used again or not.
     *
     * @param useNewData boolean whether data from the last test run should be used again or not.
     */
    public void setUseNewData(boolean useNewData) {
        this.useNewData = useNewData;
    }
}

