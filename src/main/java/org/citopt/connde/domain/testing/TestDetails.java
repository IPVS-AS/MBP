package org.citopt.connde.domain.testing;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.rules.Rule;
import org.springframework.data.annotation.Id;

import javax.persistence.GeneratedValue;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Document
public class TestDetails {

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

    private List<List<ParameterInstance>> config;

    private List<String> type;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm:ss")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date startTestTime = null;

    private long startTimeUnix;

    private long EndTimeUnix;

    private String successful;

    private boolean pdfExists;

    private String pathPDF;

    private Map<String, List<Double>> triggerValues;

    private List<String> rulesExecuted;

    private Map<String, Map<Long, Double>> simulationList;

    private boolean triggerRules;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm:ss")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date endTestTime = null;

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
     * Retruns the path of the test report
     *
     * @return pathPDF
     */
    public String getPathPDF() {
        return pathPDF;
    }

    /**
     * Sets the path of the test report
     *
     * @param pathPDF path, where the test report is saved
     */
    public void setPathPDF(String pathPDF) {
        this.pathPDF = pathPDF;
    }

    /**
     * Returns, wether a test report exists
     *
     * @return pdfExists
     */
    public boolean isPdfExists() {
        return pdfExists;
    }

    /**
     * Sets, wether a test report exists
     *
     * @param pdfExists boolean if the test report exists or not
     */
    public void setPdfExists(boolean pdfExists) {
        this.pdfExists = pdfExists;
    }

    /**
     * Returns a list of the last simulated values for the test.
     *
     * @return simulationList
     */
    public Map<String, Map<Long,Double>> getSimulationList() {
        return simulationList;
    }

    /**
     * Sets a list of the last simulated values for the test.
     *
     * @param simulationList list of simulated values of the sensors
     */
    public void setSimulationList(Map<String, Map<Long, Double>> simulationList) {
        this.simulationList = simulationList;
    }

    /**
     * Returns, wether the test was successful or not.
     *
     * @return successful
     */
    public String getSuccessful() {
        return successful;
    }

    /**
     * Sets, wether the test was successful or not.
     *
     * @param successful test success
     */
    public void setSuccessful(String successful) {
        this.successful = successful;
    }

    /**
     * Returns a map of all rules and their trigger values through the test.
     *
     * @return triggerValues
     */
    public Map<String, List<Double>> getTriggerValues() {
        return triggerValues;
    }

    /**
     * Sets a map of all rules and their trigger values through the test.
     *
     * @param triggerValues values which triggered rules through the test
     */
    public void setTriggerValues(Map<String, List<Double>> triggerValues) {
        this.triggerValues = triggerValues;
    }

    /**
     * Returns a list of all rules executed through the test.
     *
     * @return rulesExecuted
     */
    public List<String> getRulesExecuted() {
        return rulesExecuted;
    }

    /**
     * Sets a list of all rules executed through the test.
     *
     * @param rulesExecuted list of executed rules through the test
     */
    public void setRulesExecuted(List<String> rulesExecuted) {
        this.rulesExecuted = rulesExecuted;
    }

    /**
     * Returns the start time of the test in unix format.
     *
     * @return start time in Unix format
     */
    public Integer getStartTimeUnix() {
        return Math.toIntExact(startTimeUnix);
    }

    /**
     * Sets the start time of the test in unix format.
     *
     * @param startTimeUnix start time in unix format
     */
    public void setStartTimeUnix(long startTimeUnix) {
        this.startTimeUnix = startTimeUnix;
    }

    /**
     * Returns the end time of the test in unix format.
     *
     * @return end time in unix format
     */
    public Integer getEndTimeUnix() {
        return Math.toIntExact(EndTimeUnix);
    }

    /**
     * Sets the start time of the test in unix format.
     *
     * @param setEndTimeUnix end time in unix format
     */
    public void setEndTimeUnix(long setEndTimeUnix) {
        this.EndTimeUnix = setEndTimeUnix;
    }

    /**
     * Returns the end time of the test in "dd.MM.yyyy HH:mm:ss" format.
     *
     * @return endTestTime
     */
    public Date getEndTestTime() {
        return endTestTime;
    }

    /**
     * Returns the start time of the test in "dd.MM.yyyy HH:mm:ss" format.
     *
     * @return endTestTime
     */
    public Date getStartTestTime() {
        return startTestTime;
    }

    /**
     * Sets the end time of the tests in unix and "dd.MM.yyyy HH:mm:ss" format.
     */
    public void setEndTestTimeNow() {
        this.endTestTime = new Date();
        long date = new Date().getTime() / 1000;
        setEndTimeUnix(date);
    }

    /**
     * Sets the start time of the tests in unix and "dd.MM.yyyy HH:mm:ss" format.
     */
    public void setStartTestTimeNow() {
        this.startTestTime = new Date();
        long date = new Date().getTime() / 1000;
        setStartTimeUnix(date);
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

}
