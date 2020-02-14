/* global app */

'use strict';

/**
 * Directive for a modeling tool that can be used for creating and editing IoT environment models.
 */
app.directive('envModelTool',
    ['ENDPOINT_URI', '$timeout', '$q', '$controller', 'ModelService', 'ComponentService', 'DeviceService', 'CrudService',
        function (ENDPOINT_URI, $timeout, $q, $controller, ModelService, ComponentService, DeviceService, CrudService) {

            function initJSPlumb(scope) {
                const DIAGRAM_CONTAINER = $("#myDiagram");
                let jsPlumbInstance;
                let canvasId = "#canvas";
                let elementIdCount = 0; // used for canvas ID uniquness
                let properties = {}; // keeps the properties of each element to draw on canvas
                let element = ""; // the element which will be appended to the canvas
                let clicked = false; // true if an element from the palette was clicked
                let isMoving = false; //Remembers if the user is currently moving or modifying an element
                let deletionPromises = [];

                //Expose fields for template
                scope.processing = {}; // used to show/hide the progress circle
                scope.selectedOptionName = "";
                scope.loadedModels = [];
                scope.currentModel = {};
                scope.clickedComponent = {};
                scope.adapterListCtrl = $controller('ItemListController as adapterListCtrl', {
                    $scope: scope,
                    list: []
                });

                //Expose functions for template
                scope.drawModel = drawModel;
                scope.aveModel = saveModel;
                scope.deleteModel = deleteModel;
                scope.newModel = newModel;
                scope.registerComponents = registerComponents;
                scope.deployComponents = deployComponents;
                scope.undeployComponents = undeployComponents;

                // On initialization load the models from the database
                (function initController() {
                    loadModels();
                })();


                /*
                 * Define the types and look of the nodes and connections
                 */

                jsPlumbInstance = window.jsp = jsPlumb.getInstance({
                    Endpoint: ["Dot", {
                        radius: 2
                    }],
                    Connector: "StateMachine",
                    HoverPaintStyle: {
                        strokeWidth: 3
                    },
                    ConnectionOverlays: [
                        ["Label", {
                            label: "NAME",
                            id: "label",
                            cssClass: "aLabel",
                            visible: false
                        }]
                    ],
                    Container: "canvas"
                });

                let basicType = {
                    anchor: "Continuous",
                    connector: "StateMachine"
                };

                jsPlumbInstance.registerConnectionType("basic", basicType);

                let sourceEndpoint = {
                    filter: ".ep",
                    anchor: "Continuous",
                    connectorStyle: {
                        stroke: "#000000",
                        strokeWidth: 2
                    },
                    connectionType: "basic"
                };

                let targetEndpoint = {
                    dropOptions: {
                        hoverClass: "dragHover"
                    },
                    anchor: "Continuous",
                    allowLoopback: false
                };

                /**
                 * Makes the given element rotatable.
                 *
                 * @param element The element to rotate
                 */
                function makeRotatable(element) {
                    //Check if rotatability may be just enabled again
                    if (element.has(".ui-rotatable-handle").length) {
                        element.rotatable("enable").find('.ui-rotatable-handle').show();
                        return;
                    }

                    let rotateOptions = {
                        handleOffset: {'top': 5, 'left': 5},
                        rotate: function (el) {
                            jsPlumbInstance.revalidate(el);
                        },
                        start: () => (isMoving = true),
                        stop: () => ($timeout(() => (isMoving = false), 200)),
                        snap: true,
                        step: 22.5
                    };

                    element.rotatable(rotateOptions);
                }

                /*
                 * jQuery makes the given element resizable.
                 *
                 * @param element The element to resize
                 */
                function makeResizable(element) {
                    //Check if resizability may be just enabled again
                    if (element.hasClass("ui-resizable-disabled")) {
                        element.resizable("enable");
                        return;
                    }

                    //Make element resizable
                    element.resizable({
                        //Check if aspect ratio needs to be preserved
                        aspectRatio: (!element.hasClass("free-resize")),
                        resize: (event, ui) => jsPlumbInstance.revalidate(ui.helper),
                        start: (event, ui) => (isMoving = true),
                        stop: () => ($timeout(() => (isMoving = false), 200)),
                        handles: "all"
                    });
                }

                /*
                 * jQuery makes the element draggable
                 */
                function makeDraggable(id, className) {
                    $(id).draggable({
                        helper: function () {
                            return $("<div/>", {
                                class: className
                            });
                        },
                        revert: false
                    });
                }


                /*
                 * Make all the elements from the palette draggable
                 */
                makeDraggable("#roomFloorplan", "window floorplan room-floorplan custom");
                makeDraggable("#wallFloorplan", "window floorplan wall-floorplan custom");
                makeDraggable("#doorFloorplan", "window floorplan door-floorplan custom");
                makeDraggable("#windowFloorplan", "window floorplan window-floorplan custom");
                makeDraggable("#stairsFloorplan", "window floorplan stairs-floorplan custom");
                makeDraggable("#tableFloorplan", "window floorplan table-floorplan custom");
                makeDraggable("#chairFloorplan", "window floorplan chair-floorplan custom");
                makeDraggable("#couchFloorplan", "window floorplan couch-floorplan custom");
                makeDraggable("#bedFloorplan", "window floorplan bed-floorplan custom");
                makeDraggable("#kitchenSinkFloorplan", "window floorplan kitchen-sink-floorplan custom");
                makeDraggable("#bathtubFloorplan", "window floorplan bathtub-floorplan custom");
                makeDraggable("#bathSinkFloorplan", "window floorplan bath-sink-floorplan custom");
                makeDraggable("#toiletFloorplan", "window floorplan toilet-floorplan custom");

                makeDraggable("#raspberryPiDevice", "window device raspberry-pi-device custom");
                makeDraggable("#arduinoDevice", "window device arduino-device custom");
                makeDraggable("#computerDevice", "window device computer-device custom");
                makeDraggable("#laptopDevice", "window device laptop-device custom");
                makeDraggable("#tvDevice", "window device tv-device custom");
                makeDraggable("#smartphoneDevice", "window device smartphone-device custom");
                makeDraggable("#smartwatchDevice", "window device smartwatch-device custom");
                makeDraggable("#audioSystemDevice", "window device audio-system-device custom");
                makeDraggable("#voiceControllerDevice", "window device voice-controller-device custom");
                makeDraggable("#cameraDevice", "window device camera-device custom");
                makeDraggable("#defaultDevice", "window device default-device custom");

                makeDraggable("#lightActuator", "window actuator light-actuator custom");
                makeDraggable("#ledActuator", "window actuator led-actuator custom");
                makeDraggable("#speakerActuator", "window actuator speaker-actuator custom");
                makeDraggable("#buzzerActuator", "window actuator buzzer-actuator custom");
                makeDraggable("#vibrationActuator", "window actuator vibration-actuator custom");
                makeDraggable("#heaterActuator", "window actuator heater-actuator custom");
                makeDraggable("#airConditionerActuator", "window actuator air-conditioner-actuator custom");
                makeDraggable("#switchActuator", "window actuator switch-actuator custom");
                makeDraggable("#motorActuator", "window actuator motor-actuator custom");
                makeDraggable("#defaultActuator", "window actuator default-actuator custom");

                makeDraggable("#cameraSensor", "window sensor camera-sensor custom");
                makeDraggable("#soundSensor", "window sensor sound-sensor custom");
                makeDraggable("#temperatureSensor", "window sensor temperature-sensor custom");
                makeDraggable("#humiditySensor", "window sensor humidity-sensor custom");
                makeDraggable("#gasSensor", "window sensor gas-sensor custom");
                makeDraggable("#lightSensor", "window sensor light-sensor custom");
                makeDraggable("#motionSensor", "window sensor motion-sensor custom");
                makeDraggable("#locationSensor", "window sensor location-sensor custom");
                makeDraggable("#gyroscopeSensor", "window sensor gyroscope-sensor custom");
                makeDraggable("#proximitySensor", "window sensor proximity-sensor custom");
                makeDraggable("#touchSensor", "window sensor touch-sensor custom");
                makeDraggable("#vibrationSensor", "window sensor vibration-sensor custom");
                makeDraggable("#defaultSensor", "window sensor default-sensor custom");

                // jQuery makes the canvas droppable
                $(canvasId).droppable({
                    accept: ".window",
                    drop: function (event, ui) {
                        if (clicked) {
                            // Get the drop position
                            properties.top = ui.offset.top - $(this).offset().top;
                            properties.left = ui.offset.left - $(this).offset().left;
                            clicked = false;
                            elementIdCount++;
                            let id = "canvasWindow" + elementIdCount;
                            // Create and draw the element in the canvas
                            element = createElement(id);
                            drawElement(element);
                        }
                    }
                });

                /*
                 * Temporary saved properties of clicked element in palette
                 * The data is used to create the element on drop
                 */
                function loadProperties(clsName, type) {
                    properties = {};
                    properties.clsName = clsName;
                    properties.type = type;
                    clicked = true;
                }


                /*
                 * Load properties of an element once the element in the palette is clicked
                 */

                // Floorplan
                $('#roomFloorplan').mousedown(function () {
                    loadProperties("window floorplan room-floorplan custom free-resize jtk-node", undefined);
                });

                $('#wallFloorplan').mousedown(function () {
                    loadProperties("window floorplan wall-floorplan custom free-resize jtk-node", undefined);
                });

                $('#doorFloorplan').mousedown(function () {
                    loadProperties("window floorplan door-floorplan custom jtk-node", undefined);
                });

                $('#windowFloorplan').mousedown(function () {
                    loadProperties("window floorplan window-floorplan custom jtk-node", undefined);
                });

                $('#stairsFloorplan').mousedown(function () {
                    loadProperties("window floorplan stairs-floorplan custom jtk-node", undefined);
                });

                $('#tableFloorplan').mousedown(function () {
                    loadProperties("window floorplan table-floorplan custom jtk-node", undefined);
                });

                $('#chairFloorplan').mousedown(function () {
                    loadProperties("window floorplan chair-floorplan custom jtk-node", undefined);
                });

                $('#couchFloorplan').mousedown(function () {
                    loadProperties("window floorplan couch-floorplan custom jtk-node", undefined);
                });

                $('#bedFloorplan').mousedown(function () {
                    loadProperties("window floorplan bed-floorplan custom jtk-node", undefined);
                });

                $('#kitchenSinkFloorplan').mousedown(function () {
                    loadProperties("window floorplan kitchen-sink-floorplan custom jtk-node", undefined);
                });

                $('#bathtubFloorplan').mousedown(function () {
                    loadProperties("window floorplan bathtub-floorplan custom jtk-node", undefined);
                });

                $('#bathSinkFloorplan').mousedown(function () {
                    loadProperties("window floorplan bath-sink-floorplan custom jtk-node", undefined);
                });

                $('#toiletFloorplan').mousedown(function () {
                    loadProperties("window floorplan toilet-floorplan custom jtk-node", undefined);
                });

                // Devices
                $('#raspberryPiDevice').mousedown(function () {
                    loadProperties("window device raspberry-pi-device custom jtk-node", "Raspberry Pi");
                });

                $('#arduinoDevice').mousedown(function () {
                    loadProperties("window device arduino-device custom jtk-node", "Arduino");
                });

                $('#computerDevice').mousedown(function () {
                    loadProperties("window device computer-device custom jtk-node", "Computer");
                });

                $('#laptopDevice').mousedown(function () {
                    loadProperties("window device laptop-device custom jtk-node", "Laptop");
                });

                $('#tvDevice').mousedown(function () {
                    loadProperties("window device tv-device custom jtk-node", "TV");
                });

                $('#smartphoneDevice').mousedown(function () {
                    loadProperties("window device smartphone-device custom jtk-node", "Smartphone");
                });

                $('#smartwatchDevice').mousedown(function () {
                    loadProperties("window device smartwatch-device custom jtk-node", "Smartwatch");
                });

                $('#audioSystemDevice').mousedown(function () {
                    loadProperties("window device audio-system-device custom jtk-node", "Audio System");
                });

                $('#voiceControllerDevice').mousedown(function () {
                    loadProperties("window device voice-controller-device custom jtk-node", "Voice Controller");
                });

                $('#cameraDevice').mousedown(function () {
                    loadProperties("window device camera-device custom jtk-node", "Camera");
                });

                $('#defaultDevice').mousedown(function () {
                    loadProperties("window device default-device custom jtk-node", undefined);
                });

                // Actuators
                $('#lightActuator').mousedown(function () {
                    loadProperties("window actuator light-actuator custom jtk-node", "Light");
                });

                $('#ledActuator').mousedown(function () {
                    loadProperties("window actuator led-actuator custom jtk-node", "LED");
                });

                $('#speakerActuator').mousedown(function () {
                    loadProperties("window actuator speaker-actuator custom jtk-node", "Speaker");
                });

                $('#buzzerActuator').mousedown(function () {
                    loadProperties("window actuator buzzer-actuator custom jtk-node", "Buzzer");
                });

                $('#vibrationActuator').mousedown(function () {
                    loadProperties("window actuator vibration-actuator custom jtk-node", "Vibration");
                });

                $('#heaterActuator').mousedown(function () {
                    loadProperties("window actuator heater-actuator custom jtk-node", "Heater");
                });

                $('#airConditionerActuator').mousedown(function () {
                    loadProperties("window actuator air-conditioner-actuator custom jtk-node", "Air Conditioner");
                });

                $('#switchActuator').mousedown(function () {
                    loadProperties("window actuator switch-actuator custom jtk-node", "Switch");
                });

                $('#motorActuator').mousedown(function () {
                    loadProperties("window actuator motor-actuator custom jtk-node", "Motor");
                });

                $('#defaultActuator').mousedown(function () {
                    loadProperties("window actuator default-actuator custom jtk-node", undefined);
                });

                // Sensors
                $('#cameraSensor').mousedown(function () {
                    loadProperties("window sensor camera-sensor custom jtk-node", "Camera");
                });

                $('#soundSensor').mousedown(function () {
                    loadProperties("window sensor sound-sensor custom jtk-node", "Sound");
                });

                $('#temperatureSensor').mousedown(function () {
                    loadProperties("window sensor temperature-sensor custom jtk-node", "Temperature");
                });

                $('#humiditySensor').mousedown(function () {
                    loadProperties("window sensor humidity-sensor custom jtk-node", "Humidity");
                });

                $('#gasSensor').mousedown(function () {
                    loadProperties("window sensor gas-sensor custom jtk-node", "Gas");
                });

                $('#lightSensor').mousedown(function () {
                    loadProperties("window sensor light-sensor custom jtk-node", "Light");
                });

                $('#motionSensor').mousedown(function () {
                    loadProperties("window sensor motion-sensor custom jtk-node", "Motion");
                });

                $('#locationSensor').mousedown(function () {
                    loadProperties("window sensor location-sensor custom jtk-node", "Location");
                });

                $('#gyroscopeSensor').mousedown(function () {
                    loadProperties("window sensor gyroscope-sensor custom jtk-node", "Gyroscope");
                });

                $('#proximitySensor').mousedown(function () {
                    loadProperties("window sensor proximity-sensor custom jtk-node", "Proximity");
                });

                $('#touchSensor').mousedown(function () {
                    loadProperties("window sensor touch-sensor custom jtk-node", "Touch");
                });

                $('#vibrationSensor').mousedown(function () {
                    loadProperties("window sensor vibration-sensor custom jtk-node", "Vibration");
                });

                $('#defaultSensor').mousedown(function () {
                    loadProperties("window sensor default-sensor custom jtk-node", undefined);
                });

                /**
                 * Create an element to be drawn on the canvas with a certain id.
                 */
                function createElement(id) {
                    //Create element
                    let element = $('<div>').attr('id', id).addClass(properties.clsName);

                    // The position to create the dropped element
                    element.css({
                        'top': properties.top,
                        'left': properties.left
                    });

                    // Increase the size of room
                    if (element.hasClass("room-floorplan")) {
                        element.css({width: '50px', height: '50px'}).animate({
                            width: '250px',
                            height: '250px'
                        }, 1000);
                    }
                    // Add connection square on device
                    if (properties.clsName.indexOf("device") > -1) {
                        element.append("<div class=\"ep\"></div>");
                    }

                    if (properties.type) {
                        element.data("type", properties.type);
                    }

                    //Put focus on new element
                    focusElement(element);

                    return element;
                }


                /**
                 * Create an element from a given node to be drawn on the canvas with a certain id.
                 */
                function createElementFromNode(id, node) {
                    //Create element
                    let element = $('<div>').attr('id', id).addClass(node.clsName);

                    // The position to create the element
                    element.css({
                        'top': node.positionY,
                        'left': node.positionX
                    });

                    // Define the size of the element
                    element.outerWidth(node.width);
                    element.outerHeight(node.height);

                    // Set rotation angle; no need for room
                    if (node.angle && (node.clsName.indexOf("free-resize") === -1)) {
                        element.data("angle", node.angle);
                        setAngle(element, false);
                    }

                    // Append the data to the element
                    element.data(node);

                    //Append ep if device
                    if (node.nodeType === "device") {
                        element.append("<div class=\"ep\"></div>");
                    }

                    return element;
                }

                /*
                 * Draw/append the element on the canvas
                 */
                function drawElement($element) {
                    $(canvasId).append($element);
                    // Make the element on the canvas draggable
                    jsPlumbInstance.draggable(jsPlumbInstance.getSelector(".jtk-node"), {
                        filter: ".ui-resizable-handle"
                    });

                    addEndpoints($element);
                }

                /*
                 * Define the sources and targets for making connections
                 */
                function addEndpoints(element) {
                    let type = element.attr('class').toString().split(" ")[1];
                    if (type === "device") {
                        targetEndpoint.maxConnections = -1;
                        jsPlumbInstance.makeSource(element, sourceEndpoint);
                        jsPlumbInstance.makeTarget(element, targetEndpoint);
                    } else if (type === "actuator" || type === "sensor") {
                        targetEndpoint.maxConnections = 1;
                        jsPlumbInstance.makeTarget(element, targetEndpoint);
                    }
                }

                // In case the diagram container is clicked
                DIAGRAM_CONTAINER.on("click", function (event) {
                    //Ensure that user is not currently moving an element
                    if (isMoving) {
                        return;
                    }
                    let element = $(event.target).filter('.jtk-node');
                    focusElement(element);
                });

                // In case a key is pressed
                $(document).on("keydown", function (event) {
                    //Require DEL key
                    if (event.which !== 46) {
                        return;
                    }

                    //Get elements with focus and iterate over them
                    $('.clicked-element').each(function () {
                        deleteElement($(this));
                    });
                });

                // When the canvas is clicked, save the data from the input fields to the corresponding element
                $(canvasId).on('click', function (e) {
                    saveData();
                });

                /**
                 * Puts the focus on a given element while removing the focus of all other elements. Focused
                 * elements become resizable.
                 * @param element The element to focus; null, of no element is suppoesd to be focused
                 */
                function focusElement(element) {
                    //Get all focused elements and remove focus, resizability and rotatability
                    $('.clicked-element').removeClass('clicked-element').resizable("disable")
                        .rotatable("disable").find('.ui-rotatable-handle').hide();

                    //Check if a new focus element was provided
                    if ((element === null) || (element.length < 1)) {
                        return;
                    }

                    // Load the corresponding data to show it in the tool
                    loadData(element);

                    //Make element resizable
                    makeResizable(element);

                    //Make element rotatable
                    makeRotatable(element);

                    // Put focus on element
                    element.addClass("clicked-element");
                }


                /*
                 * Rotate the element with css
                 */
                function setAngle(element, rotate) {
                    let angle = 0;
                    if (rotate) {
                        angle = (element.data('angle') + 90) || 90;
                    } else {
                        angle = element.data("angle");
                    }
                    element.css({
                        '-webkit-transform': 'rotate(' + angle + 'deg)',
                        '-moz-transform': 'rotate(' + angle + 'deg)',
                        '-ms-transform': 'rotate(' + angle + 'deg)',
                        'transform': 'rotate(' + angle + 'deg)',
                    });
                    element.data('angle', angle);
                    if (element.outerHeight() > element.outerWidth()) {
                        element.outerWidth(element.outerHeight());
                    } else {
                        element.outerHeight(element.outerWidth());
                    }
                }

                /**
                 * Undeploys, unregisters and deletes an element.
                 * @param element The element to delete
                 */
                function deleteElement(element) {
                    scope.processing = {};
                    scope.processing.status = true;
                    scope.processing.finished = false;
                    scope.processing.undeployedDeregistered = true;

                    let type = element.attr('class').toString().split(" ")[1];

                    if (type === "device" && element.data("id")) { // Case: A device has attached sensors and actuators, which are deployed or registered
                        deletionPromises = [];
                        $.each(jsPlumbInstance.getConnections({
                            source: element.attr("id")
                        }), function (index, connection) {
                            let target = $(connection.target);
                            let targetType = target.attr('class').toString().split(" ")[1];
                            if (targetType === "sensor" || targetType === "actuator") {
                                // Undeploy and deregister the attached sensor/actuator
                                if (target.data("deployed")) {
                                    let promise = undeployComponent(targetType, target, false);
                                    deletionPromises.push(promise);
                                } else if (target.data("id")) {
                                    let promise = deregisterComponent(targetType, target, false);
                                    deletionPromises.push(promise);
                                }
                            }
                        });

                        // Save the model afterwards to stay updated
                        $q.all(deletionPromises).then(function () {
                            if (scope.processing.undeployedDeregistered) {
                                deregisterComponent(type, element, true);
                            } else {
                                saveModel().then(function (response) {
                                    scope.processing.message = "Sensor or actuator error";
                                    scope.processing.success = false;
                                    processingTimeout();
                                });
                            }
                        });
                    } else { // Case: device, sensor or actuator
                        if (element.data("deployed")) {
                            undeployComponent(type, element, true);
                        } else if (element.data("id")) {
                            deregisterComponent(type, element, true);
                        } else {
                            deleteElementFromCanvas(element, false);
                            scope.processing.status = false;
                        }
                    }
                }

                /*
                 * Undeploy and deregister the element
                 */
                function undeployComponent(type, element, deleteFromModel) {
                    return ComponentService.undeploy(ENDPOINT_URI + "/deploy/" + type + "/" + element.data("id")).then(
                        function (response) {
                            element.data("deployed", false);
                            element.removeData("depError");
                            element.removeClass("error-element");
                            element.removeClass("deployed-element");
                            element.addClass("success-element");
                            // Deregister the element in second step
                            let promise = deregisterComponent(type, element, deleteFromModel);
                            deletionPromises.push(promise);
                        },
                        function (response) {
                            element.data("depError", response.data ? response.data.globalMessage : response.status);
                            element.removeClass("success-element");
                            element.removeClass("deployed-element");
                            element.addClass("error-element");
                            scope.processing.undeployedDeregistered = false;
                            if (deleteFromModel) {
                                saveModel().then(function (response) {
                                    scope.processing.message = "Undeployment of " + element.data("name") + " ended with an error";
                                    scope.processing.success = false;
                                    processingTimeout();
                                });
                            }
                        });
                }

                /*
                 * Deregister and delete the element from canvas
                 */
                function deregisterComponent(type, element, deleteFromModel) {
                    let item = {};
                    item.id = element.data("id");
                    return CrudService.deleteItem(type + "s", item).then(
                        function (response) {
                            element.removeData("id");
                            element.removeData("depError");
                            element.removeData("regError");
                            element.removeClass("error-element");
                            element.removeClass("success-element");
                            // On success delete the element from canvas
                            if (deleteFromModel) {
                                deleteElementFromCanvas(element, true);
                            }
                        },
                        function (response) {
                            element.data("regError", response.status);
                            element.removeClass("success-element");
                            element.addClass("error-element");
                            scope.processing.undeployedDeregistered = false;
                            if (deleteFromModel) {
                                saveModel().then(function (response) {
                                    scope.processing.message = "Deregistration of " + element.data("name") + " ended with an error";
                                    scope.processing.success = false;
                                    processingTimeout();
                                });
                            }
                        });
                }

                /*
                 * Delete the element from the canvas and jsPlumbInstance
                 */
                function deleteElementFromCanvas(element, savingModel) {
                    $timeout(function () {
                        scope.clickedComponent = {};
                        jsPlumbInstance.remove(element);
                        if (savingModel) {
                            saveModel().then(function (response) {
                                scope.processing.message = element.data("name") + " deleted";
                                scope.processing.success = true;
                                processingTimeout();
                            });
                        }
                    });
                }


                /*
                 * Bind listeners to the connections
                 */

                // The connection is deleted on double click
                jsPlumbInstance.bind("dblclick", jsPlumbInstance.deleteConnection);

                // Show the name input on click
                jsPlumbInstance.bind("click", function (connection, originalEvent) {
                    let overlay = connection.getOverlay("label");
                    if (overlay.isVisible() && originalEvent.target.localName == 'path') {
                        overlay.hide();
                    } else if (!overlay.isVisible()) {
                        overlay.show();
                    }
                });

                // Add device name and id to sensor or actuator when a connection is created
                jsPlumbInstance.bind("connection", function (info) {
                    saveData();
                    let source = $(info.source);
                    let target = $(info.target);
                    if (target.attr("class").indexOf("device") == -1) {
                        target.data("device", source.data("name"));
                        target.data("deviceId", source.data("id"));
                    }
                });

                // Undeploy, deregister and remove device name and id from sensor or actuator when a connection is removed
                jsPlumbInstance.bind("connectionDetached", function (info) {
                    onDetach(info);
                });

                function onDetach(info) {
                    scope.processing = {};
                    scope.processing.status = true;
                    scope.processing.finished = false;
                    scope.processing.undeployedDeregistered = true;

                    let target = $(info.target);
                    let targetType = target.attr('class').toString().split(" ")[1];
                    if (targetType == "sensor" || targetType == "actuator") {
                        deletionPromises = [];
                        if (target.data("deployed")) {
                            let promise = undeployComponent(targetType, target, false);
                            deletionPromises.push(promise);
                        } else if (target.data("id")) {
                            let promise = deregisterComponent(targetType, target, false);
                            deletionPromises.push(promise);
                        } else {
                            scope.processing.status = false;
                        }

                        // Save the model after undeployment and deregistration
                        $q.all(deletionPromises).then(function () {
                            if (deletionPromises.length !== 0) {
                                saveModel().then(function (response) {
                                    if (scope.processing.undeployedDeregistered) {
                                        scope.processing.message = target.data("name") + " deregistered";
                                        scope.processing.success = true;
                                    } else {
                                        scope.processing.message = target.data("name") + " error";
                                        scope.processing.success = false;
                                    }
                                    processingTimeout();
                                });
                            }
                        });
                        target.removeData("device");
                        target.removeData("deviceId");
                    } else {
                        scope.processing.status = false;
                    }
                }

                /*
                 * Load the data from the element to show it in the tool and input fields
                 */
                function loadData(element) {
                    $timeout(function () {
                        if (element.attr("class").indexOf("device") > -1) {
                            scope.clickedComponent.category = "DEVICE";
                            scope.clickedComponent.id = element.data("id");
                            scope.clickedComponent.name = element.data("name");
                            scope.clickedComponent.type = element.data("type");
                            scope.clickedComponent.mac = element.data("mac");
                            scope.clickedComponent.ip = element.data("ip");
                            scope.clickedComponent.username = element.data("username");
                            scope.clickedComponent.password = element.data("password");
                            scope.clickedComponent.rsaKey = element.data("rsaKey");
                            scope.clickedComponent.regError = element.data("regError");
                            scope.clickedComponent.element = element;
                            // If device is registered then disable the input fields
                            if (element.data("id")) {
                                $("#deviceInfo *").attr("disabled", true).off('click');
                            } else {
                                $("#deviceInfo *").attr("disabled", false).on('click');
                                if (element.attr("class").indexOf("default") == -1) {
                                    $("#deviceTypeInput").attr("disabled", true);
                                }
                            }
                        } else if (element.attr("class").indexOf("actuator") > -1) {
                            scope.clickedComponent.category = "ACTUATOR";
                            scope.clickedComponent.id = element.data("id");
                            scope.clickedComponent.name = element.data("name");
                            scope.clickedComponent.type = element.data("type");
                            scope.clickedComponent.adapter = element.data("adapter");
                            scope.clickedComponent.device = element.data("device");
                            scope.clickedComponent.regError = element.data("regError");
                            scope.clickedComponent.depError = element.data("depError");
                            scope.clickedComponent.deployed = element.data("deployed");
                            scope.clickedComponent.element = element;
                            // Disable the input fields if registered
                            if (element.data("id")) {
                                $("#actuatorInfo *").attr("disabled", true).off('click');
                            } else {
                                $("#actuatorInfo *").attr("disabled", false).on('click');
                                $("#actuatorDeviceInput").attr("disabled", true);
                                if (element.attr("class").indexOf("default") == -1) {
                                    $("#actuatorTypeInput").attr("disabled", true);
                                }
                            }
                        } else if (element.attr("class").indexOf("sensor") > -1) {
                            scope.clickedComponent.category = "SENSOR";
                            scope.clickedComponent.id = element.data("id");
                            scope.clickedComponent.name = element.data("name");
                            scope.clickedComponent.type = element.data("type");
                            scope.clickedComponent.adapter = element.data("adapter");
                            scope.clickedComponent.device = element.data("device");
                            scope.clickedComponent.regError = element.data("regError");
                            scope.clickedComponent.depError = element.data("depError");
                            scope.clickedComponent.deployed = element.data("deployed");
                            scope.clickedComponent.element = element;
                            // Disable the input fields if registered
                            if (element.data("id")) {
                                $("#sensorInfo *").attr("disabled", true).off('click');
                            } else {
                                $("#sensorInfo *").attr("disabled", false).on('click');
                                $("#sensorDeviceInput").attr("disabled", true);
                                if (element.attr("class").indexOf("default") == -1) {
                                    $("#sensorTypeInput").attr("disabled", true);
                                }
                            }
                        }
                    });
                }

                /*
                 * Save the data from the input fields in the element
                 */
                function saveData() {
                    let element = scope.clickedComponent.element;
                    if (element) {
                        if (element.attr("class").indexOf("device") > -1) {
                            element.data("name", scope.clickedComponent.name);
                            element.data("type", scope.clickedComponent.type);
                            element.data("mac", scope.clickedComponent.mac);
                            element.data("ip", scope.clickedComponent.ip);
                            element.data("username", scope.clickedComponent.username);
                            element.data("password", scope.clickedComponent.password);
                            element.data("rsaKey", scope.clickedComponent.rsaKey);
                            updateDeviceSA(element);
                        } else if (element.attr("class").indexOf("actuator") > -1) {
                            element.data("name", scope.clickedComponent.name);
                            element.data("type", scope.clickedComponent.type);
                            element.data("adapter", scope.clickedComponent.adapter);
                        } else if (element.attr("class").indexOf("sensor") > -1) {
                            element.data("name", scope.clickedComponent.name);
                            element.data("type", scope.clickedComponent.type);
                            element.data("adapter", scope.clickedComponent.adapter);
                        }
                    }

                    $timeout(function () {
                        scope.clickedComponent = {};
                    });
                }

                /*
                 * Update device name and ID in the attached sensors and actuators
                 */
                function updateDeviceSA(device) {
                    $.each(jsPlumbInstance.getConnections({
                        source: device.attr("id")
                    }), function (index, connection) {
                        let target = $(connection.target);
                        if (target.attr("class").indexOf("device") == -1) {
                            target.data("device", device.data("name"));
                            target.data("deviceId", device.data("id"));
                        }
                    });
                }

                /*
                 * Draw a model on the canvas based on the saved JSON representation
                 */
                function drawModel(model) {
                    clearCanvas();
                    scope.currentModel = JSON.parse(model);
                    let environment = JSON.parse(scope.currentModel.value);
                    // Draw first the nodes
                    $.each(environment.nodes, function (index, node) {
                        element = createElementFromNode(node.elementId, node);
                        drawElement(element);
                        element = "";
                    });
                    // Connect the created nodes
                    $.each(environment.connections, function (index, connection) {
                        let conn = jsPlumbInstance.connect({
                            source: connection.sourceId,
                            target: connection.targetId,
                            type: "basic"
                        });
                        conn.getOverlay("label").label = connection.label;
                        if (connection.labelVisible) {
                            conn.getOverlay("label").show();
                        }
                    });
                    elementIdCount = environment.elementIdCount;
                }

                /*
                 * Load models from database
                 */
                function loadModels() {
                    return ModelService.GetModelsByUsername().then(function (response) {
                        scope.loadedModels = response.data;
                    }, function (response) {
                    });
                }

                /*
                 * Create the JSON representation and save it in the database
                 */
                function saveModel() {
                    saveData();

                    // Distinguishing between saving with button and saving after registration/deployment because of the feedback
                    let savingIndividual = true;
                    if (scope.processing.status) {
                        savingIndividual = false;
                    } else {
                        scope.processing = {};
                        scope.processing.status = true;
                        scope.processing.finished = false;
                    }
                    scope.processing.saved = true;

                    let totalCount = 0;
                    let nodes = [];

                    // Get all nodes from the canvas
                    $(".jtk-node").each(function (index, element) {
                        totalCount++;
                        let $element = $(element);
                        let type = $element.attr('class').toString().split(" ")[1];

                        if (type == "device") {
                            nodes.push({
                                nodeType: type,
                                elementId: $element.attr('id'),
                                clsName: $element.attr('class').toString(),
                                positionX: parseInt($element.css("left"), 10),
                                positionY: parseInt($element.css("top"), 10),
                                width: $element.outerWidth(),
                                height: $element.outerHeight(),
                                angle: $element.data("angle"),
                                id: $element.data("id"),
                                name: $element.data("name"),
                                type: $element.data("type"),
                                mac: $element.data("mac"),
                                ip: $element.data("ip"),
                                username: $element.data("username"),
                                password: $element.data("password"),
                                rsaKey: $element.data("rsaKey"),
                                regError: $element.data("regError")
                            });
                        } else if (type === "actuator" || type === "sensor") {
                            nodes.push({
                                nodeType: type,
                                elementId: $element.attr('id'),
                                clsName: $element.attr('class').toString(),
                                positionX: parseInt($element.css("left"), 10),
                                positionY: parseInt($element.css("top"), 10),
                                width: $element.outerWidth(),
                                height: $element.outerHeight(),
                                angle: $element.data("angle"),
                                id: $element.data("id"),
                                name: $element.data("name"),
                                type: $element.data("type"),
                                adapter: $element.data("adapter"),
                                device: $element.data("device"),
                                deviceId: $element.data("deviceId"),
                                deployed: $element.data("deployed"),
                                regError: $element.data("regError"),
                                depError: $element.data("depError")
                            });
                        } else { // Floorplans
                            nodes.push({
                                nodeType: type,
                                elementId: $element.attr('id'),
                                clsName: $element.attr('class').toString(),
                                positionX: parseInt($element.css("left"), 10),
                                positionY: parseInt($element.css("top"), 10),
                                width: $element.outerWidth(),
                                height: $element.outerHeight(),
                                angle: $element.data("angle")
                            });
                        }
                    });

                    // Get all connections
                    let connections = [];
                    $.each(jsPlumbInstance.getConnections(), function (index, connection) {
                        connections.push({
                            id: connection.id,
                            sourceId: connection.source.id,
                            targetId: connection.target.id,
                            label: connection.getOverlay("label").canvas ? connection.getOverlay("label").canvas.innerText : connection.getOverlay("label").label,
                            labelVisible: connection.getOverlay("label").isVisible()
                        });
                    });

                    // Create the JSON representation
                    let environment = {};
                    environment.nodes = nodes;
                    environment.connections = connections;
                    environment.numberOfElements = totalCount;
                    environment.elementIdCount = elementIdCount;

                    let model = {};
                    model.value = JSON.stringify(environment);
                    model.name = scope.currentModel.name;
                    model.id = scope.currentModel.id;

                    // Save the model in the database
                    return ModelService.SaveModel(model).then(
                        function (response) {
                            if (savingIndividual) {
                                scope.processing.message = "Model saved";
                                scope.processing.success = true;
                                processingTimeout();
                            }
                            scope.currentModel = response.data;
                            scope.selectedOptionName = scope.currentModel.name;
                            loadModels();
                        },
                        function (response) {
                            if (savingIndividual) {
                                scope.processing.message = response.headers('X-MBP-error') ? response.headers('X-MBP-error') : "Model saving error";
                                scope.processing.success = false;
                                processingTimeout();
                            }
                            scope.processing.saved = false;
                        });
                }

                /*
                 * Delete the model from the database considering the registration and deployment
                 */
                function deleteModel() {
                    scope.processing = {};
                    scope.processing.status = true;
                    scope.processing.finished = false;
                    scope.processing.undeployedDeregistered = true;

                    // Undeploy and deregister each node if needed
                    deletionPromises = [];
                    $(".jtk-node").each(function (index, element) {
                        let $element = $(element);
                        let type = $element.attr('class').toString().split(" ")[1];
                        if ($element.data("deployed")) {
                            let promise = undeployComponent(type, $element, false);
                            deletionPromises.push(promise);
                        } else if ($element.data("id")) {
                            let promise = deregisterComponent(type, $element, false);
                            deletionPromises.push(promise);
                        }
                    });

                    // After all deregistrations and undeployments - delete the model
                    $q.all(deletionPromises).then(function () {
                        if (scope.processing.undeployedDeregistered) {
                            ModelService.DeleteModel(scope.currentModel.name).then(function (response) {
                                scope.processing.message = scope.currentModel.name + " deleted";
                                scope.processing.success = true;
                                processingTimeout();
                                newModel();
                            }, function (response) {
                                scope.processing.message = "Deletion error";
                                scope.processing.success = false;
                                processingTimeout();
                            });
                        } else {
                            scope.processing.message = "Undeployment or deregistration error";
                            scope.processing.success = false;
                            processingTimeout();
                        }
                    });
                }

                /*
                 * Remove all elements from canvas
                 */
                function clearCanvas() {
                    jsPlumbInstance.unbind("connectionDetached");
                    jsPlumbInstance.empty("canvas");
                    $timeout(function () {
                        scope.clickedComponent = {};
                    });
                    jsPlumbInstance.bind("connectionDetached", function (info) {
                        onDetach(info);
                    });
                }

                /*
                 * Clear the canvas to make a new model
                 */
                function newModel() {
                    elementIdCount = 0;
                    scope.currentModel = {};
                    clearCanvas();
                    loadModels().then(function (response) {
                        $timeout(function () {
                            scope.selectedOptionName = "";
                            $("#select-models").val("");
                        });
                    });
                }

                /*
                 * Register all components on the canvas
                 */
                function registerComponents() {
                    saveData();

                    scope.processing = {};
                    scope.processing.status = true;
                    scope.processing.registered = true;
                    scope.processing.finished = false;

                    // First register devices
                    let devicePromises = [];
                    $(".jtk-node").each(function (index, element) {
                        let $element = $(element);
                        let type = $element.attr('class').toString().split(" ")[1];

                        if (type === "device" && !$element.data("id")) {
                            let item = {};
                            item.name = $element.data("name");
                            item.componentType = $element.data("type");
                            item.macAddress = DeviceService.normalizeMacAddress($element.data("mac"));
                            item.ipAddress = $element.data("ip");
                            item.username = $element.data("username");
                            item.password = $element.data("password");
                            item.rsaKey = $element.data("rsaKey");
                            let promise = register(type, item, $element);
                            devicePromises.push(promise);
                        }
                    });

                    // After all http requests
                    $q.all(devicePromises).then(function () {
                        // Then register actuators and sensors
                        let actuatorSensorPromises = [];
                        $(".jtk-node").each(function (index, element) {
                            let $element = $(element);
                            let type = $element.attr('class').toString().split(" ")[1];

                            if ((type == "actuator" || type == "sensor") && !$element.data("id")) {
                                let item = {};
                                item.name = $element.data("name");
                                item.componentType = $element.data("type");
                                item.adapter = ENDPOINT_URI + "/adapters/" + $element.data("adapter");
                                item.device = ENDPOINT_URI + "/devices/" + $element.data("deviceId");
                                let promise = register(type, item, $element);
                                actuatorSensorPromises.push(promise);
                            }
                        });

                        // Save the model after all registrations
                        $q.all(actuatorSensorPromises).then(function () {
                            if (devicePromises.length === 0 && actuatorSensorPromises.length === 0) {
                                scope.processing.message = "No components to register";
                                scope.processing.success = false;
                                processingTimeout();
                            } else {
                                saveModel().then(function (response) {
                                    if (scope.processing.registered && scope.processing.saved) {
                                        scope.processing.message = "Registration completed, model saved";
                                        scope.processing.success = true;
                                    } else if (scope.processing.registered && !scope.processing.saved) {
                                        scope.processing.message = "Registration completed, model saving error";
                                        scope.processing.success = false;
                                    } else if (!scope.processing.registered && scope.processing.saved) {
                                        scope.processing.message = "Registration error, model saved";
                                        scope.processing.success = false;
                                    } else if (!scope.processing.registered && !scope.processing.saved) {
                                        scope.processing.message = "Registration error, model saving error";
                                        scope.processing.success = false;
                                    }
                                    processingTimeout();
                                });
                            }
                        });
                    });
                }

                /*
                 * Register a component
                 */
                function register(type, item, element) {
                    return CrudService.addItem(type + "s", item).then(
                        function (response) {
                            element.data("id", response.id);
                            if (type === "device") {
                                // Update the attached sensors and actuators with generated ID of the device, need for their registration
                                updateDeviceSA(element);
                            }
                            element.removeData("regError");
                            element.removeData("depError");
                            element.removeClass("error-element");
                            element.addClass("success-element");
                        },
                        function (response) {
                            console.log(response);
                            if (response.response.data) {
                                element.data("regError", response.response.data.errors[0].message);
                            } else {
                                element.data("regError", response.response.status);
                            }
                            element.removeClass("success-element");
                            element.addClass("error-element");
                            scope.processing.registered = false;
                        });
                }

                /*
                 * Deploy all sensors and actuators on the canvas
                 */
                function deployComponents() {
                    scope.processing = {};
                    scope.processing.status = true;
                    scope.processing.deployed = true;
                    scope.processing.finished = false;

                    // Get and deploy all undeployed sensors and actuators
                    let deployPromises = [];
                    $(".jtk-node").each(function (index, element) {
                        let $element = $(element);
                        let type = $element.attr('class').toString().split(" ")[1];

                        if (type == "actuator" && !$element.data("deployed")) {
                            let promise = deploy(ENDPOINT_URI + "/deploy/actuator/" + $element.data("id"), $element);
                            deployPromises.push(promise);
                        } else if (type == "sensor" && !$element.data("deployed")) {
                            let promise = deploy(ENDPOINT_URI + "/deploy/sensor/" + $element.data("id"), $element);
                            deployPromises.push(promise);
                        }
                    });

                    // Save the model after the deployment
                    $q.all(deployPromises).then(function () {
                        if (deployPromises.length === 0) {
                            scope.processing.message = "No components to deploy";
                            scope.processing.success = false;
                            processingTimeout();
                        } else {
                            saveModel().then(function (response) {
                                if (scope.processing.deployed && scope.processing.saved) {
                                    scope.processing.message = "Deployment completed, model saved";
                                    scope.processing.success = true;
                                } else if (scope.processing.deployed && !scope.processing.saved) {
                                    scope.processing.message = "Deployment completed, model saving error";
                                    scope.processing.success = false;
                                } else if (!scope.processing.deployed && scope.processing.saved) {
                                    scope.processing.message = "Deployment error, model saved";
                                    scope.processing.success = false;
                                } else if (!scope.processing.deployed && !scope.processing.saved) {
                                    scope.processing.message = "Deployment error, model saving error";
                                    scope.processing.success = false;
                                }
                                processingTimeout();
                            });
                        }
                    });
                }

                /*
                 * Deploy a component
                 */
                function deploy(component, element) {
                    let parameterValues = [];
                    return ComponentService.deploy(parameterValues, component).then(
                        function (response) {
                            element.data("deployed", true);
                            element.removeData("regError");
                            element.removeData("depError");
                            element.removeClass("error-element");
                            element.removeClass("success-element");
                            element.addClass("deployed-element");
                        },
                        function (response) {
                            element.data("depError", response.data ? response.data.globalMessage : response.status);
                            element.removeClass("success-element");
                            element.removeClass("deployed-element");
                            element.addClass("error-element");
                            scope.processing.deployed = false;
                        });
                }

                /*
                 * Undeploy all deployed sensors and actuators on the canvas
                 */
                function undeployComponents() {
                    scope.processing = {};
                    scope.processing.status = true;
                    scope.processing.undeployed = true;
                    scope.processing.finished = false;

                    let undeployPromises = [];
                    $(".jtk-node").each(function (index, element) {
                        let $element = $(element);
                        let type = $element.attr('class').toString().split(" ")[1];

                        if (type == "actuator" && $element.data("deployed")) {
                            let promise = undeploy(ENDPOINT_URI + "/deploy/actuator/" + $element.data("id"), $element);
                            undeployPromises.push(promise);
                        } else if (type == "sensor" && $element.data("deployed")) {
                            let promise = undeploy(ENDPOINT_URI + "/deploy/sensor/" + $element.data("id"), $element);
                            undeployPromises.push(promise);
                        }
                    });

                    // save the model after the undeployment
                    $q.all(undeployPromises).then(function () {
                        if (undeployPromises.length === 0) {
                            scope.processing.message = "No components to undeploy";
                            scope.processing.success = false;
                            processingTimeout();
                        } else {
                            saveModel().then(function (response) {
                                if (scope.processing.undeployed && scope.processing.saved) {
                                    scope.processing.message = "Undeployment completed, model saved";
                                    scope.processing.success = true;
                                } else if (scope.processing.undeployed && !scope.processing.saved) {
                                    scope.processing.message = "Undeployment completed, model saving error";
                                    scope.processing.success = false;
                                } else if (!scope.processing.undeployed && scope.processing.saved) {
                                    scope.processing.message = "Undeployment error, model saved";
                                    scope.processing.success = false;
                                } else if (!scope.processing.undeployed && !scope.processing.saved) {
                                    scope.processing.message = "Undeployment error, model saving error";
                                    scope.processing.success = false;
                                }
                                processingTimeout();
                            });
                        }
                    });
                }

                /*
                 * Undeploy a component
                 */
                function undeploy(component, element) {
                    return ComponentService.undeploy(component).then(
                        function (response) {
                            element.data("deployed", false);
                            element.removeData("depError");
                            element.removeData("regError");
                            element.removeClass("error-element");
                            element.removeClass("deployed-element");
                            element.addClass("success-element");
                        },
                        function (response) {
                            element.data("depError", response.data ? response.data.globalMessage : response.status);
                            element.removeClass("success-element");
                            element.removeClass("deployed-element");
                            element.addClass("error-element");
                            scope.processing.undeployed = false;
                        });
                }

                /*
                 * Defines how long the feedback message is displayed in the tool
                 */
                function processingTimeout() {
                    scope.processing.status = false;
                    scope.processing.finished = true;
                    $timeout(function () {
                        scope.processing.finished = false;
                    }, 3000);
                }
            }


            /**
             * Linking function, glue code
             *
             * @param scope Scope of the directive
             * @param element Elements of the directive
             * @param attrs Attributes of the directive
             */
            let link = function (scope, element, attrs) {

                //Expose public API
                scope.api = {};

                //Initialize modelling tool
                let initFunction = initJSPlumb.bind(this, scope);
                jsPlumb.ready(initFunction);
            };

//Configure and expose the directive
            return {
                restrict: 'E', //Elements only
                template:
                    '<div id="modelingToolView">' +
                    '<!-- Palette -->' +
                    '<div id="toolPalette" style="display: inline-block; vertical-align: top; width:20%; height: 100%">' +
                    '<div class="panel-group" id="accordion">' +
                    '<div class="panel panel-default">' +
                    '<div class="panel-heading" style="overflow-x: hidden;">' +
                    '<h4 class="panel-title">' +
                    '<a data-toggle="collapse" onclick="event.preventDefault();" data-parent="#accordion" href="#collapseFloorplans">' +
                    '<i class="material-icons" style="font-size: 20px;">weekend</i>' +
                    '<span class="glyphicon glyphicon-chevron-down pull-right"></span>Floorplans</a>' +
                    '</h4>' +
                    '</div>' +
                    '<div id="collapseFloorplans" class="panel-collapse collapse in">' +
                    '<div class="panel-body canvas-wide modeling-tool canvasPalette">' +
                    '<ul class="dragList">' +
                    '<li>' +
                    '<div class="window floorplan room-floorplan" id="roomFloorplan"></div>' +
                    '<p><strong>Room</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window floorplan wall-floorplan" id="wallFloorplan"></div>' +
                    '<p><strong>Wall</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window floorplan door-floorplan" id="doorFloorplan"></div>' +
                    '<p><strong>Door</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window floorplan window-floorplan" id="windowFloorplan"></div>' +
                    '<p><strong>Window</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window floorplan stairs-floorplan" id="stairsFloorplan"></div>' +
                    '<p><strong>Stairs</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window floorplan table-floorplan" id="tableFloorplan"></div>' +
                    '<p><strong>Table</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window floorplan chair-floorplan" id="chairFloorplan"></div>' +
                    '<p><strong>Chair</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window floorplan couch-floorplan" id="couchFloorplan"></div>' +
                    '<p><strong>Couch</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window floorplan bed-floorplan" id="bedFloorplan"></div>' +
                    '<p><strong>Bed</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window floorplan kitchen-sink-floorplan" id="kitchenSinkFloorplan"></div>' +
                    '<p><strong>Kitchen sink</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window floorplan bathtub-floorplan" id="bathtubFloorplan"></div>' +
                    '<p><strong>Bathtub</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window floorplan bath-sink-floorplan" id="bathSinkFloorplan"></div>' +
                    '<p><strong>Bath sink</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window floorplan toilet-floorplan" id="toiletFloorplan"></div>' +
                    '<p><strong>Toilet</strong></p>' +
                    '</li>' +
                    '</ul>' +
                    '</div>' +
                    '</div>' +
                    '</div>' +
                    '<div class="panel panel-default">' +
                    '<div class="panel-heading" style="overflow-x: hidden;">' +
                    '<h4 class="panel-title">' +
                    '<a data-toggle="collapse" onclick="event.preventDefault();" data-parent="#accordion" href="#collapseDevices">' +
                    '<i class="material-icons" style="font-size: 20px;">devices</i>' +
                    '<span class="glyphicon glyphicon-chevron-down pull-right"></span>Device types</a>' +
                    '</h4>' +
                    '</div>' +
                    '<div id="collapseDevices" class="panel-collapse collapse">' +
                    '<div class="panel-body canvas-wide modeling-tool canvasPalette">' +
                    '<ul class="dragList">' +
                    '<li>' +
                    '<div class="window device raspberry-pi-device" id="raspberryPiDevice"></div>' +
                    '<p><strong>Raspberry Pi</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window device arduino-device" id="arduinoDevice"></div>' +
                    '<p><strong>Arduino</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window device computer-device" id="computerDevice"></div>' +
                    '<p><strong>Computer</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window device laptop-device" id="laptopDevice"></div>' +
                    '<p><strong>Laptop</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window device tv-device" id="tvDevice"></div>' +
                    '<p><strong>TV</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window device smartphone-device" id="smartphoneDevice"></div>' +
                    '<p><strong>Smartphone</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window device smartwatch-device" id="smartwatchDevice"></div>' +
                    '<p><strong>Smartwatch</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window device audio-system-device" id="audioSystemDevice"></div>' +
                    '<p><strong>Audio System</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window device voice-controller-device" id="voiceControllerDevice"></div>' +
                    '<p><strong>Voice Controller</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window device camera-device" id="cameraDevice"></div>' +
                    '<p><strong>Camera</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window device default-device" id="defaultDevice"></div>' +
                    '<p><strong>Default device</strong></p>' +
                    '</li>' +
                    '</ul>' +
                    '</div>' +
                    '</div>' +
                    '</div>' +
                    '<div class="panel panel-default">' +
                    '<div class="panel-heading" style="overflow-x: hidden;">' +
                    '<h4 class="panel-title">' +
                    '<a data-toggle="collapse" onclick="event.preventDefault();" data-parent="#accordion" href="#collapseActuators">' +
                    '<i class="material-icons" style="font-size: 20px;">wb_incandescent</i>' +
                    '<span class="glyphicon glyphicon-chevron-down pull-right"></span>Actuator types</a>' +
                    '</h4>' +
                    '</div>' +
                    '<div id="collapseActuators" class="panel-collapse collapse">' +
                    '<div class="panel-body canvas-wide modeling-tool canvasPalette">' +
                    '<ul class="dragList">' +
                    '<li>' +
                    '<div class="window actuator light-actuator" id="lightActuator"></div>' +
                    '<p><strong>Light</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window actuator led-actuator" id="ledActuator"></div>' +
                    '<p><strong>LED</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window actuator speaker-actuator" id="speakerActuator"></div>' +
                    '<p><strong>Speaker</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window actuator buzzer-actuator" id="buzzerActuator"></div>' +
                    '<p><strong>Buzzer</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window actuator vibration-actuator" id="vibrationActuator"></div>' +
                    '<p><strong>Vibration</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window actuator heater-actuator" id="heaterActuator"></div>' +
                    '<p><strong>Heater</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window actuator air-conditioner-actuator" id="airConditionerActuator"></div>' +
                    '<p><strong>Air Conditioner</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window actuator switch-actuator" id="switchActuator"></div>' +
                    '<p><strong>Switch</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window actuator motor-actuator" id="motorActuator"></div>' +
                    '<p><strong>Motor</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window actuator default-actuator" id="defaultActuator"></div>' +
                    '<p><strong>Default actuator</strong></p>' +
                    '</li>' +
                    '</ul>' +
                    '</div>' +
                    '</div>' +
                    '</div>' +
                    '<div class="panel panel-default">' +
                    '<div class="panel-heading" style="overflow-x: hidden;">' +
                    '<h4 class="panel-title">' +
                    '<a data-toggle="collapse" onclick="event.preventDefault();" data-parent="#accordion" href="#collapseSensors">' +
                    '<i class="material-icons" style="font-size: 20px;">settings_remote</i>' +
                    '<span class="glyphicon glyphicon-chevron-down pull-right"></span>Sensor types</a>' +
                    '</h4>' +
                    '</div>' +
                    '<div id="collapseSensors" class="panel-collapse collapse">' +
                    '<div class="panel-body canvas-wide modeling-tool canvasPalette">' +
                    '<ul class="dragList">' +
                    '<li>' +
                    '<div class="window sensor camera-sensor" id="cameraSensor"></div>' +
                    '<p><strong>Camera</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window sensor sound-sensor" id="soundSensor"></div>' +
                    '<p><strong>Sound</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window sensor temperature-sensor" id="temperatureSensor"></div>' +
                    '<p><strong>Temperature</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window sensor humidity-sensor" id="humiditySensor"></div>' +
                    '<p><strong>Humidity</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window sensor gas-sensor" id="gasSensor"></div>' +
                    '<p><strong>Gas</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window sensor light-sensor" id="lightSensor"></div>' +
                    '<p><strong>Light</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window sensor motion-sensor" id="motionSensor"></div>' +
                    '<p><strong>Motion</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window sensor location-sensor" id="locationSensor"></div>' +
                    '<p><strong>Location</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window sensor gyroscope-sensor" id="gyroscopeSensor"></div>' +
                    '<p><strong>Gyroscope</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window sensor proximity-sensor" id="proximitySensor"></div>' +
                    '<p><strong>Proximity</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window sensor touch-sensor" id="touchSensor"></div>' +
                    '<p><strong>Touch</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window sensor vibration-sensor" id="vibrationSensor"></div>' +
                    '<p><strong>Vibration</strong></p>' +
                    '</li>' +
                    '<li>' +
                    '<div class="window sensor default-sensor" id="defaultSensor"></div>' +
                    '<p><strong>Default sensor</strong></p>' +
                    '</li>' +
                    '</ul>' +
                    '</div>' +
                    '</div>' +
                    '</div>' +
                    '</div>' +
                    '</div>' +
                    '<!-- Canvas - Modeling Area -->' +
                    '<div id="myDiagram" style="display: inline-block; vertical-align: top; width:64%; height: 100%">' +
                    '<div class="jtk-main">' +
                    '<div class="jtk-canvas canvas-wide modeling-tool jtk-surface jtk-surface-nopan" id="canvas"></div>' +
                    '</div>' +
                    '</div>' +
                    '<!-- Info sidebar -->' +
                    '<div id="infoSidebar" style="display: inline-block; vertical-align: top; width:17%; height: 100%">' +
                    '<div class="panel panel-default" style="margin: 5px;" ng-show="clickedComponent.category == \'DEVICE\'">' +
                    '<div class="panel-heading" style="text-align: center; overflow-x: hidden;">' +
                    '<h4 class="panel-title">Device</h4>' +
                    '</div>' +
                    '<div id="deviceInfo" class="input-list">' +
                    '<ul>' +
                    '<li>' +
                    '<label for="deviceNameInput">Name</label>' +
                    '<input id="deviceNameInput" type="text" name="name" ng-model="clickedComponent.name" autocomplete="off">' +
                    '<!-- <span>Enter your full name here</span> -->' +
                    '</li>' +
                    '<li>' +
                    '<label for="deviceTypeInput">Device type</label>' +
                    '<input id="deviceTypeInput" type="text" name="type" ng-model="clickedComponent.type" autocomplete="off">' +
                    '</li>' +
                    '<li>' +
                    '<label for="deviceMacInput">MAC address</label>' +
                    '<input id="deviceMacInput" type="text" name="mac" ng-model="clickedComponent.mac" autocomplete="off" placeholder="HH-HH-HH-HH-HH-HH">' +
                    '</li>' +
                    '<li>' +
                    '<label for="deviceIpInput">IP address</label>' +
                    '<input id="deviceIpInput" type="text" name="ip" ng-model="clickedComponent.ip" autocomplete="off">' +
                    '</li>' +
                    '<li>' +
                    '<label for="deviceUsernameInput">User name</label>' +
                    '<input id="deviceUsernameInput" type="text" name="username" ng-model="clickedComponent.username" autocomplete="off">' +
                    '</li>' +
                    '<li>' +
                    '<label for="devicePasswordInput">Password</label>' +
                    '<input id="devicePasswordInput" type="password" name="password" ng-model="clickedComponent.password" autocomplete="off">' +
                    '</li>' +
                    '<li>' +
                    '<label for="deviceRsaKeyInput">RSA Key</label>' +
                    '<textarea id="deviceRsaKeyInput" type="text" name="rsaKey" placeholder="Private RSA key" ng-model="clickedComponent.rsaKey" rows="4"></textarea>' +
                    '</li>' +
                    '</ul>' +
                    '</div>' +
                    '<div class="panel-footer" style="height: 100%; overflow-x: scroll">' +
                    '<span>' +
                    '<b>Status</b>' +
                    '</span>' +
                    '<br>' +
                    '<span>' +
                    '<i class="fas fa-check-circle" ng-show="clickedComponent.id"></i>' +
                    '<i class="fas fa-times-circle" ng-show="!clickedComponent.id"></i>' +
                    'Registered' +
                    '<span style="color: red; font-size: 12px" ng-show="clickedComponent.regError">' +
                    '<br>' +
                    '{{clickedComponent.regError}}' +
                    '</span>' +
                    '</span>' +
                    '</div>' +
                    '</div>' +
                    '<div class="panel panel-default" style="margin: 5px;" ng-show="clickedComponent.category == \'ACTUATOR\'">' +
                    '<div class="panel-heading" style="text-align: center;overflow-x: hidden;">' +
                    '<h4 class="panel-title">Actuator</h4>' +
                    '</div>' +
                    '<div id="actuatorInfo" class="input-list">' +
                    '<ul>' +
                    '<li>' +
                    '<label for="actuatorNameInput">Name</label>' +
                    '<input id="actuatorNameInput" type="text" name="name" ng-model="clickedComponent.name" autocomplete="off">' +
                    '</li>' +
                    '<li>' +
                    '<label for="actuatorTypeInput">Type</label>' +
                    '<input id="actuatorTypeInput" type="text" name="type" ng-model="clickedComponent.type" autocomplete="off">' +
                    '</li>' +
                    '<li>' +
                    '<label for="actuatorAdapterInput">Adapter</label>' +
                    '<select class="form-control show-tick" id="actuatorAdapterInput" ng-model="clickedComponent.adapter" ng-options="t.id as (t.name) for t in adapterListCtrl.items">' +
                    '<option value="">Select adapter</option>' +
                    '</select>' +
                    '</li>' +
                    '<li>' +
                    '<label for="actuatorDeviceInput">Device</label>' +
                    '<input id="actuatorDeviceInput" type="text" name="device" ng-model="clickedComponent.device" autocomplete="off" disabled="disabled">' +
                    '</li>' +
                    '</ul>' +
                    '</div>' +
                    '<div class="panel-footer" style="height: 100%; overflow-x: scroll">' +
                    '<span>' +
                    '<b>Status</b>' +
                    '</span>' +
                    '<br>' +
                    '<span>' +
                    '<i class="fas fa-check-circle" ng-show="clickedComponent.id"></i>' +
                    '<i class="fas fa-times-circle" ng-show="!clickedComponent.id"></i>' +
                    'Registered' +
                    '<span style="color: red; font-size: 12px" ng-show="clickedComponent.regError">' +
                    '<br>' +
                    '{{clickedComponent.regError}}' +
                    '</span>' +
                    '</span>' +
                    '<br>' +
                    '<span>' +
                    '<i class="fas fa-check-circle" ng-show="clickedComponent.deployed"></i>' +
                    '<i class="fas fa-times-circle" ng-show="!clickedComponent.deployed"></i>' +
                    'Deployed' +
                    '<span style="color: red; font-size: 12px" ng-show="clickedComponent.depError">' +
                    '<br>' +
                    '{{clickedComponent.depError}}' +
                    '</span>' +
                    '</span>' +
                    '</div>' +
                    '</div>' +
                    '<div class="panel panel-default" style="margin: 5px;" ng-show="clickedComponent.category == \'SENSOR\'">' +
                    '<div class="panel-heading" style="text-align: center;overflow-x: hidden;">' +
                    '<h4 class="panel-title">Sensor</h4>' +
                    '</div>' +
                    '<div id="sensorInfo" class="input-list">' +
                    '<ul>' +
                    '<li>' +
                    '<label for="sensorNameInput">Name</label>' +
                    '<input id="sensorNameInput" type="text" name="name" ng-model="clickedComponent.name" autocomplete="off">' +
                    '</li>' +
                    '<li>' +
                    '<label for="sensorTypeInput">Type</label>' +
                    '<input id="sensorTypeInput" type="text" name="type" ng-model="clickedComponent.type" autocomplete="off">' +
                    '</li>' +
                    '<li>' +
                    '<label for="sensorAdapterInput">Adapter</label>' +
                    '<select class="form-control show-tick" id="sensorAdapterInput" ng-model="clickedComponent.adapter" ng-options="t.id as (t.name) for t in adapterListCtrl.items">' +
                    '<option value="">Select adapter</option>' +
                    '</select>' +
                    '</li>' +
                    '<li>' +
                    '<label for="sensorDeviceInput">Device</label>' +
                    '<input id="sensorDeviceInput" type="text" name="device" ng-model="clickedComponent.device" autocomplete="off" disabled="disabled">' +
                    '</li>' +
                    '</ul>' +
                    '</div>' +
                    '<div class="panel-footer" style="height: 100%; overflow-x: scroll">' +
                    '<span>' +
                    '<b>Status</b>' +
                    '</span>' +
                    '<br>' +
                    '<span>' +
                    '<i class="fas fa-check-circle" ng-show="clickedComponent.id"></i>' +
                    '<i class="fas fa-times-circle" ng-show="!clickedComponent.id"></i>' +
                    'Registered' +
                    '<span style="color: red; font-size: 12px" ng-show="clickedComponent.regError">' +
                    '<br>' +
                    '{{clickedComponent.regError}}' +
                    '</span>' +
                    '</span>' +
                    '<br>' +
                    '<span>' +
                    '<i class="fas fa-check-circle" ng-show="clickedComponent.deployed"></i>' +
                    '<i class="fas fa-times-circle" ng-show="!clickedComponent.deployed"></i>' +
                    'Deployed' +
                    '<span style="color: red; font-size: 12px" ng-show="clickedComponent.depError">' +
                    '<br>' +
                    '{{clickedComponent.depError}}' +
                    '</span>' +
                    '</span>' +
                    '</div>' +
                    '</div>' +
                    '</div>' +
                    '</div>'
                ,
                link: link,
                scope: {
                    //Public API
                    api: "=api"

                    /*
                    //The unit in which the statistics are supposed to be displayed
                    unit: '@unit',
                    //Functions that are called when the chart loads/finishes loading data
                    loadingStart: '&loadingStart',
                    loadingFinish: '&loadingFinish',
                    //Function for updating the value stats data
                    getStats: '&getStats'*/
                }
            };
        }]
)
;