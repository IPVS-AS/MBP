/* global app */

'use strict';

/**
 * Directive for a generic card to visualize complex sensor data, customizable in many ways.
 */
app.directive('customizableChartCard', ['ComponentService', function (ComponentService) {

    //Selectors that allow the selection of different ui cards
    const LIVE_CHART_CARD_SELECTOR = ".live-chart-card";
    const HISTORICAL_CHART_CARD_SELECTOR = ".historical-chart-card";

    /**
     * Linking function, glue code
     *
     * @param scope Scope of the directive
     * @param element Elements of the directive
     * @param attrs Attributes of the directive
     */
    var link = function (scope, element, attrs) {

        // All visualizations applicable for this component
        scope.availableVisualizationsMappings = scope.componentData.operator.dataModel.possibleVisMappings;

        // All visualization ids of all visualizations applicable for this component
        scope.idOfAllApplicableVisualizations = scope.availableVisualizationsMappings.map(visObj => visObj.visName);

        // Binding for the next chart to add (visualization id)
        scope.nextChartToAdd = "select";

        // The currently chosen chart visualization id
        scope.currChart = "select";

        scope.onCreateNewVisualizationClicked = function () {
            //alert(scope.nextChartToAdd + " for sensor " + scope.componentData.name);
            scope.currChart = scope.nextChartToAdd;
            scope.visItem.visId = scope.currChart;
            scope.visItem = new ActiveVisualization(scope.instanceId, scope.nextChartToAdd);
            //alert(scope.nextChartToAdd);
        }

        /**
         * [public]
         *
         * Actions when a deletion button of the visualization is pressed. Sets the visualization
         * id to "select" which results in the display of the start menu.
         * @param visId
         */
        scope.deleteChartCard = function (visId) {
            scope.currChart = "select";
            scope.visItem.visId = "select";
            scope.nextChartToAdd = "select";
        };


        // Represents one instance of a currently active visualization
        class ActiveVisualization {
            constructor(instanceId, visId) {
                this.instanceId = instanceId;
                this.visId = visId;
                this.fieldCollectionId = "";
                this.fieldCollectionIdInput = "";
                this.visFieldToPathMapping = {};
                this.visFieldToPathMappingInput = {};
                this.jsonPath = "";
                this.jsonPathInput = "";
                this.availableOptions = getVisualizationMappingFieldByVisId(this.visId);

                this.setFieldCollectionIdInput = function (newIdInput) {
                    alert("fieldcollection input set to " + newIdInput);
                    this.fieldCollectionIdInput = newIdInput;
                }
            }

            hasVisualizationOptionFieldCollectionWithName = function hasVisualizationOptionFieldCollectionWithName(fieldCollectionName) {
                if (this.availableOptions == null || fieldCollectionName == null) {
                    return false;
                } else {
                    var hasFieldCollection = false;
                    this.availableOptions.forEach(function (item, index) {
                        if (item.fieldCollectionName === fieldCollectionName) {
                            hasFieldCollection = true;
                        }
                    });
                    return hasFieldCollection;
                }
            }

            getJsonPathsByFieldCollectionName = function getJsonPathsByFieldCollectionName(fieldCollectionName) {
                if (this.hasVisualizationOptionFieldCollectionWithName(fieldCollectionName)) {
                    var jsonPathList = null;
                    this.availableOptions.forEach(function (item, index) {
                        if (item.fieldCollectionName === fieldCollectionName) {
                            jsonPathList = item.jsonPathPerVisualizationField;
                        }
                    });
                    return jsonPathList;
                } else {
                    return null;
                }
            }
        }

        function getVisualizationMappingFieldByVisId(visId) {
            var match = null;
            scope.availableVisualizationsMappings.forEach(function (item, index) {
                if (item.visName === visId) {
                    match = item.mappingPerVisualizationField;
                }
            });
            return match;
        }

        // --------------

        scope.visItem = new ActiveVisualization(scope.instanceId, scope.nextChartToAdd);

        /**
         * Called, when the create button in the chart settings modal is triggered.
         * Sets the jsonPath for the chart and let the modal fade out.
         */
        scope.updateJsonPath = function (visToUpdate) {
            // Convert string json object to objects
            var convertMappings = function (mappingToConvert) {
                Object.keys(mappingToConvert).forEach(function (key) {
                    if (typeof mappingToConvert[key] === "string") {
                        mappingToConvert[key] = JSON.parse(mappingToConvert[key]);
                    }
                    return mappingToConvert;
                })
            }
            convertMappings(visToUpdate.visFieldToPathMappingInput);

            visToUpdate.fieldCollectionId = visToUpdate.fieldCollectionIdInput;
            visToUpdate.visFieldToPathMapping = JSON.parse(JSON.stringify(visToUpdate.visFieldToPathMappingInput));
            $('div.modal.fade').modal('hide');
        };

        /**
         * [Public]
         * Retrieves a certain number of value log data (in a specific order) for the current component
         * as a promise.
         *
         * @param numberLogs The number of logs to retrieve
         * @param descending The order in which the value logs should be retrieved. True results in descending
         * order, false in ascending order. By default, the logs are retrieved in ascending
         * order ([oldest log] --> ... --> [most recent log])
         * @param unit The unit in which the values are supposed to be retrieved
         * @param startTime Start time for filtering
         * @param endTime End time for filtering
         * @returns A promise that passes the logs as a parameter
         */
        scope.getData = function retrieveComponentData(numberLogs, descending, unit, startTime, endTime) {
            //Set default order
            let order = 'asc';

            //Check for user option
            if (descending) {
                order = 'desc';
            }

            //Initialize parameters for the server request
            let pageDetails = {
                sort: 'time,' + order,
                size: numberLogs,
                startTime: startTime || "",
                endTime: endTime || ""
            };
            //Perform the server request in order to retrieve the data
            return ComponentService.getValueLogs(scope.componentData.id, scope.componentData.componentTypeName, pageDetails, unit);
        }

        /**
         * Function that checks whether the live chart is allowed to update its data.
         * @returns {boolean} True, if the chart may update; false otherwise
         */
        scope.isLiveChartUpdateable = function isUpdateable() {
            return scope.deploymentState === 'RUNNING';
        }

        /**
         * Function that is called when the chart loads something
         *
         * @param visInstanceId - the visulization id of the module
         * @param isLive - whether the chart is a live chart (=true) or a historical chart (=false)
         */
        scope.loadingStart = function loadingStart(visInstanceId, isLive) {
            var selector = HISTORICAL_CHART_CARD_SELECTOR;
            if (isLive) {
                selector = LIVE_CHART_CARD_SELECTOR;
            }

            //Show the waiting screen
            $(selector.replace(".", "#") + "-" + visInstanceId).waitMe({
                effect: 'bounce',
                text: 'Loading chart...',
                bg: 'rgba(255,255,255,0.85)'
            });
        }

        /**
         * Function that is called when the chart finished loading
         *
         * @param visInstanceId - the visulization id of the module
         * @param isLive - whether the chart is a live chart (=true) or a historical chart (=false)
         */
        scope.loadingFinish = function (visInstanceId, isLive) {
            var selector = HISTORICAL_CHART_CARD_SELECTOR;
            if (isLive) {
                selector = LIVE_CHART_CARD_SELECTOR;
            }

            //Hide the waiting screen for the case it was displayed before
            $(selector.replace(".", "#") + "-" + visInstanceId).waitMe("hide");
        }

        /**
         * [private]
         * Activates a tab based on a given data-target
         * @param tab the id prefix of the data-target
         * @param instanceId [optional] to specify a certain tab instead of activating all with the tab prefix.
         */
        function setTabActive(tab, instanceId) {
            if (instanceId != null) {
                $('.nav-tabs a[data-target="' + tab + "-" + instanceId + ']"').tab('show');
            } else {
                // Select the tab as active which id begins with tab
                $('.nav-tabs a[data-target^="' + tab + '"]').tab('show');
            }
        }

        // Watch the deployment status and change the chart tabs accordingly
        scope.$watch(function () {
            console.log("DEPLOYMENT STATE CHANGED!!!!")
            return scope.deploymentState;
        }, function (newValue, oldValue) {
            if (newValue === 'RUNNING') {
                // Make sure that the tab of the live chart is selected
                setTabActive('#historical-chart-card'); // Make sure that currently the selected tab is really the historical one
                setTabActive('#live-chart-card');
            } else {
                // Make sure that the tab of the historical chart is selected
                setTabActive('#live-chart-card'); // Make sure that currently the selected tab is really the live one
                setTabActive('#historical-chart-card');
            }
        });

    }

    //Configure and expose the directive
    return {
        restrict: 'E', //Elements only
        templateUrl: 'templates/customizable-chart-card-template.html',
        link: link,
        scope: {
            componentData: "=componentData",
            instanceId: "@instanceId",
            //Public api for the historical chart that provides functions for controlling the chart
            historicalChartApi: "=historicalChartApi",
            // The running state of the chart. The live chart is only visible if this is set to "RUNNING"
            deploymentState: "=deploymentState"
        }
    };
}
]);