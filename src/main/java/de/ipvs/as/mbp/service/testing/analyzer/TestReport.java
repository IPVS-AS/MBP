package de.ipvs.as.mbp.service.testing.analyzer;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.*;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.rules.RuleAction;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.repository.RuleRepository;
import de.ipvs.as.mbp.repository.TestDetailsRepository;
import de.ipvs.as.mbp.service.receiver.ValueLogReceiver;
import de.ipvs.as.mbp.service.testing.PropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;
import com.itextpdf.kernel.pdf.PdfDocument;

import com.itextpdf.kernel.colors.Color;

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
    private final String MBP_ICON_URL;

    //To resolve ${} in @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    public TestReport() throws IOException {
        propertiesService = new PropertiesService();
        RERUN_IDENTIFIER = propertiesService.getPropertiesString("testingTool.RerunIdentifier");
        CONFIG_SENSOR_NAME_KEY = propertiesService.getPropertiesString("testingTool.ConfigSensorNameKey");
        MBP_ICON_URL = propertiesService.getPropertiesString("testingTool.ReportIcon");

    }

    // Date formatter
    final String datePattern = "dd-MM-yyyy HH:mm:ss";
    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);

    // final background colors and fonts
    final Color lightBlue = new DeviceRgb(168, 232, 255);
    final Color darkBlue = new DeviceRgb(0, 191, 255);
    final Color darkGrey = new DeviceRgb(182, 182, 182);
    final Color mbpBlue = new DeviceRgb(0, 191, 255);

    /**
     * Generates the Test-Report with the Chart of the simulated Values and other important information for the user.
     *
     * @param testId      id of the specific test
     * @param rulesBefore detail information of the selected rules before the test
     * @return path where the TestReport can be found
     */
    public String generateTestReport(String testId, List<Rule> rulesBefore) throws Exception {

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        final Style white = new Style().setFont(font).setFontSize(12).setFontColor(ColorConstants.WHITE);
        final Style headerFont = new Style().setFont(font).setFontSize(10).setFontColor(mbpBlue);
        final Style tableHeader = new Style().setFont(font).setFontSize(12).setFontColor(ColorConstants.BLACK);
        final Style pageFont = new Style().setFont(font).setFontSize(22).setFontColor(ColorConstants.BLACK);
        final Style titleFont = new Style().setFont(font).setFontSize(22).setFontColor(ColorConstants.BLACK);
        final Style boldUnderlined = new Style().setFont(font).setFontSize(17).setFontColor(ColorConstants.BLACK).setUnderline();

        int counterRules = 0;
        TestDetails test = testDetailsRepository.findById(testId).get();
        // Create a new pdf, which is named with the ID of the specific test
        File testReport = new File(testId + "_" + test.getEndTimeUnix() + ".pdf");
        Path wholePath = Paths.get(testReport.getAbsolutePath());
        String path = wholePath.getParent().toString();
        PdfDocument pdfDocument = new PdfDocument(new PdfWriter(wholePath.toString()));

        // Add an event handler for the header and footer of the test report
        pdfDocument.addEventHandler(PdfDocumentEvent.START_PAGE, new ReportHeader("A Platform for Managing IoT Environments", MBP_ICON_URL));
        ReportFooter footerEvent = new ReportFooter();
        pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, footerEvent);

        com.itextpdf.layout.Document doc = new Document(pdfDocument, PageSize.A4);


        // Title of the test report
        com.itextpdf.layout.element.Paragraph title = new com.itextpdf.layout.element.Paragraph().add("Test-Report: " + test.getName()).addStyle(titleFont).setTextAlignment(TextAlignment.CENTER);


        //Page Title
        com.itextpdf.layout.element.Paragraph pageTitle = new com.itextpdf.layout.element.Paragraph().add("MBP: A Platform for Managing IoT Environments").addStyle(pageFont).setTextAlignment(TextAlignment.CENTER);


        // Creating an ImageData object from the graph of sensor values, which was previously created with the GraphPlotter
        ImageData graphSensorVal = ImageDataFactory.create(testId + ".gif");
        com.itextpdf.layout.element.Image graphSensorValImg = new com.itextpdf.layout.element.Image(graphSensorVal).setHorizontalAlignment(HorizontalAlignment.CENTER);
        graphSensorValImg.setHorizontalAlignment(HorizontalAlignment.CENTER);

        // information about the success of the test and the start/end times
        Table successInfo = getGeneralInfo(test);

        // Test-Details
        com.itextpdf.layout.element.Paragraph subtitle = new com.itextpdf.layout.element.Paragraph().add("Test-Details: ").addStyle(boldUnderlined).setTextAlignment(TextAlignment.CENTER);

        // Sensor information
        Table simulationSensors = getSimulationConfig(test, tableHeader, white);
        Table realSensors = getRealSensorConfig(test,tableHeader, white);

        // Actuator information
        Table actuatorInfos = getActuatorInfos(white);

        // Rule information
        Table ruleInfos = getRuleInfos(test,white);
        doc.add(new com.itextpdf.layout.element.Paragraph("\n"));

        // Rule details
        Table ruleDetails = new Table(4);
        doc.add(new com.itextpdf.layout.element.Paragraph("\n"));

        // add all components to the test report pdf
        //doc.add(table);
        doc.add(title);
        doc.add(new com.itextpdf.layout.element.Paragraph("\n"));
        doc.add(successInfo.setHorizontalAlignment(HorizontalAlignment.CENTER));
        doc.add(new com.itextpdf.layout.element.Paragraph("\n"));
        doc.add(new com.itextpdf.layout.element.Paragraph("\n"));
        doc.add(graphSensorValImg);
        doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
        doc.add(subtitle);
        doc.add(new com.itextpdf.layout.element.Paragraph("\n"));
        doc.add(simulationSensors);
        doc.add(new com.itextpdf.layout.element.Paragraph("\n"));
        doc.add(realSensors);
        doc.add(new com.itextpdf.layout.element.Paragraph("\n"));
        doc.add(actuatorInfos);
        doc.add(new com.itextpdf.layout.element.Paragraph("\n"));
        doc.add(ruleInfos);
        doc.add(new com.itextpdf.layout.element.Paragraph("\n"));

        // Add new table to get the details for each rule
        for (Rule rule : rulesBefore) {
            if (test.isUseNewData()) {
                ruleDetails = getRuleDetails(test, rule, counterRules,tableHeader);
                counterRules += 1;
            } else {
                if (rule.getName().contains(RERUN_IDENTIFIER)) {
                    ruleDetails = getRuleDetails(test, rule, counterRules,tableHeader);
                    counterRules += 1;
                }
            }
            doc.add(ruleDetails);
        }

        // Footer event to write the total number of pages in to the footer
        footerEvent.writeTotal(pdfDocument);
        doc.close();
        return path;
    }

    /**
     * Return a table with the general information of the test (test rerun, success, start/end time).
     *
     * @param test test for which the test report is created
     * @return table with general information of the test
     */
    private Table getGeneralInfo(TestDetails test) {
        String success;
        Table generalInfo = new Table(4);
        generalInfo.setWidth(UnitValue.createPercentValue(70));

        // information if test was a rerun
        if (!test.isUseNewData()) {
            generalInfo.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("This was a Test rerun."), lightBlue, 4));
        }

        // Success of the Test
        if (test.getSuccessful().equals("Successful")) {
            success = "Yes";
        } else {
            success = "No";
        }

        generalInfo.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("Successful"), lightBlue, 2));
        generalInfo.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(success), null, 2));

        //Start-Time pdf
        String startTestTime = simpleDateFormat.format(test.getStartTestTime());
        generalInfo.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("Start-Time"), lightBlue, 2));
        generalInfo.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(startTestTime), null, 2));

        // End-Time pdf
        String endTestTime = simpleDateFormat.format(test.getEndTestTime());
        generalInfo.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("End-Time"), lightBlue, 2));
        generalInfo.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(endTestTime), null, 2));


        return generalInfo;
    }


    /**
     * Returns a table with the included sensor simulators within the test and the user defined configurations of them or
     * a short information if no simulator was included.
     *
     * @param test test for which the test report is created
     * @return table with all sensor simulators and the configurations of them
     **/
    private Table getSimulationConfig(TestDetails test, Style tableHeader, Style white) {
        boolean sensorSimulation = false;
        int counter = 0;
        String rerunInfo = "";
        String originalName = "";

        if (!test.isUseNewData()) {
            rerunInfo = "(" + RERUN_IDENTIFIER + ")";
        }

        // Table configurations
        Table tableSensorSim = new Table(4);
        tableSensorSim.setWidth(UnitValue.createPercentValue(100));

        tableSensorSim.addCell(headerCell("Simulated Sensor(s)", white));

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
                com.itextpdf.layout.element.Paragraph sensorType = new com.itextpdf.layout.element.Paragraph().add(counter + ". Sensor-Type: " + rerunInfo + generalConfig.get(0)).addStyle(tableHeader);
                tableSensorSim.addCell(tableCell(sensorType, darkBlue, 4));

                //Event
                tableSensorSim.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("Event"), lightBlue, 2));
                tableSensorSim.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(generalConfig.get(1)), null, 2));

                //Anomaly
                tableSensorSim.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("Anomaly"), lightBlue, 2));
                tableSensorSim.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(generalConfig.get(2)), null, 2));


                // Planned simulation
                // Get config aspects available for the planned simulations
                Map<String, String> plannedConfig = plannedSim(test, type);

                // Check if simulation was planned
                if (!plannedConfig.isEmpty()) {

                    // Simulation time
                    tableSensorSim.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("Simulation-Time"), lightBlue, 2));
                    tableSensorSim.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(plannedConfig.get("simTime") + " hours"), null, 2));

                    // Amount Events
                    tableSensorSim.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("Amount Events"), lightBlue, 2));
                    tableSensorSim.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(plannedConfig.get("amountEvents")), null, 2));

                    // Amount Anomalies
                    tableSensorSim.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("Amount Anomalies"), lightBlue, 2));
                    tableSensorSim.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(plannedConfig.get("amountAnomalies")), null, 2));
                }
            }
        }

        // Information if no sensor simulator was included in the test
        if (!sensorSimulation) {
            tableSensorSim.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("No Sensor-Simulator was integrated into the test."), null, 4));
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
    private Table getRealSensorConfig(TestDetails test, Style tableHeader, Style white) {
        int counter = 0;
        boolean realSensors = false;
        String rerunInfo = "";
        String rerunName = "";
        if (!test.isUseNewData()) {
            rerunInfo = "(" + RERUN_IDENTIFIER + ")";
            rerunName = RERUN_IDENTIFIER;
        }

        // Table configurations
        Table tableRealSensors = new Table(4);
        tableRealSensors.setWidth(UnitValue.createPercentValue(100));

        tableRealSensors.addCell(headerCell("Real Sensor(s)", white));

        for (String type : test.getType()) {
            // Check if sensor is no sensor simulator
            if (!SIMULATOR_LIST.contains(rerunName + type)) {
                realSensors = true;
                counter += 1;

                //Sensor-Type
                com.itextpdf.layout.element.Paragraph sensorType = new com.itextpdf.layout.element.Paragraph().add(counter + ". Sensor-Type: " + rerunInfo + type).addStyle(tableHeader);
                tableRealSensors.addCell(tableCell(sensorType, darkBlue, 4));


                // Get sensor parameters and their values
                for (java.util.List<ParameterInstance> configSensor : test.getConfig()) {
                    for (ParameterInstance parameterInstance : configSensor) {
                        if (parameterInstance.getValue().equals(type)) {
                            for (ParameterInstance parameterInstance2 : configSensor) {
                                if (!parameterInstance2.getName().equals(CONFIG_SENSOR_NAME_KEY)) {
                                    tableRealSensors.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(parameterInstance2.getName()), lightBlue, 2));
                                    tableRealSensors.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(parameterInstance2.getValue().toString()), null, 2));
                                }

                            }
                        }
                    }
                }
            }
        }

        // Information if no real sensor was included in the test
        if (!realSensors) {
            tableRealSensors.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("No Real Sensor was integrated into the test."), null, 4));
        }
        return tableRealSensors;
    }


    /**
     * Returns a table with the Actuator information of the Test.
     *
     * @return table with the actuator information
     */
    private Table getActuatorInfos(Style white) {
        String infoText = "The actuator used for the tests does not trigger any actions if the corresponding rule is triggered. It functions as a dummy.";

        // Table configurations
        Table actuatorInfo = new Table(4);
        actuatorInfo.setWidth(UnitValue.createPercentValue(100));

        actuatorInfo.addCell(headerCell("Simulated actuator", white));
        actuatorInfo.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(infoText), null, 4));

        return actuatorInfo;
    }


    /**
     * Creates a table with all important information about the rules of the application which was tested
     *
     * @param test test for which the test report is created
     * @param rule detail information of the selected rules before the test
     * @return PDFTable with all important information about the rules in the test of a specific application
     */
    private Table getRuleDetails(TestDetails test, Rule rule, int counterRules, Style tableHeader) {
        int executionsAfter;
        String lastExecutionBefore = "NEVER";
        String lastExecutionAfter = "NEVER";
        String triggerValues = "Rule not triggered";

        // get executed Rules
        String rulesExecuted = test.getRulesExecuted().toString().replace("[", "")  //remove the right bracket
                .replace("]", "");  //remove the left bracket

        // Table configurations
        Table ruleInfos = new Table(4);
        ruleInfos.setWidth(UnitValue.createPercentValue(100));

        com.itextpdf.layout.element.Paragraph ruleName = new com.itextpdf.layout.element.Paragraph().add(counterRules + ". Rule: " + rule.getName()).addStyle(tableHeader);
        ruleInfos.addCell(tableCell(ruleName, darkBlue, 4));

        // Rule: Condition (Trigger Querey)
        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("Rule: Condition"), lightBlue, 4));
        Rule ruleAfter = ruleRepository.findByName(rule.getName()).get();
        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(ruleAfter.getTrigger().getQuery()), null, 4));

        // Rule: Action (Actuator)
        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("Rule: Action"), lightBlue, 4));

        ArrayList<String> ruleActions = new ArrayList<>();
        for (RuleAction action : ruleAfter.getActions()) {
            ruleActions.add(action.getName());
        }

        // get executed Rules
        String ruleAction = ruleActions.toString().replace("[", "")  //remove the right bracket
                .replace("]", "");  //remove the left bracket
        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(ruleAction), null, 4));


        // # Rule Executions before/after
        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("Number of executions before the test"), lightBlue, 2));
        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("Number of executions after the test"), lightBlue, 2));

        int executionsBefore = rule.getExecutions();
        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(Integer.toString(executionsBefore)), null, 2));

        if (!rulesExecuted.contains(rule.getName())) {
            executionsAfter = rule.getExecutions();
        } else {
            executionsAfter = ruleAfter.getExecutions();
        }
        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(Integer.toString(executionsAfter)), null, 2));


        // Last Execution time before/after
        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("Last execution before the test"), lightBlue, 2));
        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("Last execution after the test"), lightBlue, 2));


        // if never executed before "NEVER"
        if (rule.getLastExecution() != null) {
            lastExecutionBefore = simpleDateFormat.format(rule.getLastExecution());
        }
        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(lastExecutionBefore), null, 2));

        // If never executed after the test "NEVER"
        if (ruleAfter.getLastExecution() != null) {
            lastExecutionAfter = simpleDateFormat.format(ruleAfter.getLastExecution());
        }
        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(lastExecutionAfter), null, 2));


        // Trigger-Values of the Rule
        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("Trigger-Values"), lightBlue, 4));

        // get Trigger Values for specific rules
        if (test.getTriggerValues().containsKey(ruleAfter.getName())) {
            if (test.getTriggerValues().get(ruleAfter.getName()).size() > 0) {
                triggerValues = test.getTriggerValues().get(ruleAfter.getName()).toString().replace("[", "")  //remove the right bracket
                        .replace("]", "");  //remove the left bracket
            }
        }

        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(triggerValues), null, 4));


        return ruleInfos;

    }

    /**
     * Returns a table with detailed information of the rules belonging to the IoT-Application of the Test
     *
     * @param test test for which the test report is created
     * @return table with detailed rule information
     */
    public Table getRuleInfos(TestDetails test, Style white) {
        StringBuilder rulesUser = new StringBuilder();
        String rulesExecuted;
        String triggerRules;
        String infoSelectedRules;
        String rerunInfo = "";

        // Table configurations
        Table ruleInfos = new Table(4);
        ruleInfos.setWidth(UnitValue.createPercentValue(100));

        //Set header
        ruleInfos.addCell(headerCell("Rule-Information", white));

        // Creates a text depending on whether the rules chosen by the user should be triggered or not
        if (test.isTriggerRules()) {
            triggerRules = "The selected rules should be executed by the test";
            infoSelectedRules = "Rules, which should be triggered";
        } else {
            triggerRules = "The selected rules shouldn't be executed by the test";
            infoSelectedRules = "Rules which shouldn't be triggered";
        }

        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(triggerRules), null, 4));

        // Rules which should be triggered by the values of the sensor generated through the test defined by the user
        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(infoSelectedRules), lightBlue, 2));
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
        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(rulesUser.toString()), null, 2));


        // Rules which were triggered by the values of the sensors through the test
        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph("Rules, which were triggered"), lightBlue, 2));
        // get and format
        rulesExecuted = test.getRulesExecuted().toString().replace("[", "")  //remove the right bracket
                .replace("]", "");  //remove the left bracket


        if (rulesExecuted.equals("")) {
            rulesExecuted = "-";
        }

        ruleInfos.addCell(tableCell(new com.itextpdf.layout.element.Paragraph(rulesExecuted), null, 2));

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
            case "TESTING_TemperatureSensor":
                kindOfSensor = "Temperature Sensor";
                break;
            case "TESTING_TemperatureSensorPl":
                kindOfSensor = "Temperature Sensor (planned)";
                break;
            case "TESTING_HumiditySensor":
                kindOfSensor = "Humidity sensor";
                break;
            case "TESTING_HumiditySensorPl":
                kindOfSensor = "Humidity Sensor (planned)";
                break;
            case "TESTING_GPSSensor":
                kindOfSensor = "GPS sensor";
                break;
            case "TESTING_GPSSensorPl":
                kindOfSensor = "GPS Sensor (planned)";
                break;
            case "TESTING_AccelerationSensor":
                kindOfSensor = "Acceleration Sensor";
                basicAnomaly = false;
                break;
            case "TESTING_AccelerationSensorPl":
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
            if (anomaly == 3) {
                anomalyDtr = "Outlier";
            } else if (anomaly == 4) {
                anomalyDtr = "Missing values";
            } else if (anomaly == 5) {
                anomalyDtr = "Wrong value type";
            }
        } else {
            if (anomaly == 3) {
                anomalyDtr = "Fly bumps into the Object";
            } else if (anomaly == 4) {
                anomalyDtr = "Outliers";
            }
        }

        switch (sensorType) {
            case "TESTING_TemperatureSensor":
            case "TESTING_TemperatureSensorPl":
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
            case "TESTING_HumiditySensor":
            case "TESTING_HumiditySensorPl":
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
            case "TESTING_GPSSensorPl":
            case "TESTING_GPSSensor":
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
            case "TESTING_AccelerationSensor":
            case "TESTING_AccelerationSensorPl":
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
    public Cell tableCell(com.itextpdf.layout.element.Paragraph phrase, Color background, int colspan) {

        Cell cell = new Cell(1, colspan).add(phrase).setTextAlignment(TextAlignment.CENTER);
        cell.setHorizontalAlignment(HorizontalAlignment.CENTER);

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
    public Cell headerCell(String phrase, Style white) {
        Cell headerCell = new Cell(1, 4).add(new com.itextpdf.layout.element.Paragraph(phrase)).addStyle(white).setTextAlignment(TextAlignment.CENTER);
        headerCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
        headerCell.setBackgroundColor(darkGrey);

        return headerCell;
    }


}