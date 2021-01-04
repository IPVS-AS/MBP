package de.ipvs.as.mbp.service.testing.analyzer;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.rules.RuleAction;
import de.ipvs.as.mbp.repository.RuleRepository;
import de.ipvs.as.mbp.repository.TestDetailsRepository;
import de.ipvs.as.mbp.service.receiver.ValueLogReceiver;
import de.ipvs.as.mbp.service.testing.PropertiesService;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * The component TestReport is used for the creation of test reports for tests of applications using the testing-tool
 */
@Component
public class TestReport {


    @Autowired
    TestDetailsRepository testDetailsRepository;

    @Autowired
    TestReport testEngine;

    @Autowired
    ValueLogReceiver valueLogReceiver;

    @Autowired
    RuleRepository ruleRepository;

    @Autowired
    private PropertiesService propertiesService;

    @Value("#{'${testingTool.plannedSimulators}'.split(',')}")
    List<String> PLANNED_SIMULATORS;
    @Value("#{'${testingTool.sensorSimulators}'.split(',')}")
    List<String> SIMULATOR_LIST;


    //  private static final List<String> PLANNED_SIMULATORS = planned_simulators;
    // private final List<String> SIMULATOR_LIST = simulator_list;
    private final String RERUN_IDENTIFIER;
    private final String CONFIG_SENSOR_NAME_KEY;

