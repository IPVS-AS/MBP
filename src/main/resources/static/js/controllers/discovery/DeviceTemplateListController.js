/* global app */

/*
 * Controller for the settings page.
 */
app.controller('DeviceTemplateListController',
    ['$scope', '$interval', '$timeout', 'NotificationService',
        function ($scope, $interval, $timeout, NotificationService) {
            //Constants
            const MAP_INIT_CENTER = [9.106631254042352, 48.74518217652443];
            const MAP_INIT_ZOOM = 16;

            //Find relevant DOM elements
            const ELEMENT_MENU_BUTTONS = $("div.bubble-item");
            const ELEMENT_MENU_BUTTON_MAIN = $("div.bubble-item.bubble-devices")
            const ELEMENT_EDITORS_COLLAPSES = $('#edit-templates-group > div.collapse');

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
                if (!(["Point", "Circle", "Polygon"].includes(vm.locationInput.type))) {
                    //Disable drawing
                    vm.locationMapApi.disableDrawing();
                    return;
                }

                //Enable draw interaction for the selected type
                vm.locationMapApi.enableDrawing(vm.locationInput.type);
            }

            /**
             * [Public]
             * Handles change events with respect to the location parameters.
             */
            function onLocationChange() {
                //Geometry to add
                let geometry = null;

                //Check for location type
                switch (vm.locationInput.type) {
                    case "Point":
                        //Determine coordinates of the point
                        let coordinates = ol.proj.fromLonLat([vm.locationInput.longitude || 0,
                            vm.locationInput.latitude || 0])

                        //Create point geometry
                        geometry = new ol.geom.Point(coordinates, "XY");
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
                if (geometryType !== vm.locationInput.type) {
                    return;
                }

                //Remove the geometries that are currently visible on the map
                vm.locationMapApi.removeGeometries();

                //Update location model according to the geometry
                switch (geometryType) {
                    case "Point":
                        //Get coordinates
                        let coordinates = ol.proj.toLonLat(geometry.getCoordinates());

                        //Update models
                        $timeout(() => {
                            vm.locationInput.longitude = Math.round((coordinates[0] + Number.EPSILON) * 1e6) / 1e6;
                            vm.locationInput.latitude = Math.round((coordinates[1] + Number.EPSILON) * 1e6) / 1e6;
                        }, 10);

                    case "Circle":
                        break;
                    case "Polygon":
                        break;
                }
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

            //Expose functions that are used externally
            angular.extend(vm, {
                mapInitCenter: MAP_INIT_CENTER,
                mapInitZoom: MAP_INIT_ZOOM,
                onLocationTypeChange: onLocationTypeChange,
                onLocationChange: onLocationChange,
                onDrawingFinished: onLocationMapDrawingFinished
            });
        }
    ]);
