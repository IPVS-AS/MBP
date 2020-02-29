/* global app */

/**
 * Provides services for managing rules.
 */
app.factory('AnalyticsService', ['$http', '$resource', '$q',
    function ($http, $resource, $q) {
        //URLs for server requests
        const ANALYTICS_SERVER = 'http://localhost:5000'
        const GET_ALL_MODELS = ANALYTICS_SERVER + '/getmodels';
        const CREATE_NEW_MODEL = ANALYTICS_SERVER + '/createmodel?';
        const GET_STREAM_ALGORITHMS = ANALYTICS_SERVER + '/getstreamalgorithms';
        const GET_BATCH_ALGORITHMS = ANALYTICS_SERVER + '/getbatchalgorithms';
        const GET_MODEL_STATISTICS = ANALYTICS_SERVER + '/getstatistics?'

        var existingmodels = [{"name": "Electricity", "algorithm": "Regression", "type": "Str", "time": "23542"}, 
            {"name": "Temperature", "algorithm": "Regression", "type": "Str", "time": "23542"}]; 

        var modelType = [{"id": 1, "name": "Stream Mining"}, 
            {"id": 2, "name": "Batch Processing"}];


        function getExistingModels() {
            return $http.get(GET_ALL_MODELS);
        }
        function getModelTypes() {
            return modelType;
        }

        function getBatchAlgorithms() {
            return $http.get(GET_BATCH_ALGORITHMS);
        }

        function getStreamAlgorithms() {
            return $http.get(GET_STREAM_ALGORITHMS);
        }

        function createBatchModel(name, algorithm, sensorid){
            console.log(CREATE_NEW_MODEL + 'algorithm=' + algorithm + '&sensorid=' + sensorid + '&name=' + name);
            $http.post(CREATE_NEW_MODEL + 'algorithm=' + algorithm + '&sensorid=' + sensorid + '&name=' + name);
        }

        function createStreamModel(name, algorithm, sensorid, time){
            console.log(CREATE_NEW_MODEL + 'algorithm=' + algorithm + '&sensorid=' + sensorid + '&name=' + name + '&time=' + time);
            $http.post(CREATE_NEW_MODEL+'algorithm='+algorithm+'&sensorid='+sensorid+'&name='+name+'&time=' + time);
        }

        function getModelStatistics(name) {
            console.log(GET_MODEL_STATISTICS + 'model_name=' + name);
            return $http.get(GET_MODEL_STATISTICS + 'model_name=' + name);
        }

        //Expose public methods
        return {
            getExistingModels: getExistingModels,
            getModelTypes: getModelTypes,
            getBatchAlgorithms: getBatchAlgorithms,
            getStreamAlgorithms: getStreamAlgorithms,
            createBatchModel: createBatchModel,
            createStreamModel:createStreamModel,
            getModelStatistics: getModelStatistics

        }
    }
]);

