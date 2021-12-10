/* global app */

'use strict';

/**
 * Provides services for managing tests.
 */
app.controller('TestReportController', ['$scope', '$controller', 'HttpService', 'ENDPOINT_URI',
    function ($scope, $controller, HttpService, ENDPOINT_URI) {


        let testReport = null;
        const footerHeight = 287;

        // The last position of the pdf file on which was written
        let lastPos = 0;
        let pageSize;
        let pageWidth;

        //URL for server requests
        const URL_SIMULATION_VALUES = ENDPOINT_URI + '/test-details/test-report/';

        const iconRepeatBase64 = 'data:image/png;base64,' + "iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAAtklEQVRoge2ZQQrEMAwD9XT/vHtaWJq2l5XcyGggVylDQ3EIEELYgqo63t7DX1TVYS3xFbCV+BWwlDgL2ElcCVAl7go6lr0ARSICux0hSuhDPr3nDQFpATW8Ib9VgJ29FCgF2LlLgVKAnbkUyD+zEnuBEIIW+59Eq4DlLHQuUuW2zUPKbPtpVH4XsBKQX/VueiShnct68xHYQYIi8CRBK+jAXgAYMC0CA56BgAGPccCAJ9EQBvEBl7TmnovloTUAAAAASUVORK5CYII="


        /**
         * [Public]
         * Export the test report modal to pdf.
         *
         * @param testReport information about the test and the results
         */
        async function getPDF(testReport) {
            //const chart = await getChart();

            // create a new PDF
            const doc = new jsPDF();
            pageSize = doc.internal.pageSize;
            pageWidth = pageSize.width ? pageSize.width : pageSize.getWidth();

            //  add all necessary information to the new pdf
            addLogo(doc)
            getGeneralInfo(doc, testReport);
            await getSimulatorInfo(doc);
            await getRealSensorInfo(doc);
            //await getSensorChart(doc, chart);
            await getAllRuleInformation(doc);
            await getNextSteps(doc, testReport);
            await addSensorValueInformationToPDF(doc);


            addFooters(doc);
            doc.save(testReport.id + ".pdf");
        }


        /**
         * [Private]
         * Creates a png image of the chart shown in the modal of the test report, to be able to add it to the pdf.
         */

        /*
        function getChart() {
            const options = {
                quality: 0.95
            };

            return new Promise(function (resolve) {
                domtoimage.toPng(document.getElementById("chart"), options)
                    .then(function (blob) {
                        resolve(blob);
                    });
            })
        }
        */


        /**
         * [Private]
         *
         * Returns the dimension of a image.
         * @param image of which the dimension should be calculated
         */
        function getImageDimensions(image) {
            if (image !== false) {
                return new Promise(function (resolved) {
                    const i = new Image();
                    i.onload = function () {
                        resolved({w: i.width, h: i.height})
                    };
                    i.src = image
                })

            }
        }


        /**
         * [Private]
         *
         * Calculates if the new element can be added to the current page without overlapping the footer.
         * If the element would overlap the footer, a new page and the mbp logo are added
         *
         * @param yPosition of the element to be added
         * @param doc document to which the element should be added
         */
        function addPage(yPosition, doc) {
            if (yPosition >= footerHeight - 10) {
                lastPos = 15;
                doc.addPage()
                addLogo(doc, pageWidth);
            }
        }


        /**
         * [Private]
         *
         * Gets the mbp icon with its reference and adds it to the upper right corner of the pdf page.
         *
         * @param doc to which the mbp logo should be added
         */
        function addLogo(doc) {
            const favicon = new Image();
            const nodeList = document.getElementsByTagName("link");
            for (let i = 0; i < nodeList.length; i++) {
                if ((nodeList[i].getAttribute("rel") === "icon") || (nodeList[i].getAttribute("rel") === "shortcut icon")) {
                    favicon.src = nodeList[i].getAttribute("href");
                    doc.addImage(favicon, 'PNG', pageWidth - 14, 4, 10, 10, "icon", "NONE", 0)
                }
            }
        }


        /**
         * [Private]
         *
         * Adds the general information (name, start- and end-time, success state, rerun information) to the pdf.
         * @param doc to which the general information should be added
         * @param testReport with the information needed to be displayed
         */
        function getGeneralInfo(doc, testReport) {
            doc.setFontSize(12);
            doc.setTextColor(128, 128, 128);

            // header of the test report with the name
            const header = function (data) {
                doc.setFontSize(18);
                doc.setFontStyle('normal');
                doc.setTextColor(80, 80, 80);
                doc.text("Testing Report: " + testReport.name, data.settings.margin.left, 15);
            };


            const generalInformation = doc.autoTableHtmlToJson(document.getElementById("generalInformation"));
            const generalInfo = doc.splitTextToSize("General information", pageWidth - 35, {});

            // Header general information
            doc.text(generalInfo, 14, 25)
            lastPos = 25

            // Rerun information
            addRepetitionInfo(doc, testReport)

            // Table with start-, end-time and success info
            doc.autoTable(generalInformation.columns, generalInformation.data, {
                beforePageContent: header,
                startY: lastPos + 3,
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
            })
            lastPos = doc.autoTableEndPosY()
        }

        /**
         * [Private]
         *
         * If the test was a repetition of a other test, this information will be added
         *
         * @param doc to which this information should be added
         * @param testReport with the information about the executed test
         */
        function addRepetitionInfo(doc, testReport) {
            if (testReport.useNewData === false) {
                doc.setTextColor(128, 128, 128)
                doc.setFontSize(9)
                doc.addImage(iconRepeatBase64, 'PNG', 14, lastPos + 4.5, 3, 3, 'repetition', "NONE", 0)
                doc.text("This was a Test Rerun", 18, lastPos + 7)
                lastPos = lastPos + 7;
            }

        }

        /**
         * [Private]
         *
         * Adds the relevant information about the included sensor simulators if existing.
         *
         * @param doc to which this information should be added
         */
        async function getSimulatorInfo(doc) {
            const simulatedSensorInfo = doc.autoTableHtmlToJson(document.getElementById("simulatedSensorInfo"));
            const involvedSim = doc.splitTextToSize("Involved Sensor-Simulators", pageWidth - 35, {});

            if (simulatedSensorInfo !== null) {
                doc.setFontSize(12)
                doc.setTextColor(128, 128, 128)

                // Add the header of the simulator information
                lastPos = lastPos + 10
                addPage(lastPos, doc)
                doc.text(involvedSim, 14, lastPos)

                // Add the table with the information
                addPage(lastPos + 3, doc)
                await doc.autoTable(simulatedSensorInfo.columns, simulatedSensorInfo.data, {
                    startY: lastPos + 3,
                    headerStyles: {
                        fillColor: [0, 190, 255]
                    },
                    theme: 'striped',
                    styles: {fontSize: 9},
                })
                lastPos = doc.autoTableEndPosY()
            }
        }

        /**
         * [Private]
         *
         * Adds the relevant information about the included real sensors if existing.
         *
         * @param doc to which this information should be added
         */
        async function getRealSensorInfo(doc) {
            const realSensorInfo = doc.autoTableHtmlToJson(document.getElementById("informationRealSensors"));
            const involvedRealSensors = doc.splitTextToSize("Involved real Sensors", pageWidth - 35, {});

            if (realSensorInfo !== null) {
                doc.setFontSize(12)
                doc.setTextColor(128, 128, 128)

                // Add the header of the simulator information
                lastPos = lastPos + 10
                addPage(lastPos, doc)
                doc.text(involvedRealSensors, 14, lastPos)

                // Add the table with the information
                await doc.autoTable(realSensorInfo.columns, realSensorInfo.data, {
                    startY: lastPos + 3,
                    styles: {fontSize: 9},
                    headerStyles: {
                        fillColor: [0, 190, 255]
                    },
                    theme: 'striped',

                })
                lastPos = doc.autoTableEndPosY();

            }
        }

        /**
         * Returns a object representation of the simulation test data which is needed for the generation
         * of the report pdf.
         */
        async function getAllTestingSimulationValues() {
            var tables = [];

            // Create a table  object for each sensor simulation values
            for (var i = 0; i < $scope.simulationValues.length; i++) {

                // Generate the rows
                var rows = [];
                for (const simData of $scope.simulationValues[i].data) {
                    var timeValuePair = [];
                    timeValuePair.push(simData[0], angular.toJson(simData[1], 0));
                    rows.push(timeValuePair);
                }

                tables.push({
                    sensorName: $scope.simulationValues[i].name,
                    tableHeader: ["Time", "Value [Sensor: " + $scope.simulationValues[i].name + "]"],
                    tableRowData: rows
                })
            }

            return tables;
        }

        /**
         * Adds all testing sensor values to the pdf.
         *
         * @param the pdf doc to which the data should be added
         */
        async function addSensorValueInformationToPDF(doc) {
            doc.setFontSize(12);
            doc.setTextColor(128, 128, 128);

            const triggerRulesInfo = doc.autoTableHtmlToJson(document.getElementById("chart"));
            const sensorValText = doc.splitTextToSize("Sensor values during the test", pageWidth - 35, {});

            // Adds the header of the rule information
            addPage(lastPos, doc)
            await doc.text(sensorValText, 14, lastPos)
            lastPos += 15;

            var tableData = await getAllTestingSimulationValues();

            for (var i = 0; i < tableData.length; i++) {
                // Add a header naming the sensor


                // Adds the table with the detailed information about the triggered rules.
                const headerData = tableData[i].tableHeader;
                const bodyData = tableData[i].tableRowData;

                var addSpace = 5;
                if (i == 0) {
                    addSpace = 20;
                }
                doc.autoTable(headerData, bodyData, {
                    startY: doc.autoTableEndPosY() + addSpace,
                    headerStyles: {
                        fillColor: [0, 190, 255]
                    },
                    theme: 'striped',
                    bodyStyles: {valign: 'top'},
                    styles: {overflow: 'linebreak', columnWidth: 'wrap', fontSize: 9},
                    columnStyles: {
                        0: {columnWidth: 'wrap'},
                        1: {columnWidth: 'auto'},
                    }
                })
                lastPos = doc.autoTableEndPosY();
            }

        }

        /**
         * [Private]
         *
         * Adds the chart with the generated sensor data during the test.
         *
         * @param doc to which the chart should be added
         * @param chart png image of the chart
         */

        /*
        async function getSensorChart(doc, chart) {
            const dimensionChart = await getImageDimensions(chart);
            addPage(lastPos, doc)
            doc.addImage(chart, 'PNG', 14, lastPos, dimensionChart.w / 5, dimensionChart.h / 5, "historical chart", "NONE", 0)
            lastPos = (dimensionChart.h / 5) + lastPos
        }
        */


        /**
         * [Private]
         *
         * Adds all relevant rule information to the test report.
         * @param doc to which the information should be added
         */
        async function getAllRuleInformation(doc) {
            doc.setFontSize(12);
            doc.setTextColor(128, 128, 128);

            const triggerRulesInfo = doc.autoTableHtmlToJson(document.getElementById("triggerRulesInfo"));
            const ruleInfoText = doc.splitTextToSize("Rule Information of the tested IoT-Application ", pageWidth - 35, {});

            // Adds the header of the rule information
            addPage(lastPos + 15, doc)
            await doc.text(ruleInfoText, 14, lastPos + 15)

            // Adds the table with the information which rules should be triggered and which were actually triggered
            await doc.autoTable(triggerRulesInfo.columns, triggerRulesInfo.data, {
                startY: lastPos + 18,
                headerStyles: {
                    fillColor: [0, 190, 255]
                },
                theme: 'striped',
                styles: {fontSize: 9}
            });

            // Adds the table with the detailed information about the triggered rules.
            const headerData = ['Name', 'Condition', '# triggered', 'Trigger values'];
            const bodyData = await getRuleInformation(testReport)
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

        /**
         * [Private]
         *
         * Adds the next steps whether the test was not successful or an error occured during the test.
         *
         * @param doc to which the next steps should be added
         * @param testReport with all relevant information about the executed test
         */
        function getNextSteps(doc, testReport) {
            doc.setFontSize(12);
            doc.setTextColor(128, 128, 128);

            lastPos = lastPos + 15;
            addPage(lastPos + 5, doc);
            if (testReport.successful === "Not Successful") {
                doc.text("Next Steps", 14, lastPos)
                getNextStepsNoSuccess(doc, testReport);
            } else if (testReport.successful === "ERROR DURING TEST") {
                doc.text("Next Steps", 14, lastPos)
                getNextStepsError(doc);
            }
        }

        /**
         * [Private]
         *
         * Adds the footer with the page number to the pdf
         * @param doc to which the page number should be added
         */
        function addFooters(doc) {
            const pageCount = doc.internal.getNumberOfPages();
            for (let i = 1; i <= pageCount; i++) {
                doc.setPage(i);
                doc.setFontSize(7);
                doc.setTextColor(80, 80, 80);
                doc.text(String(i) + ' of ' + String(pageCount), doc.internal.pageSize.width / 2, footerHeight);
            }
        }

        /**
         * [Private]
         *
         * Creates an array with the relevant detailed rule information to add them to the table.
         *
         * @param testReport with the relevant information about the test
         * @return {[]}
         */
        function getRuleInformation(testReport) {
            const body = []
            angular.forEach(testReport.ruleInformationBefore, function (info) {
                const ruleInfo = []
                ruleInfo.push(info.name);
                ruleInfo.push(info.trigger.query);
                if ((info.name in testReport.amountRulesTriggered)) {
                    ruleInfo.push(testReport.amountRulesTriggered[info.name]);
                }
                if ((info.name in testReport.triggerValues)) {

                    // Convert the trigger values to a string (json) representation to be printed to a table
                    var valueString = "";
                    var values = testReport.triggerValues[info.name];
                    for (var i = 0; i < values.length; i++) {
                        valueString += "*  " + angular.toJson(values[i], 0);
                        if (i != values.length - 1) {
                            // Add line breaks if its not the last value entry
                            valueString += "\n\n";
                        }
                    }
                    ruleInfo.push(valueString);

                } else {
                    ruleInfo.push("-")
                }
                body.push(ruleInfo)
            });
            return body;


        }

        /**
         * [Private]
         *
         * Adds the next steps if there occured an error during the test
         *
         * @param doc to which the next steps should be added
         */
        function getNextStepsError(doc) {
            let arrayNextSteps = ["Possible actions to solve the problem that caused the error:",
                "• Check your WiFi and VPN connection.",
                "• Check your MQTT broker settings and may change the IP address of your broker.",
                "• Reinstall / redeploy the default testing components via the settings."
            ]

            doc.setFontSize(9);
            doc.setTextColor(80, 80, 80);
            doc.setFont(undefined, 'bold')
            let xPos = 14;


            for (let i = 0; i < arrayNextSteps.length; i++) {
                if (i === 1) {
                    doc.setFont(undefined, 'normal')
                    xPos = 19;
                }
                addPage(lastPos + 5, doc)
                doc.text(arrayNextSteps[i], xPos, lastPos + 5)
                lastPos = lastPos + 5;

            }

        }

        /**
         * [Private]
         *
         * Adds the next steps if the test was not successful.
         *
         * @param doc to which the next steps should be added
         */
        function getNextStepsNoSuccess(doc) {
            const arrayNextSteps = ["Rules were triggered that shouldn't have been triggered:",
                "1. Check which values led to the triggering of this rule.",
                "2. Check if it was just a sensor anomaly or if the condition of the rule was set too sensitively.",
                "3. Adjust the condition of the rule accordingly.",
                "• For example: increase the time window / window length, set the average higher, ...",
                "4. Repeat the test under the same conditions and check the result again.",
                "5. Repeat this procedure until the test is completed successfully.",
                "Rules which should have been triggered weren't triggered:",
                "1. Check the values of the test and the condition of the rule, why the rule was not triggered.",
                "2. Set the condition of the rule to be more sensitive to the values.",
                "• For example: decrease the time window / window length, lower the average, ...",
                "4. Repeat the test under the same conditions and check the result again.",
                "5. Repeat this procedure until the test is completed successfully."
            ]


            doc.setFontSize(9);
            doc.setTextColor(80, 80, 80);
            doc.setFont(undefined, 'bold');

            for (let i = 0; i < arrayNextSteps.length; i++) {
                doc.setFont(undefined, 'normal')
                let xPos = 19;
                if (i === 0 || i === 7) {
                    doc.setFont(undefined, 'bold')
                    xPos = 14;
                }
                if (i === 4 || i === 10) {
                    xPos = 24;
                }
                addPage(lastPos + 5, doc)
                doc.text(arrayNextSteps[i], xPos, lastPos + 5)
                lastPos = lastPos + 5;

            }

        }

        /**
         * [public]
         *
         * Converts a angular modified javascript object to a "normal" js object by removing angular attributes
         * like $$hashkey. This is needed within e.g. ng-repeat statements where objects get iterated.
         *
         * @param obj js object
         * @return a copy of the obj without angular specific keys
         */
        $scope.convertAngularJsToJs = function (obj) {
            const clone = JSON.parse(JSON.stringify(obj))
            return angular.fromJson(angular.toJson(clone));
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
         *
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
         *
         * Convert the sensor values generated during the test to add them to the sensor value table for the report.
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

            // Convert the posix time long to a user-readable date format
            for (var i = 0; i < simulationValues.length; i++) {
                for (var j = 0; j < simulationValues[i].data.length; j++) {
                    simulationValues[i].data[j][0] = new Date(simulationValues[i].data[j][0]).toLocaleString();
                }
            }

            $scope.simulationValues = simulationValues;
        }

        /**
         * [Private]
         *
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
         *
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
         *
         * Get a list of all real sensors included into the test, to display them in the test report.
         * @param report
         */
        function getRealReportSensorList(report) {
            $scope.realSensorList = []
            angular.forEach(report.sensor, function (sensor) {
                if (!sensor.name.includes("TESTING_")) {
                    $scope.realSensorList.push(sensor)
                }
            });
        }

        /**
         * [Private]
         *
         * Get a list of all simulated sensors included into the test.
         * @param report
         */
        function getSimulatedSensorList(report) {
            $scope.simulatedSensorList = []
            angular.forEach(report.sensor, function (sensor) {
                if (sensor.name.includes("TESTING_")) {
                    $scope.simulatedSensorList.push(sensor)
                }
            });
        }


        /**
         * [Private]
         *
         * Converts structure of the sensor configurations of the sensor simulators to show them correctly in the report.
         * @param testReport
         */
        function convertConfig(testReport) {
            let simulationConfig = [];
            let config = {};

            angular.forEach(testReport.config, function (config) {
                let type = "";
                let event = "";
                let anomaly = "";
                angular.forEach(config, function (configDetails) {
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
            openReport: $scope.openReport,
            convertAngularJsToJs: $scope.convertAngularJsToJs,
            getPDF: getPDF

        }

    }

]);