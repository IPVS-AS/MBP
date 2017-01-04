function RetrieverService($http, $q) {
    return {
        get: function (url) {
            return $http.get(url)
                    .then(function (response) {
                        if (typeof response.data === 'object') {
                            return response.data;
                        } else {
                            // invalid response
                            return $q.reject(response);
                        }
                    }, function (response) {
                        // something went wrong
                        return $q.reject(response);
                    });
        }, post: function (url, data) {
            return $http({
                method: 'POST',
                url: url,
                data: JSON.stringify(data), // pass in data as strings
                headers: {'Content-Type': 'application/json'}  // set the headers so angular passing info as form data (not request payload)
            }).then(function (response) {
                if (typeof response.data === 'object') {
                    return response.data;
                } else {
                    // invalid response
                    return $q.reject(response);
                }
            }, function (response) {
                // something went wrong
                
                // if has data for errors, parse/map it
                if (response.data.errors) {
                    var parsed = { parsed: true, response: response };
                    var errors = response.data.errors;
                    for (var i in errors) {
                        if (errors[i].property) {
                            if(!parsed[errors[i].property]) {
                                parsed[errors[i].property] = errors[i];
                            }
                        }
                    }
                    return $q.reject(parsed);
                }
                
                return $q.reject(response);
            });
        }
    };
}

angular.module('app')
        .factory('RetrieverService', ['$http', '$q', RetrieverService]);
