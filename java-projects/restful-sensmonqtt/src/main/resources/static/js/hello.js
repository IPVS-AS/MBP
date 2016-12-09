angular.module('sensmonqtt', [])
        .config(["$provide", function ($provide) {
                $provide.value("apiRoot", $("#linkApiRoot").attr("href"));
            }])
        .controller('home', function ($scope, $http, apiRoot) {
            $http.get(apiRoot + '/greeting').success(function (data) {
                $scope.greeting = data;
            })
        })
        .controller('locations', function ($scope, $http, apiRoot) {
            // build form
            $scope.locationFormData = {};

            // load locations
            $http.get(apiRoot + '/locations').success(function (data) {
                if (data._embedded != undefined) {
                    $scope.locations = data._embedded.locations;
                } else {
                    $scope.locations = [];
                }
            })

            // proccess form
            $scope.processLocationForm = function () {
                $http({
                    method: 'POST',
                    url: apiRoot + '/locations',
                    data: JSON.stringify($scope.locationFormData), // pass in data as strings
                    headers: {'Content-Type': 'application/json'}  // set the headers so angular passing info as form data (not request payload)
                })
                        .success(function (data) {
                            if (!data["name"]) {
                                alert("error");
                                // if name is empty, not successful!
                                //$scope.errorName = "data.errors.name";
                                //$scope.errorDescription = "data.errors.description";
                            } else {
                                // if successful, update table
                                data["newborn"] = true;
                                $scope.locations.splice(0, 0, data);
                            }
                        });
            };
        });