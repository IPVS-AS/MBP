/**
 * Controller for the index main page.
 */
app.controller('IndexController', ['$scope', '$rootScope', '$timeout', 'SettingsService',
    function ($scope, $rootScope, $timeout, SettingsService) {
        let vm = this;

        //Whether to show the loading bar for HTTP requests
        vm.loadingBarDisplay = false;
        //Number of current requests to display in the loading bar
        vm.requestCount = 0;

        /**
         * Initializing function, sets up basic things.
         */
        (function initController() {
            /* Listen for request related events */

            //At least one HTTP request is in progress
            $rootScope.$on("requestsProgressing", (e, r) => $timeout((function (event, requestCount) {
                //Show loading bar and update request count
                vm.loadingBarDisplay = true;
                vm.requestCount = requestCount;
            }).bind(null, e, r)));

            //All requests have concluded
            $rootScope.$on("requestsFinished", () => $timeout(function () {
                //Hide loading bar
                vm.loadingBarDisplay = false;
                vm.requestCount = 0;
            }));

            //Retrieve MBP info and make it globally available
            addMBPInfo();
        })();

        /**
         * [Private]
         * Performs a server request in order to retrieve information about the running MBP instance and the environment
         * in which it is operated and makes it globally available in the root scope.
         */
        function addMBPInfo() {
            //Issue server request
            SettingsService.getMBPInfo().then(function (response) {
                $rootScope.mbpinfo = response;
            });
        }

        /**
         * [Public]
         * Shows an alert with information about the MBP.
         */
        function showAboutAlert() {
            Swal.fire({
                title: '<strong>Multi-purpose Binding and Provisioning Platform</strong>',
                icon: 'info',
                html: '<strong>Version</strong>: ' + $rootScope.mbpinfo.version + '<br/><br/>' +
                    'Branch: ' + $rootScope.mbpinfo.branch + '<br/>' +
                    'Commit: ' + $rootScope.mbpinfo.commitID + '<br/><br/>' +
                    'MBP Team<br/>IPVS-AS, Universität Stuttgart' +
                    '<a class="github-fork-ribbon" href="https://github.com/IPVS-AS/MBP" data-ribbon="Visit us on GitHub" title="Visit us on GitHub">Visit us on GitHub</a>',
            })
        }

        /**
         * [Public]
         * Returns the current date.
         * @returns {Date} The current date
         */
        function getDate() {
            return new Date();
        }

        //Expose
        angular.extend(vm, {
            getDate: getDate,
            showAboutAlert: showAboutAlert
        });
    }]);