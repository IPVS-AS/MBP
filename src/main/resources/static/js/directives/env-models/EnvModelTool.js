/* global app */

'use strict';

/**
 * Directive for a modeling tool that can be used for creating and editing IoT environment models.
 */
app.directive('envModelTool',
    ['ENDPOINT_URI', '$timeout', '$q', '$controller', '$compile',
        function (ENDPOINT_URI, $timeout, $q, $controller, $compile) {

            //Class for elements of the palette
            const PaletteElement = function (name, freeResize) {
                this.name = name;
                this.freeResize = (typeof freeResize === 'undefined') ? false : freeResize;
            };

            //Default floorplan elements to offer in the palette
            const FLOORPLAN_ELEMENTS = [new PaletteElement('Room', true),
                new PaletteElement('Wall', true),
                new PaletteElement('Door'),
                new PaletteElement('Window'),
                new PaletteElement('Stairs'),
                new PaletteElement('Table'),
                new PaletteElement('Chair'),
                new PaletteElement('Couch'),
                new PaletteElement('Bed'),
                new PaletteElement('Kitchen sink'),
                new PaletteElement('Bathtub'),
                new PaletteElement('Bath sink'),
                new PaletteElement('Toilet')];

            function initialize(scope, elements) {
                const DIAGRAM_CONTAINER = $("#toolCanvasContainer", elements);
                const CANVAS = $("#canvas", elements);
                const DEVICE_DETAILS_MODAL = $("#deviceDetailsModal", elements);
                const COMPONENT_DETAILS_MODAL = $("#componentDetailsModal", elements);
                const EXPORT_MODAL = $("#exportModelModal", elements);
                const IMPORT_MODAL = $("#importModelModal", elements);
                const PALETTE_FLOORPLANS = $(".floorplan-palette", elements);
                const PALETTE_DEVICES = $(".device-palette", elements);
                const PALETTE_ACTUATORS = $(".actuator-palette", elements);
                const PALETTE_SENSORS = $(".sensor-palette", elements);
                let markingRectContainer = $(null);
                let markingRect = $(null);
                let jsPlumbInstance = null;
                let sourceEndpoint = null; //Look-and-feel of source endpoints
                let targetEndpoint = null; //Look-and-feel of target endpoints
                let elementIdCount = 0; //Used for canvas ID uniqueness
                let createElementProperties = {}; //Properties of elements that are supposed to be created
                let currentDetailsElement = $(null); //The element for which the details modal is opened
                let isMoving = false; //Remembers if the user is currently moving or modifying an element
                let markingRectPos = { //Remembers the position of the marking rect
                    x: 0,
                    y: 0
                };
                //Possible states of nodes
                const NODE_STATES = ['registered', 'deployed', 'started', 'error'];
                //Undo/Redo Manager
                const UNDO_MANAGER = new JSUndoManager({
                    limit: 50,
                    debug: true
                });
                let isUnReDoing = false; //Remembers if there is currently a undo/redo action processed
                let lastModelState = ""; //Remembers the last state of the model
                let copyClipboard = []; //Clipboard for copied elements

                //Class for data of the device details modal
                const DeviceDetails = function () {
                    this.name = '';
                    this.ipAddress = '';
                    this.username = '';
                    this.password = '';
                    this.rsaKey = '';
                };

                //Class for data of the actuator/sensor details modal
                const ComponentDetails = function () {
                    this.name = '';
                    this.operator = null;
                };

                /*
                Expose functions and fields for template
                 */
                scope.clickedComponent = {};
                scope.modalElementDetails = new DeviceDetails(); //Field values of the element details modals
                scope.saveElementDetails = saveElementDetails;
                scope.showExportModelMessage = false;
                scope.copyExportModelToClipboard = copyExportModelToClipboard;
                scope.importModelErrorMessage = false;
                scope.importModelRequest = importModelRequest;

                /*
                Build API that is available for the outside
                 */
                scope.api.getModelJSON = getModelJSON;
                scope.api.loadModel = loadModel;
                scope.api.loadEmptyModel = loadEmptyModel;
                scope.api.undo = undoAction;
                scope.api.redo = redoAction;
                scope.api.openDetails = openMarkedElementDetails;
                scope.api.copy = copyFocusedElements;
                scope.api.cut = cutFocusedElements;
                scope.api.paste = pasteElements;
                scope.api.delete = deleteFocusedElements;
                scope.api.export = exportModel;
                scope.api.import = importModel;
                scope.api.updateNodeState = updateNodeState;
                scope.api.displayComponentValue = displayComponentValue;

                /*
                Initialization
                 */
                (function initController() {
                    initPalette();
                    initCanvas();
                    initJSPlumb();
                    initMarkingRect();

                    //Save empty model state
                    lastModelState = exportToJSON();
                })();

                /**
                 * Initializes the element palette.
                 */
                function initPalette() {

                    /**
                     * Adds a list of component elements of a certain type to a given palette.
                     * @param elements The array of elements to add
                     * @param palette The palette to which the elements are supposed to be added
                     * @param className A CSS class name representing the component type
                     */
                    function addElementsToPalette(elements, palette, className) {

                        $.each(elements, function (index, elementObject) {
                            //Create DOM element
                            let element = $('<div>').addClass('window').addClass(className);

                            //Convert the element name to a CSS class with suffix
                            let elementClassName = convertNameToClass(elementObject.name) + '-' + className;
                            element.addClass(elementClassName);

                            //Check if element specifies free resizability
                            if (((typeof elementObject.freeResize) !== 'undefined') && elementObject.freeResize) {
                                element.addClass('free-resize');
                            }

                            //Add icon if existing
                            if (elementObject.hasOwnProperty("icon")) {
                                element.css('background-image', 'url("' + elementObject.icon.content + '")')
                            }

                            //Create list item
                            let listItem = $('<li>');
                            listItem.append(element);
                            listItem.append($('<p>').append($('<strong>').text(elementObject.name)));

                            //Add element to palette
                            palette.append(listItem);

                            //Make element draggable
                            makePaletteElementDraggable(element, elementObject.name);
                        });
                    }

                    //Add floorplan types to palette
                    addElementsToPalette(FLOORPLAN_ELEMENTS, PALETTE_FLOORPLANS, 'floorplan');

                    //Add device types to palette
                    addElementsToPalette(scope.deviceTypes, PALETTE_DEVICES, 'device');

                    //Add actuator types to palette
                    addElementsToPalette(scope.actuatorTypes, PALETTE_ACTUATORS, 'actuator');

                    //Add sensor types to palette
                    addElementsToPalette(scope.sensorTypes, PALETTE_SENSORS, 'sensor');
                }

                /**
                 * Initializes the canvas.
                 */
                function initCanvas() {
                    //Make the canvas droppable
                    CANVAS.droppable({
                        accept: ".window",
                        drop: function (event, ui) {
                            //Get helper element
                            let helperElement = $(this);

                            //Get and store the drop position
                            createElementProperties.left = ui.offset.left - helperElement.offset().left;
                            createElementProperties.top = ui.offset.top - helperElement.offset().top;

                            //Generate element ID
                            elementIdCount++;
                            let id = "node" + elementIdCount;

                            //Create and draw the element in the canvas
                            let element = createElement(id);
                            drawElement(element);

                            //Write model to history stack if its not a room (because of its animation)
                            if (!element.hasClass("room-floorplan")) {
                                //Indicate that the model has changed
                                onModelChanged();
                            }
                        }
                    });
                }

                /**
                 * Initializes the JSPlumb library on the canvas amd defines relevant types and the
                 * look-and-fell of nodes and connections.
                 */
                function initJSPlumb() {
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

                    sourceEndpoint = {
                        filter: ".ep",
                        anchor: "Continuous",
                        connectorStyle: {
                            stroke: "#000000",
                            strokeWidth: 2
                        },
                        connectionType: "basic"
                    };

                    targetEndpoint = {
                        dropOptions: {
                            hoverClass: "dragHover"
                        },
                        anchor: "Continuous",
                        allowLoopback: false
                    };
                }

                /**
                 * Initializes the marking rectangle.
                 */
                function initMarkingRect() {
                    //Create marking rect elements
                    markingRect = $('<div class="markingRect">');
                    markingRectContainer = $('<div class="markingRectContainer">').hide();

                    //Append elements to canvas
                    CANVAS.append(markingRectContainer.append(markingRect));
                }

                /**
                 * Makes an palette element draggable and passes the required element data to the
                 * createElementProperties object when dragging begins.
                 * @param element The element that is supposed to become draggable
                 * @param type The type of the element to pass
                 */
                function makePaletteElementDraggable(element, type) {
                    element.draggable({
                        start: (event, ui) => {
                            createElementProperties.classes = element.attr('class');
                            createElementProperties.background = element.css('background-image');
                            createElementProperties.type = type;
                        },
                        helper: () => {
                            //Create helper element
                            let helper = $('<div/>').attr("class", element.attr('class'));

                            //Add background image if necessary
                            helper.css('background-image', element.css('background-image'));

                            return helper;
                        },
                        revert: false
                    });
                }

                /**
                 * Makes the given element rotatable. Optionally, a start angle may be passed.
                 * @param element The element that is supposed to become rotatable
                 * @param startAngle The optional start angle of the rotation in radians
                 */
                function makeRotatable(element, startAngle) {
                    //Check if rotatability may be just enabled again
                    if (element.has(".ui-rotatable-handle").length) {
                        element.rotatable("enable").find('.ui-rotatable-handle').show();
                        return;
                    }

                    //Sanity check for angle
                    if (typeof startAngle === 'undefined') {
                        startAngle = 0;
                    }

                    let rotateOptions = {
                        handleOffset: {'top': 5, 'left': 5},
                        rotate: function (el) {
                            jsPlumbInstance.revalidate(el);
                        },
                        start: () => (isMoving = true),
                        stop: function (el, event) {

                            //Normalize final rotation angle
                            let finalAngle = normalizeRadians(event.angle.stop);

                            //Save rotation angle
                            element.data("angle", finalAngle);

                            //Unset moving flag
                            $timeout(() => (isMoving = false), 200);

                            //Indicate that the model has changed
                            onModelChanged();
                        },
                        radians: startAngle,
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
                        start: () => (isMoving = true),
                        stop: function (el, event) {
                            //Unset moving flag
                            $timeout(() => (isMoving = false), 200);

                            //Indicate that the model has changed
                            onModelChanged();
                        },
                        handles: "all"
                    });
                }

                /**
                 * Converts a name (e.g. the name of a component type) to a valid CSS class name.
                 * @param name The name to convert
                 */
                function convertNameToClass(name) {
                    //Replace whitespaces with hyphens
                    name = name.replace(/\s+/g, "-");

                    //Remove non-alphabetical and non-hyphen characters
                    name = name.replace(/[^A-Za-z-]/g, "");

                    //Convert to lower case
                    name = name.toLowerCase();

                    return name;
                }

                /**
                 * Normalizes an angle in radians to an element of [0, 2 * PI[
                 * @param radians
                 * @returns The normalized radian angle
                 */
                function normalizeRadians(radians) {
                    //Increase by 2 * PI if necessary
                    while (radians < 0) {
                        radians += 2 * Math.PI;
                    }

                    //Decrease by 2 * PI if necessary
                    while (radians >= 2 * Math.PI) {
                        radians -= 2 * Math.PI;
                    }

                    return radians;
                }

                /**
                 * Creates an element that is supposed to be drawn to the canvas with a certain ID.
                 * The createElementProperties object is used in order to get the required classes, the element
                 * type and the element position on the canvas.
                 *
                 * @param id The ID of the element
                 * @returns {jQuery}
                 */
                function createElement(id) {
                    //Create element
                    let element = $('<div>').attr('id', id)
                        .addClass(createElementProperties.classes)
                        .addClass('jtk-node');

                    // The position to create the dropped element
                    element.css({
                        'top': createElementProperties.top,
                        'left': createElementProperties.left,
                        'width': '50px',
                        'height': '50px',
                        'background-image': createElementProperties.background
                    });

                    // Increase the size of room
                    if (element.hasClass("room-floorplan")) {
                        element.animate({
                            width: '250px',
                            height: '250px'
                        }, 1000, function () {
                            //Save state with final size and indicate that the model has changed
                            onModelChanged();
                        });
                    }

                    if (createElementProperties.type) {
                        element.data("type", createElementProperties.type);
                    }

                    //Add connection endpoint and status indicator if required
                    enrichElement(element);

                    //Remove focus from all elements
                    clearFocus();

                    //Put focus on the new element
                    focusElement(element);

                    return element;
                }


                /**
                 * Create an element from a given node to be drawn on the canvas with a certain id.
                 */
                function createElementFromNode(id, node) {
                    //Create element
                    let element = $('<div>').attr('id', id).addClass(node.clsName);

                    //Set rotation angle if available
                    if ((typeof node.angle !== "undefined") && (node.angle > 0)) {
                        setAngle(element, node.angle);
                    }

                    //The position to create the element
                    element.css({
                        'left': node.left + 'px',
                        'top': node.top + 'px',
                        'width': node.width + 'px',
                        'height': node.height + 'px'
                    });

                    //Set background if available
                    if (node.hasOwnProperty("background")) {
                        element.css('background-image', node.background);
                    }

                    //Append the data to the element
                    element.data(node);

                    //Add connection endpoint and status indicator if required
                    enrichElement(element);

                    return element;
                }

                /**
                 * Enriches a given DOM element for a status indicator and an connection endpoint, if necessary.
                 * @param element The DOM element to enrich
                 */
                function enrichElement(element) {
                    //Check if element requires a status indicator
                    if (!(element.hasClass('device') ||
                        element.hasClass('actuator') ||
                        element.hasClass('sensor'))) {
                        return;
                    }

                    //Create status indicator
                    let statusIndicator = $('<div>').addClass('status-indicator');

                    //Append indicator
                    element.append(statusIndicator);

                    //Check if entity is a device
                    if (!element.hasClass("device")) {
                        return;
                    }

                    //Create connection endpoint
                    let endpoint = $('<div>').addClass('ep');

                    //Append endpoint
                    element.append(endpoint);
                }

                /*
                 * Draw/append the element on the canvas
                 */
                function drawElement($element) {
                    CANVAS.append($element);
                    // Make the element on the canvas draggable
                    jsPlumbInstance.draggable(jsPlumbInstance.getSelector(".jtk-node"), {
                        filter: ".ui-resizable-handle",
                        start: function (event) {
                            //Remove focus from all events
                            clearFocus();

                            //Put focus on dragged element
                            focusElement($element);
                        },
                        stop: function (event) {
                            //Indicate that the model has changed
                            onModelChanged();
                        }
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

                // In case a key is pressed
                $(document).on("keydown", function (event) {
                    //Check for pressed key
                    switch (event.which) {
                        //DEL key
                        case 46:
                            deleteFocusedElements();
                            break;
                        //Arrow left key
                        case 37:
                            moveFocusedElements("left", 3);
                            break;
                        //Arrow up key
                        case 38:
                            moveFocusedElements("up", 3);
                            break;
                        //Arrow right key
                        case 39:
                            moveFocusedElements("right", 3);
                            break;
                        //Arrow down key
                        case 40:
                            moveFocusedElements("down", 3);
                            break;
                        default:
                            return;
                    }

                    //Key event was processed, so prevent default behaviour
                    event.preventDefault();
                });

                // Events of the canvas
                CANVAS.on('click', function (e) {
                    //Ensure that user is not currently moving an element
                    if (isMoving) {
                        return;
                    }

                    //Get element
                    let element = $(event.target).filter('.jtk-node');

                    //Sanity check
                    if (!element.length) {
                        return;
                    }

                    //Remove focus from all elements
                    clearFocus();

                    //Put focus on the clicked element
                    focusElement(element);
                }).on('mousedown', function (e) {
                    //Make sure canvas is the actual target
                    if (!$(e.target).hasClass("jtk-canvas")) {
                        return;
                    }

                    //Save starting position of the marking rect
                    markingRectPos.x = e.offsetX;
                    markingRectPos.y = e.offsetY;

                    markingRect.css({
                        left: e.offsetX + 'px',
                        top: e.offsetY + 'px'
                    }).width(0).height(0);

                    //Display marking rect
                    markingRectContainer.css('display', '');

                }).on('mousemove', function (e) {
                    //Abort if marking rect is not visible
                    if (!markingRectContainer.is(":visible")) {
                        return;
                    }

                    //Do not mark if an element is currently moved
                    if (isMoving) {
                        markingRectContainer.hide();
                        return;
                    }

                    let posX = Math.min(markingRectPos.x, e.offsetX);
                    let posY = Math.min(markingRectPos.y, e.offsetY);
                    let width = Math.abs(markingRectPos.x - e.offsetX);
                    let height = Math.abs(markingRectPos.y - e.offsetY);

                    markingRect.css({
                        left: posX + 'px',
                        top: posY + 'px',
                        width: width + 'px',
                        height: height + 'px'
                    });
                });
                $('body').on('mouseup', function (event) {
                    //Abort if marking rect is not visible
                    if (!markingRectContainer.is(":visible")) {
                        return;
                    }

                    //Abort if user is currently moving an element
                    if (isMoving) {
                        markingRectContainer.hide();
                        return;
                    }

                    //Get final rect dimensions
                    let markPosition = markingRect.position();
                    let markWidth = markingRect.width();
                    let markHeight = markingRect.height();

                    //Remove focus from all elements
                    clearFocus();

                    //Get all selectable nodes
                    $('.jtk-node').each(function () {
                        let node = $(this);
                        let nodePosition = node.position();
                        let nodeWidth = node.width();
                        let nodeHeight = node.height();

                        //Check if node is within the mark
                        if ((markPosition.left < nodePosition.left)
                            && (markPosition.top < nodePosition.top)
                            && ((markPosition.left + markWidth) > (nodePosition.left + nodeWidth))
                            && ((markPosition.top + markHeight) > (nodePosition.top + nodeHeight))) {
                            //Put focus on node
                            focusElement(node);
                        }
                    });

                    //Hide marking rect
                    markingRectContainer.hide();
                });


                /*
                 * Rotates a given element by a certain angle while initializing its rotatability.
                 * @param element The element to rotate
                 * @param angle The angle in radians for which to rotate the element
                 */
                function setAngle(element, angle) {
                    makeRotatable(element, angle);
                    element.rotatable("disable")
                        .find('.ui-rotatable-handle')
                        .hide();
                }

                /**
                 * Moves all currently focused elements in a certain direction for a given number of pixels.
                 * @param direction The direction (left, up, right, down) in which to move the elements:
                 * @param pixels The absolute number of pixels to move the element
                 */
                function moveFocusedElements(direction, pixels) {
                    //Sanity check for pixels
                    pixels = Math.abs(pixels || 0);

                    //Remember move distance for each direction
                    let moveX = "+=0";
                    let moveY = "+=0";

                    switch (direction) {
                        case "left":
                            moveX = "-=" + pixels;
                            break;
                        case "up":
                            moveY = "-=" + pixels;
                            break;
                        case "right":
                            moveX = "+=" + pixels;
                            break;
                        case "down":
                            moveY = "+=" + pixels;
                            break;
                    }

                    //Get elements with focus and move them
                    $('.clicked-element').animate({
                        left: moveX,
                        top: moveY
                    }, 10).each(function () {
                        //Revalidate with jsPlumb
                        jsPlumbInstance.revalidate($(this));
                    });
                }

                /**
                 * Deletes all currently focused elements.
                 */
                function deleteFocusedElements() {
                    //Get elements with focus and delete them
                    $('.clicked-element').each(function () {
                        deleteElement($(this));
                    });

                    //Indicate that the model has changed
                    onModelChanged();

                    scope.isFocused = false;
                }

                /**
                 * Puts the focus on a given element. Focused elements become resizable and rotatable.
                 * @param element The element to focus
                 */
                function focusElement(element) {
                    //Sanity check
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

                    //Update exposed state
                    scope.isFocused = true;
                }

                /**
                 * Removes the focus from all elements. This also disables resizability and rotatability of the
                 * affected elements.
                 */
                function clearFocus() {
                    //Get all focused elements and clear them
                    $('.clicked-element')
                        .removeClass('clicked-element')
                        .resizable("disable")
                        .rotatable("disable")
                        .find('.ui-rotatable-handle')
                        .hide();

                    //Update exposed state
                    scope.isFocused = false;
                }

                /**
                 * Deletes an element from the canvas.
                 * @param element The element to delete
                 */
                function deleteElement(element) {
                    jsPlumbInstance.remove(element);
                }

                /*
                 * Bind listeners to the connections
                 */
                // The connection is deleted on double click
                jsPlumbInstance.bind("dblclick", jsPlumbInstance.deleteConnection);

                // Show the name input on click
                jsPlumbInstance.bind("click", function (connection, originalEvent) {
                    let overlay = connection.getOverlay("label");
                    if (overlay.isVisible() && originalEvent.target.localName === 'path') {
                        overlay.hide();
                    } else if (!overlay.isVisible()) {
                        overlay.show();
                    }
                });

                // Add device name and id to sensor or actuator when a connection is created
                jsPlumbInstance.bind("connection", function (info) {
                    let source = $(info.source);
                    let target = $(info.target);
                    if (target.attr("class").indexOf("device") === -1) {
                        target.data("device", source.data("name"));
                        target.data("deviceId", source.data("id"));
                    }

                    //Indicate that the model has changed
                    onModelChanged();
                });

                // Undeploy, deregister and remove device name and id from sensor or actuator when a connection is removed
                jsPlumbInstance.bind("connectionDetached", function (info) {
                    onDetach(info);
                });

                function onDetach(info) {
                    let target = $(info.target);
                    let targetType = target.attr('class').toString().split(" ")[1];
                    if (targetType === "sensor" || targetType === "actuator") {
                        target.removeData("device");
                        target.removeData("deviceId");
                    }
                }

                /**
                 * Load the data from the element to show it in the tool and input fields.
                 * @param element The element to load the data from
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
                        } else if (element.attr("class").indexOf("actuator") > -1) {
                            scope.clickedComponent.category = "ACTUATOR";
                            scope.clickedComponent.id = element.data("id");
                            scope.clickedComponent.name = element.data("name");
                            scope.clickedComponent.type = element.data("type");
                            scope.clickedComponent.operator = element.data("operator");
                            scope.clickedComponent.device = element.data("device");
                            scope.clickedComponent.regError = element.data("regError");
                            scope.clickedComponent.depError = element.data("depError");
                            scope.clickedComponent.deployed = element.data("deployed");
                            scope.clickedComponent.element = element;
                        } else if (element.attr("class").indexOf("sensor") > -1) {
                            scope.clickedComponent.category = "SENSOR";
                            scope.clickedComponent.id = element.data("id");
                            scope.clickedComponent.name = element.data("name");
                            scope.clickedComponent.type = element.data("type");
                            scope.clickedComponent.operator = element.data("operator");
                            scope.clickedComponent.device = element.data("device");
                            scope.clickedComponent.regError = element.data("regError");
                            scope.clickedComponent.depError = element.data("depError");
                            scope.clickedComponent.deployed = element.data("deployed");
                            scope.clickedComponent.element = element;
                        }
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
                        if (target.attr("class").indexOf("device") === -1) {
                            target.data("device", device.data("name"));
                            target.data("deviceId", device.data("id"));
                        }
                    });
                }

                /**
                 * Opens the element details for a certain element in case it represents a device, an actuator
                 * or a sensor.
                 */
                function openElementDetails(element) {
                    //Remember element
                    currentDetailsElement = element;

                    //Check for element type
                    if (element.hasClass('device')) {
                        //Device, load details data for this element into modal
                        scope.modalElementDetails = currentDetailsElement.data('details') || new DeviceDetails();

                        //Show device modal
                        DEVICE_DETAILS_MODAL.modal();
                    } else if (currentDetailsElement.hasClass('actuator') || currentDetailsElement.hasClass('sensor')) {
                        //Component, load details data for this element into modal
                        scope.modalElementDetails = currentDetailsElement.data('details') || new ComponentDetails();

                        //Show component modal
                        COMPONENT_DETAILS_MODAL.modal();
                    }
                }

                function saveElementDetails() {
                    //Check for element type
                    if (currentDetailsElement.hasClass('device')) {
                        DEVICE_DETAILS_MODAL.modal('hide');
                    } else if (currentDetailsElement.hasClass('actuator') || currentDetailsElement.hasClass('sensor')) {
                        COMPONENT_DETAILS_MODAL.modal('hide');
                    }

                    //Store element details
                    currentDetailsElement.data('details', scope.modalElementDetails);

                    //Model has changed
                    onModelChanged();
                }

                /**
                 * Creates and returns a node object reprsenting a given DOM element.
                 * @param element The element to export
                 */
                function exportElementToObject(element) {
                    //Read element classes and remove uninteresting ones
                    let classNames = $('<div>').addClass(element.attr('class'))
                        .removeClass('jtk-draggable')
                        .removeClass('ui-resizable')
                        .removeClass('ui-resizable-disabled')
                        .removeClass('ui-rotatable-disabled')
                        .removeClass('clicked-element')
                        .attr('class');

                    //Remove transformation to extract data from the unrotated element
                    let transformation = element.css('transform');
                    element.css('transform', '');

                    //Create basic node object
                    let nodeObject = {
                        nodeType: "floorplan", //Default, may be altered below
                        elementId: element.attr('id'),
                        clsName: classNames,
                        left: element.position().left,
                        top: element.position().top,
                        width: element.outerWidth(),
                        height: element.outerHeight()
                    };

                    //Apply transformation again
                    element.css('transform', transformation);

                    //Read data from element and merge them with the node object
                    nodeObject = Object.assign({}, element.data(), nodeObject);

                    //Remove useless properties
                    delete nodeObject['uiRotatable'];
                    delete nodeObject['uiResizable'];

                    //Determine node type
                    if (element.hasClass("device")) {
                        nodeObject['nodeType'] = "device";
                    } else if (element.hasClass("actuator")) {
                        nodeObject['nodeType'] = "actuator";
                    } else if (element.hasClass("sensor")) {
                        nodeObject['nodeType'] = "sensor";
                    }

                    //Set background if necessary
                    if (element.hasClass("device") || element.hasClass("actuator") || element.hasClass("sensor")) {
                        nodeObject['background'] = element.css('background-image');
                    }

                    //Return the final object
                    return nodeObject;
                }

                /**
                 * Exports the model in its current state to a JSON string.
                 * @return The exported JSON string representing the model
                 */
                function exportToJSON() {
                    let totalCount = 0;
                    let nodes = [];

                    //Iterate over all elements of the canvas
                    $(".jtk-node").each(function (index) {
                        totalCount++;

                        //Export a node object from the current element
                        let nodeObject = exportElementToObject($(this));

                        //Push node object
                        nodes.push(nodeObject);
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

                    //Stringify JSON
                    return JSON.stringify(environment);
                }

                /**
                 * Imports the model from a JSON string.
                 * @param jsonString The JSON string to import the model from
                 */
                function importFromJSON(jsonString) {
                    //Parse the JSON string
                    let jsonObject = JSON.parse(jsonString);

                    //Remove all current nodes from the canvas
                    clearCanvas();

                    //First create the nodes
                    $.each(jsonObject.nodes, function (index, node) {
                        let element = createElementFromNode(node.elementId, node);
                        drawElement(element);
                    });

                    //Connect the created nodes
                    $.each(jsonObject.connections, function (index, connection) {
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
                    elementIdCount = jsonObject.elementIdCount || 0;
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
                    initMarkingRect();
                }

                /**
                 * Writes the model in its current state to the undo manager.
                 */
                function saveForUndo() {
                    //Abort if there is an undo/redo processed at the moment
                    if (isUnReDoing) {
                        return;
                    }

                    let undoModelState = lastModelState;
                    lastModelState = exportToJSON();
                    let redoModelState = lastModelState;

                    //Record the model in its current state for undo and redo
                    UNDO_MANAGER.record({
                        undo: function () {
                            importFromJSON(undoModelState);
                            lastModelState = undoModelState;
                        },
                        redo: function () {
                            importFromJSON(redoModelState);
                            lastModelState = redoModelState;
                        }
                    });

                    //Update exposed states
                    updateExposedStates();
                }

                /**
                 * Must be called in case the model changed, so that the exposed "onChanged"
                 * callback function may be triggered and the model can be pushed on the history stack
                 * for undo/redo.
                 */
                function onModelChanged() {
                    //Save current model for undo
                    saveForUndo();

                    //Trigger the exposed callback
                    scope.onModelChanged();
                }

                /**
                 * Updates all states that are exposed to the outside and indicates which options are currently available.
                 */
                function updateExposedStates() {
                    scope.canUndo = UNDO_MANAGER.canUndo();
                    scope.canRedo = UNDO_MANAGER.canRedo();
                    scope.canPaste = (copyClipboard.length > 0);
                }

                /**
                 * [Public]
                 * Exports and returns the current model as JSON string.
                 * @returns The current model as JSON string
                 */
                function getModelJSON() {
                    //Export model
                    return exportToJSON();
                }

                /**
                 * [Public]
                 * Loads a model, given as JSON string.
                 * @param modelJSON The JSON string representing the model to load
                 */
                function loadModel(modelJSON) {
                    //Load model
                    importFromJSON(modelJSON);

                    //Reset undo manager
                    UNDO_MANAGER.reset();
                    lastModelState = "";
                    copyClipboard = [];

                    //Update exposed states
                    updateExposedStates();
                }

                /**
                 * [Public]
                 * Loads an empty model so that the user may create a new one.
                 */
                function loadEmptyModel() {
                    //Load empty model
                    loadModel("{}");

                    //Reset ID count
                    elementIdCount = 0;
                }

                /**
                 * [Public]
                 * Undoes the most recent action.
                 */
                function undoAction() {
                    //Undo is processed
                    isUnReDoing = true;

                    //Perform undo
                    UNDO_MANAGER.undo();

                    //Update exposed states
                    updateExposedStates();

                    //Undo is finished
                    isUnReDoing = false;
                }

                /**
                 * [Public]
                 * Redoes the most recent undone action.
                 */
                function redoAction() {
                    //Redo is processed
                    isUnReDoing = true;

                    //Perform redo
                    UNDO_MANAGER.redo();

                    //Update exposed states
                    updateExposedStates();

                    //Redo is finished
                    isUnReDoing = false;
                }

                /**
                 * [Public]
                 * Opens the element details modal for the selected elements.
                 */
                function openMarkedElementDetails() {
                    //Get focused device, actuator and sensor elements
                    let focusedElements = $('.clicked-element.device, .clicked-element.actuator, .clicked-element.sensor');

                    //Check if elements could be found
                    if (focusedElements.length < 1) {
                        return;
                    }

                    //Just open the element details modal for the first element
                    openElementDetails(focusedElements.first());
                }

                /**
                 * [Public]
                 * Copies the currently selected elements to the clipboard.
                 */
                function copyFocusedElements() {
                    //Get focused elements
                    let focusedElements = $('.clicked-element');

                    //Check if there are focused elements
                    if (focusedElements.length < 1) {
                        return;
                    }

                    //Clear clipboard
                    copyClipboard = [];

                    //Iterate over all focused elements
                    focusedElements.each(function (index) {
                        //Export the current element to object
                        let nodeObject = exportElementToObject($(this));

                        //Push object to clipboard
                        copyClipboard.push(nodeObject);
                    });

                    //Update exposed states
                    updateExposedStates();
                }

                /**
                 * [Public]
                 * Removes the currently selected elements and copies them to the clipboard.
                 */
                function cutFocusedElements() {
                    //Get focused elements
                    let focusedElements = $('.clicked-element');

                    //Check if there are focused elements
                    if (focusedElements.length < 1) {
                        return;
                    }

                    //Clear clipboard
                    copyClipboard = [];

                    //Iterate over all focused elements
                    focusedElements.each(function (index) {
                        //Wrap current element
                        let element = $(this);

                        //Export the current element to object
                        let nodeObject = exportElementToObject(element);

                        //Push object to clipboard
                        copyClipboard.push(nodeObject);

                        //Delete element
                        deleteElement(element);
                    });

                    //Update exposed states
                    updateExposedStates();
                }

                /**
                 * [Public]
                 * Pastes the elements in the clipboard and adds them to the canvas.
                 */
                function pasteElements() {
                    //Abort if there are no elements in the clipboard
                    if (copyClipboard.length < 1) {
                        return;
                    }

                    //Remove focus from all elements
                    clearFocus();

                    //Iterate over the clipboard
                    $.each(copyClipboard, function (index, nodeObject) {
                        //Generate new element ID
                        let elementID = "node" + (++elementIdCount);

                        //Create element from node object
                        let newElement = createElementFromNode(elementID, nodeObject);

                        drawElement(newElement);

                        //Mve the element away from its original position
                        newElement.animate({
                            left: "+=20",
                            top: "+=20"
                        }, 0);

                        //Put focus on the element
                        focusElement(newElement);
                    });

                    //Indicate that the model has changed
                    onModelChanged();
                }

                /**
                 * [Public]
                 * Opens a modal dialog to export the current model as JSON string.
                 */
                function exportModel() {
                    //Export model
                    scope.exportModelString = exportToJSON();

                    //Reset copy message
                    scope.showExportModelMessage = false;

                    //Show modal
                    EXPORT_MODAL.modal();
                }

                /**
                 * [Public]
                 * Opens a modal dialog to import a model given as JSON string.
                 */
                function importModel() {
                    IMPORT_MODAL.modal();
                }

                /**
                 * [Public]
                 * Updates the state of a node with a given ID.
                 * @param nodeId The ID of the node that is supposed to be updated
                 * @param newState The new state of the node (registered|deployed|started|error)
                 */
                function updateNodeState(nodeId, newState) {
                    //Find element of corresponding status indicator
                    let indicatorElement = $('#' + nodeId + ' .status-indicator');

                    //Check if element could be found
                    if (indicatorElement.length < 1) {
                        return;
                    }

                    //Remove all state classes from the indicator
                    indicatorElement.removeClass(NODE_STATES.join(" "));

                    //Check for no state
                    if ((typeof newState === 'undefined') || (NODE_STATES.indexOf(newState) < 0)) {
                        return;
                    }

                    //Add status as class to the element
                    indicatorElement.addClass(newState);
                }

                /**
                 * [Public]
                 * Displays a received value together with its unit next to its node with a certain ID.
                 * @param nodeId The ID of the node that is supposed to be updated
                 * @param unit The unit of the received component value
                 * @param value The received component value
                 */
                function displayComponentValue(nodeId, unit, value) {
                    //Find element of the node
                    let element = $('#' + nodeId);

                    //Check if element could be found
                    if (element.length < 1) {
                        return;
                    }

                    //Put display text together
                    let displayText = JSON.stringify(value, null, 2) + " " + unit;

                    //Get element position
                    let elementPos = element.position();

                    //Determine position of the value container
                    let x = elementPos.left + element.outerWidth();
                    let y = elementPos.top + (element.outerHeight() / 2) - 10;

                    //Create value container
                    let valueContainer = $('<div class="component-value">').css({
                        'left': x + 'px',
                        'top': y + 'px',
                        'font-size': '12px',
                        'opacity': '0'
                    }).on('mouseover', function () {
                        // If the user hovers over the value display, the values should be still visible
                        // which is realized by setting the fade out timer back.
                        $(this).stop(true, false)
                            .animate({opacity: 1}, 200, 'easeOutExpo')
                            .animate({opacity: 1}, 3000)
                            .animate({opacity: 0}, 1000, 'easeInQuint', function() {
                                // Callback after fade out, remove the element
                                $(this).remove();
                        });
                    });

                    scope.value = value;

                    let jsonFormatter = '<json-formatter json="value" open="1"></json-formatter>';
                    let compiledJsonFormatter = $compile(jsonFormatter)(scope);

                    valueContainer.append(compiledJsonFormatter);

                    //Append container to canvas
                    CANVAS.append(valueContainer);

                    //Animate the value container (appearance and disappearance)
                    valueContainer.stop(true, false)
                        .animate({opacity: 1}, 200, 'easeOutExpo')
                        .animate({opacity: 1}, 3000)
                        .animate({opacity: 0}, 1000, 'easeInQuint', function() {
                            // Callback after fade out, remove the element
                            $(this).remove();
                        });
                }

                /**
                 * Copies the model in the export text field of the modal dialog to the clipboard.
                 */
                function copyExportModelToClipboard() {
                    let textArea = EXPORT_MODAL.find('textarea');
                    textArea.select();
                    document.execCommand('copy');
                    scope.showExportModelMessage = true;
                }

                /**
                 * Called when the user wants to import a modal from the import dialog.
                 */
                function importModelRequest() {
                    //Get JSON string
                    let modelJSON = scope.importModelString;

                    //First sanity check
                    if ((modelJSON == null) || (modelJSON === "")) {
                        scope.importModelErrorMessage = "No model data provided.";
                        return;
                    }

                    //Save current model
                    let currentModel = exportToJSON();

                    //Try to import the model
                    try {
                        //Import the model
                        importFromJSON(modelJSON);

                        //Import was successful, so indicate that the model has changed
                        onModelChanged();

                        //Clear error message and hide the modal
                        scope.importModelErrorMessage = false;
                        IMPORT_MODAL.modal('hide');
                    } catch (e) {
                        //Set error message
                        scope.importModelErrorMessage = "Invalid model data provided.";

                        //Reset model
                        importFromJSON(currentModel);
                    }
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
                let initFunction = initialize.bind(this, scope, element);
                jsPlumb.ready(initFunction);
            };

            //Configure and expose the directive
            return {
                restrict: 'E', //Elements only
                template:
                    '<div id="modelingToolView">' +
                    '<!-- Palette -->' +
                    '<div id="toolPalette" style="display: inline-block; vertical-align: top; width: 220px;">' +
                    '<div class="panel-group" id="accordion">' +
                    '<div class="panel panel-default">' +
                    '<div class="panel-heading" style="overflow-x: hidden;">' +
                    '<h4 class="panel-title">' +
                    '<a class="clickable" data-toggle="collapse" data-target="#collapseFloorplans" aria-expanded="false">' +
                    '<span class="material-icons" style="font-size: 20px;">weekend</span>Floorplans' +
                    '<i class="material-icons" style="float: right;">keyboard_arrow_down</i></a>' +
                    '</h4>' +
                    '</div>' +
                    '<div id="collapseFloorplans" class="panel-collapse collapse in">' +
                    '<div class="panel-body canvas-wide modeling-tool canvasPalette">' +
                    '<ul class="dragList floorplan-palette">' +
                    '</ul>' +
                    '</div>' +
                    '</div>' +
                    '</div>' +
                    '<div class="panel panel-default">' +
                    '<div class="panel-heading" style="overflow-x: hidden;">' +
                    '<h4 class="panel-title">' +
                    '<a class="clickable collapsed" data-toggle="collapse" data-target="#collapseDevices" aria-expanded="false">' +
                    '<span class="material-icons" style="font-size: 20px;">devices</span>Device types' +
                    '<i class="material-icons" style="float: right;">keyboard_arrow_down</i></a>' +
                    '</h4>' +
                    '</div>' +
                    '<div id="collapseDevices" class="panel-collapse collapse">' +
                    '<div class="panel-body canvas-wide modeling-tool canvasPalette">' +
                    '<ul class="dragList device-palette">' +
                    '</ul>' +
                    '</div>' +
                    '</div>' +
                    '</div>' +
                    '<div class="panel panel-default">' +
                    '<div class="panel-heading" style="overflow-x: hidden;">' +
                    '<h4 class="panel-title">' +
                    '<a class="clickable collapsed" data-toggle="collapse" data-target="#collapseSensors" aria-expanded="false">' +
                    '<span class="material-icons" style="font-size: 20px;">settings_remote</span>Sensor types' +
                    '<i class="material-icons" style="float: right;">keyboard_arrow_down</i></a>' +
                    '</h4>' +
                    '</div>' +
                    '<div id="collapseSensors" class="panel-collapse collapse">' +
                    '<div class="panel-body canvas-wide modeling-tool canvasPalette">' +
                    '<ul class="dragList sensor-palette">' +
                    '</ul>' +
                    '</div>' +
                    '</div>' +
                    '</div>' +
                    '<div class="panel panel-default">' +
                    '<div class="panel-heading" style="overflow-x: hidden;">' +
                    '<h4 class="panel-title">' +
                    '<a class="clickable collapsed" data-toggle="collapse" data-target="#collapseActuators" aria-expanded="false">' +
                    '<span class="material-icons" style="font-size: 20px;">wb_incandescent</span>Actuator types' +
                    '<i class="material-icons" style="float: right;">keyboard_arrow_down</i></a>' +
                    '</h4>' +
                    '</div>' +
                    '<div id="collapseActuators" class="panel-collapse collapse">' +
                    '<div class="panel-body canvas-wide modeling-tool canvasPalette">' +
                    '<ul class="dragList actuator-palette">' +
                    '</ul>' +
                    '</div>' +
                    '</div>' +
                    '</div>' +
                    '</div>' +
                    '</div>' +
                    '<!-- Canvas - Modeling Area -->' +
                    '<div id="toolCanvasContainer">' +
                    '<div class="jtk-main">' +
                    '<div class="jtk-canvas canvas-wide modeling-tool jtk-surface jtk-surface-nopan" id="canvas">' +
                    '</div>' +
                    '</div>' +
                    '</div>' +
                    '</div>' +
                    '<div class="modal fade" id="exportModelModal" tabindex="-1" role="dialog">' +
                    '<div class="modal-dialog" role="document">' +
                    '<div class="modal-content">' +
                    '<div class="modal-header">' +
                    '<h5 class="modal-title"><i class="material-icons" style="vertical-align: bottom;">file_upload</i>&nbsp;Export model' +
                    '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
                    '</h5></div>' +
                    '<form><fieldset>' +
                    '<div class="modal-body"><div class="form-group"><div class="form-line">' +
                    '<textarea class="form-control" style="height: 120px;" ng-model="exportModelString" onclick="this.select()"></textarea>' +
                    '</div></div>' +
                    '<span class="text-success" ng-show="showExportModelMessage"><i class="material-icons" style="vertical-align:bottom;">done</i>&nbsp;Copied model to clipboard.</span></div>' +
                    '<div class="modal-footer">' +
                    '<button type="button" class="btn btn-secondary m-t-0 waves-effect" data-dismiss="modal">Close</button>' +
                    '<button type="button" class="btn btn-primary m-t-0 waves-effect" ng-click="copyExportModelToClipboard()">Copy to Clipboard</button>' +
                    '</div></fieldset></form></div></div></div>' +
                    '<div class="modal fade" id="importModelModal" tabindex="-1" role="dialog">' +
                    '<div class="modal-dialog" role="document">' +
                    '<div class="modal-content">' +
                    '<div class="modal-header">' +
                    '<h5 class="modal-title"><i class="material-icons" style="vertical-align: bottom;">file_download</i>&nbsp;Import model' +
                    '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
                    '</h5></div>' +
                    '<form ng-submit="importModelRequest()"><fieldset>' +
                    '<div class="modal-body"><div class="form-group"><div class="form-line">' +
                    '<span>Please paste the model that is supposed to be imported below:</span>' +
                    '<textarea class="form-control" style="height: 120px;" ng-model="importModelString" onclick="document.execCommand(\'paste\');"></textarea>' +
                    '</div></div>' +
                    '<span class="text-danger" ng-show="importModelErrorMessage"><i class="material-icons" style="vertical-align:bottom;">error_outline</i>&nbsp;{{importModelErrorMessage}}</span></div>' +
                    '<div class="modal-footer">' +
                    '<button type="button" class="btn btn-secondary m-t-0 waves-effect" data-dismiss="modal">Close</button>' +
                    '<button type="submit" class="btn btn-primary m-t-0 waves-effect">Import</button>' +
                    '</div></fieldset></form></div></div></div>' +
                    '<div class="modal fade" id="deviceDetailsModal" tabindex="-1" role="dialog">' +
                    '<div class="modal-dialog" role="document">' +
                    '<div class="modal-content">' +
                    '<div class="modal-header">' +
                    '<h5 class="modal-title">Device settings' +
                    '<button type="button" class="close" data-dismiss="modal" aria-label="Close">' +
                    '<span aria-hidden="true">&times;</span>' +
                    '</button></h5></div>' +
                    '<form ng-submit="saveElementDetails()"><fieldset>' +
                    '<div class="modal-body">' +
                    '<div class="form-group"><div class="form-line">' +
                    '<input class="form-control" type="text" placeholder="Name" ng-model="modalElementDetails.name"/></div></div>' +
                    '<div class="form-group"><div class="form-line">' +
                    '<input class="form-control" type="text" placeholder="IP address" ng-model="modalElementDetails.ipAddress"/>' +
                    '</div></div>' +
                    '<div class="form-group"><div class="form-line">' +
                    '<input class="form-control" type="text" placeholder="User name" ng-model="modalElementDetails.username"/>' +
                    '</div></div>' +
                    '<div class="form-group"><div class="form-line">' +
                    '<input class="form-control" type="password" placeholder="Password" autocomplete="no" ng-model="modalElementDetails.password"/>' +
                    '</div></div>' +
                    '<div class="form-group"><div class="form-line">' +
                    '<select class="form-control show-tick" ng-model="modalElementDetails.keyPair" ng-options="t.id as (t.name) for t in keyPairList">' +
                    '<option value="">Select SSH key pair<option/>' +
                    '</select></div></div></div>' +
                    '<div class="modal-footer">' +
                    '<button type="button" class="btn btn-secondary m-t-0 waves-effect" data-dismiss="modal">Close</button>' +
                    '<button type="submit" class="btn btn-primary m-t-0 waves-effect">Save</button>' +
                    '</div></fieldset></form></div></div></div>' +
                    '<div class="modal fade" id="componentDetailsModal" tabindex="-1" role="dialog">' +
                    '<div class="modal-dialog" role="document">' +
                    '<div class="modal-content">' +
                    '<div class="modal-header">' +
                    '<h5 class="modal-title">Component settings' +
                    '<button type="button" class="close" data-dismiss="modal" aria-label="Close">' +
                    '<span aria-hidden="true">&times;</span>' +
                    '</button></h5></div>' +
                    '<form ng-submit="saveElementDetails()"><fieldset>' +
                    '<div class="modal-body">' +
                    '<div class="form-group"><div class="form-line">' +
                    '<input class="form-control" type="text" placeholder="Name" ng-model="modalElementDetails.name"/></div></div>' +
                    '<div class="form-group"><div class="form-line">' +
                    '<select class="form-control show-tick" ng-model="modalElementDetails.operator" ng-options="t.id as (t.name) for t in operatorList">' +
                    '<option value="">Select operator<option/>' +
                    '</select></div></div></div>' +
                    '<div class="modal-footer">' +
                    '<button type="button" class="btn btn-secondary m-t-0 waves-effect" data-dismiss="modal">Close</button>' +
                    '<button type="submit" class="btn btn-primary m-t-0 waves-effect">Save</button>' +
                    '</div></fieldset></form></div></div></div>'
                ,
                link: link,
                scope: {
                    //Public API
                    api: "=api",
                    //Hold the current state of the undo/redo functions
                    canUndo: '=canUndo',
                    canRedo: '=canRedo',
                    canPaste: '=canPaste',
                    isFocused: '=isFocused',

                    //Input
                    keyPairList: '=keyPairList',
                    operatorList: '=operatorList',
                    deviceTypes: '=deviceTypes',
                    actuatorTypes: '=actuatorTypes',
                    sensorTypes: '=sensorTypes',

                    //Callbacks
                    onModelChanged: '&onChanged'
                }
            };
        }]
)
;