/* global app */

'use strict';

/**
 * Directive which creates a chart for displaying live gps values with a certain refresh time.
 * In addition, a fake progress bar is added to visualize the timespan between refreshments.
 */
app.directive('liveGeoMapChart', ['$timeout', '$interval', function ($timeout, $interval) {
    //Maximum number of elements that may be displayed in the chart
    const CHART_MAX_ELEMENTS = 1;

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
        var markers = null;

        //Progress jQuery element
        var progressBar = element.find('.progress-bar');

        /**
         * [Private]
         * Initializes the chart.
         */
        function initChart() {
            //Create chart
            if (chart != undefined) {
                chart.remove();
            }
            chart = new L.map(chartContainer).setView([48.74516, 9.10682], 5);

            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            }).addTo(chart);

            //Data will be loaded
            scope.loadingStart();
        }

        /**
         * [Private]
         * Initializes the update mechanics that are required in order to retrieve the most recent value logs
         * from the server and to update the chart accordingly.
         */
        function initChartUpdate() {

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
                    values = values.map(applyJsonPath);

                    function applyJsonPath(value, index, array) {
                        var newVal = [];
                        console.log(jsonPathAsObj.latitude.path);
                        console.log(jsonPathAsObj.longitude.path);
                        if (scope.fieldCollectionId === 'default') {
                            console.log(value);
                            // Push time
                            newVal.push(value[0]);
                            // Push coordinates
                            newVal.push([
                                parseFloat(JSONPath.JSONPath({
                                    path: jsonPathAsObj.latitude.path,
                                    json: value[1]
                                }).toString()),
                                parseFloat(JSONPath.JSONPath({
                                    path: jsonPathAsObj.longitude.path,
                                    json: value[1]
                                }).toString())]);
                        } else {
                            // Alternative approach
                        }
                        return newVal;
                    }
                    console.log("LIVECHARTVALS");
                    console.log(values);

                    console.log("End JsonPath retrieval");

                    /*
                     * The server requests returns a number of most recent logs; however, it is possible
                     * that some of the value logs are already displayed in the chart and do not need
                     * to be added again. Thus, filtering is needed, for which the variable lastDate
                     * is used to remember the date of the most recent log displayed in the chart.
                     */

                    /**
                     * Displays array of data in the following format:
                     * values = [time, [latitude, longitude]]}
                     * @param values
                     */
                    function displayData(values) {
                        // Clear possibly already existing markers
                        if (markers != null) {
                            markers.clearLayers();
                        }

                        var latLngArr = [];

                        markers = L.layerGroup([]);

                        for (var i = 0; i < values.length; i++) {
                            var marker = L.marker([values[i][1][0], values[i][1][1]]);
                            latLngArr.push([values[i][1][0], values[i][1][1]])
                            marker.bindPopup("<b>" + values[i][0] + "</b><br> Lat=" + values[i][1][0] + "°<br>Long=" + values[i][1][1] + "°");

                            // add marker
                            markers.addLayer(marker);
                        }
                        chart.addLayer(markers);
                        chart.fitBounds(new L.LatLngBounds(latLngArr), {
                            maxZoom: 5
                        });
                    }

                    // Display data, as only the current live position is visualized no extra handlings are needed
                    displayData(values);

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
            initChartUpdate();
        });

        //Watch the fieldCollectionId parameter
        scope.$watch(function () {
            return scope.fieldCollectionId;
        }, function (newValue, oldValue) {
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
            '<div class="chart-container" id="mapLiveId"' + 'style="height: 400px;"></div>',
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