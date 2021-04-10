/* global app */

'use strict';

/**
 * Directive which creates a chart for displaying live values with a certain refresh time.
 * In addition, a fake progress bar is added to visualize the timespan between refreshments.
 *
 * @author Jan
 */
app.directive('liveChart', ['$timeout', '$interval', function ($timeout, $interval) {
    //Maximum number of elements that may be displayed in the chart
    const CHART_MAX_ELEMENTS = 20;

    /**
     * Linking function, glue code
     *
     * @param scope Scope of the directive
     * @param element Elements of the directive
     * @param attrs Attributes of the directive
     */
    var link = function (scope, element, attrs) {

        //Chart objects
        var chartContainer = element.find('.chart-container').get(0);
        var chart = null;
        var chartInterval = null;

        //Progress jQuery element
        var progressBar = element.find('.progress-bar');

        /**
         * [Private]
         * Initializes the chart.
         */
        function initChart() {
            //Set required global library options
            Highcharts.setOptions({
                global: {
                    useUTC: false
                }
            });

            //Destroy chart if already existing
            if (chart) {
                chart.destroy();
            }

            //Create new chart with certain options
            chart = Highcharts.stockChart(chartContainer, {
                title: {
                    text: ''
                },
                rangeSelector: {
                    enabled: false
                },
                xAxis: {
                    type: 'datetime',
                    labels: {
                        format: '{value}'
                    }
                },
                yAxis: {
                    opposite: false
                },
                navigator: {
                    xAxis: {
                        type: 'datetime',
                        labels: {
                            format: '{value}'
                        }
                    }
                },
                series: [{
                    name: 'Value',
                    data: []
                }],
                tooltip: {
                    valueDecimals: 2,
                    valuePrefix: '',
                    valueSuffix: ' ' + scope.unit
                }
            });

            //Set y-axis unit
            chart.yAxis[0].labelFormatter = function () {
                return this.value + ' ' + scope.unit;
            };

            //Data will be loaded
            scope.loadingStart();
        }

        /**
         * [Private]
         * Initializes the update mechanics that are required in order to retrieve the most recent value logs
         * from the server and to update the chart accordingly.
         */
        function initChartUpdate() {
            //Counts the number of retrieved logs for ensuring the log limit
            var count = 0;

            //Get series from the chart that is supposed to be updated
            var series = chart.series[0];

            var lastDate = null;

            //Define the update function that can be called on a regular basis
            var intervalFunction = function () {
                //Ensure that the chart has already been initialized
                if (chart == null) {
                    console.error("The live chart has not been initialized yet.");
                    return;
                }

                //Do not update in case it is not possible/necessary
                if (!scope.isUpdateable()) {
                    return;
                }

                //Retrieve the most recent component data
                scope.getData({
                    numberLogs: CHART_MAX_ELEMENTS,
                    descending: true,
                    unit: scope.unit
                }).then(function (values) {
                    //Abort of no data is available
                    if (values.length < 1) {
                        return;
                    }

                    /*
                     * The server requests returns a number of most recent logs; however, it is possible
                     * that some of the value logs are already displayed in the chart and do not need
                     * to be added again. Thus, filtering is needed, for which the variable lastDate
                     * is used to remember the date of the most recent log displayed in the chart.
                     */

                    //Check if there is already data in the chart
                    if (lastDate == null) {
                        //No data in the chart, thus add all received value logs
                        for (var i = values.length - 1; i >= 0; i--) {
                            series.addPoint(values[i], true, (++count >= CHART_MAX_ELEMENTS));
                        }
                    } else {
                        /* There is already data in the chart, so iterate over all value logs but
                         only take the ones from the array that occur before the log with lastDate */
                        var insert = false;
                        for (var i = values.length - 1; i >= 0; i--) {
                            //Try to find the log with lastdate in the array
                            if (values[i][0] === lastDate) {
                                insert = true;
                            } else if (insert) {
                                //This is a log before the log with lastedate
                                series.addPoint(values[i], true, (++count >= CHART_MAX_ELEMENTS));
                            }
                        }

                        /* In case the log with lastDate could not be found, this means that all data is relevant
                         and needs to be added to the chart */
                        if (!insert) {
                            for (var i = values.length - 1; i >= 0; i--) {
                                series.addPoint(values[i], true, (++count >= CHART_MAX_ELEMENTS));
                            }
                        }
                    }
                    //Update lastDate with the most recent log that was added to the chart
                    lastDate = values[0][0];
                }).then(function () {
                    //Loading finished
                    scope.loadingFinish();

                    //Visualize the time until the next refreshment
                    runProgress(scope.interval);
                });
            };

            //Create an interval that calls the update function on a regular basis
            chartInterval = $interval(intervalFunction, 1000 * scope.interval);

            //Ensure that the interval is cancelled in case the user switches the page
            scope.$on('$destroy', function () {
                cancelChartUpdate();
            });
        }

        /**
         * [Private]
         * Cancels the chart update.
         */
        function cancelChartUpdate() {
            if (chartInterval) {
                $interval.cancel(chartInterval);
            }
        }

        /**
         * [Private]
         *  Starts the progress bar and lets it run for a certain time. The progress will be finished
         *  when the time is over.
         *
         * @param time The time in seconds during which the progress bar is supposed to be active
         */
        function runProgress(time) {
            progressBar.stop(true).width(0).animate({
                width: "100%",
            }, time * 1000);
        }

        //Watch the unit parameter
        scope.$watch(function () {
            return scope.unit;
        }, function (newValue, oldValue) {
            //Update chart if unit was changed
            cancelChartUpdate();
            initChart();
            initChartUpdate();
        });

        //Watch the interval parameter
        scope.$watch(function () {
            return scope.interval;
        }, function (newValue, oldValue) {
            //Create new update interval with the new value
            scope.interval = newValue;
            cancelChartUpdate();
            initChartUpdate();
        });

        //Initialize chart
        initChart();
        initChartUpdate();
    };

    //Configure and expose the directive
    return {
        restrict: 'E', //Elements only
        template:
            '<div class="progress chart-progress">' +
            '<div class="progress-bar progress-bar-success progress-bar-striped active" role="progressbar" style="transition: unset">' +
            '<span class="sr-only"></span>' +
            '</div>' +
            '</div>' +
            '<div class="chart-container"></div>',
        link: link,
        scope: {
            //The unit in which the values are supposed to be displayed
            unit: '@unit',
            //Functions that are called when the chart loads/finishes loading data
            loadingStart: '&loadingStart',
            loadingFinish: '&loadingFinish',
            //Function for updating the displayed data
            getData: '&getData',
            //Function that checks whether the chart is allowed to update its data
            isUpdateable: '&isUpdateable',
            //Refresh interval
            interval: '@interval'
        }
    };
}]);