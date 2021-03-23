/**
 * Controller for the component details pages that can be used to extend more specific controllers with a default behaviour.
 */
app.controller('ComponentDetailsController',
    ['$scope', '$rootScope', '$routeParams', '$interval', 'componentDetails', 'ComponentService', 'DeviceService', 'UnitService', 'NotificationService',
        function ($scope, $rootScope, $routeParams, $interval, componentDetails, ComponentService, DeviceService, UnitService, NotificationService) {
            //Selectors that allow the selection of different ui cards
            const LIVE_CHART_CARD_SELECTOR = ".live-chart-card";
            const HISTORICAL_CHART_CARD_SELECTOR = ".historical-chart-card";
            const DEPLOYMENT_CARD_SELECTOR = ".deployment-card";
            const STATS_CARD_SELECTOR = ".stats-card";

            //Important properties of the currently considered component
            const COMPONENT_ID = $routeParams.id;
            const COMPONENT_TYPE = componentDetails.componentTypeName;
            const COMPONENT_TYPE_URL = COMPONENT_TYPE + 's';
            const COMPONENT_OPERATOR_UNIT = componentDetails.operator.unit;

            //Initialization of variables that are used in the frontend by angular
            let vm = this;
            vm.component = componentDetails;
            vm.isLoading = false;
            vm.deploymentState = 'UNKNOWN';
            vm.deviceState = 'UNKNOWN';
            vm.displayUnit = COMPONENT_OPERATOR_UNIT;
            vm.displayUnitInput = COMPONENT_OPERATOR_UNIT;

            //Stores the parameters and their values as assigned by the user
            vm.parameterValues = [];

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Disable the loading bar
                $rootScope.showLoading = false;

                //Initialize parameters and retrieve states and stats
                initParameters();
                updateDeploymentState();
                updateDeviceState();

                //Initialize value log stats
                initValueLogStats();

                //Initialize charts
                initLiveChart();
                initHistoricalChart();

                //Interval for updating states on a regular basis
                let interval = $interval(function () {
                    updateDeploymentState(true);
                    updateDeviceState();
                }, 2 * 60 * 1000);

                //Cancel interval on route change and enable the loading bar again
                $scope.$on('$destroy', function () {
                    $interval.cancel(interval);
                    $rootScope.showLoading = true;
                });
            })();

            /**
             * {Public]
             * Updates the deployment state of the currently considered component. By default, a waiting screen
             * is displayed during the update. However, this can be deactivated.
             *
             * @param noWaitingScreen If set to true, no waiting screen is displayed during the refreshment
             */
            function updateDeploymentState(noWaitingScreen) {
                //Check if waiting screen is supposed to be displayed
                if (!noWaitingScreen) {
                    showDeploymentWaitingScreen("Retrieving component state...");
                }

                //Retrieve the state of the current component
                ComponentService.getComponentState(COMPONENT_ID, COMPONENT_TYPE_URL).then(function (response) {
                    //Success
                    vm.deploymentState = response.content;
                }, function (response) {
                    //Failure
                    vm.deploymentState = 'UNKNOWN';
                    NotificationService.notify('Could not retrieve deployment state.', 'error');
                }).then(function () {
                    //Finally hide the waiting screen again
                    hideDeploymentWaitingScreen();
                    $scope.$apply();
                });
            }

            /**
             * [Public]
             *
             * Updates the state of the device that is dedicated to the component.
             */
            function updateDeviceState() {
                vm.deviceState = 'LOADING';

                //Retrieve device state
                DeviceService.getDeviceState(componentDetails.device.id).then(function (response) {
                    //Success
                    vm.deviceState = response.content;
                }, function (response) {
                    //Failure
                    vm.deviceState = 'UNKNOWN';
                    NotificationService.notify('Could not load device state.', 'error');
                }).then(function () {
                    $scope.$apply();
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
                UnitService.checkUnitsForCompatibility(COMPONENT_OPERATOR_UNIT, inputUnit).then(function (response) {
                    //Check compatibility according to server response
                    if (!response) {
                        NotificationService.notify("The entered unit is not compatible to the operator unit.", "error");
                        return;
                    }

                    //Units are compatible, take user input as new unit
                    vm.displayUnit = vm.displayUnitInput;

                    //Update UI
                    $scope.$apply();

                }, function () {
                    NotificationService.notify("The entered unit is invalid.", "error");
                });
            }

            /**
             * [Public]
             * Deploys the current component and shows a waiting screen during the deployment.
             */
            function deploy() {
                //Show waiting screen
                showDeploymentWaitingScreen("Deploying...");

                //Execute deployment request
                ComponentService.deploy(componentDetails._links.deploy.href).then(
                    function (response) {
                        //Notify user
                        vm.deploymentState = 'DEPLOYED';
                        NotificationService.notify('Component deployed successfully.', 'success');
                    },
                    function (response) {
                        //Failure
                        vm.deploymentState = 'UNKNOWN';
                        NotificationService.notify('Deployment failed.', 'error');
                    }).then(function () {
                    //Finally hide the waiting screen
                    hideDeploymentWaitingScreen();
                    $scope.$apply();
                });
            }

            /**
             * [Public]
             * Undeploys the current component and shows a waiting screen during the undeployment.
             */
            function undeploy() {
                //Show waiting screen
                showDeploymentWaitingScreen("Undeploying...");

                //Execute undeployment request
                ComponentService.undeploy(componentDetails._links.deploy.href).then(
                    function (response) {
                        //Notify user
                        vm.deploymentState = 'READY';
                        NotificationService.notify('Component undeployed successfully.', 'success');
                    },
                    function (response) {
                        //Failure
                        vm.deploymentState = 'UNKNOWN';
                        NotificationService.notify('Undeployment failed.', 'error');
                    }).then(function () {
                    //Finally hide the waiting screen
                    hideDeploymentWaitingScreen();
                    $scope.$apply();
                });
            }

            /**
             * [Public]
             * Starts the current component (in case it has been stopped before) and shows a waiting screen during
             * the start progress.
             */
            function startComponent() {
                //Show waiting screen
                showDeploymentWaitingScreen("Starting...");

                //Execute start request
                ComponentService.startComponent(COMPONENT_ID, COMPONENT_TYPE, vm.parameterValues)
                    .then(function (response) {
                            //Notify user
                            vm.deploymentState = 'RUNNING';
                            NotificationService.notify('Component started successfully.', 'success');
                        },
                        function (response) {
                            //Failure, check status code of response
                            if(response.status !== 400){
                                vm.deploymentState = 'UNKNOWN';
                            }
                        }).then(function () {
                    //Finally hide the waiting screen
                    hideDeploymentWaitingScreen();
                    $scope.$apply();
                });
            }

            /**
             * [Public]
             * Stops the current component and shows a waiting screen during the stop progress.
             */
            function stopComponent() {
                //Show waiting screen
                showDeploymentWaitingScreen("Stopping...");

                //Execute stop request
                ComponentService.stopComponent(COMPONENT_ID, COMPONENT_TYPE).then(function (response) {
                        //Notify user
                        vm.deploymentState = 'DEPLOYED';
                        NotificationService.notify('Component stopped successfully.', 'success');
                    },
                    function (response) {
                        //Failure
                        vm.deploymentState = 'UNKNOWN';
                        NotificationService.notify('Stopping failed.', 'error');
                    }).then(function () {
                    //Finally hide the waiting screen
                    hideDeploymentWaitingScreen();
                    $scope.$apply();
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
             * @returns A promise that passes the logs as a parameter
             */
            function retrieveComponentData(numberLogs, descending, unit) {
                //Set default order
                let order = 'asc';

                //Check for user option
                if (descending) {
                    order = 'desc';
                }

                //Initialize parameters for the server request
                let pageDetails = {
                    sort: 'time,' + order,
                    size: numberLogs
                };

                //Perform the server request in order to retrieve the data
                return ComponentService.getValueLogs(COMPONENT_ID, COMPONENT_TYPE, pageDetails, unit);
            }

            /**
             * [Public]
             * Asks the user if he really wants to delete all value logs for the current component. If this is the case,
             * the deletion is executed by creating the corresponding server request.
             */
            function deleteValueLogs() {
                /**
                 * Executes the deletion of the value logs by performing the server request.
                 */
                function executeDeletion() {
                    ComponentService.deleteValueLogs(COMPONENT_ID, COMPONENT_TYPE).then(function (response) {
                        //Update historical chart and stats
                        $scope.historicalChartApi.updateChart();
                        $scope.valueLogStatsApi.updateStats();

                        NotificationService.notify("Value logs were deleted successfully.", "success");
                    }, function (response) {
                        NotificationService.notify("Could not delete value logs.", "error");
                    });
                }

                //Ask the user to confirm the deletion
                return Swal.fire({
                    title: 'Delete value data',
                    type: 'warning',
                    html: "Are you sure you want to delete all value data that has been recorded so far for this " +
                        "component? This action cannot be undone.",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                }).then(function (result) {
                    //Check if the user confirmed the deletion
                    if (result.value) {
                        executeDeletion();
                    }
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
                    $(STATS_CARD_SELECTOR).waitMe({
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
                    $(STATS_CARD_SELECTOR).waitMe("hide");
                }

                /**
                 * Function that is used by the value log stats display to retrieve the statistics in a specific unit
                 * from the server.
                 */
                function getStats(unit) {
                    return ComponentService.getValueLogStats(COMPONENT_ID, COMPONENT_TYPE_URL, unit).then(function (response) {
                        //Success, pass statistics data
                        return response;
                    }, function (response) {
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
             * Initializes the live chart for displaying the most recent sensor values.
             */
            function initLiveChart() {
                /**
                 * Function that is called when the chart loads something
                 */
                function loadingStart(visInstanceId) {
                    //Show the waiting screen
                    $(LIVE_CHART_CARD_SELECTOR.replace(".", "#") + "-" + visInstanceId).waitMe({
                        effect: 'bounce',
                        text: 'Loading chart...',
                        bg: 'rgba(255,255,255,0.85)'
                    });
                }

                /**
                 * Function that is called when the chart finished loading
                 */
                function loadingFinish(visInstanceId) {
                    //Hide the waiting screen for the case it was displayed before
                    $(LIVE_CHART_CARD_SELECTOR.replace(".", "#") + "-" + visInstanceId).waitMe("hide");
                }

                /**
                 * Function that checks whether the chart is allowed to update its data.
                 * @returns {boolean} True, if the chart may update; false otherwise
                 */
                function isUpdateable() {
                    return vm.deploymentState === 'RUNNING';
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
                function loadingStart(visInstanceId) {
                    //Show the waiting screen

                    $(HISTORICAL_CHART_CARD_SELECTOR.replace(".", "#") + "-" + visInstanceId).waitMe({
                        effect: 'bounce',
                        text: 'Loading chart...',
                        bg: 'rgba(255,255,255,0.85)'
                    });
                }

                /**
                 * Function that is called when the chart finished loading
                 */
                function loadingFinish(visInstanceId) {
                    //Hide the waiting screen for the case it was displayed before
                    $(HISTORICAL_CHART_CARD_SELECTOR.replace(".", "#") + "-" + visInstanceId).waitMe("hide");
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
             * Initializes the data structures that are required for the deployment parameters.
             */
            function initParameters() {
                //Retrieve all formal parameters for this component
                let requiredParams = componentDetails.operator.parameters;


                //Iterate over all parameters
                for (let i = 0; i < requiredParams.length; i++) {
                    //Set empty default values for these parameters
                    let value = "";

                    if (requiredParams[i].type === "Switch") {
                        value = false;
                    }
                    if (requiredParams[i].name === "device_code") {
                        console.log("Requesting code for required parameter device_code.");
                        value = getDeviceCode();
                        continue;
                    }

                    //For each parameter, add a tuple (name, value) to the globally accessible parameter array
                    vm.parameterValues.push({
                        "name": requiredParams[i].name,
                        "value": value
                    });
                }
            }

            /**
             * Retrieve authorization code for the device from the OAuth Authorization server.
             */
            function getDeviceCode() {
                fetch(location.origin + '/MBP/oauth/authorize?client_id=device-client&response_type=code&scope=write', {
                    headers: {
                        // Basic http authentication with username "device-client" and the according password from MBP
                        'Authorization': 'Basic ZGV2aWNlLWNsaWVudDpkZXZpY2U='
                    }
                }).then(function (response) {
                    let chars = response.url.split('?');
                    let code = chars[1].split('=');
                    vm.parameterValues.push({
                        "name": "device_code",
                        "value": code[1]
                    });
                });
            }

            /**
             * [Private]
             * Displays a waiting screen with a certain text for the deployment DOM container.
             * @param text The text to display
             */
            function showDeploymentWaitingScreen(text) {
                //Set a default text
                if (!text) {
                    text = 'Please wait...';
                }

                //Show waiting screen
                $(DEPLOYMENT_CARD_SELECTOR).waitMe({
                    effect: 'bounce',
                    text: text,
                    bg: 'rgba(255,255,255,0.85)'
                });
            }

            /**
             * [Private]
             * Hides the waiting screen for the deployment DOM container.
             */
            function hideDeploymentWaitingScreen() {
                $(DEPLOYMENT_CARD_SELECTOR).waitMe("hide");
            }

            // ---------------------------- NEW VISUALIZATION STUFF -----------------------------

            // All visualizations applicable for this sensor
            vm.availableVisualizationsMappings = vm.component.operator.dataModel.possibleVisMappings;

            // All visualization ids of all visualizations applicable for this sensor
            vm.idOfAllApplicableVisualizations = vm.availableVisualizationsMappings.map(visObj => visObj.visName);

            // Binding for the next chart to add (visualization id)
            vm.nextChartToAdd = "";

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
                        var hasFieldCollection = false;
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
                        var jsonPathList = null;
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
                var match = null;
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
                var activeVisualizationsComponentRepresentation = vm.component.activeVisualizations;

                // Sanity check
                if (activeVisualizationsComponentRepresentation == null) {
                    return;
                }
                activeVisualizationsComponentRepresentation.forEach(function (item, index) {
                    var visToAdd = new ActiveVisualization(item.instanceId, item.visId);
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
            var cardCount = 0;
            function onCreateNewVisualizationClicked() {
                var visToAdd = new ActiveVisualization(cardCount.toString(), vm.nextChartToAdd);
                cardCount += 1;

                // Perform put request to update the components
                ComponentService.addNewActiveVisualization(vm.component.id, {
                    instanceId: visToAdd.instanceId,
                    visId: visToAdd.visId
                }).then(function (data) {
                    // Request succeeded --> update the vis component instance id and add the new active vis
                    visToAdd.instanceId = data.idOfLastAddedVisualization;
                    vm.allActiveVisualizations.push(visToAdd);
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
                ComponentService.deleteActiveVisualization(vm.component.id, instanceId).then(function (data) {
                    // Find the chart to remove by instance id
                    var visToRemove = null;
                    for (var i = 0; i < vm.allActiveVisualizations.length; i += 1) {
                        if (vm.allActiveVisualizations[i].instanceId == instanceId) {
                            visToRemove = vm.allActiveVisualizations[i];
                            break;
                        }
                    }

                    if (visToRemove == null) {
                        return;
                    }

                    // Remove the chart by value
                    var index = vm.allActiveVisualizations.indexOf(visToRemove);
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

                // visToUpdate.visFieldToPathMapping = "default";
                console.log(visToUpdate.visFieldToPathMappingInput);
                console.log(JSON.parse(JSON.stringify(visToUpdate.visFieldToPathMappingInput)));

                // Convert string json object to objects
                var convertMappings = function(mappingToConvert) {
                    Object.keys(mappingToConvert).forEach(function(key) {
                        if (typeof mappingToConvert[key] === "string") {
                            mappingToConvert[key] = JSON.parse(mappingToConvert[key]);
                        }
                        return mappingToConvert;
                    })
                }

                convertMappings(visToUpdate.visFieldToPathMappingInput);
                console.log(visToUpdate.visFieldToPathMappingInput);

                // Perform put request to update the chart components
                ComponentService.addNewActiveVisualization(vm.component.id, {
                    instanceId: visToUpdate.instanceId,
                    visId: visToUpdate.visId,
                    fieldCollectionId: visToUpdate.fieldCollectionIdInput,
                    visFieldToPathMapping: JSON.parse(JSON.stringify(visToUpdate.visFieldToPathMappingInput))
                    /*{
                    value: visToUpdate.jsonPathInput
                }*/
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
                return vm.deploymentState;
            }, function (newValue, oldValue) {
                if (newValue === 'RUNNING') {
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
                    $('.nav-tabs a[data-target="' + tab + "-" + instanceId + ']"').tab('show');
                } else {
                    // Select the tab as active which id begins with tab
                    $('.nav-tabs a[data-target^="' + tab + '"]').tab('show');
                }
            }

            //Extend the controller object for the public functions to make them available from outside
            angular.extend(vm, {
                updateDeploymentState: updateDeploymentState,
                updateDeviceState: updateDeviceState,
                onDisplayUnitChange: onDisplayUnitChange,
                startComponent: startComponent,
                stopComponent: stopComponent,
                deploy: deploy,
                undeploy: undeploy,
                deleteValueLogs: deleteValueLogs,
                updateJsonPath: updateJsonPath,
                onCreateNewVisualizationClicked: onCreateNewVisualizationClicked,
                deleteChartCard: deleteChartCard,
            });
        }]
);
