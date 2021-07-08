/* global app */

'use strict';

/**
 * Provides services for managing tests.
 */
app.controller('TestReportController', ['$scope', '$controller', 'HttpService', 'ENDPOINT_URI',
    function ($scope, $controller, HttpService, ENDPOINT_URI) {


        let testReport = null;
        const footerHeight = 287;
        let lastPos = 0;
        let pageSize;
        let pageWidth;

        //URL for server requests
        const URL_SIMULATION_VALUES = ENDPOINT_URI + '/test-details/test-report/';
        const iconRepeatBase64 = 'data:image/png;base64,' + "iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAAtklEQVRoge2ZQQrEMAwD9XT/vHtaWJq2l5XcyGggVylDQ3EIEELYgqo63t7DX1TVYS3xFbCV+BWwlDgL2ElcCVAl7go6lr0ARSICux0hSuhDPr3nDQFpATW8Ib9VgJ29FCgF2LlLgVKAnbkUyD+zEnuBEIIW+59Eq4DlLHQuUuW2zUPKbPtpVH4XsBKQX/VueiShnct68xHYQYIi8CRBK+jAXgAYMC0CA56BgAGPccCAJ9EQBvEBl7TmnovloTUAAAAASUVORK5CYII="

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
         *
         * @param chart
         * @param testReport
         */
        async function generateReport(chart, testReport) {

            const doc = new jsPDF();
            pageSize = doc.internal.pageSize;
            pageWidth = pageSize.width ? pageSize.width : pageSize.getWidth();

            addLogo(doc, pageWidth)

            getGeneralInfo(doc, pageWidth, testReport);
            await getSimulatorInfo(doc, pageWidth);
            await getRealSensorInfo(doc, pageWidth);
            await getSensorChart(doc, chart);
            await getAllRuleInformation(doc, pageWidth);
            await getNextSteps(doc, testReport, pageWidth);


            addFooters(doc);
            doc.save(testReport.id + ".pdf");
        }

        function addPage(y, doc) {
            if (y >= footerHeight - 10) {
                lastPos = 0;
                return doc.addPage(), addLogo(doc, pageWidth);
            }
        }


        function addLogo(doc, pageWidth) {
            var favicon = new Image();
            var nodeList = document.getElementsByTagName("link");
            for (var i = 0; i < nodeList.length; i++) {
                if ((nodeList[i].getAttribute("rel") === "icon") || (nodeList[i].getAttribute("rel") === "shortcut icon")) {

                    favicon.src = nodeList[i].getAttribute("href");
                    doc.addImage(favicon, 'PNG', pageWidth - 14, 4, 10, 10, "icon", "NONE", 0)
                }
            }


        }

        function getGeneralInfo(doc, pageWidth, testReport) {
            const header = function (data) {
                doc.setFontSize(18);
                doc.setFontStyle('normal');
                doc.setTextColor(80, 80, 80);
                doc.text("Testing Report: " + testReport.name, data.settings.margin.left, 15);
            };

            const generalInformation = doc.autoTableHtmlToJson(document.getElementById("generalInformation"));
            const generalInfo = doc.splitTextToSize("General information", pageWidth - 35, {});
            doc.setFontSize(12);
            doc.setTextColor(128, 128, 128);
            addPage(25, doc);
            return doc.text(generalInfo, 14, 25),
                lastPos = 25,
                addRepetitionInfo(doc, testReport),
                doc.autoTable(generalInformation.columns, generalInformation.data, {
                    beforePageContent: header,
                    startY: lastPos,
                    headerStyles: {
                        fillColor: [0, 190, 255]
                    },
                    margin: {top: 28},
                    styles: {fontSize: 9},
                    createdCell: function (cell, data) {
                        if (data.column.dataKey === 2) {
                            if (cell.text[0] === "Not Successful" || cell.text[0] === "ERROR DURING TEST") {
                                cell.styles.textColor = [255, 255, 255];
                                cell.styles.fillColor = [251, 72, 58];

                            } else {
                                cell.styles.textColor = [255, 255, 255];
                                cell.styles.fillColor = [76, 175, 80];
                            }
                        }
                    }
                }),
                lastPos = doc.autoTableEndPosY()
        }

        function addRepetitionInfo(doc, testReport) {
            if (testReport.useNewData === false) {
                doc.setFontSize(12)
                doc.setTextColor(128, 128, 128)
                doc.setFillColor(128, 128, 128);
                doc.setDrawColor(128, 128, 128)
                doc.setFontSize(9)
                return doc.addImage(iconRepeatBase64, 'PNG', 14, lastPos + 4.5, 3, 3, 'repetition', "NONE", 0), doc.text("This was a Test Rerun", 18, lastPos + 7),
                    lastPos = lastPos + 10;
            }

        }

        async function getSimulatorInfo(doc, pageWidth) {
            doc.setFontSize(12);
            doc.setTextColor(128, 128, 128);

            const simulatedSensorInfo = doc.autoTableHtmlToJson(document.getElementById("simulatedSensorInfo"));
            const involvedSim = doc.splitTextToSize("Involved Sensor-Simulators", pageWidth - 35, {});
            if (simulatedSensorInfo !== null) {
                return doc.setFontSize(12),
                    doc.setTextColor(128, 128, 128),
                    lastPos = lastPos + 10,
                    addPage(lastPos, doc),
                    doc.text(involvedSim, 14, lastPos),
                    addPage(lastPos, doc),
                    await doc.autoTable(simulatedSensorInfo.columns, simulatedSensorInfo.data, {
                        startY: lastPos + 3,
                        headerStyles: {
                            fillColor: [0, 190, 255]
                        },
                        theme: 'striped',
                        styles: {fontSize: 9},
                    }),
                    lastPos = doc.autoTableEndPosY()
            }
        }

        async function getRealSensorInfo(doc, pageWidth) {
            const realSensorInfo = doc.autoTableHtmlToJson(document.getElementById("informationRealSensors"));
            const involvedRealSensors = doc.splitTextToSize("Involved real Sensors", pageWidth - 35, {});


            if (realSensorInfo !== null) {
                return doc.setFontSize(12),
                    doc.setTextColor(128, 128, 128),
                    lastPos = lastPos + 10,
                    addPage(lastPos, doc),
                    await doc.text(involvedRealSensors, 14, lastPos),
                    await doc.autoTable(realSensorInfo.columns, realSensorInfo.data, {
                        startY: lastPos + 3,
                        styles: {fontSize: 9},
                        headerStyles: {
                            fillColor: [0, 190, 255]
                        },
                        theme: 'striped',

                    }),
                    lastPos = doc.autoTableEndPosY();

            }
        }

        async function getSensorChart(doc, chart) {
            const dimensionChart = await getImageDimensions(chart);
            return addPage(lastPos, doc),
                doc.addImage(chart, 'PNG', 14, lastPos, dimensionChart.w / 5, dimensionChart.h / 5, "historical chart", "NONE", 0),
                doc.addImage(chart, 'PNG', 14, lastPos, dimensionChart.w / 5, dimensionChart.h / 5, "historical chart", "NONE", 0),
                lastPos = (dimensionChart.h / 5) + lastPos;
        }

        async function getAllRuleInformation(doc, pageWidth) {
            const triggerRulesInfo = doc.autoTableHtmlToJson(document.getElementById("triggerRulesInfo"));
            const ruleInfoText = doc.splitTextToSize("Rule Information of the tested IoT-Application ", pageWidth - 35, {});


            doc.setFontSize(12);
            doc.setTextColor(128, 128, 128);

            addPage(lastPos + 15, doc);
            await doc.text(ruleInfoText, 14, lastPos + 15)
            await doc.autoTable(triggerRulesInfo.columns, triggerRulesInfo.data, {
                startY: lastPos + 18,
                headerStyles: {
                    fillColor: [0, 190, 255]
                },
                theme: 'striped',
                styles: {fontSize: 9}
            });

            const bodyData = await getRuleInformation(testReport)
            const headerData = ['Name', 'Condition', '# triggered', 'Trigger values'];

            doc.autoTable(headerData, bodyData, {
                startY: doc.autoTableEndPosY() + 3,
                headerStyles: {
                    fillColor: [0, 190, 255]
                },
                theme: 'striped',
                bodyStyles: {valign: 'top'},
                styles: {overflow: 'linebreak', columnWidth: 'wrap', fontSize: 9},
                columnStyles: {
                    0: {columnWidth: 'wrap'},
                    1: {columnWidth: 'auto'},
                    2: {columnWidth: 'wrap'},
                    3: {columnWidth: 'auto'}
                }
            })
            lastPos = doc.autoTableEndPosY();
        }

        function getNextSteps(doc, testReport, pageWidth) {

            doc.setFontSize(12);
            doc.setTextColor(128, 128, 128);
            lastPos = lastPos + 15;
            addPage(lastPos, doc);
            if (testReport.successful === "Not Successful") {
                return doc.text("Next Steps", 14, lastPos),
                    getNextStepsNoSuccess(doc, pageWidth, testReport);
            } else if (testReport.successful === "Not Successful") {
                return doc.text("Next Steps", 14, lastPos),
                    getNextStepsError(doc);
            }
        }

        function addFooters(doc) {
            const pageCount = doc.internal.getNumberOfPages();
            for (var i = 1; i <= pageCount; i++) {
                doc.setPage(i);
                doc.setFontSize(7);
                doc.setTextColor(80, 80, 80);
                doc.text(String(i) + ' of ' + String(pageCount), doc.internal.pageSize.width / 2, footerHeight);
            }
        }

        function getRuleInformation(testReport) {
            const body = []
            angular.forEach(testReport.ruleInformationBefore, function (info, bla) {
                const ruleInfo = []
                ruleInfo.push(info.name);
                ruleInfo.push(info.trigger.query);
                if ((info.name in testReport.amountRulesTriggered)) {
                    ruleInfo.push(testReport.amountRulesTriggered[info.name]);
                }
                if ((info.name in testReport.triggerValues)) {
                    ruleInfo.push(testReport.triggerValues[info.name].toString());

                } else {
                    ruleInfo.push("-")
                }
                body.push(ruleInfo)
            });
            return body;


        }

        function getNextStepsError(doc) {
            const lineOne = "Possible actions to solve the problem that caused the error:"
            const lineTwo = "• Check your WiFi and VPN connection."
            const lineThree = "• Check your MQTT broker settings and may change the IP adress of your broker."
            const lineFour = "• Reinstall / redeploy the default testing components via the settings."

            doc.setFontSize(9);
            doc.setTextColor(80, 80, 80);
            doc.setFont(undefined, 'bold');

            return addPage(lastPos + 15, doc), doc.text(lineOne, 14, lastPos + 15),
                doc.setFont(undefined, 'normal'),
                addPage(lastPos + 20, doc), doc.text(lineTwo, 19, lastPos + 20),
                addPage(lastPos + 25, doc), doc.text(lineThree, 19, lastPos + 25),
                addPage(lastPos + 30, doc), doc.text(lineFour, 19, lastPos + 30)

        }

        function getNextStepsNoSuccess(doc, pageWidth) {
            const lineOne = "Rules were triggered that shouldn't have been triggered:"
            const lineTwo = "1. Check which values led to the triggering of this rule."
            const lineThree = "2. Check if it was just a sensor anomaly or if the condition of the rule was set too sensitively."
            const lineFour = "3. Adjust the condition of the rule accordingly."
            const lineFive = "• For example: increase the time window / window length, set the average higher, ..."
            const lineSix = "4. Repeat the test under the same conditions and check the result again."
            const lineSeven = "5. Repeat this procedure until the test is completed successfully."
            const lineEight = "Rules which should have been triggered weren't triggered:"
            const lineNine = "1. Check the values of the test and the condition of the rule, why the rule was not triggered."
            const lineTen = "2. Set the condition of the rule to be more sensitive to the values."
            const lineEleven = "• For example: decrease the time window / window length, lower the average, ..."
            const lineTwelve = "4. Repeat the test under the same conditions and check the result again."
            const lineThirteen = "5. Repeat this procedure until the test is completed successfully."

            doc.setFontSize(9);
            doc.setTextColor(80, 80, 80);
            doc.setFont(undefined, 'bold');

            return addPage(lastPos + 5, doc), doc.text(lineOne, 14, lastPos + 5).setFont(undefined, 'normal'),
                addPage(lastPos + 10, doc), doc.text(lineTwo, 19, lastPos + 10),
                addPage(lastPos + 15, doc), doc.text(lineThree, 19, lastPos + 15),
                addPage(lastPos + 20, doc), doc.text(lineFour, 19, lastPos + 20),
                addPage(lastPos + 25, doc), doc.text(lineFive, 24, lastPos + 25),
                addPage(lastPos + 30, doc), doc.text(lineSix, 19, lastPos + 30),
                addPage(lastPos + 35, doc), doc.text(lineSeven, 19, lastPos + 35),
                addPage(lastPos + 40, doc), doc.setDrawColor(194, 194, 194), doc.line(14, lastPos + 40, pageWidth - 55, lastPos + 40),
                addPage(lastPos + 45, doc), doc.setFont(undefined, 'bold'), doc.text(lineEight, 14, lastPos + 45).setFont(undefined, 'normal'),
                addPage(lastPos + 50, doc), doc.text(lineNine, 19, lastPos + 50),
                addPage(lastPos + 55, doc), doc.text(lineTen, 19, lastPos + 55),
                addPage(lastPos + 60, doc), doc.text(lineEleven, 24, lastPos + 60),
                addPage(lastPos + 65, doc), doc.text(lineTwelve, 19, lastPos + 65),
                addPage(lastPos + 70, doc), doc.text(lineThirteen, 19, lastPos + 70)
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