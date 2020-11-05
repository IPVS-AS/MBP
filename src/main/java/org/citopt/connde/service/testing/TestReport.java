package org.citopt.connde.service.testing;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.rules.RuleAction;
import org.citopt.connde.domain.testing.TestDetails;
import org.citopt.connde.repository.RuleRepository;
import org.citopt.connde.repository.TestDetailsRepository;
import org.citopt.connde.service.receiver.ValueLogReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * The component TestReport is used for the creation of test reports for tests of applications using the testing-tool
 */
@Component
public class TestReport {


    @Autowired
    private TestDetailsRepository testDetailsRepository;

    @Autowired
    TestReport testEngine;

    @Autowired
    ValueLogReceiver valueLogReceiver;

    @Autowired
    RuleRepository ruleRepository;


    // Date formatter
    final String datePattern = "dd-MM-yyyy HH:mm:ss";
    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
    final Font white = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
    String[] simSensors = {"TestingTemperaturSensor", "TestingTemperaturSensorPl", "TestingFeuchtigkeitsSensor", "TestingFeuchtigkeitsSensorPl", "TestingBeschleunigungsSensor", "TestingBeschleunigungsSensorPl", "TestingGPSSensor", "TestingGPSSensorPl"};


    /**
     * Generates the Test-Report with the Chart of the simulated Values and other important informations for the user.
     *
     * @param testId      id of the specific test
     * @param rulesBefore detail information of the selected rules before the test
     * @return path where the TestReport can be found
     */
    public String generateTestreport(String testId, java.util.List<Rule> rulesBefore) throws Exception {
        int counterRules = 0;
        TestDetails test = testDetailsRepository.findById(testId);
        Document doc = new Document();

        // Create a new pdf, which is named with the ID of the specific test
        File tempFile = new File(testId + "_" + test.getEndTimeUnix() + ".pdf");
        if (tempFile.exists() && tempFile.isFile()) {
            tempFile.delete();
        }
        File testReport = new File(testId + "_" + test.getEndTimeUnix() + ".pdf");
        Path wholePath = Paths.get(testReport.getAbsolutePath());
        String path = wholePath.getParent().toString();


        FileOutputStream pdfFileout = new FileOutputStream(testReport);
        PdfWriter.getInstance(doc, pdfFileout);
        doc.open();


        // Title of the test report
        String title = "Test-Report: " + test.getName();
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.BLACK);
        Paragraph titel = new Paragraph(title, titleFont);
        titel.setAlignment(Element.ALIGN_CENTER);


        // Creating an ImageData object from the graph of sensor values, which was previously created with the GraphPlotter
        Image graphSensorVal = Image.getInstance(testId + ".gif");
        graphSensorVal.setAlignment(Element.ALIGN_CENTER);

        // get information about the success of the test and the start/end times
        PdfPTable successInfo = getSucessInfo(test);

        // Test-Details
        Font testDetails = new Font(Font.FontFamily.HELVETICA, 17, Font.BOLD | Font.UNDERLINE, BaseColor.BLACK);
        Paragraph para3 = new Paragraph("Test-Details: ", testDetails);
        para3.setAlignment(Element.ALIGN_CENTER);

        PdfPTable simulationSensors = getSimulationConfig(test);
        PdfPTable realSensors = getRealSensorConfig(test);

        // Actuator informations
        PdfPTable actuatorInfos = getActuatorInfos();


        // add all components to the test report pdf
        doc.add(titel);
        doc.add(Chunk.NEWLINE);
        doc.add(successInfo);
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
        doc.add(graphSensorVal);
        doc.newPage();
        doc.add(para3);
        doc.add(Chunk.NEWLINE);
        doc.add(simulationSensors);
        doc.add(Chunk.NEWLINE);
        doc.add(realSensors);
        doc.add(Chunk.NEWLINE);
        doc.add(actuatorInfos);
        doc.add(Chunk.NEWLINE);
        // Rule-Informations
        PdfPTable ruleInfos = getRuleInfos(test);
        ruleInfos.setSpacingAfter(14f);
        doc.add(ruleInfos);


