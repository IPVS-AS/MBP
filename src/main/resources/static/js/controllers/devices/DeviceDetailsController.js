/* global app */

/**
 * Controller for the sensor details page that implements the ComponentDetailsController.
 */
app.controller('DeviceDetailsController',
    ['$scope', '$controller', '$routeParams', '$interval', 'deviceDetails', 'compatibleAdapters', 'DeviceService', 'NotificationService',
        function ($scope, $controller, $routeParams, $interval, deviceDetails, compatibleAdapters, DeviceService, NotificationService) {

            //Selectors that allow the selection of different ui cards
            const DETAILS_CARD_SELECTOR = ".details-card";

            //Important properties of the currently considered device
            const DEVICE_ID = $routeParams.id;

            var vm = this;


            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Check if the compatible adapters were retrieved successfully
                if (compatibleAdapters == null) {
                    NotificationService.notify("Could not retrieve compatible adapters.", "error");
                }

                //Initialize device state
                vm.deviceState = 'UNKNOWN';

                //Make device details and list of compatible adapters available
                vm.device = deviceDetails;
                vm.compatibleAdapters = compatibleAdapters;

                //Initialize deployment parameters
                initParameters();

                //Interval for updating states on a regular basis
                var interval = $interval(function () {
                    updateDeviceState(true);
                }, 2 * 60 * 1000);

                //Cancel interval on route change
                $scope.$on('$destroy', function () {
                    $interval.cancel(interval);
                });

                //Load device state for the first time
                updateDeviceState();
            })();

            function initParameters() {
                //Stores the parameters and their values as assigned by the user
                vm.parameterValues = [];

                //Extend parameter array for one array per compatible adapter
                for (var i = 0; i < compatibleAdapters.length; i++) {
                    //Get formal parameters for the current adapter
                    var formalParams = compatibleAdapters[i].parameters;

                    //Array for the parameters of the current adapter
                    var adapterParameterArray = [];

                    //Iterate over all formal parameters
                    for (var j = 0; j < formalParams.length; j++) {
                        //Set empty default values for the current parameter
                        var value = "";
                        if (formalParams[j].type == "Switch") {
                            value = false;
                        }

                        //Add a tuple (name, value) for the current parameter to the adapter array
                        adapterParameterArray.push({
                            "name": formalParams[j].name,
                            "value": value
                        });
                    }

                    //Add parameter array for this adapter to the global array
                    vm.parameterValues.push(adapterParameterArray);
                }
            }

            /**
             * {Public]
             * Updates the device state. By default, a waiting screen is displayed during the update.
             * However, this can be deactivated.
             *
             * @param noWaitingScreen If set to true, no waiting screen is displayed during the refreshment
             */
            function updateDeviceState(noWaitingScreen) {
                //Check if waiting screen is supposed to be displayed
                if (!noWaitingScreen) {
                    showDetailsWaitingScreen("Retrieving device state...");
                }

                //Retrieve the state of the current device
                DeviceService.getDeviceState(DEVICE_ID).then(function (response) {
                    //Success
                    vm.deviceState = response.data;
                }, function (response) {
                    //Failure
                    vm.deviceState = 'UNKNOWN';
                    NotificationService.notify('Could not retrieve device state.', 'error');
                }).then(function () {
                    //Finally hide the waiting screen again
                    hideDetailsWaitingScreen();
                });
            }

            /**
             * [Private]
             * Displays a waiting screen with a certain text for the details DOM container.
             * @param text The text to display
             */
            function showDetailsWaitingScreen(text) {
                //Set a default text
                if (!text) {
                    text = 'Please wait...';
                }

                //Show waiting screen
                $(DETAILS_CARD_SELECTOR).waitMe({
                    effect: 'bounce',
                    text: text,
                    bg: 'rgba(255,255,255,0.85)'
                });
            }

            /**
             * [Private]
             * Hides the waiting screen for the details DOM container.
             */
            function hideDetailsWaitingScreen() {
                $(DETAILS_CARD_SELECTOR).waitMe("hide");
            }

            angular.extend(vm, {
                updateDeviceState: updateDeviceState
            });
        }]
);