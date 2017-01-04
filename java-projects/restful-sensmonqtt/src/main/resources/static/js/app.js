var app = angular.module('app', ['ngRoute']);

app.config(['$provide', '$routeProvider', '$locationProvider',
    function ($provide, $routeProvider, $locationProvider) {
        // gets link from html (provided by Thymeleaf - server sided)
        uriRest = $("#uriRestIndex").attr("href");

        $provide.value("uriRestIndex", uriRest);

        $provide.value("uriRestSensors", uriRest + "/sensors");
        $provide.value("uriRestActuators", uriRest + "/actuators");
        $provide.value("uriRestDevices", uriRest + "/devices");
        $provide.value("uriRestTypes", uriRest + "/types");
    }]);