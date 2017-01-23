'use strict';

var app = angular.module('app', ['ngRoute', 'ngCookies']);

app.config(['$provide', '$routeProvider', '$locationProvider',
    function ($provide, $routeProvider, $locationProvider) {
        // gets link from html (provided by Thymeleaf - server sided)
        var restUri = $('#restUri').attr('href');
        $provide.value('restUri', restUri);

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
                                return ComponentService.getValues($route.current.params.id);
                            }]
                    }
                })

                // Devices List and Register
                .when(viewPrefix + '/devices', {
                    category: 'devices',
                    templateUrl: 'templates/devices',
                    controller: 'DeviceListController as ctrl',
                    resolve: {
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

app.directive('menuItem', ['$location', function (location) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs, controller) {
                var path = attrs.href;
                if (path.startsWith('#')) {
                    path = path.substring(1); // skip '#'
                }
                if (!path.startsWith('/')) {
                    path = '/' + path; // add '/'
                }
                scope.location = location;
                scope.$watch('location.path()', function (newPath) {
                    if (path === newPath) {
                        element.addClass('active');
                    } else {
                        element.removeClass('active');
                    }
                });
            }
        };
    }]);


app.directive('fileModel', ['$parse', function ($parse) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {

                var model = $parse(attrs.fileModel);
                var isMultiple = attrs.multiple;
                var modelSetter = model.assign;

                element.bind('drop', function (e) {
                    e.stopPropagation();
                    e.preventDefault();


//                    var droppedFiles = e.dataTransfer.files;
//                    console.log('dropped', droppedFiles);
                });

                element.bind('change', function () {
                    var values = [];

                    angular.forEach(element[0].files, function (item) {
                        values.push(item);
                    });

                    scope.$apply(function () {
                        if (isMultiple) {
                            modelSetter(scope, values);
                        } else {
                            modelSetter(scope, values[0]);
                        }
                    });
                });
            }
        };
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