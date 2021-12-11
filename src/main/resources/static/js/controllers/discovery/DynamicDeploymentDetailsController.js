/**
 * Controller for the dynamic peripherals details pages.
 */
app.controller('DynamicDeploymentDetailsController',
    ['$scope', '$routeParams', '$interval', '$timeout', 'dynamicDeploymentDetails', 'discoveryLogs',
        'DiscoveryService', 'ComponentService', 'UnitService', 'NotificationService',
        function ($scope, $routeParams, $interval, $timeout, dynamicDeploymentDetails, discoveryLogs,
                  DiscoveryService, ComponentService, UnitService, NotificationService) {
            //Selectors for various UI cards within the DOM
            const SELECTOR_INFO_CARD = ".details-info-card";
            const SELECTOR_LIVE_CHART_CARD = ".live-chart-card";
            const SELECTOR_HISTORICAL_CHART_CARD = ".historical-chart-card";
            const SELECTOR_STATS_CARD = ".stats-card";

            //Relevant DOM elements
            const ELEMENT_DISCOVERY_LOGS_TABLE = $("#discovery-logs-table");

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

                //Initialize UI elements
                $(document).ready(() => {
                    $('[data-toggle="popover"]').popover();
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
             * Retrieves a certain number of value log data (in a specific order) for the current dynamic deployment
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
             * [Public]
             * Performs a server request in order to refresh the candidate devices that are stored for the device
             * template underlying this dynamic deployment. Thereby, also the subscriptions at the discovery
             * repositories for asynchronous notifications about changes are renewed.
             */
            function refreshCandidateDevices() {
                DiscoveryService.refreshCandidateDevices(dynamicDeploymentDetails.deviceTemplate.id).then(() => {
                    //Update discovery logs
                    updateDiscoveryLogs();
                    //Notify the user
                    NotificationService.notify("Update for candidate devices in progress.", "success");
                }, () => {
                    NotificationService.notify("Could not update the candidate devices.", "error");
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
                 * Function that is called when the chart loads something.
                 * @param visualizationId The ID of the affected visualization
                 */
                function loadingStart(visualizationId) {
                    //Show the waiting screen
                    $(SELECTOR_LIVE_CHART_CARD.replace(".", "#") + "-" + visualizationId).waitMe({
                        effect: 'bounce',
                        text: 'Loading chart...',
                        bg: 'rgba(255,255,255,0.85)'
                    });
                }

                /**
                 * Function that is called when the chart finished loading.
                 * @param visualizationId The ID of the affected visualization
                 */
                function loadingFinish(visualizationId) {
                    //Hide the waiting screen for the case it was displayed before
                    $(SELECTOR_LIVE_CHART_CARD.replace(".", "#") + "-" + visualizationId).waitMe("hide");
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
             * Initializes the historical chart for displaying all values of the dynamic deployment
             * (up to a certain limit).
             */
            function initHistoricalChart() {
                /**
                 * Function that is called when the chart loads something.
                 * @param visualizationId The ID of the affected visualization
                 */
                function loadingStart(visualizationId) {
                    //Show the waiting screen
                    $(SELECTOR_HISTORICAL_CHART_CARD.replace(".", "#") + "-" + visualizationId).waitMe({
                        effect: 'bounce',
                        text: 'Loading chart...',
                        bg: 'rgba(255,255,255,0.85)'
                    });
                }

                /**
                 * Function that is called when the chart finished loading.
                 * @param visualizationId The ID of the affected visualization
                 */
                function loadingFinish(visualizationId) {
                    //Hide the waiting screen for the case it was displayed before
                    $(SELECTOR_HISTORICAL_CHART_CARD.replace(".", "#") + "-" + visualizationId).waitMe("hide");
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


            // ---------------------------- MODULAR SENSOR VALUE VISUALIZATION START -----------------------------

            // All visualizations applicable for this sensor
            vm.availableVisualizationsMappings = dynamicDeploymentDetails.operator.dataModel.possibleVisMappings;

            // All visualization ids of all visualizations applicable for this sensor
            vm.idOfAllApplicableVisualizations = vm.availableVisualizationsMappings.map(visObj => visObj.visName);

            // Binding for the next chart to add (visualization id)
            vm.nextChartToAdd = "select";

            // Represents one instance of a currently active visualization
            class ActiveVisualization {
                constructor(instanceId, visId) {
                    this.instanceId = instanceId;
                    this.visId = visId;
                    this.fieldCollectionId = "";
                    this.fieldCollectionIdInput = "";
                    this.visFieldToPathMapping = {};
                    this.visFieldToPathMappingInput = {};
                    this.jsonPath = "";
                    this.jsonPathInput = "";
                    this.availableOptions = getVisualizationMappingFieldByVisId(this.visId);

                    this.setFieldCollectionIdInput = function (newIdInput) {
                        alert("fieldcollection input set to " + newIdInput);
                        this.fieldCollectionIdInput = newIdInput;
                    }
                }

                hasVisualizationOptionFieldCollectionWithName = function hasVisualizationOptionFieldCollectionWithName(fieldCollectionName) {
                    if (this.availableOptions == null || fieldCollectionName == null) {
                        return false;
                    } else {
                        let hasFieldCollection = false;
                        this.availableOptions.forEach(function (item, index) {
                            if (item.fieldCollectionName === fieldCollectionName) {
                                hasFieldCollection = true;
                            }
                        });
                        return hasFieldCollection;
                    }
                }

                getJsonPathsByFieldCollectionName = function getJsonPathsByFieldCollectionName(fieldCollectionName) {
                    if (this.hasVisualizationOptionFieldCollectionWithName(fieldCollectionName)) {
                        let jsonPathList = null;
                        this.availableOptions.forEach(function (item, index) {
                            if (item.fieldCollectionName === fieldCollectionName) {
                                jsonPathList = item.jsonPathPerVisualizationField;
                            }
                        });
                        return jsonPathList;
                    } else {
                        return null;
                    }
                }
            }

            function getVisualizationMappingFieldByVisId(visId) {
                let match = null;
                vm.availableVisualizationsMappings.forEach(function (item, index) {
                    if (item.visName === visId) {
                        match = item.mappingPerVisualizationField;
                    }
                });
                return match;
            }


            vm.allActiveVisualizations = [];

            /**
             * [private]
             * Handles the proper creation of already existent visualization card information.
             * The information comes with the sensor data from the server and must be converted
             * to a format usable for this details view controller as well as added to the
             * local data structures which are the model of the view.
             */
            function initActiveVisualizations() {
                let activeVisualizationsComponentRepresentation = dynamicDeploymentDetails.activeVisualizations;

                // Sanity check
                if (activeVisualizationsComponentRepresentation == null) {
                    return;
                }
                activeVisualizationsComponentRepresentation.forEach(function (item, index) {
                    let visToAdd = new ActiveVisualization(item.instanceId, item.visId);
                    if (item.fieldCollectionId != null) {
                        visToAdd.fieldCollectionId = item.fieldCollectionId;
                        visToAdd.fieldCollectionIdInput = item.fieldCollectionId;
                    }
                    if (item.visFieldToPathMapping != null) {
                        visToAdd.visFieldToPathMapping = item.visFieldToPathMapping;
                        visToAdd.visFieldToPathMappingInput = JSON.parse(JSON.stringify(item.visFieldToPathMapping));
                    }
                    vm.allActiveVisualizations.push(visToAdd);
                });
            }

            initActiveVisualizations();


            // Actions when the "Add a chart" button is clicked
            let cardCount = 0;

            function onCreateNewVisualizationClicked() {

                if (vm.nextChartToAdd === "select") {
                    return;
                }

                let visToAdd = new ActiveVisualization(cardCount.toString(), vm.nextChartToAdd);
                cardCount += 1;

                // Perform put request to update the components
                ComponentService.addNewActiveVisualization(DEPLOYMENT_ID, {
                    instanceId: visToAdd.instanceId,
                    visId: visToAdd.visId
                }).then(function (data) {
                    // Request succeeded --> update the vis component instance id and add the new active vis
                    visToAdd.instanceId = data.idOfLastAddedVisualization;
                    vm.allActiveVisualizations.push(visToAdd);

                    // Workaround START for preventing a tab display error when adding a chart while the sensor is currently running
                    if (vm.dynamicDeployment.lastState === 'deployed') {
                        $(document).ready(function () {
                            setTabActive('#historical-chart-card', visToAdd.instanceId);
                        });
                        $(document).ready(function () {
                            setTabActive('#live-chart-card', visToAdd.instanceId);
                        });
                    }
                    // Workaround END

                    NotificationService.notify('Chart was added successfully.', 'success');
                }, function (errData) {
                    // Request failed --> notify the user and do nothing
                    NotificationService.notify('Creation of new visualization instance failed.', 'error');
                });
            }

            /**
             * [public]
             * Removes a chart card by id. Performs a HTTP-Delete request in order to keep
             * the view persistent.
             *
             * @param instanceId The id of the visualization card instance which should be removed.
             */
            function deleteChartCard(instanceId) {
                ComponentService.deleteActiveVisualization(DEPLOYMENT_ID, instanceId).then(function (data) {
                    // Find the chart to remove by instance id
                    let visToRemove = null;
                    for (let i = 0; i < vm.allActiveVisualizations.length; i += 1) {
                        if (vm.allActiveVisualizations[i].instanceId === instanceId) {
                            visToRemove = vm.allActiveVisualizations[i];
                            break;
                        }
                    }

                    if (visToRemove == null) {
                        return;
                    }

                    // Remove the chart by value
                    let index = vm.allActiveVisualizations.indexOf(visToRemove);
                    if (index !== -1) {
                        vm.allActiveVisualizations.splice(index, 1);
                    }
                    NotificationService.notify('Chart was removed successfully.', 'success');
                }, function (errData) {
                    NotificationService.notify('Chart could not be removed.', 'error');
                });
            }

            /**
             * Called, when the create button in the chart settings modal is triggered.
             * Sets the jsonPath for the chart and let the modal fade out.
             */
            function updateJsonPath(visToUpdate) {

                // Convert string json object to objects
                let convertMappings = function (mappingToConvert) {
                    Object.keys(mappingToConvert).forEach(function (key) {
                        if (typeof mappingToConvert[key] === "string") {
                            mappingToConvert[key] = JSON.parse(mappingToConvert[key]);
                        }
                        return mappingToConvert;
                    })
                }

                convertMappings(visToUpdate.visFieldToPathMappingInput);

                // Perform put request to update the chart components
                ComponentService.addNewActiveVisualization(DEPLOYMENT_ID, {
                    instanceId: visToUpdate.instanceId,
                    visId: visToUpdate.visId,
                    fieldCollectionId: visToUpdate.fieldCollectionIdInput,
                    visFieldToPathMapping: JSON.parse(JSON.stringify(visToUpdate.visFieldToPathMappingInput))
                }).then(function (data) {
                    // Request succeeded --> notify the user
                    visToUpdate.fieldCollectionId = visToUpdate.fieldCollectionIdInput;
                    visToUpdate.visFieldToPathMapping = JSON.parse(JSON.stringify(visToUpdate.visFieldToPathMappingInput));
                    $('div.modal.fade').modal('hide');
                    NotificationService.notify('Chart was updated successfully.', 'success');
                }, function (errData) {
                    // Request failed --> notify the user and do nothing
                    alert(JSON.stringify(errData));
                    NotificationService.notify('Update of visualization instance failed.', 'error');
                });
            }

            // Watch the deployment status and change the chart tabs accordingly
            $scope.$watch(function () {
                return vm.dynamicDeployment.lastState;
            }, function (newValue, oldValue) {
                if (newValue === 'deployed') {
                    // Make sure that the tab of the live chart is selected
                    setTabActive('#live-chart-card');
                } else {
                    // Make sure that the tab of the historical chart is selected
                    setTabActive('#historical-chart-card');
                }
            });

            /**
             * [private]
             * Activates a tab based on a given data-target
             * @param tab the id prefix of the data-target
             * @param instanceId [optional] to specify a certain tab instead of activating all with the tab prefix.
             */
            function setTabActive(tab, instanceId) {
                if (instanceId != null) {
                    $('.nav-tabs a[data-target*="' + tab + "-" + instanceId + '"]').tab('show');
                } else {
                    // Select the tab as active which id begins with tab
                    $('.nav-tabs a[data-target^="' + tab + '"]').tab('show');
                }
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
                deleteDiscoveryLogs: deleteDiscoveryLogs,
                refreshCandidateDevices: refreshCandidateDevices,
                updateJsonPath: updateJsonPath,
                onCreateNewVisualizationClicked: onCreateNewVisualizationClicked,
                deleteChartCard: deleteChartCard
            });
        }
    ]
);
