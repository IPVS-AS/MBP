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
         * [Private]
         * Export the test report modal to pdf.
         */
        async function getPDF(report) {
            const chart = await getChart();
            await generateReport(chart, report);
        }


        function getChart() {
            const options = {
                quality: 0.95
            };

            return new Promise(function (resolve, reject) {
                domtoimage.toPng(document.getElementById("chart"), options)
                    .then(function (blob) {
                        resolve(blob);
                    });
            })
        }


        function getImageDimensions(file) {
            if (file !== false) {
                return new Promise(function (resolved, rejected) {
                    var i = new Image()
                    i.onload = function () {
                        resolved({w: i.width, h: i.height})
                    };
                    i.src = file
                })

            }
        }

        /**
         * [Public]
         *
         * Converts the png of the test report into a pdf.
         * @param testId The id of the test to be started
         */
        async function generateReport(chart, testReport) {
            const dimensionChart = await getImageDimensions(chart);

            var doc = new jsPDF()

            var generalInformation = doc.autoTableHtmlToJson(document.getElementById("generalInformation"));
            var simulatedSensorInfo = doc.autoTableHtmlToJson(document.getElementById("simulatedSensorInfo"));
            var realSensorInfo = doc.autoTableHtmlToJson(document.getElementById("informationRealSensors"));

            var pageSize = doc.internal.pageSize
            var pageWidth = pageSize.width ? pageSize.width : pageSize.getWidth()
            var generalInfo = doc.splitTextToSize("General information", pageWidth - 35, {})
            var involvedSim = doc.splitTextToSize("Involved Sensor-Simulators", pageWidth - 35, {})
            var involvedRealSensors = doc.splitTextToSize("Involved real Sensors", pageWidth - 35, {})


            doc.setFontSize(12);
            doc.setTextColor(128, 128, 128);
            doc.text(generalInfo, 14, 20)
            doc.autoTable(generalInformation.columns, generalInformation.data, {
                margin: {top: 23}, headStyles: {
                    fillColor: [241, 196, 15],
                    fontSize: 15,
                }
            });

            var header = function (data) {
                doc.setFontSize(18);
                doc.setTextColor(40);
                doc.setFontStyle('normal');
                //doc.addImage(headerImgData, 'JPEG', data.settings.margin.left, 20, 50, 50);
                doc.text("Testing Report: " + testReport.name, data.settings.margin.left, 10);
            };

            var options = {
                beforePageContent: header,
                margin: {
                    top: 80
                },
                startY: doc.autoTableEndPosY() + 8,
                theme: 'striped',
            };

            doc.setFontSize(12);
            doc.setTextColor(128, 128, 128);

            if (simulatedSensorInfo !== null) {
                doc.text(involvedSim, 14, doc.autoTableEndPosY() + 5)
                doc.autoTable(simulatedSensorInfo.columns, simulatedSensorInfo.data, options);
            }

            if (realSensorInfo !== null) {
                doc.text(involvedRealSensors, 14, doc.autoTableEndPosY() + 5)
                doc.autoTable(realSensorInfo.columns, realSensorInfo.data, options);
            }
            doc.addImage(chart, 'PNG', 15, doc.autoTableEndPosY()+5, dimensionChart.w / 6, dimensionChart.h / 6, "chart", "NONE", 0);

            doc.save("table.pdf");


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
            getSimulatedSensorList(testReport);
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
         * Get a list of all simulated sensors included into the test.
         * @param report
         */
        function getSimulatedSensorList(report) {
            $scope.simulatedSensorList = []
            angular.forEach(report.sensor, function (sensor, key) {
                if (sensor.name.includes("TESTING_")) {
                    $scope.simulatedSensorList.push(sensor)
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
            getPDF: getPDF

        }

    }

]);