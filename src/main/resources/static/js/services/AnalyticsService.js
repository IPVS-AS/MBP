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
        const GET_MODEL_STATISTICS = ANALYTICS_SERVER + '/getstatistics?';
        const GET_PREDICTION = ANALYTICS_SERVER + '/getprediction?';
        const DELETE_MODEL = ANALYTICS_SERVER + '/deletemodel?';

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

        function createBatchModel(name, modeldescription,algorithm, sensorid){
            console.log(CREATE_NEW_MODEL + 'algorithm=' + algorithm + '&sensorid=' + sensorid + '&name=' + name + '&description=' + modeldescription);
            $http.post(CREATE_NEW_MODEL + 'algorithm=' + algorithm + '&sensorid=' + sensorid + '&name=' + name + '&description=' + modeldescription);
        }

        function createStreamModel(name, modeldescription, algorithm, sensorid, time){
            console.log(CREATE_NEW_MODEL + 'algorithm=' + algorithm + '&sensorid=' + sensorid + '&name=' + name + '&time=' + time + '&description=' + modeldescription);
            $http.post(CREATE_NEW_MODEL+'algorithm='+algorithm+'&sensorid='+sensorid+'&name='+name+'&time=' + time + '&description=' + modeldescription);
        }

        function getModelStatistics(name) {
            console.log(GET_MODEL_STATISTICS + 'model_name=' + name);
            return $http.get(GET_MODEL_STATISTICS + 'model_name=' + name);
        }

        function getPrediction(modelname, value) {
            console.log(GET_PREDICTION + 'model_name=' + modelname + '&value=' + value);
            return $http.get(GET_PREDICTION + 'model_name=' + modelname + '&value=' + value);
        }

        function deletemodel(modelname) {
            console.log(DELETE_MODEL + 'model_name=' + modelname);
            return $http.delete(DELETE_MODEL + 'model_name=' + modelname);
        }

        //Expose public methods
        return {
            getExistingModels: getExistingModels,
            getModelTypes: getModelTypes,
            getBatchAlgorithms: getBatchAlgorithms,
            getStreamAlgorithms: getStreamAlgorithms,
            createBatchModel: createBatchModel,
            createStreamModel:createStreamModel,
            getModelStatistics: getModelStatistics,
            getPrediction: getPrediction,
            deletemodel: deletemodel
        }
    }
]);

