/* global app */

'use strict';

/**
 * Directive which creates a table for displaying descriptive statistics for value logs of a certain component.
 */
app.directive('valueLogStats', ['$interval', function ($interval) {
    //Time interval with that the value statistics are supposed to be refreshed
    const REFRESH_DELAY_SECONDS = 60 * 2;

    /**
     * Linking function, glue code
     *
     * @param scope Scope of the directive
     * @param element Elements of the directive
     * @param attrs Attributes of the directive
     */
    let link = function (scope, element, attrs) {


        //Stores the interval object for regular updates
        let updateInterval = null;

        //Attribute in which the statistics data is stored
        scope.statisticsData = {};

        /**
         * [Public]
         * Updates the value statistics and refreshes the display. Before the refreshment, the loadingStart function
         * is called. After the update is finished, the loadingFinished function is called.
         */
        function updateStats(noCallback) {
            //Loading start callback if desired
            if (!noCallback) {
                scope.loadingStart();
            }

            //Retrieve value log stats for this component
            scope.getStats({unit: scope.unit}).then(function (receivedData) {
                //Take received data
                scope.statisticsData = receivedData;

                //Loading finish callback if desired
                if (!noCallback) {
                    scope.loadingFinish();
                }

                //Update UI
                scope.$apply();
            });
        }

        /**
         * [Private]
         * Initializes the update mechanics that are responsible for updating the statistics data on
         * a regular basis.
         */
        function createUpdateInterval() {
            //Create an interval that calls the update function on a regular basis
            updateInterval = $interval(updateStats, 1000 * REFRESH_DELAY_SECONDS);

            //Ensure that the interval is cancelled in case the user switches the page
            scope.$on('$destroy', function () {
                cancelUpdateInterval();
            });
        }

        /**
         * [Private]
         * Cancels the statistics update.
         */
        function cancelUpdateInterval() {
            if (updateInterval) {
                $interval.cancel(updateInterval);
            }
        }

        /**
         * [public]
         * Converts epoch time to a date string
         * @param timeToConvert
         * @return {string} The date string
         */
        function convertTime(timeToConvert = 0) {
            let time = timeToConvert * 1000;
            time = dateToString(new Date(time));
            return time;
        }

        scope.getTimeString = convertTime;

        /**
         * [Private]
         * Converts a javascript date object to a human-readable date string in the "dd.mm.yyyy hh:mm:ss" format.
         *
         * @param date The date object to convert
         * @returns The generated date string in the corresponding format
         */
        function dateToString(date) {
            //Retrieve all properties from the date object
            let year = date.getFullYear();
            let month = '' + (date.getMonth() + 1);
            let day = '' + date.getDate();
            let hours = '' + date.getHours();
            let minutes = '' + date.getMinutes();
            let seconds = '' + date.getSeconds();

            //Add a leading zero (if necessary) to all properties except the year
            let values = [day, month, hours, minutes, seconds];
            for (let i = 0; i < values.length; i++) {
                if (values[i].length < 2) {
                    values[i] = '0' + values[i];
                }
            }

            //Generate and return the date string
            return ([values[0], values[1], year].join('.')) +
                ' ' + ([values[2], values[3], values[4]].join(':'));
        }

        //Watch the unit parameter
        scope.$watch(function () {
            return scope.unit;
        }, function (newValue, oldValue) {
            //Update statistics if unit was changed
            updateStats();
        });

        //Expose public api
        scope.api = {
            updateStats: updateStats
        };

        //Load and display statistics initially
        updateStats();
        createUpdateInterval();
    };

    //Configure and expose the directive
    return {
        restrict: 'E', //Elements only
        template:
            '<span ng-show="!(statisticsData.numberLogs > 0)">No values received yet.</span>' +
            '<table class="table table-hover" ng-show="statisticsData.numberLogs > 0">' +
            '<tbody>' +
            '<tr>' +
            '<th>Number of values:</th>' +
            '<td>{{statisticsData.numberLogs}}</td>' +
            '</tr>' +
            '<tr>' +
            '<th>First value:</th>' +
            '<td>{{getTimeString(statisticsData.firstLog.time)}}<json-formatter json="statisticsData.firstLog.value" open="1"></json-formatter></td>' +
            '<span>&nbsp;{{(unit)}}</span></td>' +
            '</tr>' +
            '<tr>' +
            '<th>Last value:</th>' +
            '<td>{{getTimeString(statisticsData.lastLog.time)}}<json-formatter json="statisticsData.lastLog.value" open="1"></json-formatter></td>' +
            '<span>&nbsp;{{(unit)}}</span></td>' +
            '</tr>' +
            '</tbody>' +
            '</table>'
        ,
        link: link,
        scope: {
            //Public api that provides functions for controlling the stats display
            api: "=api",
            //The unit in which the statistics are supposed to be displayed
            unit: '@unit',
            //Functions that are called when the chart loads/finishes loading data
            loadingStart: '&loadingStart',
            loadingFinish: '&loadingFinish',
            //Function for updating the value stats data
            getStats: '&getStats',
        }
    };
}]);