        for (Rule rule : rulesBefore) {
            counterRules += 1;
            PdfPTable ruleDetails = getRuleDetails(test, rule, counterRules);
            ruleDetails.setSpacingAfter(15f);
            doc.add(ruleDetails);
        }

        doc.close();
        return path;
    }

    /**
     * Return a table with the success information of the test and the start/end time.
     *
     * @param test test for which the test report is created
     * @return table with success and time informations
     */
    private PdfPTable getSucessInfo(TestDetails test) {
        String startTestTime;
        String endTestTime;// Test-Success
        Font fontConfig = new Font();
        Chunk bullet = new Chunk("\u2022", fontConfig);
        List successful = new List(List.UNORDERED);
        successful.setListSymbol(bullet);
        String success;
        if (test.getSuccessful().equals("Successful")) {
            success = "Yes";
        } else {
            success = "No";
        }

        PdfPTable successInfo = new PdfPTable(4);
        successInfo.setWidthPercentage(100f);
        PdfPCell c1 = new PdfPCell(new Phrase("Successful"));
        c1.setColspan(2);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBackgroundColor(new BaseColor(191, 220, 227));
        successInfo.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(success));
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        c2.setColspan(2);
        successInfo.addCell(c2);


        // List of test times
        List testTimes = new List(List.UNORDERED);
        testTimes.setListSymbol(bullet);

        // get the times saved for the specific test
        startTestTime = simpleDateFormat.format(test.getStartTestTime());
        endTestTime = simpleDateFormat.format(test.getEndTestTime());

        //Start-Time pdf
        c1 = new PdfPCell(new Phrase("Start-Time"));
        c1.setColspan(2);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBackgroundColor(new BaseColor(191, 220, 227));
        successInfo.addCell(c1);

        c2 = new PdfPCell(new Phrase(startTestTime));
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        c2.setColspan(2);
        successInfo.addCell(c2);

        // End-Time pdf
        successInfo.setWidthPercentage(100f);
        c1 = new PdfPCell(new Phrase("End-Time"));
        c1.setColspan(2);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBackgroundColor(new BaseColor(191, 220, 227));
        successInfo.addCell(c1);

        c2 = new PdfPCell(new Phrase(endTestTime));
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        c2.setColspan(2);
        successInfo.addCell(c2);
        return successInfo;
    }

    /**
     * Returns a table with the user defined configurations of the simulated Sensors of the test
     *
     * @param test test for which the test report is created
     * @return table with user configurations of the test
     */
    private PdfPTable getSimulationConfig(TestDetails test) {
        int counter = 0;
        PdfPCell c1;
        PdfPCell c2;
        String simTime;
        String amountEvents;
        String amountOutliers;
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        Chunk text = new Chunk("Simulated Sensor(s)", white);
        PdfPCell c0 = new PdfPCell(new Phrase(text));
        c0.setHorizontalAlignment(Element.ALIGN_CENTER);
        c0.setColspan(4);
        c0.setBackgroundColor(new BaseColor(117, 117, 117));
        table.addCell(c0);


        for (String type : test.getType()) {
            if (Arrays.asList(simSensors).contains(type)) {
                counter += 1;
                ArrayList<String> simDet = getSensorTypePDF(test, type);

                //Sensor-Type
                c1 = new PdfPCell(new Phrase(counter + ".: Sensor-Type"));
                c1.setColspan(2);
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                c1.setBackgroundColor(new BaseColor(157, 213, 227));
                table.addCell(c1);

                c2 = new PdfPCell(new Phrase(simDet.get(0)));
                c2.setColspan(2);
                c2.setBackgroundColor(new BaseColor(157, 213, 227));
                table.addCell(c2);

                //TestCase
                c1 = new PdfPCell(new Phrase("Event"));
                c1.setColspan(2);
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                c1.setBackgroundColor(new BaseColor(191, 220, 227));
                table.addCell(c1);
                c2 = new PdfPCell(new Phrase(simDet.get(1)));
                c2.setColspan(2);
                table.addCell(c2);

                //Combination
                c1 = new PdfPCell(new Phrase("Anomaly"));
                c1.setColspan(2);
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                c1.setBackgroundColor(new BaseColor(191, 220, 227));
                table.addCell(c1);
                c2 = new PdfPCell(new Phrase(simDet.get(2)));
                c2.setColspan(2);
                table.addCell(c2);

                // Planned simulation adds informations about the simulation time, amount of events and outliers
                if (type.equals("TestingTemperaturSensorPl") || type.equals("TestingFeuchtigkeitsSensorPl") || type.equals("TestingGPSSensorPl") || type.equals("TestingBeschleunigungsSensorPl")) {
                    for (java.util.List<ParameterInstance> configSensor : test.getConfig()) {
                        for (ParameterInstance parameterInstance : configSensor) {
                            switch (parameterInstance.getName()) {
                                case "simTime":
                                    simTime = String.valueOf(parameterInstance.getValue());
                                    c1 = new PdfPCell(new Phrase("Simulation-Time"));
                                    c1.setColspan(2);
                                    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    c1.setBackgroundColor(new BaseColor(191, 220, 227));
                                    table.addCell(c1);

                                    c2 = new PdfPCell(new Phrase(simTime + " hours"));
                                    c2.setColspan(2);
                                    table.addCell(c2);

                                    break;
                                case "amountEvents":
                                    amountEvents = String.valueOf(parameterInstance.getValue());
                                    c1 = new PdfPCell(new Phrase("Amount Events"));
                                    c1.setColspan(2);
                                    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    c1.setBackgroundColor(new BaseColor(191, 220, 227));
                                    table.addCell(c1);

                                    c2 = new PdfPCell(new Phrase(amountEvents));
                                    c2.setColspan(2);
                                    table.addCell(c2);

                                    break;
                                case "amountAnomalies":
                                    amountOutliers = String.valueOf(parameterInstance.getValue());
                                    c1 = new PdfPCell(new Phrase("Amount Anomalies"));
                                    c1.setColspan(2);
                                    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    c1.setBackgroundColor(new BaseColor(191, 220, 227));
                                    table.addCell(c1);

                                    c2 = new PdfPCell(new Phrase(amountOutliers));
                                    c2.setColspan(2);
                                    table.addCell(c2);

                                    break;
                            }

                        }
                    }
                }
            }


        }
        return table;
    }


    /**
     * Returns a table with the user defined configurations of the real Sensors of the test
     *
     * @param test test for which the test report is created
     * @return table with user configurations of the test
     */
    private PdfPTable getRealSensorConfig(TestDetails test) {
        int counter = 0;
        PdfPCell c1;
        PdfPCell c2;
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        Chunk text = new Chunk("Real Sensor(s)", white);
        PdfPCell c0 = new PdfPCell(new Phrase(text));
        c0.setHorizontalAlignment(Element.ALIGN_CENTER);
        c0.setColspan(4);
        c0.setBackgroundColor(new BaseColor(117, 117, 117));
        table.addCell(c0);

        for (String type : test.getType()) {
            if (!Arrays.asList(simSensors).contains(type)) {
                counter += 1;

                //Sensor-Type
                c1 = new PdfPCell(new Phrase(counter + ".: Sensor-Type"));
                c1.setColspan(2);
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                c1.setBackgroundColor(new BaseColor(157, 213, 227));
                table.addCell(c1);

                c2 = new PdfPCell(new Phrase(type));
                c2.setColspan(2);
                c2.setBackgroundColor(new BaseColor(157, 213, 227));
                table.addCell(c2);


                for (java.util.List<ParameterInstance> configSensor : test.getConfig()) {
                    for (ParameterInstance parameterInstance : configSensor) {
                        if (parameterInstance.getValue().equals(type)) {
                            for (ParameterInstance parameterInstance2 : configSensor) {
                                if (!parameterInstance2.getName().equals("ConfigName")) {
                                    c1 = new PdfPCell(new Phrase(parameterInstance2.getName().toString()));
                                    c1.setColspan(2);
                                    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    c1.setBackgroundColor(new BaseColor(191, 220, 227));
                                    table.addCell(c1);
                                    c2 = new PdfPCell(new Phrase(parameterInstance2.getValue().toString()));
                                    c2.setColspan(2);
                                    table.addCell(c2);
                                }

                            }
                        }
                    }
                }
            }
        }

        if(counter==0){
            PdfPCell realSens = new PdfPCell(new Phrase("No Real Sensor was integrated into the test."));
            realSens.setColspan(4);
            realSens.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(realSens);
        }
        return table;
    }


    /**
     * Retruns a table with the Actuator informations of the Test.
     *
     * @return table with the actuator information
     */
    private PdfPTable getActuatorInfos() {
        // Dummy-Actuator
        PdfPTable actuatorInfos = new PdfPTable(4);
        actuatorInfos.setWidthPercentage(100f);

        Chunk chunkActuatorInf = new Chunk("Simulated actuator", white);
        PdfPCell actuatorInf = new PdfPCell(new Phrase(chunkActuatorInf));
        actuatorInf.setHorizontalAlignment(Element.ALIGN_CENTER);
        actuatorInf.setColspan(4);
        actuatorInf.setBackgroundColor(new BaseColor(117, 117, 117));
        actuatorInfos.addCell(actuatorInf);
        PdfPCell actuatorInf2 = new PdfPCell(new Phrase("The actuator used for the tests does not trigger any actions if the corresponding rule is triggered. It functions as a dummy."));
        actuatorInf2.setColspan(4);
        actuatorInf2.setHorizontalAlignment(Element.ALIGN_CENTER);
        actuatorInfos.addCell(actuatorInf2);
        return actuatorInfos;
    }


    /**
     * Creates a table with all important informations about the rules of the application which was tested
     *
     * @param test test for which the test report is created
     * @param rule detail information of the selected rules before the test
     * @return PDFTable with all important informations about the rules in the test of a specific application
     */
    @SuppressWarnings("DuplicatedCode")
    private PdfPTable getRuleDetails(TestDetails test, Rule rule, int counterRules) {

        // get executed Rules
        String rulesExecuted = test.getRulesExecuted().toString().replace("[", "")  //remove the right bracket
                .replace("]", "");  //remove the left bracket

        PdfPTable ruleInfos = new PdfPTable(4);
        ruleInfos.setWidthPercentage(100f);
        PdfPCell c0;

        Rule ruleAfter = ruleRepository.findByName(rule.getName());
        c0 = new PdfPCell(new Phrase(counterRules + ". Rule: " + rule.getName()));
        c0.setHorizontalAlignment(Element.ALIGN_CENTER);
        c0.setColspan(4);
        c0.setBackgroundColor(new BaseColor(157, 213, 227));
        ruleInfos.addCell(c0);


        // Rule: Condition (Trigger Querey)
        PdfPCell c1 = new PdfPCell(new Phrase("Rule: Condition"));
        c1.setColspan(4);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBackgroundColor(new BaseColor(191, 220, 227));
        ruleInfos.addCell(c1);

        c1 = new PdfPCell(new Phrase(ruleAfter.getTrigger().getQuery()));
        c1.setColspan(4);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        ruleInfos.addCell(c1);

        // Rule: Action (Actuator)
        c1 = new PdfPCell(new Phrase("Rule: Action"));
        c1.setColspan(4);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBackgroundColor(new BaseColor(191, 220, 227));
        ruleInfos.addCell(c1);


        ArrayList ruleActions = new ArrayList();
        for (RuleAction action : ruleAfter.getActions()) {
            ruleActions.add(action.getName());
        }


        // get executed Rules
        String ruleAction = ruleActions.toString().replace("[", "")  //remove the right bracket
                .replace("]", "");  //remove the left bracket


        c1 = new PdfPCell(Phrase.getInstance(ruleAction));
        c1.setColspan(4);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        ruleInfos.addCell(c1);


        // # Rule Executions before/after
        c1 = new PdfPCell(new Phrase("Number of executions before the test"));
        c1.setColspan(2);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBackgroundColor(new BaseColor(191, 220, 227));
        ruleInfos.addCell(c1);

        c1 = new PdfPCell(new Phrase("Number of executions after the test"));
        c1.setColspan(2);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBackgroundColor(new BaseColor(191, 220, 227));
        ruleInfos.addCell(c1);

        int executionsBefore = rule.getExecutions();
        PdfPCell c2 = new PdfPCell(new Phrase(Integer.toString(executionsBefore)));
        c2.setColspan(2);
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        ruleInfos.addCell(c2);
        int executionsAfter;
        if (!rulesExecuted.contains(rule.getName())) {
            executionsAfter = rule.getExecutions();
        } else {
            executionsAfter = ruleAfter.getExecutions();
        }
        c2 = new PdfPCell(new Phrase(Integer.toString(executionsAfter)));
        c2.setColspan(2);
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        ruleInfos.addCell(c2);


        // Last Execution time before/after
        c1 = new PdfPCell(new Phrase("Last execution before the test"));
        c1.setColspan(2);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBackgroundColor(new BaseColor(191, 220, 227));
        ruleInfos.addCell(c1);

        c1 = new PdfPCell(new Phrase("Last execution after the test"));
        c1.setColspan(2);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBackgroundColor(new BaseColor(191, 220, 227));
        ruleInfos.addCell(c1);


        if (rule.getLastExecution() == null) {
            c2 = new PdfPCell(new Phrase("NEVER"));
        } else {
            c2 = new PdfPCell(new Phrase(simpleDateFormat.format(rule.getLastExecution())));
        }

        c2.setColspan(2);
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        ruleInfos.addCell(c2);


        if (ruleAfter.getLastExecution() == null) {
            c2 = new PdfPCell(new Phrase("NEVER"));
        } else {
            c2 = new PdfPCell(new Phrase(simpleDateFormat.format(ruleAfter.getLastExecution())));
        }
        c2.setColspan(2);
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        ruleInfos.addCell(c2);

        // Trigger-Values of the Rule
        c1 = new PdfPCell(new Phrase("Trigger-Values"));
        c1.setColspan(4);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBackgroundColor(new BaseColor(191, 220, 227));
        ruleInfos.addCell(c1);

        // get Trigger Values for specific rules
        if (test.getTriggerValues().containsKey(ruleAfter.getName())) {
            if (test.getTriggerValues().get(ruleAfter.getName()).size() == 0) {
                c1 = new PdfPCell(new Phrase("Rule not triggered"));
            } else {
                String tiggerValues = test.getTriggerValues().get(ruleAfter.getName()).toString().replace("[", "")  //remove the right bracket
                        .replace("]", "");  //remove the left bracket
                c1 = new PdfPCell(new Phrase(tiggerValues));
            }
        } else {
            c1 = new PdfPCell(new Phrase("Rule not triggered"));
        }
        c1.setColspan(4);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        ruleInfos.addCell(c1);


        return ruleInfos;

    }

    /**
     * Returns a table with detailed informations of the rules belonging to the IoT-Application of the Test
     *
     * @param test test for which the test report is created
     * @return table with detailed rule informations
     */
    public PdfPTable getRuleInfos(TestDetails test) {
        //noinspection MismatchedQueryAndUpdateOfCollection
        ArrayList rules = new ArrayList();
        StringBuilder rulesUser = new StringBuilder();
        String rulesExecuted;
        String triggerRules;
        String infoSelectedRules;

        PdfPTable ruleInfos = new PdfPTable(4);
        ruleInfos.setWidthPercentage(100f);
        Chunk text = new Chunk("Rule-Informations", white);
        PdfPCell c0 = new PdfPCell(new Phrase(text));
        c0.setHorizontalAlignment(Element.ALIGN_CENTER);
        c0.setColspan(4);
        c0.setBackgroundColor(new BaseColor(117, 117, 117));
        ruleInfos.addCell(c0);


        if (test.isTriggerRules()) {
            triggerRules = "The selected rules should be executed by the test";
            infoSelectedRules = "Rules, which should be triggered";
        } else {
            triggerRules = "The selected rules shouldn't be executed by the test";
            infoSelectedRules = "Rules which shouldn't be triggered";
        }

        c0 = new PdfPCell(new Phrase(triggerRules));
        c0.setHorizontalAlignment(Element.ALIGN_CENTER);
        c0.setColspan(4);
        ruleInfos.addCell(c0);


        // Rules which should be triggered by the values of the sensor simulation
        PdfPCell c1 = new PdfPCell(new Phrase(infoSelectedRules));
        c1.setColspan(2);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBackgroundColor(new BaseColor(191, 220, 227));
        ruleInfos.addCell(c1);


        // Rules which were  triggered by the simulated sensor values
        c1 = new PdfPCell(new Phrase("Rules, which were triggered"));
        c1.setColspan(2);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBackgroundColor(new BaseColor(191, 220, 227));
        ruleInfos.addCell(c1);

        // get selected Rules from User
        for (int i = 0; i < test.getRules().size(); i++) {
            if (i != 0 && i != test.getRules().size()) {
                rulesUser.append(", ");
            }
            rules.add(test.getRules().get(i).getName());
            rulesUser.append(test.getRules().get(i).getName());
        }

        PdfPCell c2 = new PdfPCell(new Phrase(rulesUser.toString()));
        c2.setColspan(2);
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        ruleInfos.addCell(c2);

        // get executed Rules
        rulesExecuted = test.getRulesExecuted().toString().replace("[", "")  //remove the right bracket
                .replace("]", "");  //remove the left bracket

        PdfPCell c3;
        if (!rulesExecuted.equals("")) {
            c3 = new PdfPCell(new Phrase(rulesExecuted));
        } else {
            c3 = new PdfPCell(new Phrase("-"));
        }

        c3.setColspan(2);
        c3.setHorizontalAlignment(Element.ALIGN_CENTER);
        ruleInfos.addCell(c3);

        return ruleInfos;
    }

    /**
     * Converts the configuration of the Test Sensor into a readable output for the test report
     *
     * @param test test for which the test report is created
     * @return List of configurations readable for the test report
     */
    public ArrayList<String> getSensorTypePDF(TestDetails test, String sensorType) {

        ArrayList<String> simDet = new ArrayList<>();
        String kindOfSensor = "";
        String testCaseStr = "";
        String combiStr = "";
        int testCase = 0;
        int combination = 0;
        java.util.List<ParameterInstance> config = null;


        for (java.util.List<ParameterInstance> configSensor : test.getConfig()) {
            for (ParameterInstance parameterInstance : configSensor) {
                if (parameterInstance.getName().equals("ConfigName") && parameterInstance.getValue().equals(sensorType)) {
                    config = configSensor;
                    break;
                }
            }
        }


        for (ParameterInstance parameterInstance : Objects.requireNonNull(config)) {
            if (parameterInstance.getName().equals("event")) {
                Object dd = parameterInstance.getValue();
                testCase = Integer.parseInt(String.valueOf(parameterInstance.getValue()));
            }
            if (parameterInstance.getName().equals("anomaly")) {
                combination = Integer.parseInt(String.valueOf(parameterInstance.getValue()));
            }
        }


        switch (sensorType) {
            case "TestingTemperaturSensor":
                kindOfSensor = "Temperature Sensor";
                break;
            case "TestingTemperaturSensorPl":
                kindOfSensor = "Temperature Sensor (planned)";
                break;
            case "TestingFeuchtigkeitsSensor":
                kindOfSensor = "Humidity sensor";
                break;
            case "TestingFeuchtigkeitsSensorPl":
                kindOfSensor = "Humidity Sensor (planned)";
                break;
            case "TestingGPSSensor":
                kindOfSensor = "GPS sensor";
                break;
            case "TestingGPSSensorPl":
                kindOfSensor = "GPS Sensor (planned)";
                break;
            case "TestingBeschleunigungsSensor":
                kindOfSensor = "Acceleration Sensor";
                break;
            case "TestingBeschleunigungsSensorPl":
                kindOfSensor = "Acceleration Sensor (planned)";
                break;
        }

        simDet.add(0, kindOfSensor);

        switch (sensorType) {
            case "TestingTemperaturSensor":
            case "TestingTemperaturSensorPl":
                if (testCase == 1) {
                    testCaseStr = "Temperature rise";
                } else if (testCase == 2) {
                    testCaseStr = "Temperature drop";
                } else if (testCase == 3) {
                    testCaseStr = "-";
                    combiStr = "Outliers";
                } else if (testCase == 4) {
                    testCaseStr = "-";
                    combiStr = "Missing values";
                } else if (testCase == 5 || combination == 5) {
                    testCaseStr = "-";
                    combiStr = "Wrong value type";
                }

                if (combination == 6) {
                    combiStr = "-";
                } else if (combination == 3) {
                    combiStr = "Outliers";
                } else if (combination == 4) {
                    combiStr = "Missing values";
                } else if (combination == 5) {
                    combiStr = "Wrong value type";
                }
                break;
            case "TestingFeuchtigkeitsSensor":
            case "TestingFeuchtigkeitsSensorPl":
                if (testCase == 1) {
                    testCaseStr = "Humidity rise";
                } else if (testCase == 2) {
                    testCaseStr = "Humidity decrease";
                } else if (testCase == 3) {
                    testCaseStr = "-";
                    combiStr = "Outlier";
                } else if (testCase == 4) {
                    testCaseStr = "-";
                    combiStr = "Missing values";
                } else if (testCase == 5 || combination == 5) {
                    testCaseStr = "-";
                    combiStr = "Wrong value type";
                }

                if (combination == 6) {
                    combiStr = "-";
                } else if (combination == 3) {
                    combiStr = "Outlier";
                } else if (combination == 4) {
                    combiStr = "Missing values";
                } else if (combination == 5) {
                    combiStr = "Wrong value type";
                }
                break;
            case "TestingGPSSensorPl":
            case "TestingGPSSensor":
                if (testCase == 1) {
                    testCaseStr = "Approach";
                } else if (testCase == 2) {
                    testCaseStr = "Moving away";
                } else if (testCase == 3) {
                    testCaseStr = "-";
                    combiStr = "Outliers";
                } else if (testCase == 4) {
                    testCaseStr = "-";
                    combiStr = "Missing values";
                } else if (testCase == 5 || combination == 5) {
                    testCaseStr = "-";
                    combiStr = "Wrong value type";
                }

                if (combination == 6) {
                    combiStr = "-";
                } else if (combination == 3) {
                    combiStr = "Outliers";
                } else if (combination == 4) {
                    combiStr = "Missing values";
                } else if (combination == 5) {
                    combiStr = "Wrong value type";
                }
                break;
            case "TestingBeschleunigungsSensor":
            case "TestingBeschleunigungsSensorPl":
                if (testCase == 1) {
                    testCaseStr = "Object is not moving";
                } else if (testCase == 2) {
                    testCaseStr = "Object is moving";
                } else if (testCase == 3) {
                    testCaseStr = "-";
                    combiStr = "Fly bumps into the Object";
                } else if (testCase == 4) {
                    testCaseStr = "-";
                    combiStr = "Outliers";
                } else if (testCase == 5 || combination == 5) {
                    testCaseStr = "-";
                    combiStr = "Wrong value type";
                }

                if (combination == 6) {
                    combiStr = "-";
                } else if (combination == 3) {
                    combiStr = "Fly bumps into the Object";
                } else if (combination == 4) {
                    combiStr = "Outliers";
                } else if (combination == 5) {
                    combiStr = "Wrong value type";
                }
                break;
        }

        simDet.add(1, testCaseStr);
        simDet.add(2, combiStr);


        return simDet;
    }
}

    
