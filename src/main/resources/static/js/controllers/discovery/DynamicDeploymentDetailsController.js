/**
 * Controller for the dynamic peripherals details pages.
 */
app.controller('DynamicDeploymentDetailsController',
    ['$scope', '$routeParams', '$interval', '$timeout', 'dynamicDeploymentDetails',
        'DiscoveryService', 'UnitService', 'NotificationService',
        function ($scope, $routeParams, $interval, $timeout, dynamicDeploymentDetails,
                  DiscoveryService, UnitService, NotificationService) {
            //Selectors that allow the selection of different ui cards
            const INFO_CARD_SELECTOR = ".info-card";
            const LIVE_CHART_CARD_SELECTOR = ".live-chart-card";
            const HISTORICAL_CHART_CARD_SELECTOR = ".historical-chart-card";
            const STATS_CARD_SELECTOR = ".stats-card";

            const COMPONENT_TYPE = "dynamic_deployment"

            //Properties of the current dynamic deployment
            const DEPLOYMENT_ID = $routeParams.id;
            const OPERATOR_UNIT = dynamicDeploymentDetails.operator.unit;

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

                //Interval for updating states on a regular basis
                let interval = $interval(function () {
                    //Update states
                }, 2 * 60 * 1000);

                //Cancel interval on route change
                $scope.$on('$destroy', () => {
                    $interval.cancel(interval);
                });
            })();

            /**
             * [Public]
             * Toggles the activation intention of a certain dynamic deployment, given by its ID.
             *
             * @param id The ID of the dynamic deployment
             */
            function toggleActivationIntention(id) {
                alert(id);
            }


            /**
             * [Public]
             * Reloads and updates the deployment state of a certain dynamic deployment, given by its ID.
             *
             * @param id The ID of the dynamic deployment
             */
            function reloadDeploymentState(id) {
                alert(id);
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
             * Asks the user if he really wants to delete all value logs for the current dynamix deployment.
             * If this is the case, the deletion is executed by initiating the corresponding server request.
             */
            function deleteValueLogs() {
                /**
                 * Executes the deletion of the value logs by performing the server request.
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
                    $(LIVE_CHART_CARD_SELECTOR).waitMe({
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
                    $(LIVE_CHART_CARD_SELECTOR).waitMe("hide");
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
                    $(HISTORICAL_CHART_CARD_SELECTOR).waitMe({
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
                    $(HISTORICAL_CHART_CARD_SELECTOR).waitMe("hide");
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
             * Displays a waiting screen with a certain text for the deployment DOM container.
             * @param text The text to display
             */
            function showWaitingScreen(text) {
                //Set a default text
                if (!text) {
                    text = 'Please wait...';
                }

                //Show waiting screen
                $(INFO_CARD_SELECTOR).waitMe({
                    effect: 'bounce',
                    text: text,
                    bg: 'rgba(255,255,255,0.85)'
                });
            }

            /**
             * [Private]
             * Hides the waiting screen for the deployment DOM container.
             */
            function hideWaitingScreen() {
                $(INFO_CARD_SELECTOR).waitMe("hide");
            }

            //Expose the public variables and functions
            angular.extend(vm, {
                dynamicDeployment: dynamicDeploymentDetails,
                displayUnit: OPERATOR_UNIT,
                displayUnitInput: OPERATOR_UNIT,
                toggleActivationIntention: toggleActivationIntention,
                reloadDeploymentState: reloadDeploymentState,
                onDisplayUnitChange: onDisplayUnitChange,
                deleteValueLogs: deleteValueLogs
            });
        }]
);
