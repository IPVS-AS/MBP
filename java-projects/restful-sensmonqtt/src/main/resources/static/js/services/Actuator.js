function Actuator($q, RetrieverService, uriRestActuators) {
    return {
        getActuators: function () {
            return RetrieverService.get(uriRestActuators)
                    .then(function (data) {
                        return data._embedded.actuators;
                    }, function (response) {
                        return $q.reject(response);
                    });
        }, add: function (data) {
            return RetrieverService.post(uriRestActuators, data)
                    .then(function (data) {
                        return data;
                    }, function (response) {
                        return $q.reject(response);
                    });
        }
    };
}

angular.module('app')
        .factory('Actuator', ['$q', 'RetrieverService', 'uriRestActuators', Actuator]);

