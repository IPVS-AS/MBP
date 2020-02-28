/* global app */

/**
 * Provides services for managing rules.
 */
app.factory('AnalyticsService', ['$http', '$resource', '$q', 'ENDPOINT_URI',
    function ($http, $resource, $q, ENDPOINT_URI) {
        //URLs for server requests
        const URL_GEt_RULE_ACTION_TYPES = ENDPOINT_URI + '/rule-actions/types';
        const URL_TEST_RULE_ACTION = ENDPOINT_URI + '/rule-actions/test/';
        const URL_ENABLE_RULE = ENDPOINT_URI + '/rules/enable/';
        const URL_DISABLE_RULE = ENDPOINT_URI + '/rules/disable/';


        var existingmodels = [{"id": 1, "name": "Electricity", "algorithm": "Regression", "type": "Str", "time": "23542"}, 
            {"id": 2, "name": "Temperature", "algorithm": "Regression", "type": "Str", "time": "23542"}]; 

        var modelType = [{"id": 1, "name": "Stream Mining"}, 
            {"id": 2, "name": "Batch Processing"}];

        var batchAlgo = ["Regression", "Classification"];

        var streamAlgo = ["KNN", "Stream K means"];    

        function getExistingModels() {
            return existingmodels;
        }
        function getModelTypes() {
            return modelType;
        }
        function getBatchAlgorithms() {
            return batchAlgo;
        }
        function getStreamAlgorithms() {
            return streamAlgo;
        }
        function createBatchModel(name, algorithm, sensorid){
            console.log('got batch request');
        }
        function createStreamModel(name, algorithm, sensorid, time){
            console.log('got stream request');
        }



        //Expose public methods
        return {
            getExistingModels: getExistingModels,
            getModelTypes: getModelTypes,
            getBatchAlgorithms: getBatchAlgorithms,
            getStreamAlgorithms: getStreamAlgorithms,
            createBatchModel: createBatchModel,
            createStreamModel:createStreamModel

        }
    }
]);

