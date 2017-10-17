'use strict';

var app = angular.module('app', ['ngRoute', 'ngResource', 'ngCookies', 'ngSanitize', 'smart-table', 'ui.bootstrap', 'ui.select', 'ngFileUpload', 'thatisuday.dropzone']);

app.config(['$provide', '$routeProvider', '$locationProvider', '$resourceProvider', 'dropzoneOpsProvider',
    function ($provide, $routeProvider, $locationProvider, $resourceProvider, dropzoneOpsProvider) {

        // enable HTML5mode to disable hashbang urls
        $locationProvider.html5Mode(true);

        // Don't strip trailing slashes from calculated URLs
        $resourceProvider.defaults.stripTrailingSlashes = false;

        // gets link from html (provided by Thymeleaf - server sided)
        var ENDPOINT_URI = $('#ENDPOINT_URI').attr('href');
        $provide.value('ENDPOINT_URI', ENDPOINT_URI);

        function redirectExpert($location, SessionService) {
            if (!SessionService.isExpert()) {
                $location.path('/');
            }
        }

        dropzoneOpsProvider.setOptions({
            url: 'a',
            maxFilesize: '100',
            autoProcessQueue: false
        });

        var viewPrefix = '/view';
        // configure the routing rules here
        $routeProvider

                // Home
                .when('/', {
                    templateUrl: 'templates/home',
                    controller: 'HomeController as ctrl',
                    resolve: {
                        countActuators: ['CrudService', function (CrudService) {
                                return CrudService.countItems('actuators').then(
                                        function (count) {
                                            return count;
                                        },
                                        function (response) {
                                            return 0;
                                        }
                                );
                            }],
                        countSensors: ['CrudService', function (CrudService) {
                                return CrudService.countItems('sensors').then(
                                        function (count) {
                                            return count;
                                        },
                                        function (response) {
                                            return 0;
                                        }
                                );
                            }],
                        countDevices: ['CrudService', function (CrudService) {
                                return CrudService.countItems('devices').then(
                                        function (count) {
                                            return count;
                                        },
                                        function (response) {
                                            return 0;
                                        }
                                );
                            }],
                        countTypes: ['CrudService', function (CrudService) {
                                return CrudService.countItems('types').then(
                                        function (count) {
                                            return count;
                                        },
                                        function (response) {
                                            return 0;
                                        }
                                );
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
                        deleteDevice: ['CrudService', function (CrudService) {
                                // bind category parameter
                                return angular.bind(this, CrudService.deleteItem, 'devices');
                                //return CrudService.deleteItem('devices', this);
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
                        actuatorDetails: ['$route', '$location', 'CrudService', function ($route, $location, CrudService) {
                                return CrudService.fetchSpecificItem('actuators', $route.current.params.id).then(
                                        function (data) {
                                            return data;
                                        },
                                        function () {
                                            console.log('404');
                                            $location.url(viewPrefix + '/404');
                                        });
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
                        deleteSensor: ['CrudService', function (CrudService) {
                                // bind category parameter
                                return angular.bind(this, CrudService.deleteItem, 'sensors');
                            }],
                        deviceList: ['CrudService', function (CrudService) {
                                return CrudService.fetchAllItems('devices');
                            }],
                        addDevice: ['CrudService', function (CrudService) {
                                // bind category parameter
                                return angular.bind(this, CrudService.addItem, 'devices');
                            }],
                        deleteDevice: ['CrudService', function (CrudService) {
                                // bind category parameter
                                return angular.bind(this, CrudService.deleteItem, 'devices');
                                //return CrudService.deleteItem('devices', this);
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
                        sensorDetails: ['$route', '$location', 'CrudService', function ($route, $location, CrudService) {
                                return CrudService.fetchSpecificItem('sensors', $route.current.params.id).then(
                                        function (data) {
                                            console.log(data);
                                            return data;
                                        },
                                        function () {
                                            console.log('404');
                                            $location.url(viewPrefix + '/404');
                                        });
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
                            }],
                        deleteDevice: ['CrudService', function (CrudService) {
                                // bind category parameter
                                return angular.bind(this, CrudService.deleteItem, 'devices');
                                //return CrudService.deleteItem('devices', this);
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
                
                // Types List and Register
                .when(viewPrefix + '/404', {
                    templateUrl: 'templates/404'
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