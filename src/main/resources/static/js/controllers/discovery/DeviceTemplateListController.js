/* global app */

/*
 * Controller for the settings page.
 */
app.controller('DeviceTemplateListController',
    ['$scope', '$controller', '$interval', '$timeout',
        'requestTopicList',
        'deviceTemplateList', 'addDeviceTemplate', 'updateDeviceTemplate', 'deleteDeviceTemplate',
        'locationTemplateList', 'addLocationTemplate', 'updateLocationTemplate', 'deleteLocationTemplate',
        'DiscoveryService', 'NotificationService',
        function ($scope, $controller, $interval, $timeout,
                  requestTopicList,
                  deviceTemplateList, addDeviceTemplate, updateDeviceTemplate, deleteDeviceTemplate,
                  locationTemplateList, addLocationTemplate, updateLocationTemplate, deleteLocationTemplate,
                  DiscoveryService, NotificationService) {
            //Constants
            const MAP_INIT_CENTER = [9.106631254042352, 48.74518217652443];
            const MAP_INIT_ZOOM = 16;

            //Find relevant DOM elements
            const ELEMENT_MENU_BUTTONS = $("div.bubble-item");
            const ELEMENT_MENU_BUTTON_MAIN = $("div.bubble-item.bubble-devices")
            const ELEMENT_EDITORS_COLLAPSES = $('#edit-templates-group > div.collapse');
            const ELEMENT_EDITORS_DEVICE = $('#edit-devices');
            const ELEMENT_EDITORS_DEVICE_EDITOR = $('#edit-devices-editor');
            const ELEMENT_EDITORS_DEVICE_TEST_RESULTS = $('#edit-devices-test-results');
            const ELEMENT_EDITORS_LOCATION = $('#edit-locations');
            const ELEMENT_EDITORS_LOCATION_EDITOR = $('#edit-locations-editor');

            //Relevant CSS classes
            const CLASS_MENU_BUTTON_CONNECTOR = 'bubble-connector';

            let vm = this;

            //Remember last location template type
            let lastLocationTemplateType = "";

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Initialize the templates menu and editor windows
                initTemplatesMenu();
                initEditorWindows();

                //Initialize UI elements
                $(document).ready(() => {
                    $('[data-toggle="popover"]').popover();
                });
            })();

            /**
             * [Public]
             * Performs a server request in order to test the results that the currently edited device template
             * produces in a discovery query for a given list of request topics.
             * @param requestTopics Array of request topic IDs on which the device template is supposed to be tested
             */
            function testDeviceTemplate(requestTopics) {
                //Retrieve and copy current device template from controller
                let deviceTemplate = Object.assign({}, vm.addDeviceTemplateCtrl.item);

                //Remove irrelevant fields
                let fieldsWhitelist = ['id', 'name', 'requirements', 'scoringCriteria'];
                for (let key in deviceTemplate) {
                    //Check for own property
                    if (!deviceTemplate.hasOwnProperty(key)) {
                        continue;
                    }

                    //Remove current field if not whitelisted
                    if (!fieldsWhitelist.includes(key)) {
                        delete deviceTemplate[key];
                    }
                }

                //Show loading spinner
                vm.deviceTemplateTestLoading = true;

                //Perform server request
                DiscoveryService.testDeviceTemplate(deviceTemplate, requestTopics).then(function (result) {
                    //Expose test results
                    vm.deviceTemplateTestResults = result || [];

                    //Remove all map locations
                    vm.deviceLocationMapApi.clearLocations();

                    //Add locations of devices to the map
                    for (let i = 0; i < vm.deviceTemplateTestResults.length; i++) {
                        //Get current device description
                        let deviceDescription = vm.deviceTemplateTestResults[i];

                        //Check if location is available
                        if ((!deviceDescription.hasOwnProperty("location")) || (!deviceDescription.location) ||
                            (!deviceDescription.location.hasOwnProperty("coordinates")) ||
                            (!deviceDescription.location.coordinates) ||
                            (!deviceDescription.location.coordinates.hasOwnProperty("lon")) ||
                            (!deviceDescription.location.coordinates.hasOwnProperty("lat"))) {
                            continue;
                        }

                        //Add device location to map
                        vm.deviceLocationMapApi.addLocation(deviceDescription.location.coordinates.lon,
                            deviceDescription.location.coordinates.lat, deviceDescription.name || "");
                    }

                    //First hide and then show the editor
                    ELEMENT_EDITORS_DEVICE_TEST_RESULTS.slideUp().slideDown(400, function () {
                        //Update location marker map
                        vm.deviceLocationMapApi.updateMapSize();
                        vm.deviceLocationMapApi.viewAllLocations();

                        //Clear displayed validation errors
                        $timeout(() => {
                            vm.addDeviceTemplateCtrl.item.errors = {};
                        }, 10);

                        //After animation completed, scroll to the top of the visible card
                        $('html, body').animate({
                            scrollTop: ELEMENT_EDITORS_DEVICE_TEST_RESULTS.offset().top
                        }, 500);
                    });
                }, function (response) {
                    //An error occurred, check whether validation error messages are available
                    if ((response.status !== 400) || (!response.hasOwnProperty("responseJSON")) || (!response.responseJSON) ||
                        (!response.responseJSON.hasOwnProperty("detailMessages")) || (!response.responseJSON.detailMessages)) {
                        return;
                    }

                    //Expose validation errors
                    vm.addDeviceTemplateCtrl.item.errors = response.responseJSON.detailMessages;
                }).always(() => {
                    //Hide the loading spinner
                    vm.deviceTemplateTestLoading = false;
                });
            }


            /**
             * [Public]
             * Shows the device template editor with an animation.
             */
            function showDeviceTemplatesEditor() {
                //First hide and then show the editor
                ELEMENT_EDITORS_DEVICE_EDITOR.slideUp().slideDown(400, () => {
                    //Refresh all select pickers after animation completed
                    $('.selectpicker').selectpicker('refresh').selectpicker("selectAll");
                });
            }

            /**
             * [Public]
             * Prepares the editing of a certain device template, given by its ID.
             *
             * @param templateId The ID of the device template
             */
            function editDeviceTemplate(templateId) {
                //Find the device template with this ID
                let deviceTemplate = null;
                for (let i = 0; i < deviceTemplateList.length; i++) {
                    if (templateId === deviceTemplateList[i].id) {
                        //Found, remember device template
                        deviceTemplate = deviceTemplateList[i];
                        break;
                    }
                }

                //Sanity check
                if (deviceTemplate == null) return;

                //Set copy of location template
                vm.addDeviceTemplateCtrl.item = Object.assign({}, deviceTemplate)

                //Show template editor
                showDeviceTemplatesEditor();
            }

            /**
             * [Public]
             * Saves the currently modified device template, either by creating a new one or by updating an
             * existing one.
             */
            function saveDeviceTemplate() {
                //Check if device template ID is set
                if (vm.addDeviceTemplateCtrl.item.hasOwnProperty("id") && (vm.addDeviceTemplateCtrl.item.id.length > 0)) {
                    //Copy location template data to update controller
                    vm.updateDeviceTemplateCtrl.item = vm.addDeviceTemplateCtrl.item;

                    //Remove location template field from location requirements (only ID needed)
                    for (let i = 0; i < vm.updateDeviceTemplateCtrl.item.requirements.length; i++) {
                        delete vm.updateDeviceTemplateCtrl.item.requirements[i].locationTemplate;
                    }

                    //Update existing location template
                    vm.updateDeviceTemplateCtrl.updateItem().then((data) => {
                        $scope.$apply();
                    });
                } else {
                    //Create new device template
                    vm.addDeviceTemplateCtrl.addItem();
                }
            }

            /**
             * [Public]
             * Shows the location template editor with an animation.
             */
            function showLocationTemplatesEditor() {
                //First hide and then show the editor
                ELEMENT_EDITORS_LOCATION_EDITOR.slideUp().slideDown(400, function () {
                    //Remove geometries from map and update map interaction
                    onLocationTypeChange();

                    //Adjust size of location map to changed UI
                    vm.locationMapApi.updateMapSize();
                });
            }

            /**
             * [Public]
             * Handles change events with respect to the location type, triggered by the user via the select element.
             */
            function onLocationTypeChange() {
                //Remove the geometries that are currently visible on the map
                vm.locationMapApi.removeGeometries();

                //Check for selected location type
                if (!(["Point", "Circle", "Polygon"].includes(vm.addLocationTemplateCtrl.item.type))) {
                    //Disable drawing
                    vm.locationMapApi.disableDrawing();
                    return;
                }

                //Enable draw interaction for the selected type
                vm.locationMapApi.enableDrawing(vm.addLocationTemplateCtrl.item.type);
            }

            /**
             * [Public]
             * Handles change events with respect to the location parameters.
             */
            function onLocationChange() {
                //Geometry to add
                let geometry = null;

                //Check for location type
                if (vm.addLocationTemplateCtrl.item.type === "Point") {
                    //Determine coordinates of the point
                    let coordinates = ol.proj.fromLonLat([vm.addLocationTemplateCtrl.item.longitude || 0,
                        vm.addLocationTemplateCtrl.item.latitude || 0])

                    //Create point geometry
                    geometry = new ol.geom.Point(coordinates, "XY");
                } else if (vm.addLocationTemplateCtrl.item.type === "Circle") {
                    //Determine center coordinates and radius
                    let center = ol.proj.fromLonLat([vm.addLocationTemplateCtrl.item.longitude || 0, vm.addLocationTemplateCtrl.item.latitude || 0]);
                    let radius = vm.locationMapApi.distanceFromMeters(center, vm.addLocationTemplateCtrl.item.radius || 0);

                    //Create circle geometry
                    geometry = new ol.geom.Circle(center, radius, "XY");
                } else if (vm.addLocationTemplateCtrl.item.type === "Polygon") {
                    //Determine points of the polygon
                    let points = vm.addLocationTemplateCtrl.item.pointsList.split("\n").map(x => x.split("|").map(s => parseFloat(s)));

                    //Sanity check
                    if (points.length < 3) {
                        return;
                    }

                    //Create polygon from the points and transform the coordinates
                    geometry = new ol.geom.Polygon([points], "XY").transform('EPSG:4326', 'EPSG:3857');
                } else {
                    return;
                }

                //Prepare map and UI
                onLocationMapDrawingFinished(geometry);

                //Add new geometry to map
                vm.locationMapApi.addGeometry(geometry);

                //Move view of the map in order to show the geometry
                vm.locationMapApi.viewGeometry(geometry);
            }

            /**
             * [Public]
             * Handles the events when the user finished the drawing of a geometry.
             *
             * @param geometry The created geometry
             */
            function onLocationMapDrawingFinished(geometry) {
                //Get geometry type
                let geometryType = geometry.getType();

                //Check whether selected type and geometry match
                if (geometryType !== vm.addLocationTemplateCtrl.item.type) {
                    return;
                }

                //Remove the geometries that are currently visible on the map
                vm.locationMapApi.removeGeometries();

                //Update location model according to the geometry
                if (geometryType === "Point") {
                    //Get coordinates
                    let coordinates = ol.proj.toLonLat(geometry.getCoordinates());

                    //Update models
                    $timeout(() => {
                        vm.addLocationTemplateCtrl.item.longitude = Math.round((coordinates[0] + Number.EPSILON) * 1e6) / 1e6;
                        vm.addLocationTemplateCtrl.item.latitude = Math.round((coordinates[1] + Number.EPSILON) * 1e6) / 1e6;
                    }, 10);
                } else if (geometryType === "Circle") {
                    //Get center coordinates and radius
                    let center = geometry.getCenter();
                    let coordinates = ol.proj.toLonLat(center);
                    let radius = geometry.getRadius();

                    //Update models
                    $timeout(() => {
                        vm.addLocationTemplateCtrl.item.longitude = Math.round((coordinates[0] + Number.EPSILON) * 1e6) / 1e6;
                        vm.addLocationTemplateCtrl.item.latitude = Math.round((coordinates[1] + Number.EPSILON) * 1e6) / 1e6;
                        vm.addLocationTemplateCtrl.item.radius = Math.round(vm.locationMapApi.distanceToMeters(radius, center) * 10) / 10;
                    }, 10);
                } else if (geometryType === "Polygon") {
                    //Extract and transform the outline points of the polygon
                    let points = geometry.clone().transform('EPSG:3857', 'EPSG:4326').getCoordinates()[0];

                    //Create string from all points
                    let pointsString = points.map(p => p.map(s => Math.round(s * 1e6) / 1e6).join("|")).join("\n");

                    // Update model
                    $timeout(() => {
                        vm.addLocationTemplateCtrl.item.pointsList = pointsString;
                    }, 10);
                }
            }

            /**
             * [Public]
             * Prepares the editing of a certain location template, given by its ID.
             *
             * @param templateId The ID of the location template
             */
            function editLocationTemplate(templateId) {
                //Find the location template with this ID
                let locationTemplate = null;
                for (let i = 0; i < locationTemplateList.length; i++) {
                    if (templateId === locationTemplateList[i].id) {
                        //Found, remember location template
                        locationTemplate = locationTemplateList[i];
                        break;
                    }
                }

                //Sanity check
                if (locationTemplate == null) return;

                //Set copy of location template
                vm.addLocationTemplateCtrl.item = Object.assign({}, locationTemplate)

                //Create string from coordinate array if polygon
                if (locationTemplate.type === "Polygon") {
                    vm.addLocationTemplateCtrl.item.pointsList = locationTemplate.points.map(p => p.map(s => Math.round(s * 1e6) / 1e6).join("|")).join("\n");
                }

                //Show template editor
                showLocationTemplatesEditor();
            }

            /**
             * [Public]
             * Saves the currently modified location template, either by creating a new one or by updating an
             * existing one.
             */
            function saveLocationTemplate() {
                //Remember last location template type
                lastLocationTemplateType = vm.addLocationTemplateCtrl.item.type;

                //Check if location template ID is set
                if (vm.addLocationTemplateCtrl.item.hasOwnProperty("id") && (vm.addLocationTemplateCtrl.item.id.length > 0)) {
                    //Copy location template data to update controller
                    vm.updateLocationTemplateCtrl.item = vm.addLocationTemplateCtrl.item;

                    //Update existing location template
                    vm.updateLocationTemplateCtrl.updateItem().then((data) => {
                        $scope.$apply();
                    });
                } else {
                    //Create new location template
                    vm.addLocationTemplateCtrl.addItem();
                }
            }

            /**
             * [Private]
             * Takes a location template object and transforms the string of polygon points, if existing, to an array
             * of coordinates in case the location template is of type polygon area.
             *
             * @param locationTemplate The location template to transform
             * @return {*} The transformed location template
             */
            function transformPolygonPoints(locationTemplate) {
                //Check if data contains polygon points string
                if (!locationTemplate.hasOwnProperty("pointsList")) {
                    //Nothing to do
                    return locationTemplate;
                }
                //Transform string to array of coordinates
                locationTemplate.points = locationTemplate.pointsList.split("\n").map(x => x.split("|").map(s => parseFloat(s)));

                //Remove string from data object
                delete locationTemplate.pointsList;

                return locationTemplate;
            }

            /**
             * [Private]
             * Initializes the templates main menu.
             */
            function initTemplatesMenu() {
                /**
                 * Calculates the center coordinates of a given jQuery element and returns them as object.
                 * @param element The element to calculate the center coordinates for
                 * @return The center coordinates of the element as object
                 */
                function getElementCenter(element) {
                    //Get offset
                    let offset = element.position();

                    //Calculate and return center
                    return {
                        'x': offset.left + element.width() / 2,
                        'y': offset.top + element.height() / 2
                    };
                }

                /**
                 * Adjusts the menu to the current positions and sizes of its menu items.
                 */
                function adjustMenu() {
                    //Get center of main button
                    let mainCenter = getElementCenter(ELEMENT_MENU_BUTTON_MAIN);

                    //Iterate over all menu items
                    ELEMENT_MENU_BUTTONS.each(function (index) {
                        //Wrap element
                        let elem = $(this);

                        //Skip main button
                        if (ELEMENT_MENU_BUTTON_MAIN.is(elem)) return;

                        //Calculate center of element
                        let elemCenter = getElementCenter(elem);

                        //Find associated connector and adjust its position to the menu elements
                        elem.children('.' + CLASS_MENU_BUTTON_CONNECTOR).css({
                            'width': Math.hypot(mainCenter.y - elemCenter.y, mainCenter.x - elemCenter.x) + 'px',
                            'transformOrigin': 'left 50%',
                            'transform': 'rotate(' + Math.atan2(mainCenter.y - elemCenter.y, mainCenter.x - elemCenter.x) * 180 / Math.PI + 'deg)'
                        });
                    });
                }

                //Register event listeners for adjusting the menu on position changes
                $(document).ready(adjustMenu);
                $(window).on('resize', adjustMenu);

                //Adjust menu with some delay
                $interval(adjustMenu, 500, 3);
            }

            /**
             * [Private]
             * Initializes the several editor windows.
             */
            function initEditorWindows() {
                //Listen to show events of collapses
                ELEMENT_EDITORS_COLLAPSES.on('show.bs.collapse', function (e) {
                    //Hide the other collapses
                    ELEMENT_EDITORS_COLLAPSES.not(e.target).collapse('hide')
                }).on('shown.bs.collapse', function (e) {
                    //Adjust size of location map to changed UI
                    vm.locationMapApi.updateMapSize();

                    //Scroll to top of the visible collapse
                    $('html, body').animate({
                        scrollTop: $(e.target).offset().top + 200
                    }, 1000);
                });
            }

            /**
             * [Private]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain entity.
             *
             * @param categoryName Name of the category to which the entity belongs
             * @param entityList Reference to the list of entities to which the pertained entity belongs
             * @param data A data object that contains the id of the entity that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(categoryName, entityList, data) {
                let entityId = data.id;
                let entityName = "";

                //Determines the entity's name by checking the given list
                for (let i = 0; i < entityList.length; i++) {
                    if (entityId === entityList[i].id) {
                        entityName = entityList[i].name;
                        break;
                    }
                }

                //Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete ' + categoryName,
                    type: 'warning',
                    html: "Are you sure you want to delete the " + categoryName + " \"<strong>" + entityName + "</strong>\"?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }


            //Watch addition of requirements or scoring criteria and enable popovers
            $scope.$watchGroup([() => (vm.addDeviceTemplateCtrl.item.requirements||[]).length,
                () => (vm.addDeviceTemplateCtrl.item.scoringCriteria||[]).length], () => {
                    $('[data-toggle="popover"]').popover({container: 'body'});
                }
            );

            //Watch controller result of device template additions
            $scope.$watch(() => vm.addDeviceTemplateCtrl.result, (data) => {
                    //Sanity check
                    if (!data) return;

                    //Callback, close device editor
                    ELEMENT_EDITORS_DEVICE_EDITOR.slideUp();

                    //Add device template to list
                    vm.deviceTemplateListCtrl.pushItem(data);
                }
            );

            //Watch controller result of device template updates
            $scope.$watch(() => vm.updateDeviceTemplateCtrl.result, (data) => {
                //Sanity check
                if (!data) return;

                //Callback, close device editor
                ELEMENT_EDITORS_DEVICE_EDITOR.slideUp();

                //Update device template list
                vm.deviceTemplateListCtrl.updateItem(data);

                //Fix device template name in overview table (hacky solution)
                ELEMENT_EDITORS_DEVICE.find('table tr#' + data.id + ' td').first().html(data.name);
            });

            //Watch controller result of device template deletions
            $scope.$watch(() => vm.deleteDeviceTemplateCtrl.result, (data) => {
                //Sanity check
                if (!data) return;

                //Callback, remove device template from list
                vm.deviceTemplateListCtrl.removeItem(vm.deleteDeviceTemplateCtrl.result);

                //Check if the deleted template was edited
                if (vm.addDeviceTemplateCtrl.item.id === vm.deleteDeviceTemplateCtrl.result) {
                    //Hide the device template editor
                    ELEMENT_EDITORS_DEVICE_EDITOR.slideUp();
                }
            });

            //Watch controller result of location template additions
            $scope.$watch(() => vm.addLocationTemplateCtrl.result, (data) => {
                    //Sanity check
                    if (!data) return;

                    //Callback, close location editor
                    ELEMENT_EDITORS_LOCATION_EDITOR.slideUp();

                    //Extend data for location template type
                    data.type = lastLocationTemplateType;

                    //Add location template to list
                    vm.locationTemplateListCtrl.pushItem(data);
                }
            );

            //Watch controller result of location template updates
            $scope.$watch(() => vm.updateLocationTemplateCtrl.result, (data) => {
                //Sanity check
                if (!data) return;

                //Callback, close location editor
                ELEMENT_EDITORS_LOCATION_EDITOR.slideUp();

                //Extend data for location template type
                data.type = lastLocationTemplateType;

                //Update location template list
                vm.locationTemplateListCtrl.updateItem(data);

                //Fix location template name in overview table (hacky solution)
                ELEMENT_EDITORS_LOCATION.find('table tr#' + data.id + ' td').first().html(data.name);
            });

            //Watch controller result of location template deletions
            $scope.$watch(() => vm.deleteLocationTemplateCtrl.result, (data) => {
                //Sanity check
                if (!data) return;

                //Callback, remove location template from list
                vm.locationTemplateListCtrl.removeItem(vm.deleteLocationTemplateCtrl.result);

                //Check if the deleted template was edited
                if (vm.addLocationTemplateCtrl.item.id === vm.deleteLocationTemplateCtrl.result) {
                    //Hide the location template editor
                    ELEMENT_EDITORS_LOCATION_EDITOR.slideUp();
                }
            });

            //Expose functions that are used externally
            angular.extend(vm, {
                requestTopicList: requestTopicList,
                deviceTemplateListCtrl: $controller('ItemListController as deviceTemplateListCtrl', {
                    $scope: $scope,
                    list: deviceTemplateList
                }),
                addDeviceTemplateCtrl: $controller('AddItemController as addDeviceTemplateCtrl', {
                    $scope: $scope,
                    entity: 'device template',
                    addItem: addDeviceTemplate
                }),
                updateDeviceTemplateCtrl: $controller('UpdateItemController as updateDeviceTemplateCtrl', {
                    $scope: $scope,
                    updateItem: updateDeviceTemplate
                }),
                deleteDeviceTemplateCtrl: $controller('DeleteItemController as deleteDeviceTemplateCtrl', {
                    $scope: $scope,
                    deleteItem: deleteDeviceTemplate,
                    confirmDeletion: confirmDelete.bind(null, 'device template', deviceTemplateList)
                }),
                locationTemplateListCtrl: $controller('ItemListController as locationTemplateListCtrl', {
                    $scope: $scope,
                    list: locationTemplateList
                }),
                addLocationTemplateCtrl: $controller('AddItemController as addLocationTemplateCtrl', {
                    $scope: $scope,
                    entity: 'location template',
                    addItem: function (data) {
                        //Sanity check for location template type
                        if ((!data.hasOwnProperty("type")) || (!addLocationTemplate.hasOwnProperty(data.type))) return;

                        //Extend request
                        return addLocationTemplate[data.type](transformPolygonPoints(data));
                    }
                }),
                updateLocationTemplateCtrl: $controller('UpdateItemController as updateLocationTemplateCtrl', {
                    $scope: $scope,
                    updateItem: function (data) {
                        //Sanity check for location template type
                        if ((!data.hasOwnProperty("type")) || (!addLocationTemplate.hasOwnProperty(data.type))) return;

                        //Extend request
                        return updateLocationTemplate[data.type](transformPolygonPoints(data));
                    }
                }),
                deleteLocationTemplateCtrl: $controller('DeleteItemController as deleteLocationTemplateCtrl', {
                    $scope: $scope,
                    deleteItem: deleteLocationTemplate,
                    confirmDeletion: confirmDelete.bind(null, 'location template', locationTemplateList)
                }),
                deviceTemplateTestResults: [],
                deviceTemplateTestLoading: false,
                mapInitCenter: MAP_INIT_CENTER,
                mapInitZoom: MAP_INIT_ZOOM,
                testDeviceTemplate: testDeviceTemplate,
                showDeviceTemplatesEditor: showDeviceTemplatesEditor,
                editDeviceTemplate: editDeviceTemplate,
                saveDeviceTemplate: saveDeviceTemplate,
                showLocationTemplatesEditor: showLocationTemplatesEditor,
                editLocationTemplate: editLocationTemplate,
                saveLocationTemplate: saveLocationTemplate,
                onLocationTypeChange: onLocationTypeChange,
                onLocationChange: onLocationChange,
                onDrawingFinished: onLocationMapDrawingFinished
            });
        }
    ]);
