/**
 * Controller for the dynamic peripherals details pages.
 */
app.controller('DynamicDeploymentDetailsController',
    ['$scope', '$routeParams', '$interval', '$timeout', 'dynamicDeploymentDetails', 'discoveryLogs',
        'DiscoveryService', 'UnitService', 'NotificationService',
        function ($scope, $routeParams, $interval, $timeout, dynamicDeploymentDetails, discoveryLogs,
                  DiscoveryService, UnitService, NotificationService) {
            //Selectors for various UI cards within the DOM
            const SELECTOR_INFO_CARD = ".details-info-card";
            const SELECTOR_LIVE_CHART_CARD = ".live-chart-card";
            const SELECTOR_HISTORICAL_CHART_CARD = ".historical-chart-card";
            const SELECTOR_STATS_CARD = ".stats-card";

            //Relevant DOM elements
            const ELEMENT_DISCOVERY_LOGS_TABLE = $("#discovery-logs-table");
            const ELEMENT_STACKTRACE_MODAL = $("#showStackTraceModal")

            //Properties of the current dynamic deployment
            const DEPLOYMENT_ID = $routeParams.id;
            const OPERATOR_UNIT = dynamicDeploymentDetails.operator.unit;

            //Reference to the DataTable of discovery logs
            let discoveryLogsDataTable = null;

            //Initialization of frontend variables
            let vm = this;
            vm.isLoading = false;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //TODO
                console.log("Logs:");
                console.log(discoveryLogs);

                //Initialize value log stats
                initValueLogStats();

                //Initialize charts
                initLiveChart();
                initHistoricalChart();

                //Initialize discovery logs table
                initDiscoveryLogsTable();

                //Interval for updating deployment details information
                let interval = $interval(reloadDeploymentDetails, 30 * 1000);

                //Cancel interval on route change
                $scope.$on('$destroy', () => {
                    $interval.cancel(interval);
                });
            })();


            /**
             * [Public]
             * Reloads and updates the details information of the current dynamic deployment.
             */
            function reloadDeploymentDetails() {
                //Show waiting screen
                showWaitingScreen("Retrieving data...");

                //Retrieve details data about the dynamic deployment
                DiscoveryService.getDynamicDeployment(DEPLOYMENT_ID).then((data) => {
                    //Update fields
                    $timeout(() => {
                        vm.dynamicDeployment.inProgress = data.inProgress;
                        vm.dynamicDeployment.lastState = data.lastState;
                        vm.dynamicDeployment.activatingIntended = data.activatingIntended;
                        vm.dynamicDeployment.lastDeviceDetails = data.lastDeviceDetails;
                    }, 10);
                }).always(() => {
                    //Hide waiting screen again
                    hideWaitingScreen();
                });
            }

            /**
             * [Public]
             * Executes a server request in order to activate the current dynamic deployment.
             */
            function activateDeployment() {
                //Create the server request
                DiscoveryService.activateDynamicDeployment(DEPLOYMENT_ID).then(() => {
                    //Notify the user
                    NotificationService.notify("The dynamic deployment was activated.", "success");

                    //Update the deployment details
                    reloadDeploymentDetails();
                });
            }

            /**
             * [Public]
             * Executes a server request in order to deactivate the current dynamic deployment.
             */
            function deactivateDeployment() {
                //Create the server request
                DiscoveryService.deactivateDynamicDeployment(DEPLOYMENT_ID).then(() => {
                    //Notify the user
                    NotificationService.notify("The dynamic deployment was deactivated.", "success");

                    //Update the deployment details
                    reloadDeploymentDetails();
                });
            }

            /**
             * [Public]
             * Called, when the user updates the unit in which the values should be displayed
             * by clicking on the update button.
             */
            function onDisplayUnitChange() {
                //Retrieve entered unit
                let inputUnit = vm.displayUnitInput;

                //Check whether the entered unit is compatible with the operator unit
                UnitService.checkUnitsForCompatibility(OPERATOR_UNIT, inputUnit).then((response) => {
                    //Check compatibility according to server response
                    if (!response) {
                        NotificationService.notify("The entered unit is not compatible to the operator unit.", "error");
                        return;
                    }

                    //Units are compatible, take user input as new unit
                    $timeout(() => {
                        vm.displayUnit = vm.displayUnitInput;
                    }, 10);
                }, () => {
                    NotificationService.notify("The entered unit is invalid.", "error");
                });
            }

            /**
             * [Public]
             * Retrieves a certain number of value log data (in a specific order) for the current component
             * as a promise.
             *
             * @param numberLogs The number of logs to retrieve
             * @param descending The order in which the value logs should be retrieved. True results in descending
             * order, false in ascending order. By default, the logs are retrieved in ascending
             * order ([oldest log] --> ... --> [most recent log])
             * @param unit The unit in which the values are supposed to be retrieved
             * @param startTime Start time for filtering
             * @param endTime End time for filtering
             * @returns A promise that passes the logs as a parameter
             */
            function retrieveComponentData(numberLogs, descending, unit, startTime, endTime) {
                //Set default order
                let order = 'asc';

                //Check for user option
                if (descending) {
                    order = 'desc';
                }

                //Initialize parameters for the server request
                let pageDetails = {
                    sort: 'time,' + order,
                    size: numberLogs,
                    startTime: startTime || "",
                    endTime: endTime || ""
                };

                //Perform the server request in order to retrieve the data
                return DiscoveryService.getValueLogs(DEPLOYMENT_ID, pageDetails, unit);
            }

            /**
             * [Public]
             * Asks the user if he really wants to delete all value logs for the current dynamic deployment.
             * If this is the case, the deletion is executed by initiating the corresponding server request.
             */
            function deleteValueLogs() {
                /**
                 * Deletes the value logs by creating the corresponding server request.
                 */
                function executeDeletion() {
                    DiscoveryService.deleteValueLogs(DEPLOYMENT_ID).then(() => {
                        //Update historical chart and stats
                        $scope.historicalChartApi.updateChart();
                        $scope.valueLogStatsApi.updateStats();

                        //Notify the user
                        NotificationService.notify("Value logs were deleted successfully.", "success");
                    }, () => {
                        NotificationService.notify("Could not delete value logs.", "error");
                    });
                }

                //Ask the user to confirm the deletion
                return Swal.fire({
                    title: 'Delete value data',
                    type: 'warning',
                    html: "Are you sure you want to delete all value data that has been recorded so far for this " +
                        "dynamic deployment? This action cannot be undone.",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                }).then((result) => {
                    //Check if the user confirmed the deletion
                    if (result.value) executeDeletion();
                });
            }

            /**
             * [Public]
             * Triggers an update of the discovery logs DataTable by creating a server request for retrieving
             * the most recent data.
             */
            function updateDiscoveryLogs() {
                //Ask the DataTable to reload its data
                discoveryLogsDataTable.ajax.reload();
            }

            /**
             * [Public]
             * Asks the user if he really wants to delete all discovery logs for the current dynamic deployment.
             * If this is the case, the deletion is executed by initiating the corresponding server request.
             */
            function deleteDiscoveryLogs() {
                /**
                 * Deletes the discovery logs by creating the corresponding server request.
                 */
                function executeDeletion() {
                    DiscoveryService.deleteDiscoveryLogs(DEPLOYMENT_ID).then(() => {
                        //Update the discovery logs table
                        updateDiscoveryLogs();
                        //Notify the user
                        NotificationService.notify("Discovery logs were deleted successfully.", "success");
                    }, () => {
                        NotificationService.notify("Could not delete discovery logs.", "error");
                    });
                }

                //Ask the user to confirm the deletion
                return Swal.fire({
                    title: 'Delete discovery logs',
                    type: 'warning',
                    html: "Are you sure you want to delete all discovery logs that has been recorded so far for this " +
                        "dynamic deployment? This action cannot be undone.",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                }).then((result) => {
                    //Check if the user confirmed the deletion
                    if (result.value) executeDeletion();
                });
            }


            /**
             * [Private]
             * Initializes the value log stats display.
             */
            function initValueLogStats() {
                /**
                 * Function that is called when the value log stats display loads something
                 */
                function loadingStart() {
                    //Show waiting screen
                    $(SELECTOR_STATS_CARD).waitMe({
                        effect: 'bounce',
                        text: 'Loading value statistics...',
                        bg: 'rgba(255,255,255,0.85)'
                    });
                }

                /**
                 * Function that is called when the value log stats display finished loading
                 */
                function loadingFinish() {
                    //Hide the waiting screen for the case it was displayed before
                    $(SELECTOR_STATS_CARD).waitMe("hide");
                }

                /**
                 * Function that is used by the value log stats display to retrieve the statistics in a specific unit
                 * from the server.
                 */
                function getStats(unit) {
                    return DiscoveryService.getValueLogStats(DEPLOYMENT_ID, unit).then(function (response) {
                        return response;
                    }, function () {
                        //Failure
                        NotificationService.notify('Could not load value log statistics.', 'error');
                        return {};
                    });
                }

                vm.valueLogStats = {
                    loadingStart: loadingStart,
                    loadingFinish: loadingFinish,
                    getStats: getStats
                };
            }

            /**
             * [Private]
             * Initializes the live chart for displaying the most recent values of the dynamic deployment.
             */
            function initLiveChart() {
                /**
                 * Function that is called when the chart loads something
                 */
                function loadingStart() {
                    //Show the waiting screen
                    $(SELECTOR_LIVE_CHART_CARD).waitMe({
                        effect: 'bounce',
                        text: 'Loading chart...',
                        bg: 'rgba(255,255,255,0.85)'
                    });
                }

                /**
                 * Function that is called when the chart finished loading
                 */
                function loadingFinish() {
                    //Hide the waiting screen for the case it was displayed before
                    $(SELECTOR_LIVE_CHART_CARD).waitMe("hide");
                }

                /**
                 * Function that checks whether the chart is allowed to update its data.
                 * @returns {boolean} True, if the chart may update; false otherwise
                 */
                function isUpdateable() {
                    return vm.dynamicDeployment.lastState === 'deployed';
                }

                //Expose
                vm.liveChart = {
                    loadingStart: loadingStart,
                    loadingFinish: loadingFinish,
                    isUpdateable: isUpdateable,
                    getData: retrieveComponentData
                };
            }

            /**
             * [Private]
             * Initializes the historical chart for displaying all sensor values (up to a certain limit).
             */
            function initHistoricalChart() {
                /**
                 * Function that is called when the chart loads something
                 */
                function loadingStart() {
                    //Show the waiting screen
                    $(SELECTOR_HISTORICAL_CHART_CARD).waitMe({
                        effect: 'bounce',
                        text: 'Loading chart...',
                        bg: 'rgba(255,255,255,0.85)'
                    });
                }

                /**
                 * Function that is called when the chart finished loading
                 */
                function loadingFinish() {
                    //Hide the waiting screen for the case it was displayed before
                    $(SELECTOR_HISTORICAL_CHART_CARD).waitMe("hide");
                }

                //Expose
                vm.historicalChart = {
                    loadingStart: loadingStart,
                    loadingFinish: loadingFinish,
                    getData: retrieveComponentData
                };
            }

            /**
             * [Private]
             * Initializes the DataTable providing an overview about the recorded discovery logs.
             */
            function initDiscoveryLogsTable() {
                //Check whether table has already been initialized
                if (discoveryLogsDataTable != null) return;

                discoveryLogsDataTable = ELEMENT_DISCOVERY_LOGS_TABLE.DataTable({
                    data: discoveryLogs,
                    serverSide: true,
                    searching: false,
                    ordering: false,
                    columns: [
                        {
                            data: 'startTime',
                            render: epochMilliToString
                        },
                        {data: 'taskName'},
                        {
                            data: 'trigger',
                            render: (data) => {
                                let labelClass = (data === 'MBP' ? 'label-primary' : (data === 'User' ? 'label-success' : (data === 'Discovery Repository' ? 'label-warning' : 'label-default')));
                                return '<span class="label ' + labelClass + '">' + data + '</span>';
                            }
                        },
                        {
                            data: 'endTime',
                            render: epochMilliToString
                        },
                        {
                            data: 'messages',
                            render: (data) => {
                                //Sanity check
                                if (data.length < 1) return 'None';

                                //Create collapse button, table container and table element
                                let collapseButton = $('<button type="button" class="btn btn-primary log-messages-toggle"><i class="material-icons">remove_red_eye</i>&nbsp;' + data.length + ' log message' + (data.length ? 's' : '') + '</button>');
                                let tableContainer = $('<div>').css('display', 'none');
                                let table = $('<table>').addClass('table log-messages');

                                //Populate the table
                                data.forEach(m => {
                                    //Determine label class for message type
                                    let labelClass = (m.type === 'Info' ? 'label-default' : (m.type === 'Success' ? 'label-success' : (m.type === 'Undesirable' ? 'label-warning' : 'label-danger')));
                                    //Create and populate table row
                                    table.append($('<tr>')
                                        .append($('<td>' + epochMilliToString(m.time) + '</td>'))
                                        .append($('<td><span class="label ' + labelClass + '">' + m.type + '</span></td>'))
                                        .append($('<td>' + m.message.replaceAll('\n', '<br/>') + '</td>')));
                                });

                                //Put elements together and transform them to HTML
                                return $('<div>').append(collapseButton).append(tableContainer.append(table)).html();
                            }
                        },
                    ],
                    ajax: function (data, callback) {
                        //Calculate page number
                        let pageNumber = Math.floor(data.start / data.length);

                        //Perform request in order to retrieve the discovery logs
                        DiscoveryService.getDiscoveryLogs(DEPLOYMENT_ID, data.length, pageNumber, 'startTime,desc').then(function (response) {
                            //Trigger callback with the data from the response
                            callback({
                                draw: data.draw,
                                data: response.content,
                                recordsTotal: response.totalElements,
                                recordsFiltered: response.totalElements,
                            });
                        });
                    },
                    "drawCallback": function () {
                        //Find toggle buttons for log messages and register click handler
                        $('.log-messages-toggle').on('click', function () {
                            $(this).next().slideToggle();
                        });
                    },
                    language: {
                        "decimal": "",
                        "emptyTable": "No discovery logs available.",
                        "info": "Showing _START_ to _END_ of _TOTAL_ logs",
                        "infoEmpty": "Showing 0 to 0 of 0 logs",
                        "infoFiltered": "(filtered from _MAX_ total logs)",
                        "thousands": ".",
                        "lengthMenu": "Show _MENU_ logs",
                        "zeroRecords": "No matching logs found",
                    }
                });
            }

            /**
             * [Private]
             * Displays a waiting screen with a certain text above the details information card.
             * @param text The text to display
             */
            function showWaitingScreen(text) {
                //Set a default text
                if (!text) {
                    text = 'Please wait...';
                }

                //Show waiting screen
                $(SELECTOR_INFO_CARD).waitMe({
                    effect: 'bounce',
                    text: text,
                    bg: 'rgba(255,255,255,0.85)'
                });
            }

            /**
             * [Private]
             * Hides the waiting screen above the details information card.
             */
            function hideWaitingScreen() {
                $(SELECTOR_INFO_CARD).waitMe("hide");
            }

            /**
             * [Private]
             * Converts given epoch milliseconds to a human-readable date string.
             * @param millis The milliseconds to convert
             */
            function epochMilliToString(millis) {
                return new Date(millis).toLocaleString('de-DE', {
                    'year': 'numeric',
                    'month': '2-digit',
                    'day': '2-digit',
                    'hour': '2-digit',
                    'minute': '2-digit',
                    'second': '2-digit'
                }).replace(',', '');
            }

            //Expose the public variables and functions
            angular.extend(vm, {
                dynamicDeployment: dynamicDeploymentDetails,
                displayUnit: OPERATOR_UNIT,
                displayUnitInput: OPERATOR_UNIT,
                activateDeployment: activateDeployment,
                deactivateDeployment: deactivateDeployment,
                reloadDeploymentDetails: reloadDeploymentDetails,
                onDisplayUnitChange: onDisplayUnitChange,
                deleteValueLogs: deleteValueLogs,
                updateDiscoveryLogs: updateDiscoveryLogs,
                deleteDiscoveryLogs: deleteDiscoveryLogs
            });
        }
    ]
);
