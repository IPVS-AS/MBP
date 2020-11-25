/**
 * Controller for the index main page.
 */
app.controller('IndexController', ['$scope', '$rootScope', '$timeout', function ($scope, $rootScope, $timeout) {
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
    })();

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
        getDate: getDate
    });
}]);