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

    //Interval with that the chart data is refreshed (seconds)
    const REFRESH_DELAY_SECONDS = 15;

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
            var series = chart.series;

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

                console.log("get data");
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

                    // As the directive takes the jsonPath parameter as string a conversion to an object is necessary
                    var jsonPathAsObj = JSON.parse(scope.jsonPath);

                    console.log("Start JsonPath retrieval");
                    // Retrieve double values from the values by using JsonPath
                    values.forEach(applyJsonPath);

                    function applyJsonPath(value, index, array) {
                        if (scope.fieldCollectionId === 'default') {
                            array[index][1] = parseFloat(JSONPath.JSONPath({
                                path: jsonPathAsObj.value.path,
                                json: array[index][1]
                            }).toString());
                        } else {
                            array[index][1] = JSONPath.JSONPath({
                                path: jsonPathAsObj.arrVal.path,
                                json: array[index][1]
                            });
                        }
                    }

                    console.log("End JsonPath retrieval");

                    if (scope.fieldCollectionId != 'default') {
                        // Add series if array visualization needs more series elements
                        for (var i = series.length-1; i < values[0][1].length; i++) {
                            chart.addSeries({
                                name: 'Value' + i,
                                data: [],
                                showInNavigator: true
                            });
                            console.log("addChart");
                        }
                        // Update legend
                        for (var i = 0; i < chart.series.length; i++) {
                            chart.series[i].update({
                                name: jsonPathAsObj.arrVal.name  + "[" + i + "]",
                                tooltip: {valueSuffix: ' ' + (jsonPathAsObj.arrVal.unit ? jsonPathAsObj.arrVal.unit : '')}
                            }, true);
                        }
                    } else {
                        series[0].update({
                            name: jsonPathAsObj.value.name,
                            tooltip: {valueSuffix: ' ' + (jsonPathAsObj.value.unit ? jsonPathAsObj.value.unit : '')}
                        })
                    }

                    /*
                     * The server requests returns a number of most recent logs; however, it is possible
                     * that some of the value logs are already displayed in the chart and do not need
                     * to be added again. Thus, filtering is needed, for which the variable lastDate
                     * is used to remember the date of the most recent log displayed in the chart.
                     */

                    var addArrayPointsToChart = function (valueIndex) {
                        ++count;
                        for (var j = 0; j < series.length; j++) {
                            var newVal = [];
                            newVal.push(values[valueIndex][0]);
                            newVal.push(values[valueIndex][1][j])
                            series[j].addPoint(newVal, true, (count >= CHART_MAX_ELEMENTS));
                            //chart.xAxis.setExtremes();
                        }
                    }

                    //Check if there is already data in the chart
                    if (lastDate == null) {
                        //No data in the chart, thus add all received value logs
                        console.log("No data in chart: Start adding points.");
                        for (var i = values.length - 1; i >= 0; i--) {
                            if (scope.fieldCollectionId === 'default') {
                                series[0].addPoint(values[i], true, (++count >= CHART_MAX_ELEMENTS));
                            } else {
                                addArrayPointsToChart(i);
                            }
                        }
                        console.log("No data in chart: Finish adding points.");
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
                                if (scope.fieldCollectionId === 'default') {
                                    series[0].addPoint(values[i], true, (++count >= CHART_MAX_ELEMENTS));
                                } else {
                                    addArrayPointsToChart(i);
                                }
                            }
                        }

                        console.log("Data check of live chart compelted.");

                        /* In case the log with lastDate could not be found, this means that all data is relevant
                         and needs to be added to the chart */
                        if (!insert) {
                            for (var i = values.length - 1; i >= 0; i--) {
                                if (scope.fieldCollectionId === 'default') {
                                    series[0].addPoint(values[i], true, (++count >= CHART_MAX_ELEMENTS));
                                } else {
                                    addArrayPointsToChart(i);
                                }
                            }
                        }
                    }

                    console.log("Added points to chart");
                    //Update lastDate with the most recent log that was added to the chart
                    lastDate = values[0][0];
                }).then(function () {
                    //Loading finished
                    scope.loadingFinish();

                    //Visualize the time until the next refreshment
                    runProgress(REFRESH_DELAY_SECONDS);
                });
            };

            //Create an interval that calls the update function on a regular basis
            chartInterval = $interval(intervalFunction, 1000 * REFRESH_DELAY_SECONDS);

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
            }, 15 * 1000);
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

        //Watch the jsonPath parameter
        scope.$watch(function () {
            return scope.jsonPath;
        }, function (newValue, oldValue) {
            /*
             * Update chart if jsonPath was changed, which means for the live chart: redraw everything
             * by accepting the discard of some values
            */
            cancelChartUpdate();
            initChart();
            initChartUpdate();
        });

        //Watch the fieldCollectionId parameter
        scope.$watch(function () {
            return scope.fieldCollectionId;
        }, function (newValue, oldValue) {
            cancelChartUpdate();
            initChart();
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
            // The json path which should be used to interpret the json value data
            jsonPath: '@jsonPath',
            // The name of the field collection which the visualization uses
            fieldCollectionId: '@fieldCollectionId',
            //Functions that are called when the chart loads/finishes loading data
            loadingStart: '&loadingStart',
            loadingFinish: '&loadingFinish',
            //Function for updating the displayed data
            getData: '&getData',
            //Function that checks whether the chart is allowed to update its data
            isUpdateable: '&isUpdateable'
        }
    };
}]);