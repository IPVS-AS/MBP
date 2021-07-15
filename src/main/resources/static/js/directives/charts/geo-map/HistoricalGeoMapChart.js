/* global app */

'use strict';

/**
 * Directive whreich creates a chart for displaying historical gps values. Control elements are provided with
 * which the user has the possibility to select the data that he wants to display.
 */
app.directive('historicalGeoMapChart', ['$timeout', '$interval', function ($timeout, $interval) {
    //Initial number of elements to display in the chart
    const CHART_INITIAL_ELEMENTS_NUMBER = 200;

    //Minimum/maximum number of elements that can be displayed in the chart
    const CHART_MIN_ELEMENTS = 0;
    const CHART_MAX_ELEMENTS = 5000;

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
        var markers = null;

        //Slider objects
        var sliderContainer = element.find('.chart-slider');

        //Define chart settings that can be adjusted by the user
        scope.settings = {
            numberOfValues: CHART_INITIAL_ELEMENTS_NUMBER,
            mostRecent: true,
            startTime: "",
            endTime: ""
        };

        /**
         * [Private]
         * Initializes the chart.
         */
        function initChart() {

            //Create chart
            chart = new L.map(chartContainer).setView([48.74516, 9.10682], 5);

            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            }).addTo(chart);

            //Initialize slider
            sliderContainer.ionRangeSlider({
                skin: "flat",
                type: "single",
                grid: true,
                grid_num: 5,
                grid_snap: false,
                step: 1,
                min: CHART_MIN_ELEMENTS,
                max: CHART_MAX_ELEMENTS,
                from: scope.settings.numberOfValues,
                onFinish: function (data) {
                    //Update chart with new values
                    scope.settings.numberOfValues = data.from;
                    updateChart();
                }
            });

            //Watch value border and update chart on change
            scope.$watch(
                function () {
                    return scope.settings.mostRecent;
                },
                function () {
                    //Update chart on change
                    updateChart();
                }
            );

            //Expose public api
            scope.api = {
                updateChart: updateChart
            };

            //Watch time filter setting and update chart on change
            scope.$watch(
                function () {
                    //Check start time and end time for changes
                    return scope.settings.startTime + scope.settings.endTime;
                },
                function () {
                    //Update chart on change
                    updateChart();
                }
            );

            //Populate the chart
            updateChart();
        }

        /**
         * [Public]
         * Updates the chart and refreshes its data. Before the refreshment, the loadingStart function is called. After
         * the update is finished, the loadingFinished function is called.
         */
        function updateChart() {

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
                    marker.bindPopup("<b>" + new Date(values[i][0]).toLocaleString() + "</b><br> Lat=" + values[i][1][0] + "°<br>Long=" + values[i][1][1] + "°");
                    marker.bindTooltip("" + i, {
                        permanent: true,
                        opacity: 0.7
                    })
                    // add marker
                    markers.addLayer(marker);
                }
                chart.addLayer(markers);
                chart.fitBounds(new L.LatLngBounds(latLngArr));
            }

            //Ensure that the chart has already been initialized
            if (chart == null) {
                console.error("The historical chart has not been initialized yet.");
                return;
            }

            //Data will be loaded
            scope.loadingStart();

            //Retrieve a fixed number of value logs from the server
            scope.getData({
                numberLogs: scope.settings.numberOfValues,
                descending: scope.settings.mostRecent,
                unit: scope.unit,
                startTime: scope.settings.startTime ? new Date(scope.settings.startTime).getTime() : -1,
                endTime: scope.settings.endTime ? new Date(scope.settings.endTime).getTime() : -1
            }).then(function (values) {
                //Reverse the values array if ordered in descending order
                if (scope.settings.mostRecent) {
                    values = values.reverse();
                }

                // As the directive takes the jsonPath parameter as string a conversion to an object is necessary
                var jsonPathAsObj = JSON.parse(scope.jsonPath);

                // Apply jsonPath on the values
                var valuesToVisualize = values.map(applyJsonPath);

                function applyJsonPath(value, index, array) {
                    var newVal = [];

                    if (scope.fieldCollectionId === 'default') {
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

                if (scope.fieldCollectionId === 'default') {
                    //Update chart
                    displayData(valuesToVisualize);
                } else {
                    // Alternative approach
                }
                //Loading finished
                scope.loadingFinish();
            });
        }

        //Watch the unit parameter
        scope.$watch(function () {
            return scope.unit;
        }, function (newValue, oldValue) {
            //Update chart if unit was changed
            updateChart();
        });

        //Watch the jsonPath parameter
        scope.$watch(function () {
            return scope.jsonPath;
        }, function (newValue, oldValue) {
            //Update chart if jsonPath was changed
            updateChart();
        });

        //Watch the fieldCollectionId parameter
        scope.$watch(function () {
            return scope.fieldCollectionId;
        }, function (newValue, oldValue) {

            //initChart();
            updateChart();
        });

        //Initialize chart
        initChart();
    };

    //Configure and expose the directive
    return {
        restrict: 'E', //Elements only
        template:
            '<div class="chart-container" id="mapid"' + 'style="height: 400px;"></div>' +
            '<br/>' +
            '<table>' +
            '<tr>' +
            '<th style="min-width: 195px">Values to display:</th>' +
            '<th style="width: 100%">Number of values:</th>' +
            '<th></th>' +
            '</tr>' +
            '<tr>' +
            '<td>' +
            '<div class="switch">' +
            '<label>' +
            'Oldest' +
            '<input type="checkbox" ng-model="settings.mostRecent">' +
            '<span class="lever"></span>' +
            'Most recent' +
            '</label>' +
            '</td>' +
            '<td>' +
            '<div class="range-slider">' +
            '<input type="text" class="chart-slider"/>' +
            '</div>' +
            '</td>' +
            '<td></td>' +
            '</tr>' +
            '<tr>' +
            '<th>Start time:</th>' +
            '<th>End time:</th>' +
            '<th></th>' +
            '</tr>' +
            '<tr>' +
            '<td><input type="datetime-local" style="width: 170px; margin-right: 10px;" ng-model="settings.startTime"></td>' +
            '<td><input type="datetime-local" style="width: 170px; margin-right: 10px" ng-model="settings.endTime">' +
            '<button class="btn btn-primary waves-effect" style="width: 100px; height:30px;" ng-click="settings.startTime=\'\';settings.endTime=\'\'">Clear</button>' +
            '</td>' +
            '</tr>' +
            '</table>',
        link: link,
        scope: {
            //Public api that provides functions for controlling the chart
            api: "=api",
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
            getData: '&getData'
        }
    };
}]);