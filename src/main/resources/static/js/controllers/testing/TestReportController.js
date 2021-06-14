/* global app */

'use strict';

/**
 * Provides services for managing tests.
 */
app.controller('TestReportController', ['$scope', '$controller', 'HttpService', 'ENDPOINT_URI',
    function ($scope, $controller, HttpService, ENDPOINT_URI) {


        let testReport = null;


        //URL for server requests
        const URL_SIMULATION_VALUES = ENDPOINT_URI + '/test-details/test-report/';

        /**
         * [Public]
         *
         * Converts the png of the test report into a pdf.
         * @param testId The id of the test to be started
         */
        function generateReport(table2) {
            const doc = new jsPDF("p", "mm", "a4");

            doc.setFontSize(40);
            doc.setFontSize(18);
            doc.text(18, 25, "Test-Report");
            doc.addImage(table2, 'PNG', 15, 25, 180, 280, "dc", "NONE", 0);

            let data = [{id: 1, name: "Peter"}, {id: 2, name: "Chris"}];
            doc.table(20, 30, data);
            doc.save('TestReport.pdf');
        }


        // convert all needed information for the specific test report and open the modal
        $scope.openReport = function (report) {
            testReport = report.report;
            $scope.testReportAnzeige = testReport;
            getSimulationValuesTestReport(testReport.id);
            convertConfig(testReport);
            convertRulesTriggered(testReport.amountRulesTriggered);
            convertTriggerList(testReport.triggerValues);
            getRealReportSensorList(testReport);
            $('#testReport').modal('show');
        };

        /**
         * [Private]
         * Create a server request to get the sensor values generated during the test and create a chart out of them.
         * @param reportId
         * @return {*}
         */
        function getSimulationValuesTestReport(reportId) {
            //Execute request
            return HttpService.getRequest(URL_SIMULATION_VALUES + reportId).then(function (response) {
                convertSimulationValues(response);
            });
        }

        /**
         * [Private]
         * Convert the sensor values generated during the test to add them to the chart for the report.
         * @param response
         */
        function convertSimulationValues(response) {
            let simulationValues = [];

            angular.forEach(response, function (timeValue, sensorName) {
                simulationValues.push({
                    name: sensorName,
                    data: timeValue
                });
            });
            getReportChart(simulationValues);
        }

        /**
         * [Private]
         * Creates the charts for the test reports with the generated sensor values during the test.
         * @param dataSeries
         */
        function getReportChart(dataSeries) {
            Highcharts.chart('reportChart', {
                    chart: {
                        type: 'line',
                        zoomType: 'xy'
                    }, title: {
                        text: ''
                    },
                    xAxis: {
                        type: 'datetime'
                    },

                    tooltip: {
                        valueDecimals: 2,
                        valuePrefix: '',
                    },
                    legend: {
                        layout: 'horizontal',
                        align: 'center',
                        verticalAlign: 'bottom',
                        x: 0,
                        y: 0,
                        showInLegend: true
                    },
                    series: dataSeries
                }
            )

        }

        /**
         * [Private]
         * Converts the structure of the value trigger list to show them correctly in the report.
         * @param triggerValues
         */
        function convertTriggerList(triggerValues) {
            let triggeredValuesList = {};
            let triggeredValuesLi = [];
            angular.forEach(triggerValues, function (triggerValues, ruleName) {
                triggeredValuesLi.push({
                    "ruleName": ruleName,
                    "triggerValues": triggerValues
                })
            });
            triggeredValuesList.table = triggeredValuesLi;
            $scope.triggerValues = triggeredValuesList.table;
        }


        /**
         * [Private]
         * Convert the structure of the rule execution information to show this correctly in the report.
         * @param amountRulesTriggered
         */
        function convertRulesTriggered(amountRulesTriggered) {
            let amountRulesTriggeredList = {};
            let amountRulesTriggeredL = [];
            angular.forEach(amountRulesTriggered, function (executions, rule) {
                amountRulesTriggeredL.push({
                    "name": rule,
                    "executions": executions
                })
            });
            amountRulesTriggeredList.table = amountRulesTriggeredL;
            $scope.rulesTriggered = amountRulesTriggeredList.table;
        }


        /**
         * [Private]
         * Get a list of all real sensors included into the test, to display them in the test report.
         * @param report
         */
        function getRealReportSensorList(report) {
            $scope.realSensorList = []
            angular.forEach(report.sensor, function (sensor, key) {
                if (!sensor.name.includes("TESTING_")) {
                    $scope.realSensorList.push(sensor)
                }
            });
        }

        /**
         * [Private]
         * Converts structure of the sensor configurations of the sensor simulators to show them correctly in the report.
         * @param testReport
         */
        function convertConfig(testReport) {
            let simulationConfig = [];
            let config = {};

            angular.forEach(testReport.config, function (config, key) {
                let type = "";
                let event = "";
                let anomaly = "";
                angular.forEach(config, function (configDetails, key) {
                    if (configDetails["name"] === "Type") {
                        type = configDetails["value"];
                    } else if (configDetails["name"] === "eventType") {
                        event = configDetails["value"];
                    } else if (configDetails["name"] === "anomalyType") {
                        anomaly = configDetails["value"];
                    }
                });
                if (type && event && anomaly) {
                    simulationConfig.push({
                        "type": type,
                        "event": event,
                        "anomaly": anomaly
                    })
                }


            });
            config.configTable = simulationConfig;
            $scope.configTable = config.configTable;
        }

        //Expose public methods
        return {
            generateReport: generateReport,
            openReport: $scope.openReport,

        }

    }

]);