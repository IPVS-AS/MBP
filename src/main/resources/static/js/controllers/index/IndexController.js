/**
 * Controller for the index main page.
 */
app.controller('IndexController', ['$scope', '$rootScope', function ($scope, $rootScope) {
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
        $rootScope.$on("requestsProgressing", function (event, requestCount) {
            vm.loadingBarDisplay = true;
            vm.requestCount = requestCount;
            $scope.$apply();
        });

        //All requests have concluded
        $rootScope.$on("requestsFinished", function () {
            vm.loadingBarDisplay = false;
            vm.requestCount = 0;
            $scope.$apply();
        });
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