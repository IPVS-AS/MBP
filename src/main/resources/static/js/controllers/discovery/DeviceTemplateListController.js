/* global app */

/*
 * Controller for the settings page.
 */
app.controller('DeviceTemplateListController',
    ['$scope', '$interval', 'NotificationService',
        function ($scope, $interval, NotificationService) {
            //Find relevant DOM elements
            const ELEMENT_MENU_BUTTONS = $("div.bubble-item");
            const ELEMENT_MENU_BUTTON_MAIN = $("div.bubble-item.bubble-devices")

            //Relevant CSS classes
            const CLASS_MENU_BUTTON_CONNECTOR = 'bubble-connector';

            let vm = this;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Initialize the templates menu
                initTemplatesMenu();
            })();

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

            //Expose functions that are used externally
            angular.extend(vm, {});
        }
    ]);
