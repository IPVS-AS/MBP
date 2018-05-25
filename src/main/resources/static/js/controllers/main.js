var app = angular.module('app', []);

app.config(["$provide",
    function ($provide) {
        // gets link from html (provided by Thymeleaf - server sided)
        uriRest = $("#uriRestIndex").attr("href");

        $provide.value("uriRestIndex", uriRest);

        $provide.value("uriRestSensors", uriRest + "/sensors");
        $provide.value("uriRestActuators", uriRest + "/actuators");
        $provide.value("uriRestDevices", uriRest + "/devices");
        $provide.value("uriRestTypes", uriRest + "/types");
    }]);

function formatMacAddress(address) {
    if (address) {
        return address.match(/.{1,2}/g).join('-').toUpperCase();
    }
    return address;
}

function normalizeMacAddress(address) {
    if (address) {
        norm = address.replace(new RegExp('-', 'g'), '');
        return norm.toLowerCase();
    }
    return address;
}

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
        }, delete: function (url, id) {
          var deletionURL = url + '/' + id;
          return $http({
           method: 'DELETE',
           url: deletionURL
          }).then(function (response) {
            if (response.data.errors) {
             alert("An error occurred during deletion...");
            }
          }); 
        }
    };
}

function Device($q, RetrieverService, uriRestDevices) {
    return {
        formatMacAddress: formatMacAddress,
        normalizeMacAddress: normalizeMacAddress,
        getDevices: function () {
            return RetrieverService.get(uriRestDevices)
                    .then(function (data) {
                        devices = data._embedded.devices;
                        for (var i in devices) {
                            devices[i].formattedMacAddress =
                                    formatMacAddress(devices[i].macAddress);
                        }
                        //data._embedded.devices = devices;
                        return devices;
                    }, function (response) {
                        return $q.reject(response);
                    });
        }, add: function (data) {
            return RetrieverService.post(uriRestDevices, data)
                    .then(function (data) {
                        return data;
                    }, function (response) {
                        return $q.reject(response);
                    });
        }, deleteDevice: function (uriRestDevices, id) {
          return RetrieverService.delete(uriRestDevices, id)
                    .then(function (data) {
                        return data;
                    }, function (response) {
                        return $q.reject(response);
                    });
        } 
    };
}

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

app
        .factory('RetrieverService', ['$http', '$q', RetrieverService])
        .factory('Device', ['$q', 'RetrieverService', 'uriRestDevices', Device])
        .factory('Sensor', ['$q', 'RetrieverService', 'uriRestSensors', Sensor])
        .factory('Actuator', ['$q', 'RetrieverService', 'uriRestActuators', Actuator]);