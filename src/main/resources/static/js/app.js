'use strict';

var app = angular.module('app', ['ngRoute', 'ngResource', 'ngCookies', 'ngSanitize', 'smart-table', 'ui.bootstrap', 'ngFileUpload', 'thatisuday.dropzone', 'angular-loading-bar']);

app.config(['$provide', '$routeProvider', '$locationProvider', '$resourceProvider', 'dropzoneOpsProvider',
    function ($provide, $routeProvider, $locationProvider, $resourceProvider, dropzoneOpsProvider) {

        // enable HTML5mode to disable hashbang urls
        $locationProvider.html5Mode(true);

        // Don't strip trailing slashes from calculated URLs
        $resourceProvider.defaults.stripTrailingSlashes = false;

        // gets link from html (provided by Thymeleaf - server sided)
        var ENDPOINT_URI = $('#ENDPOINT_URI').attr('href');
        var BASE_URI = $('#BASE_URI').attr('href');
        $provide.value('ENDPOINT_URI', ENDPOINT_URI);
        $provide.value('BASE_URI', BASE_URI);

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
                    countModels: ['CrudService', function (CrudService) {
                        return CrudService.countItems('env-models').then(
                            (count) => {
                                return count;
                            }, (response) => {
                                return 0;
                            }
                        );
                    }],
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
                    countAdapters: ['CrudService', function (CrudService) {
                        return CrudService.countItems('adapters').then(
                            function (count) {
                                return count;
                            },
                            function (response) {
                                return 0;
                            }
                        );
                    }],
                    countMonitoringAdapters: ['CrudService', function (CrudService) {
                        return CrudService.countItems('monitoring-adapters').then(
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

            // Login
            .when('/login', {
                templateUrl: 'templates/login',
                controller: 'LoginController as vm'
            })

            // Register
            .when('/register', {
                templateUrl: 'templates/register',
                controller: 'RegisterController as vm'
            })

            // Users
            .when(viewPrefix + '/users', {
                templateUrl: 'templates/users',
                controller: 'UsersController as vm'
            })

            // Environment Model
            .when(viewPrefix + '/env-models', {
                category: 'env-models',
                templateUrl: 'templates/env-models',
                controller: 'EnvModelListController as ctrl',
                resolve: {
                    envModelList: ['CrudService', function (CrudService) {
                        return CrudService.fetchAllItems('env-models');
                    }],
                    addEnvModel: ['CrudService', function (CrudService) {
                        return angular.bind(this, CrudService.addItem, 'env-models');
                    }],
                    deleteEnvModel: ['CrudService', function (CrudService) {
                        return angular.bind(this, CrudService.deleteItem, 'env-models');
                    }]
                }
            })

            // Rules list
            .when(viewPrefix + '/rules', {
                category: 'rules',
                templateUrl: 'templates/rules',
                controller: 'RuleListController as ctrl',
                resolve: {
                    ruleList: ['CrudService', function (CrudService) {
                        return CrudService.fetchAllItems('rules');
                    }],
                    addRule: ['CrudService', function (CrudService) {
                        return angular.bind(this, CrudService.addItem, 'rules');
                    }],
                    deleteRule: ['CrudService', function (CrudService) {
                        return angular.bind(this, CrudService.deleteItem, 'rules');
                    }],
                    ruleActionList: ['CrudService', function (CrudService) {
                        return CrudService.fetchAllItems('rule-actions');
                    }],
                    ruleTriggerList: ['CrudService', function (CrudService) {
                        return CrudService.fetchAllItems('rule-triggers');
                    }]
                }
            })

            // Rule actions list
            .when(viewPrefix + '/rule-actions', {
                category: 'rule-actions',
                templateUrl: 'templates/rule-actions',
                controller: 'RuleActionListController as ctrl',
                resolve: {
                    ruleActionList: ['CrudService', function (CrudService) {
                        return CrudService.fetchAllItems('rule-actions');
                    }],
                    ruleActionTypesList: ['RuleService', function (RuleService) {
                        return RuleService.getRuleActionTypes().then(function (response) {
                            return response.data || [];
                        });
                    }],
                    actuatorList: ['CrudService', function (CrudService) {
                        return CrudService.fetchAllItems('actuators');
                    }],
                    sensorList: ['CrudService', function (CrudService) {
                        return CrudService.fetchAllItems('sensors');
                    }],
                    addRuleAction: ['CrudService', function (CrudService) {
                        return angular.bind(this, CrudService.addItem, 'rule-actions');
                    }],
                    deleteRuleAction: ['CrudService', function (CrudService) {
                        return angular.bind(this, CrudService.deleteItem, 'rule-actions');
                    }]
                }
            })

            // Rule triggers list
            .when(viewPrefix + '/rule-triggers', {
                category: 'rule-triggers',
                templateUrl: 'templates/rule-triggers',
                controller: 'RuleTriggerListController as ctrl',
                resolve: {
                    ruleTriggerList: ['CrudService', function (CrudService) {
                        return CrudService.fetchAllItems('rule-triggers');
                    }],
                    addRuleTrigger: ['CrudService', function (CrudService) {
                        return angular.bind(this, CrudService.addItem, 'rule-triggers');
                    }],
                    deleteRuleTrigger: ['CrudService', function (CrudService) {
                        return angular.bind(this, CrudService.deleteItem, 'rule-triggers');
                    }],
                    actuatorList: ['CrudService', function (CrudService) {
                        return CrudService.fetchAllItems('actuators');
                    }],
                    sensorList: ['CrudService', function (CrudService) {
                        return CrudService.fetchAllItems('sensors');
                    }],
                    monitoringComponentList: ['MonitoringService', function (MonitoringService) {
                        return MonitoringService.getMonitoringComponents().then(function (response) {
                            if (response.data) {
                                return response.data;
                            } else {
                                return [];
                            }
                        });
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
                    deleteActuator: ['CrudService', function (CrudService) {
                        // bind category parameter
                        return angular.bind(this, CrudService.deleteItem, 'actuators');
                    }],
                    deviceList: ['CrudService', function (CrudService) {
                        return CrudService.fetchAllItems('devices');
                    }],
                    adapterList: ['CrudService', function (CrudService) {
                        return CrudService.fetchAllItems('adapters');
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
                    adapterList: ['CrudService', function (CrudService) {
                        return CrudService.fetchAllItems('adapters');
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

            //Device Details
            .when(viewPrefix + '/devices/:id', {
                category: 'devices',
                templateUrl: 'templates/device-id.html',
                controller: 'DeviceDetailsController as ctrl',
                resolve: {
                    deviceDetails: ['$route', '$location', 'CrudService', function ($route, $location, CrudService) {
                        return CrudService.fetchSpecificItem('devices', $route.current.params.id).then(
                            function (data) {
                                return data;
                            },
                            function () {
                                $location.url(viewPrefix + '/404');
                            });
                    }],
                    compatibleAdapters: ['$route', 'MonitoringService', function ($route, MonitoringService) {
                        return MonitoringService.getCompatibleMonitoringAdapters($route.current.params.id).then(function (response) {
                            return response.data;
                        }, function () {
                            return null;
                        });
                    }]
                }
            })

            // Adapters list
            .when(viewPrefix + '/adapters', {
                category: 'adapters',
                templateUrl: 'templates/adapters',
                controller: 'AdapterListController as ctrl',
                resolve: {
                    adapterPreprocessing: function () {
                    },
                    parameterTypesList: ['ParameterTypeService', function (ParameterTypeService) {
                        return ParameterTypeService.getAll().then(function (response) {
                            if (response.success) {
                                return response.data;
                            } else {
                                return [];
                            }
                        });
                    }],
                    adapterList: ['CrudService', function (CrudService) {
                        return CrudService.fetchAllItems('adapters');
                    }],
                    addAdapter: ['CrudService', function (CrudService) {
                        // bind category parameter
                        return angular.bind(this, CrudService.addItem, 'adapters');
                    }],
                    deleteAdapter: ['CrudService', function (CrudService) {
                        // bind category parameter
                        return angular.bind(this, CrudService.deleteItem, 'adapters');
                    }]
                }
            })

            //Monitoring adapters
            .when(viewPrefix + '/monitoring-adapters', {
                category: 'monitoring-adapters',
                templateUrl: 'templates/monitoring-adapters',
                controller: 'MonitoringAdapterListController as ctrl',
                resolve: {
                    deviceTypesList: ['ComponentTypeService', function (ComponentTypeService) {
                        return ComponentTypeService.GetByComponent('device').then(function (response) {
                            return response.data;
                        }, function () {
                            return [];
                        });
                    }],
                    parameterTypesList: ['ParameterTypeService', function (ParameterTypeService) {
                        return ParameterTypeService.getAll().then(function (response) {
                            if (response.success) {
                                return response.data;
                            } else {
                                return [];
                            }
                        });
                    }],
                    monitoringAdapterList: ['CrudService', function (CrudService) {
                        return CrudService.fetchAllItems('monitoring-adapters');
                    }],
                    addMonitoringAdapter: ['CrudService', function (CrudService) {
                        return angular.bind(this, CrudService.addItem, 'monitoring-adapters');
                    }],
                    deleteMonitoringAdapter: ['CrudService', function (CrudService) {
                        return angular.bind(this, CrudService.deleteItem, 'monitoring-adapters');
                    }]
                }
            })

            // Settings
            .when(viewPrefix + '/settings', {
                category: 'settings',
                templateUrl: 'templates/settings',
                controller: 'SettingsController as ctrl',
                resolve: {
                    settings: ['SettingsService', function (SettingsService) {
                        //Retrieve settings initially
                        return SettingsService.getSettings().then(function (response) {
                            return response.data;
                        });
                    }],
                    documentationMetaData: ['SettingsService', function (SettingsService) {
                        //Retrieve settings initially
                        return SettingsService.getDocumentationMetaData().then(function (response) {
                            return response.data;
                        });
                    }]
                }
            })

            // Error 404
            .when(viewPrefix + '/404', {
                templateUrl: 'templates/404'
            })

            .otherwise({
                redirectTo: '/'
            });
    }
]);

app.run(['$rootScope', '$timeout', 'SessionService', '$location', '$cookieStore', '$http',
    function ($rootScope, $timeout, SessionService, $location, $cookieStore, $http) {

        // keep user logged in after page refresh
        $rootScope.globals = $cookieStore.get('globals') || {};
        if ($rootScope.globals.currentUser) {
            $http.defaults.headers.common['Authorization'] = 'Basic ' + $rootScope.globals.currentUser.authdata;
        }

        $rootScope.$on('$locationChangeStart', function (event, next, current) {
            // redirect to login page if not logged in and trying to access a restricted page
            var restrictedPage = $.inArray($location.path(), ['/login', '/register']) === -1;
            var loggedIn = $rootScope.globals.currentUser;
            if (restrictedPage && !loggedIn) {
                $location.path('/login');
            }
        });

        $rootScope.$on('$viewContentLoaded', function () {
            $timeout(function () {

                $rootScope.loggedIn = $rootScope.globals.currentUser;

                if ($rootScope.loggedIn) {
                    $rootScope.username = $rootScope.globals.currentUser.username;
                    $rootScope.userData = $rootScope.globals.currentUser.userData;
                }
            });
        });
    }
]);

app.controller('MainCtrl', ['$route', '$rootScope', '$routeParams', '$location',
    function MainCtrl($route, $rootScope, $routeParams, $location) {
        var vm = this;
        vm.$route = $route;
        vm.$location = $location;
        vm.$routeParams = $routeParams;

        $rootScope.getMenuItemClass = function (path) {
            return ($location.path().substr(0, path.length) === path) ? 'toggled' : '';
        };
    }
]);