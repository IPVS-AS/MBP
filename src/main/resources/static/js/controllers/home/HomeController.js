/**
 * Controller for the Home page.
 */
app.controller('HomeController',
    ['$scope', 'countEnvModels', 'countActuators', 'countSensors', 'countDevices', 'countPolicies',
        function ($scope, countEnvModels, countActuators, countSensors, countDevices, countPolicies) {
            //DOM elements
            const ELEMENT_WELCOME_CARD = $('#welcome-card-body');

            //Local storage keys
            const STORAGE_KEY_WELCOME = "show_welcome_card";

            let vm = this;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Read flag for hiding/showing the welcome card from local storage
                let retrievedFlag = localStorage.getItem(STORAGE_KEY_WELCOME) || "true";

                vm.showWelcomeCard = (retrievedFlag === "true");

                //Bootstrap events for showing and hiding the welcome card
                ELEMENT_WELCOME_CARD.on('show.bs.collapse', function () {
                    localStorage.setItem(STORAGE_KEY_WELCOME, "true");
                }).on('hide.bs.collapse', function () {
                    localStorage.setItem(STORAGE_KEY_WELCOME, "false");
                })

                //Trigger info-box count effect
                $(document).ready(() => {
                    $('.count-to').countTo();
                });
            })();

            //Expose
            angular.extend(vm, {
                countEnvModels: countEnvModels,
                countActuators: countActuators,
                countSensors: countSensors,
                countDevices: countDevices,
                countPolicies: countPolicies
            });
        }]
);

