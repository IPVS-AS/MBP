/* global app */

'use strict';

/**
 * Directive which creates a table for displaying descriptive statistics for value logs of a certain component.
 *
 * @author Jan
 */
app.directive('valueLogStats', ['$interval', function ($interval) {
    /**
     * Linking function, glue code
     *
     * @param scope Scope of the directive
     * @param element Elements of the directive
     * @param attrs Attributes of the directive
     */
    var link = function (scope, element, attrs) {
        //Stores the interval object for regular updates
        var updateInterval = null;

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
            updateInterval = $interval(updateStats, 1000 * scope.interval);

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

        //Watch the unit parameter
        scope.$watch(function () {
            return scope.unit;
        }, function (newValue, oldValue) {
            //Update statistics if unit was changed
            updateStats();
        });

        //Watch the interval parameter
        scope.$watch(function () {
            return scope.interval;
        }, function (newValue, oldValue) {
            //Create new update interval with the new value
            scope.interval = newValue;
            cancelUpdateInterval();
            createUpdateInterval();
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
            '<th>Average:</th>' +
            '<td>{{statisticsData.average}}&nbsp;{{unit}}</td>' +
            '</tr>' +
            '<tr>' +
            '<th>Variance:</th>' +
            '<td>{{statisticsData.variance}}&nbsp;{{unit ? "(" + unit + ")&sup2;" : ""}}</td>' +
            '</tr>' +
            '<tr>' +
            '<th>Standard deviation:</th>' +
            '<td>{{statisticsData.standardDeviation}}&nbsp;{{(unit)}}</td>' +
            '</tr>' +
            '<tr>' +
            '<th>First value:</th>' +
            '<td><button uib-popover="{{statisticsData.firstLog.message}}"' +
            'popover-title="{{statisticsData.firstLog.date}}" type="button"' +
            'class="btn btn-default">{{statisticsData.firstLog.value}}</button>' +
            '<span>&nbsp;{{(unit)}}</span></td>' +
            '</tr>' +
            '<tr>' +
            '<th>Last value:</th>' +
            '<td><button uib-popover="{{statisticsData.lastLog.message}}"' +
            'popover-title="{{statisticsData.lastLog.date}}" type="button"' +
            'class="btn btn-default">{{statisticsData.lastLog.value}}</button>' +
            '<span>&nbsp;{{(unit)}}</span></td>' +
            '</tr>' +
            '<tr>' +
            '<th>Minimum value:</th>' +
            '<td><button uib-popover="{{statisticsData.minimumLog.message}}"' +
            'popover-title="{{statisticsData.minimumLog.date}}" type="button"' +
            'class="btn btn-default">{{statisticsData.minimumLog.value}}</button>' +
            '<span>&nbsp;{{(unit)}}</span></td>' +
            '</tr>' +
            '<tr>' +
            '<th>Maximum value:</th>' +
            '<td><button uib-popover="{{statisticsData.maximumLog.message}}"' +
            'popover-title="{{statisticsData.maximumLog.date}}" type="button"' +
            'class="btn btn-default">{{statisticsData.maximumLog.value}}</button>' +
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
            //Refresh interval
            interval: '@interval'
        }
    };
}]);