'use strict';

var app = angular.module('app', ['ngRoute', 'ngCookies', 'ngSanitize', 'ui.bootstrap', 'ui.select']);

app.config(['$provide', '$routeProvider', '$locationProvider',
    function ($provide, $routeProvider, $locationProvider) {
        // gets link from html (provided by Thymeleaf - server sided)
        var restUri = $('#restUri').attr('href');
        $provide.value('restUri', restUri);

        function redirectExpert($location, SessionService) {
            if (!SessionService.isExpert()) {
                $location.path('/');
            }
        }

        var viewPrefix = '/view';
        // configure the routing rules here
        $routeProvider

                // Home
                .when('/', {
                    templateUrl: 'templates/home',
                    controller: 'HomeController as ctrl',
                    resolve: {
                        countActuators: ['CrudService', function (CrudService) {
                                return CrudService.countItems('actuators');
                            }],
                        countSensors: ['CrudService', function (CrudService) {
                                return CrudService.countItems('sensors');
                            }],
                        countDevices: ['CrudService', function (CrudService) {
                                return CrudService.countItems('devices');
                            }],
                        countTypes: ['CrudService', function (CrudService) {
                                return CrudService.countItems('types');
                            }],
                        actuatorValues: ['ComponentService', function (ComponentService) {
                                return ComponentService.getValues(ComponentService.COMPONENT.ACTUATOR, undefined);
                            }],
                        sensorValues: ['ComponentService', function (ComponentService) {
                                return ComponentService.getValues(ComponentService.COMPONENT.SENSOR, undefined);
                            }]
                    }
                })

                // Home - with prefix
                .when(viewPrefix, {
                    templateUrl: 'templates/home',
                    controller: 'HomeController as ctrl',
                    resolve: {
                        countActuators: ['CrudService', function (CrudService) {
                                return CrudService.countItems('actuators');
                            }],
                        countSensors: ['CrudService', function (CrudService) {
                                return CrudService.countItems('sensors');
                            }],
                        countDevices: ['CrudService', function (CrudService) {
                                return CrudService.countItems('devices');
                            }],
                        countTypes: ['CrudService', function (CrudService) {
                                return CrudService.countItems('types');
                            }]
                    }
                })
                
                // Actuator List and Register (includes Device List and Register)
                .when(viewPrefix + '/actuators', {
                    category: 'actuators',
                    templateUrl: 'templates/actuators',
                    controller: 'ActuatorListController as ctrl',
                    resolve: {
                        actuatorList: ['CrudService', function (CrudService) {
                                return CrudService.fetchAllItems('actuators');
                            }],
                        addActuator: ['CrudService', function (CrudService) {
                                // bind category parameter
                                return angular.bind(this, CrudService.addItem, 'actuators');
                            }],
                        deviceList: ['CrudService', function (CrudService) {
                                return CrudService.fetchAllItems('devices');
                            }],
                        addDevice: ['CrudService', function (CrudService) {
                                // bind category parameter
                                return angular.bind(this, CrudService.addItem, 'devices');
                            }],
                        typeList: ['CrudService', function (CrudService) {
                                return CrudService.fetchAllItems('types');
                            }]
                    }
                })

                // Actuator Details
                .when(viewPrefix + '/actuators/:id', {
                    category: 'actuators',
                    templateUrl: 'templates/actuator-id.html',
                    controller: 'ActuatorDetailsController as ctrl',
                    resolve: {
                        actuatorDetails: ['$route', 'CrudService', function ($route, CrudService) {
                                return CrudService.fetchSpecificItem('actuators', $route.current.params.id);
                            }],
                        values: ['$route', 'ComponentService', function ($route, ComponentService) {
                                console.log($route.current.params.id);
                                return ComponentService.getValues(undefined, $route.current.params.id);
                            }]
                    }
                })

                // Sensors List and Register (includes Device List and Register)
                .when(viewPrefix + '/sensors', {
                    category: 'sensors',
                    templateUrl: 'templates/sensors',
                    controller: 'SensorListController as ctrl',
                    resolve: {
                        sensorList: ['CrudService', function (CrudService) {
                                return CrudService.fetchAllItems('sensors');
                            }],
                        addSensor: ['CrudService', function (CrudService) {
                                // bind category parameter
                                return angular.bind(this, CrudService.addItem, 'sensors');
                            }],
                        deviceList: ['CrudService', function (CrudService) {
                                return CrudService.fetchAllItems('devices');
                            }],
                        addDevice: ['CrudService', function (CrudService) {
                                // bind category parameter
                                return angular.bind(this, CrudService.addItem, 'devices');
                            }],
                        typeList: ['CrudService', function (CrudService) {
                                return CrudService.fetchAllItems('types');
                            }]
                    }
                })

                // Sensor Details
                .when(viewPrefix + '/sensors/:id', {
                    category: 'sensors',
                    templateUrl: 'templates/sensor-id.html',
                    controller: 'SensorDetailsController as ctrl',
                    resolve: {
                        sensorDetails: ['$route', 'CrudService', function ($route, CrudService) {
                                return CrudService.fetchSpecificItem('sensors', $route.current.params.id);
                            }],
                        values: ['$route', 'ComponentService', function ($route, ComponentService) {
                                console.log($route.current.params.id);
                                return ComponentService.getValues(undefined, $route.current.params.id);
                            }]
                    }
                })

                // Devices List and Register
                .when(viewPrefix + '/devices', {
                    category: 'devices',
                    templateUrl: 'templates/devices',
                    controller: 'DeviceListController as ctrl',
                    resolve: {
                        isExpert: ['$location', 'SessionService', redirectExpert],
                        deviceList: ['CrudService', function (CrudService) {
                                return CrudService.fetchAllItems('devices');
                            }],
                        addDevice: ['CrudService', function (CrudService) {
                                // bind category parameter
                                return angular.bind(this, CrudService.addItem, 'devices');
                            }]
                    }
                })

                // Types List and Register
                .when(viewPrefix + '/types', {
                    category: 'types',
                    templateUrl: 'templates/types',
                    controller: 'TypeListController as ctrl',
                    resolve: {
                        isExpert: ['$location', 'SessionService', redirectExpert],
                        typeList: ['CrudService', function (CrudService) {
                                return CrudService.fetchAllItems('types');
                            }],
                        addType: ['CrudService', function (CrudService) {
                                // bind category parameter
                                return angular.bind(this, CrudService.addItem, 'types');
                            }]
                    }
                })

                // Go expert
                .when(viewPrefix + '/expert', {
                    redirectTo: function () {
                        return '/';
                    },
                    resolve: {
                        goExpert: ['SessionService', function (SessionService) {
                                SessionService.goExpert();
                            }]
                    }
                })

                // Back to normal
                .when(viewPrefix + '/no-expert', {
                    redirectTo: function () {
                        return '/';
                    },
                    resolve: {
                        leaveExpert: ['SessionService', function (SessionService) {
                                SessionService.leaveExpert();
                            }]
                    }
                })

                .otherwise({redirectTo: '/'});

        // enable HTML5mode to disable hashbang urls
        $locationProvider.html5Mode(true);
    }]);

app.run(['$rootScope', '$timeout', 'SessionService',
    function ($rootScope, $timeout, SessionService) {
        $rootScope.$on('$viewContentLoaded', function () {
            $timeout(function () {
                // set expert
                $rootScope.expert = SessionService.isExpert();

                // copied from admin.js
                var loadAdminBSB = function () {
                    $.AdminBSB.browser.activate();
                    $.AdminBSB.leftSideBar.activate();
                    //$.AdminBSB.navbar.activate();
                    $.AdminBSB.dropdownMenu.activate();
                    $.AdminBSB.input.activate();
                    $.AdminBSB.select.activate();
                    $.AdminBSB.search.activate();

                    setTimeout(function () {
                        $('.page-loader-wrapper').fadeOut();
                    }, 50);
                };
                loadAdminBSB();
            });
        });
    }]);

app.controller('MainCtrl', ['$route', '$rootScope', '$routeParams', '$location',
    function MainCtrl($route, $rootScope, $routeParams, $location) {
        var vm = this;
        vm.$route = $route;
        vm.$location = $location;
        vm.$routeParams = $routeParams;

        $rootScope.getMenuItemClass = function (path) {
            return ($location.path().substr(0, path.length) === path) ? 'toggled' : '';
        };
    }]);