    //To resolve ${} in @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    public TestReport() throws IOException {
        propertiesService = new PropertiesService();
        RERUN_IDENTIFIER = propertiesService.getPropertiesString("testingTool.RerunIdentifier");
        CONFIG_SENSOR_NAME_KEY = propertiesService.getPropertiesString("testingTool.ConfigSensorNameKey");

    }

    // Date formatter
    final String datePattern = "dd-MM-yyyy HH:mm:ss";
    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);

    // final background colors and fonts
    final BaseColor lightBlue = new BaseColor(191, 220, 227);
    final BaseColor darkBlue = new BaseColor(157, 213, 227);
    final BaseColor darkGrey = new BaseColor(117, 117, 117);
    final Font white = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
    final Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.BLACK);
    final Font boldUnderlined = new Font(Font.FontFamily.HELVETICA, 17, Font.BOLD | Font.UNDERLINE, BaseColor.BLACK);


    /**
     * Generates the Test-Report with the Chart of the simulated Values and other important information for the user.
     *
     * @param testId      id of the specific test
     * @param rulesBefore detail information of the selected rules before the test
     * @return path where the TestReport can be found
     */
    public String generateTestReport(String testId, List<Rule> rulesBefore) throws Exception {
        int counterRules = 0;
        TestDetails test = testDetailsRepository.findById(testId).get();
        Document doc = new Document();


        // Create a new pdf, which is named with the ID of the specific test
        File testReport = new File(testId + "_" + test.getEndTimeUnix() + ".pdf");
        Path wholePath = Paths.get(testReport.getAbsolutePath());
        String path = wholePath.getParent().toString();

        FileOutputStream pdfFileout = new FileOutputStream(testReport);
        PdfWriter.getInstance(doc, pdfFileout);
        doc.open();


        // Title of the test report
        Paragraph title = new Paragraph("Test-Report: " + test.getName(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);


        // Creating an ImageData object from the graph of sensor values, which was previously created with the GraphPlotter
        Image graphSensorVal = Image.getInstance(testId + ".gif");
        graphSensorVal.setAlignment(Element.ALIGN_CENTER);

        // information about the success of the test and the start/end times
        PdfPTable successInfo = getGeneralInfo(test);

        // Test-Details
        Paragraph subtitle = new Paragraph("Test-Details: ", boldUnderlined);
        subtitle.setAlignment(Element.ALIGN_CENTER);

        // Sensor information
        PdfPTable simulationSensors = getSimulationConfig(test);
        PdfPTable realSensors = getRealSensorConfig(test);

        // Actuator information
        PdfPTable actuatorInfos = getActuatorInfos();

        // Rule information
        PdfPTable ruleInfos = getRuleInfos(test);
        ruleInfos.setSpacingAfter(14f);

        // Rule details
        PdfPTable ruleDetails = new PdfPTable(4);
        ruleDetails.setSpacingAfter(15f);

        // add all components to the test report pdf
        doc.add(title);
        doc.add(Chunk.NEWLINE);
        doc.add(successInfo);
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
        doc.add(graphSensorVal);
        doc.newPage();
        doc.add(subtitle);
        doc.add(Chunk.NEWLINE);
        doc.add(simulationSensors);
        doc.add(Chunk.NEWLINE);
        doc.add(realSensors);
        doc.add(Chunk.NEWLINE);
        doc.add(actuatorInfos);
        doc.add(Chunk.NEWLINE);
        doc.add(ruleInfos);

        // Add new table to get the details for each rule
        for (Rule rule : rulesBefore) {
            if (test.isUseNewData()) {
                ruleDetails = getRuleDetails(test, rule, counterRules);
                counterRules += 1;
            } else {
                if (rule.getName().contains(RERUN_IDENTIFIER)) {
                    ruleDetails = getRuleDetails(test, rule, counterRules);
                    counterRules += 1;
                }
            }
            doc.add(ruleDetails);
        }

        doc.close();
        return path;
    }

    /**
     * Return a table with the general information of the test (test rerun, success, start/end time).
     *
     * @param test test for which the test report is created
     * @return table with general information of the test
     */
    private PdfPTable getGeneralInfo(TestDetails test) {
        String success;
        PdfPTable generalInfo = new PdfPTable(4);
        generalInfo.setWidthPercentage(100f);

        // information if test was a rerun
        if (!test.isUseNewData()) {
            generalInfo.addCell(tableCell("This was a Test rerun.", lightBlue, 4));
        }

        // Success of the Test
        if (test.getSuccessful().equals("Successful")) {
            success = "Yes";
        } else {
            success = "No";
        }

        generalInfo.addCell(tableCell("Successful", lightBlue, 2));
        generalInfo.addCell(tableCell(success, null, 2));

        //Start-Time pdf
        String startTestTime = simpleDateFormat.format(test.getStartTestTime());
        generalInfo.addCell(tableCell("Start-Time", lightBlue, 2));
        generalInfo.addCell(tableCell(startTestTime, null, 2));

        // End-Time pdf
        String endTestTime = simpleDateFormat.format(test.getEndTestTime());
        generalInfo.setWidthPercentage(100f);
        generalInfo.addCell(tableCell("End-Time", lightBlue, 2));
        generalInfo.addCell(tableCell(endTestTime, null, 2));


        return generalInfo;
    }


    /**
     * Returns a table with the included sensor simulators within the test and the user defined configurations of them or
     * a short information if no simulator was included.
     *
     * @param test test for which the test report is created
     * @return table with all sensor simulators and the configurations of them
     **/
    private PdfPTable getSimulationConfig(TestDetails test) {
        boolean sensorSimulation = false;
        int counter = 0;
        String rerunInfo = "";
        String originalName = "";

        if (!test.isUseNewData()) {
            rerunInfo = "(" + RERUN_IDENTIFIER + ")";
        }

        // Table configurations
        PdfPTable tableSensorSim = new PdfPTable(4);
        tableSensorSim.setWidthPercentage(100f);

        tableSensorSim.addCell(headerCell("Simulated Sensor(s)"));

        for (String type : test.getType()) {

            // Get the original name of the rerun sensor
            if (type.contains(RERUN_IDENTIFIER)) {
                String[] split = type.split(RERUN_IDENTIFIER);
                originalName = split[1];
            }
            // Check if (rerun) sensor is simulated sensor
            if (SIMULATOR_LIST.contains(type) || SIMULATOR_LIST.contains(originalName)) {
                sensorSimulation = true;
                counter += 1;

                // Get general config aspects for every simulated sensor
                ArrayList<String> generalConfig = getGeneralConfig(test, type);

                //Sensor-Type
                tableSensorSim.addCell(tableCell(counter + ".: Sensor-Type", darkBlue, 2));
                tableSensorSim.addCell(tableCell(rerunInfo + generalConfig.get(0), darkBlue, 2));

                //Event
                tableSensorSim.addCell(tableCell("Event", lightBlue, 2));
                tableSensorSim.addCell(tableCell(generalConfig.get(1), null, 2));

                //Anomaly
                tableSensorSim.addCell(tableCell("Anomaly", lightBlue, 2));
                tableSensorSim.addCell(tableCell(generalConfig.get(2), null, 2));


                // Planned simulation
                // Get config aspects available for the planned simulations
                Map<String, String> plannedConfig = plannedSim(test, type);

                // Check if simulation was planned
                if (!plannedConfig.isEmpty()) {

                    // Simulation time
                    tableSensorSim.addCell(tableCell("Simulation-Time", lightBlue, 2));
                    tableSensorSim.addCell(tableCell(plannedConfig.get("simTime") + " hours", null, 2));

                    // Amount Events
                    tableSensorSim.addCell(tableCell("Amount Events", lightBlue, 2));
                    tableSensorSim.addCell(tableCell(plannedConfig.get("amountEvents"), null, 2));

                    // Amount Anomalies
                    tableSensorSim.addCell(tableCell("Amount Anomalies", lightBlue, 2));
                    tableSensorSim.addCell(tableCell(plannedConfig.get("amountAnomalies"), null, 2));
                }
            }
        }

        // Information if no sensor simulator was included in the test
        if (!sensorSimulation) {
            tableSensorSim.addCell(tableCell("No Sensor-Simulator was integrated into the test.", null, 4));
        }

        return tableSensorSim;
    }

    /**
     * Converts the general configuration of a simulated sensor into a readable output for the test report.
     *
     * @param test       test for which the test report is created
     * @param sensorType of the sensor to get out the configuration
     * @return List of general configurations (without planned sensor configurations)
     */
    public ArrayList<String> getGeneralConfig(TestDetails test, String sensorType) {

        int event = 0;
        int anomaly = 0;
        java.util.List<ParameterInstance> config = null;


        // Get the configurations especially for the given type of sensor
        for (java.util.List<ParameterInstance> configSensor : test.getConfig()) {
            for (ParameterInstance parameterInstance : configSensor) {
                if (parameterInstance.getName().equals(CONFIG_SENSOR_NAME_KEY) && parameterInstance.getValue().equals(sensorType)) {
                    config = configSensor;
                    break;
                }
            }
        }

        // Get the kind of event and anomaly as a number
        for (ParameterInstance parameterInstance : Objects.requireNonNull(config)) {
            if (parameterInstance.getName().equals("event")) {
                event = Integer.parseInt(String.valueOf(parameterInstance.getValue()));
            }
            if (parameterInstance.getName().equals("anomaly")) {
                anomaly = Integer.parseInt(String.valueOf(parameterInstance.getValue()));
            }
        }


        return convertGeneralConfig(sensorType, event, anomaly);
    }


    /**
     * Returns a Map with the configurations time and amount of events/anomalies of a planned simulation.
     *
     * @param test       for which the test report is created
     * @param sensorType of the sensor to get out the configuration
     * @return empty map or map with configurations of a planned sensor
     */
    private Map<String, String> plannedSim(TestDetails test, String sensorType) {
        Map<String, String> plannedConfig = new HashMap<>();

        if (PLANNED_SIMULATORS.contains(sensorType)) {
            for (java.util.List<ParameterInstance> configSensor : test.getConfig()) {
                for (ParameterInstance parameterInstance : configSensor) {
                    switch (parameterInstance.getName()) {
                        case "simTime":
                            plannedConfig.put("simTime", String.valueOf(parameterInstance.getValue()));
                            break;
                        case "amountEvents":
                            plannedConfig.put("amountEvents", String.valueOf(parameterInstance.getValue()));
                            break;
                        case "amountAnomalies":
                            plannedConfig.put("amountAnomalies", String.valueOf(parameterInstance.getValue()));
                            break;
                    }
                }
            }
        }
        return plannedConfig;
    }


    /**
     * Returns a table with the included real sensors within the test and the user defined configurations of them or
     * a short information if no real sensor was included.
     *
     * @param test test for which the test report is created
     * @return table with user configurations of the test
     */
    private PdfPTable getRealSensorConfig(TestDetails test) {
        int counter = 0;
        boolean realSensors = false;
        String rerunInfo = "";
        String rerunName = "";
        if (!test.isUseNewData()) {
            rerunInfo = "(" + RERUN_IDENTIFIER + ")";
            rerunName = RERUN_IDENTIFIER;
        }

        // Table configurations
        PdfPTable tableRealSensors = new PdfPTable(4);
        tableRealSensors.setWidthPercentage(100f);

        tableRealSensors.addCell(headerCell("Real Sensor(s)"));

        for (String type : test.getType()) {
            // Check if sensor is no sensor simulator
            if (!SIMULATOR_LIST.contains(rerunName + type)) {
                realSensors = true;
                counter += 1;

                //Sensor-Type
                tableRealSensors.addCell(tableCell(counter + ".: Sensor-Type", darkBlue, 2));
                tableRealSensors.addCell(tableCell(rerunInfo + type, darkBlue, 2));


                // Get sensor parameters and their values
                for (java.util.List<ParameterInstance> configSensor : test.getConfig()) {
                    for (ParameterInstance parameterInstance : configSensor) {
                        if (parameterInstance.getValue().equals(type)) {
                            for (ParameterInstance parameterInstance2 : configSensor) {
                                if (!parameterInstance2.getName().equals(CONFIG_SENSOR_NAME_KEY)) {
                                    tableRealSensors.addCell(tableCell(parameterInstance2.getName(), lightBlue, 2));
                                    tableRealSensors.addCell(tableCell(parameterInstance2.getValue().toString(), null, 2));
                                }

                            }
                        }
                    }
                }
            }
        }

        // Information if no real sensor was included in the test
        if (!realSensors) {
            tableRealSensors.addCell(tableCell("No Real Sensor was integrated into the test.", null, 4));
        }
        return tableRealSensors;
    }


    /**
     * Returns a table with the Actuator information of the Test.
     *
     * @return table with the actuator information
     */
    private PdfPTable getActuatorInfos() {
        String infoText = "The actuator used for the tests does not trigger any actions if the corresponding rule is triggered. It functions as a dummy.";

        // Table configurations
        PdfPTable actuatorInfo = new PdfPTable(4);
        actuatorInfo.setWidthPercentage(100f);

        actuatorInfo.addCell(headerCell("Simulated actuator"));
        actuatorInfo.addCell(tableCell(infoText, null, 4));

        return actuatorInfo;
    }


    /**
     * Creates a table with all important information about the rules of the application which was tested
     *
     * @param test test for which the test report is created
     * @param rule detail information of the selected rules before the test
     * @return PDFTable with all important information about the rules in the test of a specific application
     */
    private PdfPTable getRuleDetails(TestDetails test, Rule rule, int counterRules) {
        int executionsAfter;
        String lastExecutionBefore = "NEVER";
        String lastExecutionAfter = "NEVER";
        String triggerValues = "Rule not triggered";

        // get executed Rules
        String rulesExecuted = test.getRulesExecuted().toString().replace("[", "")  //remove the right bracket
                .replace("]", "");  //remove the left bracket

        // Table configurations
        PdfPTable ruleInfos = new PdfPTable(4);
        ruleInfos.setWidthPercentage(100f);


        ruleInfos.addCell(tableCell(counterRules + ". Rule: " + rule.getName(), darkBlue, 4));

        // Rule: Condition (Trigger Querey)
        ruleInfos.addCell(tableCell("Rule: Condition", lightBlue, 4));
        Rule ruleAfter = ruleRepository.findByName(rule.getName()).get();
        ruleInfos.addCell(tableCell(ruleAfter.getTrigger().getQuery(), null, 4));

        // Rule: Action (Actuator)
        ruleInfos.addCell(tableCell("Rule: Action", lightBlue, 4));

        ArrayList<String> ruleActions = new ArrayList<>();
        for (RuleAction action : ruleAfter.getActions()) {
            ruleActions.add(action.getName());
        }

        // get executed Rules
        String ruleAction = ruleActions.toString().replace("[", "")  //remove the right bracket
                .replace("]", "");  //remove the left bracket
        ruleInfos.addCell(tableCell(ruleAction, null, 4));


        // # Rule Executions before/after
        ruleInfos.addCell(tableCell("Number of executions before the test", lightBlue, 2));
        ruleInfos.addCell(tableCell("Number of executions after the test", lightBlue, 2));

        int executionsBefore = rule.getExecutions();
        ruleInfos.addCell(tableCell(Integer.toString(executionsBefore), null, 2));

        if (!rulesExecuted.contains(rule.getName())) {
            executionsAfter = rule.getExecutions();
        } else {
            executionsAfter = ruleAfter.getExecutions();
        }
        ruleInfos.addCell(tableCell(Integer.toString(executionsAfter), null, 2));


        // Last Execution time before/after
        ruleInfos.addCell(tableCell("Last execution before the test", lightBlue, 2));
        ruleInfos.addCell(tableCell("Last execution after the test", lightBlue, 2));


        // if never executed before "NEVER"
        if (rule.getLastExecution() != null) {
            lastExecutionBefore = simpleDateFormat.format(rule.getLastExecution());
        }
        ruleInfos.addCell(tableCell(lastExecutionBefore, null, 2));

        // If never executed after the test "NEVER"
        if (ruleAfter.getLastExecution() != null) {
            lastExecutionAfter = simpleDateFormat.format(ruleAfter.getLastExecution());
        }
        ruleInfos.addCell(tableCell(lastExecutionAfter, null, 2));


        // Trigger-Values of the Rule
        ruleInfos.addCell(tableCell("Trigger-Values", lightBlue, 4));

        // get Trigger Values for specific rules
        if (test.getTriggerValues().containsKey(ruleAfter.getName())) {
            if (test.getTriggerValues().get(ruleAfter.getName()).size() > 0) {
                triggerValues = test.getTriggerValues().get(ruleAfter.getName()).toString().replace("[", "")  //remove the right bracket
                        .replace("]", "");  //remove the left bracket
            }
        }

        ruleInfos.addCell(tableCell(triggerValues, null, 4));


        return ruleInfos;

    }

    /**
     * Returns a table with detailed information of the rules belonging to the IoT-Application of the Test
     *
     * @param test test for which the test report is created
     * @return table with detailed rule information
     */
    public PdfPTable getRuleInfos(TestDetails test) {
        StringBuilder rulesUser = new StringBuilder();
        String rulesExecuted;
        String triggerRules;
        String infoSelectedRules;
        String rerunInfo = "";

        // Table configurations
        PdfPTable ruleInfos = new PdfPTable(4);
        ruleInfos.setWidthPercentage(100f);

        //Set header
        ruleInfos.addCell(headerCell("Rule-Information"));

        // Creates a text depending on whether the rules chosen by the user should be triggered or not
        if (test.isTriggerRules()) {
            triggerRules = "The selected rules should be executed by the test";
            infoSelectedRules = "Rules, which should be triggered";
        } else {
            triggerRules = "The selected rules shouldn't be executed by the test";
            infoSelectedRules = "Rules which shouldn't be triggered";
        }

        ruleInfos.addCell(tableCell(triggerRules, null, 4));

        // Rules which should be triggered by the values of the sensor generated through the test defined by the user
        ruleInfos.addCell(tableCell(infoSelectedRules, lightBlue, 2));
        // get an format
        if (!test.isUseNewData()) {
            rerunInfo = RERUN_IDENTIFIER;
        }
        for (int i = 0; i < test.getRules().size(); i++) {
            if (i != 0 && i != test.getRules().size()) {
                rulesUser.append(", ");
            }
            rulesUser.append(rerunInfo).append(test.getRules().get(i).getName());
        }
        ruleInfos.addCell(tableCell(rulesUser.toString(), null, 2));


        // Rules which were triggered by the values of the sensors through the test
        ruleInfos.addCell(tableCell("Rules, which were triggered", lightBlue, 2));
        // get and format
        rulesExecuted = test.getRulesExecuted().toString().replace("[", "")  //remove the right bracket
                .replace("]", "");  //remove the left bracket


        if (rulesExecuted.equals("")) {
            rulesExecuted = "-";
        }

        ruleInfos.addCell(tableCell(rulesExecuted, null, 2));

        return ruleInfos;
    }


    /**
     * Converts the general configuration of a simulated sensor into a readable output for the test report.
     *
     * @param sensorType type of the simulated sensor
     * @param event      that has been simulated
     * @param anomaly    that was integrated
     * @return ArrayList
     */
    private ArrayList<String> convertGeneralConfig(String sensorType, int event, int anomaly) {
        ArrayList<String> simDet = new ArrayList<>();
        boolean basicAnomaly = true;
        String kindOfSensor = "";
        String eventStr = "";
        String anomalyDtr = "";

        switch (sensorType) {
            case "TestingTemperatureSensor":
                kindOfSensor = "Temperature Sensor";
                break;
            case "TestingTemperatureSensorPl":
                kindOfSensor = "Temperature Sensor (planned)";
                break;
            case "TestingHumiditySensor":
                kindOfSensor = "Humidity sensor";
                break;
            case "TestingHumiditySensorPl":
                kindOfSensor = "Humidity Sensor (planned)";
                break;
            case "TestingGPSSensor":
                kindOfSensor = "GPS sensor";
                break;
            case "TestingGPSSensorPl":
                kindOfSensor = "GPS Sensor (planned)";
                break;
            case "TestingAccelerationSensor":
                kindOfSensor = "Acceleration Sensor";
                basicAnomaly = false;
                break;
            case "TestingAccelerationSensorPl":
                kindOfSensor = "Acceleration Sensor (planned)";
                basicAnomaly = false;
                break;
        }

        simDet.add(0, kindOfSensor);

        if (anomaly == 6) {
            anomalyDtr = "-";
        } else if (anomaly == 5) {
            anomalyDtr = "Wrong value type";
        } else if (basicAnomaly) {
            if (anomaly == 4) {
                anomalyDtr = "Missing values";
            } else if (anomaly == 5) {
                anomalyDtr = "Wrong value type";
            }
        } else if (!basicAnomaly) {
            if (anomaly == 3) {
                anomalyDtr = "Fly bumps into the Object";
            } else if (anomaly == 4) {
                anomalyDtr = "Outliers";
            }
        }

        switch (sensorType) {
            case "TestingTemperatureSensor":
            case "TestingTemperatureSensorPl":
                if (event == 1) {
                    eventStr = "Temperature rise";
                } else if (event == 2) {
                    eventStr = "Temperature drop";
                } else if (event == 3) {
                    eventStr = "-";
                    anomalyDtr = "Outliers";
                } else if (event == 4) {
                    eventStr = "-";
                    anomalyDtr = "Missing values";
                } else if (event == 5 || anomaly == 6) {
                    eventStr = "-";
                    anomalyDtr = "Wrong value type";
                }


                break;
            case "TestingHumiditySensor":
            case "TestingHumiditySensorPl":
                if (event == 1) {
                    eventStr = "Humidity rise";
                } else if (event == 2) {
                    eventStr = "Humidity decrease";
                } else if (event == 3) {
                    eventStr = "-";
                    anomalyDtr = "Outlier";
                } else if (event == 4) {
                    eventStr = "-";
                    anomalyDtr = "Missing values";
                } else if (event == 5 || anomaly == 5) {
                    eventStr = "-";
                    anomalyDtr = "Wrong value type";
                }

                break;
            case "TestingGPSSensorPl":
            case "TestingGPSSensor":
                if (event == 1) {
                    eventStr = "Approach";
                } else if (event == 2) {
                    eventStr = "Moving away";
                } else if (event == 3) {
                    eventStr = "-";
                    anomalyDtr = "Outliers";
                } else if (event == 4) {
                    eventStr = "-";
                    anomalyDtr = "Missing values";
                } else if (event == 5 || anomaly == 5) {
                    eventStr = "-";
                    anomalyDtr = "Wrong value type";
                }

                break;
            case "TestingAccelerationSensor":
            case "TestingAccelerationSensorPl":
                if (event == 1) {
                    eventStr = "Object is not moving";
                } else if (event == 2) {
                    eventStr = "Object is moving";
                } else if (event == 3) {
                    eventStr = "-";
                    anomalyDtr = "Fly bumps into the Object";
                } else if (event == 4) {
                    eventStr = "-";
                    anomalyDtr = "Outliers";
                } else if (event == 5 || anomaly == 5) {
                    eventStr = "-";
                    anomalyDtr = "Wrong value type";
                }

                break;
        }

        simDet.add(1, eventStr);
        simDet.add(2, anomalyDtr);


        return simDet;
    }


    /**
     * Creates a table cell with the requested phase, color and colspan.
     *
     * @param phrase     that the cell should contain
     * @param background color of the cell
     * @param colspan    of the cell
     * @return PdfPCell
     */
    public PdfPCell tableCell(String phrase, BaseColor background, int colspan) {

        PdfPCell cell = new PdfPCell(new Phrase(phrase));
        cell.setColspan(colspan);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        if (background != null) {
            cell.setBackgroundColor(background);
        }
        return cell;
    }

    /**
     * Creates a header cell with the requested phase, dark grey background and colspan 4.
     *
     * @param phrase of the header
     * @return PdfPCell as header
     */
    public PdfPCell headerCell(String phrase) {
        Chunk text = new Chunk(phrase, white);
        PdfPCell headerCell = new PdfPCell(new Phrase(text));
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setColspan(4);
        headerCell.setBackgroundColor(darkGrey);

        return headerCell;
    }


}