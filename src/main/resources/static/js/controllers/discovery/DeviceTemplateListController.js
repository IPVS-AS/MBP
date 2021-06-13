/* global app */

/*
 * Controller for the settings page.
 */
app.controller('DeviceTemplateListController',
    ['$scope', '$controller', '$interval', '$timeout', 'locationTemplateList', 'addLocationTemplate', 'deleteLocationTemplate', 'DiscoveryService', 'NotificationService',
        function ($scope, $controller, $interval, $timeout, locationTemplateList, addLocationTemplate, deleteLocationTemplate, DiscoveryService, NotificationService) {
            //Constants
            const MAP_INIT_CENTER = [9.106631254042352, 48.74518217652443];
            const MAP_INIT_ZOOM = 16;

            //Find relevant DOM elements
            const ELEMENT_MENU_BUTTONS = $("div.bubble-item");
            const ELEMENT_MENU_BUTTON_MAIN = $("div.bubble-item.bubble-devices")
            const ELEMENT_EDITORS_COLLAPSES = $('#edit-templates-group > div.collapse');
            const ELEMENT_EDITORS_LOCATION_EDITOR = $('#edit-locations-editor');

            //Relevant CSS classes
            const CLASS_MENU_BUTTON_CONNECTOR = 'bubble-connector';

            let vm = this;

            //Define models for UI input
            vm.locationInput = {};

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Initialize the templates menu and editor windows
                initTemplatesMenu();
                initEditorWindows();
            })();

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
             * Shows the location template editor with an animation.
             */
            function showLocationTemplatesEditor() {
                ELEMENT_EDITORS_LOCATION_EDITOR.slideUp().slideDown(400, function () {
                    //Remove geometries from map
                    vm.locationMapApi.removeGeometries();

                    //Adjust size of location map to changed UI
                    vm.locationMapApi.updateMapSize();
                });
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

            //Watch controller result of location template additions
            $scope.$watch(() => vm.addLocationTemplateCtrl.result, () => {
                    //Callback, close location editor
                    ELEMENT_EDITORS_LOCATION_EDITOR.slideUp();
                }
            );

            //Watch controller result of location template deletions
            $scope.$watch(() => vm.deleteLocationTemplateCtrl.result, () => {
                //Callback, remove location template from list
                vm.locationTemplateListCtrl.removeItem(vm.deleteLocationTemplateCtrl.result);
            });

            //Expose functions that are used externally
            angular.extend(vm, {
                locationTemplateListCtrl: $controller('ItemListController as locationTemplateListCtrl', {
                    $scope: $scope,
                    list: locationTemplateList
                }),
                addLocationTemplateCtrl: $controller('AddItemController as addLocationTemplateCtrl', {
                    $scope: $scope,
                    entity: 'location template',
                    addItem: function (data) {
                        //Sanity check for location template type
                        if ((!data.hasOwnProperty("type")) || (!addLocationTemplate.hasOwnProperty(data.type))) {
                            return;
                        }

                        //Check if data contains polygon points string
                        if (data.hasOwnProperty("pointsList")) {
                            //Transform string to array of coordinates
                            data.points = data.pointsList.split("\n").map(x => x.split("|").map(s => parseFloat(s)));

                            //Remove string from data object
                            delete data.pointsList;
                        }

                        //Extend request
                        return addLocationTemplate[data.type](data);
                    }
                }),
                deleteLocationTemplateCtrl: $controller('DeleteItemController as deleteLocationTemplateCtrl', {
                    $scope: $scope,
                    deleteItem: deleteLocationTemplate,
                    confirmDeletion: confirmDelete.bind(null, 'location template', locationTemplateList)
                }),
                mapInitCenter: MAP_INIT_CENTER,
                mapInitZoom: MAP_INIT_ZOOM,
                onLocationTypeChange: onLocationTypeChange,
                onLocationChange: onLocationChange,
                onDrawingFinished: onLocationMapDrawingFinished,
                showLocationTemplatesEditor: showLocationTemplatesEditor
            });
        }
    ]);
