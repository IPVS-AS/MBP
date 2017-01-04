function Sensor($q, RetrieverService, uriRestSensors) {
    return {
        getSensors: function () {
            return RetrieverService.get(uriRestSensors)
                    .then(function (data) {
                        return data._embedded.sensors;
                    }, function (response) {
                        return $q.reject(response);
                    });
        }, add: function (data) {
            return RetrieverService.post(uriRestSensors, data)
                    .then(function (data) {
                        return data;
                    }, function (response) {
                        return $q.reject(response);
                    });
        }
    };
}

angular.module('app')
        .factory('Sensor', ['$q', 'RetrieverService', 'uriRestSensors', Sensor]);

