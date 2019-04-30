(function() {
  'use strict';

  angular
    .module('app')
    .controller('ModelController', ModelController);

  ModelController.$inject = ['ENDPOINT_URI', '$scope', '$timeout', '$q', '$controller',
    'ModelService', 'ComponentService', 'DeviceService', 'CrudService', 'adapterList'
  ];

  function ModelController(ENDPOINT_URI, $scope, $timeout, $q, $controller,
    ModelService, ComponentService, DeviceService, CrudService, adapterList) {
    var vm = this;

    jsPlumb.ready(function() {
      var jsPlumbInstance;
      var canvasId = "#canvas";
      var elementIdCount = 0;
      var properties = {}; // keeps the properties of each element
      var element = ""; // the element which will be appended to the canvas
      var clicked = false; // true if an element from the palette was clicked
      vm.processing = {};
      vm.deletionPromises = [];
      vm.selectedOptionName = "";
      vm.loadedModels = [];
      vm.currentModel = {};
      vm.clickedComponent = {};
      vm.drawModel = drawModel;
      vm.loadModels = loadModels;
      vm.saveModel = saveModel;
      vm.deleteModel = deleteModel;
      vm.clearCanvas = clearCanvas;
      vm.newModel = newModel;
      vm.registerComponents = registerComponents;
      vm.deployComponents = deployComponents;
      vm.undeployComponents = undeployComponents;

      (function initController() {
        vm.loadModels();
      })();

      angular.extend(vm, {
        adapterListCtrl: $controller('ItemListController as adapterListCtrl', {
          $scope: $scope,
          list: adapterList
        })
      });

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

      var basicType = {
        anchor: "Continuous",
        connector: "StateMachine"
      };

      jsPlumbInstance.registerConnectionType("basic", basicType);

      var sourceEndpoint = {
        filter: ".ep",
        anchor: "Continuous",
        connectorStyle: {
          stroke: "#000000",
          strokeWidth: 2
        },
        connectionType: "basic"
      };

      var targetEndpoint = {
        dropOptions: {
          hoverClass: "dragHover"
        },
        anchor: "Continuous",
        allowLoopback: false
      };

      function makeResizable(id) {
        $(id).resizable({
          start: function(event, ui) {
            $(".close-icon").hide();
            $(id).css({
              'outline': "2px solid grey"
            });
          },
          resize: function(event, ui) {
            jsPlumbInstance.revalidate(ui.helper);
          },
          stop: function(event, ui) {
            $(id).css({
              'outline': "none"
            });
          },
          handles: "all"
        });
      }

      function makeDraggable(id, className) {
        $(id).draggable({
          helper: function() {
            return $("<div/>", {
              class: className
            });
          },
          revert: false
        });
      }

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
      makeDraggable("#aContainer", "window as-container custom");

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
      makeDraggable("#sContainer", "window as-container custom");

      //make the editor canvas droppable
      $(canvasId).droppable({
        accept: ".window",
        drop: function(event, ui) {
          if (clicked) {
            properties.top = ui.offset.top - $(this).offset().top;
            properties.left = ui.offset.left - $(this).offset().left;
            clicked = false;
            elementIdCount++;
            var id = "canvasWindow" + elementIdCount;
            element = createElement(id, undefined);
            drawElement(element);
            // element = "";
          }
        }
      });

      //take the x, y coordinates of the current mouse position
      // var x, y;
      // $(document).on("mousemove", function(event) {
      //   x = event.pageX;
      //   y = event.pageY;
      //   if (clicked) {
      //     properties.top = y - 108;
      //     properties.left = x - 268;
      //   }
      // });

      // Temporary saved properties of clicked element in palette
      // The data is used to create the element on drop
      function loadProperties(clsName, type) {
        properties = {};
        properties.clsName = clsName;
        properties.type = type;
        clicked = true;
      }

      //load properties of an element once the element in the palette is clicked
      $('#roomFloorplan').mousedown(function() {
        loadProperties("window floorplan room-floorplan custom jtk-node", undefined);
      });

      $('#wallFloorplan').mousedown(function() {
        loadProperties("window floorplan wall-floorplan custom jtk-node", undefined);
      });

      $('#doorFloorplan').mousedown(function() {
        loadProperties("window floorplan door-floorplan custom jtk-node", undefined);
      });

      $('#windowFloorplan').mousedown(function() {
        loadProperties("window floorplan window-floorplan custom jtk-node", undefined);
      });

      $('#stairsFloorplan').mousedown(function() {
        loadProperties("window floorplan stairs-floorplan custom jtk-node", undefined);
      });

      $('#tableFloorplan').mousedown(function() {
        loadProperties("window floorplan table-floorplan custom jtk-node", undefined);
      });

      $('#chairFloorplan').mousedown(function() {
        loadProperties("window floorplan chair-floorplan custom jtk-node", undefined);
      });

      $('#couchFloorplan').mousedown(function() {
        loadProperties("window floorplan couch-floorplan custom jtk-node", undefined);
      });

      $('#bedFloorplan').mousedown(function() {
        loadProperties("window floorplan bed-floorplan custom jtk-node", undefined);
      });

      $('#kitchenSinkFloorplan').mousedown(function() {
        loadProperties("window floorplan kitchen-sink-floorplan custom jtk-node", undefined);
      });

      $('#bathtubFloorplan').mousedown(function() {
        loadProperties("window floorplan bathtub-floorplan custom jtk-node", undefined);
      });

      $('#bathSinkFloorplan').mousedown(function() {
        loadProperties("window floorplan bath-sink-floorplan custom jtk-node", undefined);
      });

      $('#toiletFloorplan').mousedown(function() {
        loadProperties("window floorplan toilet-floorplan custom jtk-node", undefined);
      });

      $('#raspberryPiDevice').mousedown(function() {
        loadProperties("window device raspberry-pi-device custom jtk-node", "Raspberry Pi");
      });

      $('#arduinoDevice').mousedown(function() {
        loadProperties("window device arduino-device custom jtk-node", "Arduino");
      });

      $('#computerDevice').mousedown(function() {
        loadProperties("window device computer-device custom jtk-node", "Computer");
      });

      $('#laptopDevice').mousedown(function() {
        loadProperties("window device laptop-device custom jtk-node", "Laptop");
      });

      $('#tvDevice').mousedown(function() {
        loadProperties("window device tv-device custom jtk-node", "TV");
      });

      $('#smartphoneDevice').mousedown(function() {
        loadProperties("window device smartphone-device custom jtk-node", "Smartphone");
      });

      $('#smartwatchDevice').mousedown(function() {
        loadProperties("window device smartwatch-device custom jtk-node", "Smartwatch");
      });

      $('#audioSystemDevice').mousedown(function() {
        loadProperties("window device audio-system-device custom jtk-node", "Audio System");
      });

      $('#voiceControllerDevice').mousedown(function() {
        loadProperties("window device voice-controller-device custom jtk-node", "Voice Controller");
      });

      $('#cameraDevice').mousedown(function() {
        loadProperties("window device camera-device custom jtk-node", "Camera");
      });

      $('#defaultDevice').mousedown(function() {
        loadProperties("window device default-device custom jtk-node", undefined);
      });

      $('#lightActuator').mousedown(function() {
        loadProperties("window actuator light-actuator custom jtk-node", "Light");
      });

      $('#ledActuator').mousedown(function() {
        loadProperties("window actuator led-actuator custom jtk-node", "LED");
      });

      $('#speakerActuator').mousedown(function() {
        loadProperties("window actuator speaker-actuator custom jtk-node", "Speaker");
      });

      $('#buzzerActuator').mousedown(function() {
        loadProperties("window actuator buzzer-actuator custom jtk-node", "Buzzer");
      });

      $('#vibrationActuator').mousedown(function() {
        loadProperties("window actuator vibration-actuator custom jtk-node", "Vibration");
      });

      $('#heaterActuator').mousedown(function() {
        loadProperties("window actuator heater-actuator custom jtk-node", "Heater");
      });

      $('#airConditionerActuator').mousedown(function() {
        loadProperties("window actuator air-conditioner-actuator custom jtk-node", "Air Conditioner");
      });

      $('#switchActuator').mousedown(function() {
        loadProperties("window actuator switch-actuator custom jtk-node", "Switch");
      });

      $('#motorActuator').mousedown(function() {
        loadProperties("window actuator motor-actuator custom jtk-node", "Motor");
      });

      $('#defaultActuator').mousedown(function() {
        loadProperties("window actuator default-actuator custom jtk-node", undefined);
      });

      $('#aContainer').mousedown(function() {
        loadProperties("window as-container custom jtk-node", undefined);
      });

      $('#cameraSensor').mousedown(function() {
        loadProperties("window sensor camera-sensor custom jtk-node", "Camera");
      });

      $('#soundSensor').mousedown(function() {
        loadProperties("window sensor sound-sensor custom jtk-node", "Sound");
      });

      $('#temperatureSensor').mousedown(function() {
        loadProperties("window sensor temperature-sensor custom jtk-node", "Temperature");
      });

      $('#humiditySensor').mousedown(function() {
        loadProperties("window sensor humidity-sensor custom jtk-node", "Humidity");
      });

      $('#gasSensor').mousedown(function() {
        loadProperties("window sensor gas-sensor custom jtk-node", "Gas");
      });

      $('#lightSensor').mousedown(function() {
        loadProperties("window sensor light-sensor custom jtk-node", "Light");
      });

      $('#motionSensor').mousedown(function() {
        loadProperties("window sensor motion-sensor custom jtk-node", "Motion");
      });

      $('#locationSensor').mousedown(function() {
        loadProperties("window sensor location-sensor custom jtk-node", "Location");
      });

      $('#gyroscopeSensor').mousedown(function() {
        loadProperties("window sensor gyroscope-sensor custom jtk-node", "Gyroscope");
      });

      $('#proximitySensor').mousedown(function() {
        loadProperties("window sensor proximity-sensor custom jtk-node", "Proximity");
      });

      $('#touchSensor').mousedown(function() {
        loadProperties("window sensor touch-sensor custom jtk-node", "Touch");
      });

      $('#vibrationSensor').mousedown(function() {
        loadProperties("window sensor vibration-sensor custom jtk-node", "Vibration");
      });

      $('#defaultSensor').mousedown(function() {
        loadProperties("window sensor default-sensor custom jtk-node", undefined);
      });

      $('#sContainer').mousedown(function() {
        loadProperties("window as-container custom jtk-node", undefined);
      });

      //create an element to be drawn on the canvas
      function createElement(id, node) {
        if (node) {
          // Use node for loaded model
          var element = $('<div>').addClass(node.clsName).attr('id', id);
          // The position to create the element
          element.css({
            'top': node.positionY,
            'left': node.positionX
          });
          // Define the size of the element
          element.outerWidth(node.width);
          element.outerHeight(node.height);

          // Set rotation angle; no need for room
          if (node.angle && (node.clsName.indexOf("room-floorplan") == -1)) {
            element.data("angle", node.angle);
            setAngle(element, false);
          }

          if (node.nodeType == "device") {
            element.append("<div class=\"ep\"></div>");
            element.data("id", node.id);
            element.data("name", node.name);
            element.data("type", node.type);
            element.data("mac", node.mac);
            element.data("ip", node.ip);
            element.data("username", node.username);
            element.data("rsaKey", node.rsaKey);
            element.data("regError", node.regError);
          } else if (node.nodeType == "actuator" || node.nodeType == "sensor") {
            element.data("id", node.id);
            element.data("name", node.name);
            element.data("type", node.type);
            element.data("adapter", node.adapter);
            element.data("device", node.device);
            element.data("deviceId", node.deviceId);
            element.data("deployed", node.deployed);
            element.data("regError", node.regError);
            element.data("depError", node.depError);
          } else if (node.nodeType == "as-container") {
            element.data("containerNodes", node.containerNodes);
          }
        } else {
          // Use properties on drop
          var element = $('<div>').addClass(properties.clsName).attr('id', id);
          // The position to created the dropped element
          element.css({
            'top': properties.top,
            'left': properties.left
          });
          // Increase the size of room
          if (properties.clsName.indexOf("room-floorplan") > -1) {
            element.outerWidth("250px");
            element.outerHeight("250px");
          }
          // Add connection square on device
          if (properties.clsName.indexOf("device") > -1) {
            element.append("<div class=\"ep\"></div>");
          }

          if (properties.type) {
            element.data("type", properties.type);
          }
        }

        element.append("<i style='display: none' class=\"fa fa-times fa-lg close-icon\"><\/i>");
        return element;
      }

      function drawElement($element) {
        $(canvasId).append($element);
        jsPlumbInstance.draggable(jsPlumbInstance.getSelector(".jtk-node"), {
          filter: ".ui-resizable-handle"
        });

        if ($element.attr("class").indexOf("as-container") > -1) {
          console.log("Actuator node draw " + $element.attr("id"));
          // jsPlumbInstance.draggable(jsPlumbInstance.getSelector(".group-node"), {
          //   filter: ".ui-resizable-handle"
          // });
          $element.droppable({
            accept: ".actuator, .sensor",
            drop: function(event, ui) {
              console.log("Actuator node dropped");
              element.css({
                'top': ui.offset.top - $(this).offset().top,
                'left': ui.offset.left - $(this).offset().left
              });
              // clicked = false;
              // elementIdCount++;
              // var id = "canvasWindow" + elementIdCount;
              // element = createElement(id, undefined);
              $element.append(element);
              // jsPlumbInstance.draggable(jsPlumbInstance.getSelector(".jtk-node"), {
              //   filter: ".ui-resizable-handle"
              // });
              // addEndpoints(element);
              // makeResizable(".custom");
              // element = "";
            }
          });

          if ($element.data("containerNodes")) {
            var containerNodes = $element.data("containerNodes");
            containerNodes.forEach(function(value, index, array) {
              var nodeElement = createElement(value.elementId, value);
              $element.append(nodeElement);
              addEndpoints(nodeElement);
            });
          }
        }
        $element.scroll(function() {
          jsPlumbInstance.repaintEverything();
        });
        addEndpoints($element);
        makeResizable(".custom");
      }

      function addEndpoints(element) {
        var type = element.attr('class').toString().split(" ")[1];
        if (type == "device") {
          targetEndpoint.maxConnections = -1;
          jsPlumbInstance.makeSource(element, sourceEndpoint);
          jsPlumbInstance.makeTarget(element, targetEndpoint);
        } else if (type == "actuator" || type == "sensor") {
          targetEndpoint.maxConnections = 1;
          jsPlumbInstance.makeTarget(element, targetEndpoint);
        }
      }

      $(document).on("click", ".jtk-node", function() {
        loadData($(this));

        var marginLeft = $(this).outerWidth() + 6 + "px";
        $(".close-icon").prop("title", "Delete the element");
        $(this).find("i").css({
          'margin-left': marginLeft
        }).show();

        $(this).addClass("clicked-element");
        if ($(this).attr("class").indexOf("room-floorplan") > -1) {
          $(this).css({
            'outline': "2px solid #4863A0"
          });
        }

      });

      $(document).on('dblclick', ".jtk-node", function() {
        if ($(this).attr("class").indexOf("room-floorplan") == -1 &&
          $(this).attr("class").indexOf("as-container") == -1) {
          setAngle($(this), true);
        }
      });

      function setAngle(element, rotate) {
        var angle = 0;
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

      $(canvasId).on('click', function(e) {
        saveData();
      });

      $(document).on("click", ".close-icon", function() {
        vm.processing = {};
        vm.processing.status = true;
        vm.processing.finished = false;
        vm.processing.undeployedDeregistered = true;

        var element = $(this).parent();
        var type = element.attr('class').toString().split(" ")[1];

        // Case: A device has attached sensors and actuators, which are deployed or registered
        if (type == "device" && element.data("id")) {
          vm.deletionPromises = [];
          $.each(jsPlumbInstance.getConnections({
            source: element.attr("id")
          }), function(index, connection) {
            var target = $(connection.target);
            var targetType = target.attr('class').toString().split(" ")[1];
            if (targetType == "sensor" || targetType == "actuator") {
              if (target.data("deployed")) {
                var promise = undeployComponent(targetType, target, false);
                vm.deletionPromises.push(promise);
              } else if (target.data("id")) {
                var promise = deregisterComponent(targetType, target, false);
                vm.deletionPromises.push(promise);
              }
            }
          });

          $q.all(vm.deletionPromises).then(function() {
            if (vm.processing.undeployedDeregistered) {
              deregisterComponent(type, element, true);
            } else {
              saveModel().then(function(response) {
                vm.processing.message = "Sensor or actuator error";
                vm.processing.success = false;
                processingTimeout();
              });
            }
          });
        } else {
          if (element.data("deployed")) {
            undeployComponent(type, element, true);
          } else if (element.data("id")) {
            deregisterComponent(type, element, true);
          } else {
            deleteElementFromCanvas(element, false);
            vm.processing.status = false;
          }
        }
      });

      function undeployComponent(type, element, deleteFromModel) {
        return ComponentService.undeploy(ENDPOINT_URI + "/deploy/" + type + "/" + element.data("id")).then(
          function(response) {
            console.log(response);
            element.data("deployed", false);
            element.removeData("depError");
            element.removeClass("error-element");
            element.removeClass("deployed-element");
            element.addClass("success-element");
            var promise = deregisterComponent(type, element, deleteFromModel);
            vm.deletionPromises.push(promise);
          },
          function(response) {
            console.log(response);
            element.data("depError", response.data ? response.data.globalMessage : response.status);
            element.removeClass("success-element");
            element.removeClass("deployed-element");
            element.addClass("error-element");
            vm.processing.undeployedDeregistered = false;
            if (deleteFromModel) {
              saveModel().then(function(response) {
                vm.processing.message = "Undeployment of " + element.data("name") + " ended with an error";
                vm.processing.success = false;
                processingTimeout();
              });
            }
          });
      }

      function deregisterComponent(type, element, deleteFromModel) {
        var item = {};
        item.id = element.data("id");
        return CrudService.deleteItem(type + "s", item).then(
          function(response) {
            console.log(response);
            element.removeData("id");
            element.removeData("depError");
            element.removeData("regError");
            element.removeClass("error-element");
            element.removeClass("success-element");
            if (deleteFromModel) {
              deleteElementFromCanvas(element, true);
            }
          },
          function(response) {
            console.log(response);
            element.data("regError", response.name ? response.name.message : response.status);
            element.removeClass("success-element");
            element.addClass("error-element");
            vm.processing.undeployedDeregistered = false;
            if (deleteFromModel) {
              saveModel().then(function(response) {
                vm.processing.message = "Deregistration of " + element.data("name") + " ended with an error";
                vm.processing.success = false;
                processingTimeout();
              });
            }
          });
      }

      function deleteElementFromCanvas(element, savingModel) {
        $timeout(function() {
          vm.clickedComponent = {};
          jsPlumbInstance.remove(element);
          if (savingModel) {
            saveModel().then(function(response) {
              vm.processing.message = element.data("name") + " deleted";
              vm.processing.success = true;
              processingTimeout();
            });
          }
        });
      }

      // bind a click listener to each connection; the connection is deleted on double click
      jsPlumbInstance.bind("dblclick", jsPlumbInstance.deleteConnection);
      jsPlumbInstance.bind("click", function(connection, originalEvent) {
        var overlay = connection.getOverlay("label");
        if (overlay.isVisible() && originalEvent.target.localName == 'path') {
          overlay.hide();
        } else if (!overlay.isVisible()) {
          overlay.show();
        }
      });

      // Add device name and id to sensor or actuator when a connection was created
      jsPlumbInstance.bind("connection", function(info) {
        saveData();
        var source = $(info.source);
        var target = $(info.target);
        if (target.attr("class").indexOf("device") == -1) {
          target.data("device", source.data("name"));
          target.data("deviceId", source.data("id"));
        }
      });

      // Undeploy, deregister and remove device name and id from sensor or actuator when a connection is removed
      jsPlumbInstance.bind("connectionDetached", function(info) {
        onDetach(info);
      });

      function onDetach(info) {
        vm.processing = {};
        vm.processing.status = true;
        vm.processing.finished = false;
        vm.processing.undeployedDeregistered = true;

        var target = $(info.target);
        var targetType = target.attr('class').toString().split(" ")[1];
        if (targetType == "sensor" || targetType == "actuator") {
          vm.deletionPromises = [];
          if (target.data("deployed")) {
            var promise = undeployComponent(targetType, target, false);
            vm.deletionPromises.push(promise);
          } else if (target.data("id")) {
            var promise = deregisterComponent(targetType, target, false);
            vm.deletionPromises.push(promise);
          } else {
            vm.processing.status = false;
          }

          $q.all(vm.deletionPromises).then(function() {
            if (vm.deletionPromises.length !== 0) {
              saveModel().then(function(response) {
                if (vm.processing.undeployedDeregistered) {
                  vm.processing.message = target.data("name") + " deregistered";
                  vm.processing.success = true;
                } else {
                  vm.processing.message = target.data("name") + " error";
                  vm.processing.success = false;
                }
                processingTimeout();
              });
            }
          });
          target.removeData("device");
          target.removeData("deviceId");
        } else {
          vm.processing.status = false;
        }
      }

      // Update device name in sensor or actuator
      function updateDeviceSA(device) {
        $.each(jsPlumbInstance.getConnections({
          source: device.attr("id")
        }), function(index, connection) {
          var target = $(connection.target);
          if (target.attr("class").indexOf("device") == -1) {
            target.data("device", device.data("name"));
            target.data("deviceId", device.data("id"));
          }
        });
      }

      function loadData(element) {
        $timeout(function() {
          if (element.attr("class").indexOf("device") > -1) {
            vm.clickedComponent.category = "DEVICE";
            vm.clickedComponent.id = element.data("id");
            vm.clickedComponent.name = element.data("name");
            vm.clickedComponent.type = element.data("type");
            vm.clickedComponent.mac = element.data("mac");
            vm.clickedComponent.ip = element.data("ip");
            vm.clickedComponent.username = element.data("username");
            vm.clickedComponent.rsaKey = element.data("rsaKey");
            vm.clickedComponent.regError = element.data("regError");
            vm.clickedComponent.element = element;
            if (element.data("id")) {
              $("#deviceInfo *").attr("disabled", true).off('click');
            } else {
              $("#deviceInfo *").attr("disabled", false).on('click');
              if (element.attr("class").indexOf("default") == -1) {
                $("#deviceTypeInput").attr("disabled", true);
              }
            }
          } else if (element.attr("class").indexOf("actuator") > -1) {
            vm.clickedComponent.category = "ACTUATOR";
            vm.clickedComponent.id = element.data("id");
            vm.clickedComponent.name = element.data("name");
            vm.clickedComponent.type = element.data("type");
            vm.clickedComponent.adapter = element.data("adapter");
            vm.clickedComponent.device = element.data("device");
            vm.clickedComponent.regError = element.data("regError");
            vm.clickedComponent.depError = element.data("depError");
            vm.clickedComponent.deployed = element.data("deployed");
            vm.clickedComponent.element = element;
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
            vm.clickedComponent.category = "SENSOR";
            vm.clickedComponent.id = element.data("id");
            vm.clickedComponent.name = element.data("name");
            vm.clickedComponent.type = element.data("type");
            vm.clickedComponent.adapter = element.data("adapter");
            vm.clickedComponent.device = element.data("device");
            vm.clickedComponent.regError = element.data("regError");
            vm.clickedComponent.depError = element.data("depError");
            vm.clickedComponent.deployed = element.data("deployed");
            vm.clickedComponent.element = element;
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

      function saveData() {
        var element = vm.clickedComponent.element;
        if (element) {
          if (element.attr("class").indexOf("device") > -1) {
            element.data("name", vm.clickedComponent.name);
            element.data("type", vm.clickedComponent.type);
            element.data("mac", vm.clickedComponent.mac);
            element.data("ip", vm.clickedComponent.ip);
            element.data("username", vm.clickedComponent.username);
            element.data("rsaKey", vm.clickedComponent.rsaKey);
            updateDeviceSA(element);
          } else if (element.attr("class").indexOf("actuator") > -1) {
            element.data("name", vm.clickedComponent.name);
            element.data("type", vm.clickedComponent.type);
            element.data("adapter", vm.clickedComponent.adapter);
          } else if (element.attr("class").indexOf("sensor") > -1) {
            element.data("name", vm.clickedComponent.name);
            element.data("type", vm.clickedComponent.type);
            element.data("adapter", vm.clickedComponent.adapter);
          }
        }

        $timeout(function() {
          vm.clickedComponent = {};
        });
        $(".close-icon").hide();
        $(".jtk-node").removeClass("clicked-element").css({
          'outline': "none"
        });
      }

      function drawModel(model) {
        clearCanvas();
        vm.currentModel = JSON.parse(model);
        console.log(vm.currentModel);
        var environment = JSON.parse(vm.currentModel.value);
        $.each(environment.nodes, function(index, node) {
          element = createElement(node.elementId, node);
          drawElement(element);
          element = "";
        });
        $.each(environment.connections, function(index, connection) {
          var conn = jsPlumbInstance.connect({
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

      function loadModels() {
        return ModelService.GetModelsByUsername().then(function(response) {
          console.log(response);
          vm.loadedModels = response.data;
        }, function(response) {
          console.log(response);
        });
      }

      function saveModel() {
        saveData();

        var savingIndividual = true;
        if (vm.processing.status) {
          savingIndividual = false;
        } else {
          vm.processing = {};
          vm.processing.status = true;
          vm.processing.finished = false;
        }
        vm.processing.saved = true;

        var totalCount = 0;
        var nodes = [];

        $(".jtk-node").each(function(index, element) {
          totalCount++;
          var $element = $(element);
          var type = $element.attr('class').toString().split(" ")[1];

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
              rsaKey: $element.data("rsaKey"),
              regError: $element.data("regError")
            });
          } else if (type == "actuator" || type == "sensor") {
            var parent = $($element.parent());
            // Do not add if parent is container. The elements are added below
            if (parent.attr("class").indexOf("as-container") == -1) {
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
            }
          } else if (type == "as-container") {
            var containerNodes = [];
            // Get the chlidren from the container, which are sensors and actuators
            $element.children(".jtk-node").each(function(indexC, elementC) {
              var $elementC = $(elementC);
              containerNodes.push({
                nodeType: $elementC.attr('class').toString().split(" ")[1],
                elementId: $elementC.attr('id'),
                clsName: $elementC.attr('class').toString(),
                positionX: parseInt($elementC.css("left"), 10),
                positionY: parseInt($elementC.css("top"), 10),
                width: $elementC.outerWidth(),
                height: $elementC.outerHeight(),
                angle: $elementC.data("angle"),
                id: $elementC.data("id"),
                name: $elementC.data("name"),
                type: $elementC.data("type"),
                adapter: $elementC.data("adapter"),
                device: $elementC.data("device"),
                deviceId: $elementC.data("deviceId"),
                deployed: $elementC.data("deployed"),
                regError: $elementC.data("regError"),
                depError: $elementC.data("depError")
              });
            });
            nodes.push({
              nodeType: type,
              elementId: $element.attr('id'),
              clsName: $element.attr('class').toString(),
              positionX: parseInt($element.css("left"), 10),
              positionY: parseInt($element.css("top"), 10),
              width: $element.outerWidth(),
              height: $element.outerHeight(),
              angle: $element.data("angle"),
              containerNodes: containerNodes
            });
          } else {
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

        var connections = [];
        $.each(jsPlumbInstance.getConnections(), function(index, connection) {
          connections.push({
            id: connection.id,
            sourceId: connection.source.id,
            targetId: connection.target.id,
            label: connection.getOverlay("label").canvas ? connection.getOverlay("label").canvas.innerText : connection.getOverlay("label").label,
            labelVisible: connection.getOverlay("label").isVisible()
          });
        });

        var environment = {};
        environment.nodes = nodes;
        environment.connections = connections;
        environment.numberOfElements = totalCount;
        environment.elementIdCount = elementIdCount;

        var model = {};
        model.value = JSON.stringify(environment);
        model.name = vm.currentModel.name;
        model.id = vm.currentModel.id;

        console.log(model);

        return ModelService.SaveModel(model).then(
          function(response) {
            if (savingIndividual) {
              vm.processing.message = "Model saved";
              vm.processing.success = true;
              processingTimeout();
            }
            console.log(response);
            vm.currentModel = response.data;
            vm.selectedOptionName = vm.currentModel.name;
            loadModels();
          },
          function(response) {
            if (savingIndividual) {
              vm.processing.message = response.headers('X-MBP-error') ? response.headers('X-MBP-error') : "Model saving error";
              vm.processing.success = false;
              processingTimeout();
            }
            vm.processing.saved = false;
            console.log(response);
          });

      }

      function deleteModel() {
        vm.processing = {};
        vm.processing.status = true;
        vm.processing.finished = false;
        vm.processing.undeployedDeregistered = true;

        vm.deletionPromises = [];
        $(".jtk-node").each(function(index, element) {
          var $element = $(element);
          var type = $element.attr('class').toString().split(" ")[1];
          if ($element.data("deployed")) {
            var promise = undeployComponent(type, $element, false);
            vm.deletionPromises.push(promise);
          } else if ($element.data("id")) {
            var promise = deregisterComponent(type, $element, false);
            vm.deletionPromises.push(promise);
          }
        });

        $q.all(vm.deletionPromises).then(function() {
          if (vm.processing.undeployedDeregistered) {
            ModelService.DeleteModel(vm.currentModel.name).then(function(response) {
              vm.processing.message = vm.currentModel.name + " deleted";
              vm.processing.success = true;
              processingTimeout();
              console.log(response);
              newModel();
            }, function(response) {
              vm.processing.message = "Deletion error";
              vm.processing.success = false;
              processingTimeout();
              console.log(response);
            });
          } else {
            vm.processing.message = "Undeployment or deregistration error";
            vm.processing.success = false;
            processingTimeout();
          }
        });
      }

      function clearCanvas() {
        jsPlumbInstance.unbind("connectionDetached");
        jsPlumbInstance.empty("canvas");
        $timeout(function() {
          vm.clickedComponent = {};
        });
        jsPlumbInstance.bind("connectionDetached", function(info) {
          onDetach(info);
        });
      }

      function newModel() {
        elementIdCount = 0;
        vm.currentModel = {};
        clearCanvas();
        loadModels().then(function(response) {
          $timeout(function() {
            vm.selectedOptionName = "";
            $("#select-models").val("");
          });
        });
      }

      function registerComponents() {
        saveData();

        vm.processing = {};
        vm.processing.status = true;
        vm.processing.registered = true;
        vm.processing.finished = false;

        // First register devices
        var devicePromises = [];
        $(".jtk-node").each(function(index, element) {
          var $element = $(element);
          var type = $element.attr('class').toString().split(" ")[1];

          if (type == "device" && !$element.data("id")) {
            var item = {};
            item.name = $element.data("name");
            item.componentType = $element.data("type");
            item.macAddress = DeviceService.normalizeMacAddress($element.data("mac"));
            item.ipAddress = $element.data("ip");
            item.username = $element.data("username");
            item.rsaKey = $element.data("rsaKey");
            var promise = register(type, item, $element);
            devicePromises.push(promise);
          }
        });

        // After all http requests
        $q.all(devicePromises).then(function() {
          // Then register actuators and sensors
          var actuatorSensorPromises = [];
          $(".jtk-node").each(function(index, element) {
            var $element = $(element);
            var type = $element.attr('class').toString().split(" ")[1];

            if ((type == "actuator" || type == "sensor") && !$element.data("id")) {
              var item = {};
              item.name = $element.data("name");
              item.componentType = $element.data("type");
              item.adapter = ENDPOINT_URI + "/adapters/" + $element.data("adapter");
              item.device = ENDPOINT_URI + "/devices/" + $element.data("deviceId");
              var promise = register(type, item, $element);
              actuatorSensorPromises.push(promise);
            }
          });

          $q.all(actuatorSensorPromises).then(function() {
            if (devicePromises.length === 0 && actuatorSensorPromises.length === 0) {
              vm.processing.message = "No components to register";
              vm.processing.success = false;
              processingTimeout();
            } else {
              saveModel().then(function(response) {
                if (vm.processing.registered && vm.processing.saved) {
                  vm.processing.message = "Registration completed, model saved";
                  vm.processing.success = true;
                } else if (vm.processing.registered && !vm.processing.saved) {
                  vm.processing.message = "Registration completed, model saving error";
                  vm.processing.success = false;
                } else if (!vm.processing.registered && vm.processing.saved) {
                  vm.processing.message = "Registration error, model saved";
                  vm.processing.success = false;
                } else if (!vm.processing.registered && !vm.processing.saved) {
                  vm.processing.message = "Registration error, model saving error";
                  vm.processing.success = false;
                }
                processingTimeout();
              });
            }
          });
        });
      }

      function register(type, item, element) {
        return CrudService.addItem(type + "s", item).then(
          function(response) {
            console.log(response);
            element.data("id", response.id);
            if (type == "device") {
              updateDeviceSA(element);
            }
            element.removeData("regError");
            element.removeData("depError");
            element.removeClass("error-element");
            element.addClass("success-element");
          },
          function(response) {
            console.log(response);
            element.data("regError", response.name ? response.name.message : response.status);
            element.removeClass("success-element");
            element.addClass("error-element");
            vm.processing.registered = false;
          });
      }

      function deployComponents() {
        vm.processing = {};
        vm.processing.status = true;
        vm.processing.deployed = true;
        vm.processing.finished = false;

        var deployPromises = [];
        $(".jtk-node").each(function(index, element) {
          var $element = $(element);
          var type = $element.attr('class').toString().split(" ")[1];

          if (type == "actuator" && !$element.data("deployed")) {
            var promise = deploy(ENDPOINT_URI + "/deploy/actuator/" + $element.data("id"), $element);
            deployPromises.push(promise);
          } else if (type == "sensor" && !$element.data("deployed")) {
            var promise = deploy(ENDPOINT_URI + "/deploy/sensor/" + $element.data("id"), $element);
            deployPromises.push(promise);
          }
        });

        $q.all(deployPromises).then(function() {
          if (deployPromises.length === 0) {
            vm.processing.message = "No components to deploy";
            vm.processing.success = false;
            processingTimeout();
          } else {
            saveModel().then(function(response) {
              if (vm.processing.deployed && vm.processing.saved) {
                vm.processing.message = "Deployment completed, model saved";
                vm.processing.success = true;
              } else if (vm.processing.deployed && !vm.processing.saved) {
                vm.processing.message = "Deployment completed, model saving error";
                vm.processing.success = false;
              } else if (!vm.processing.deployed && vm.processing.saved) {
                vm.processing.message = "Deployment error, model saved";
                vm.processing.success = false;
              } else if (!vm.processing.deployed && !vm.processing.saved) {
                vm.processing.message = "Deployment error, model saving error";
                vm.processing.success = false;
              }
              processingTimeout();
            });
          }
        });

        // $("#saveModelBtn").attr("disabled", true);
        // $("#clearCanvasBtn").attr("disabled", true);
        // $("#deleteModelBtn").attr("disabled", true);
        // $("#registerComponentsBtn").attr("disabled", true);
        // $("#deployComponentsBtn").attr("disabled", true);
        // $("#undeployComponentsBtn").attr("disabled", false);
      }

      function deploy(component, element) {
        vm.parameterValues = [];
        return ComponentService.deploy(vm.parameterValues, component).then(
          function(response) {
            console.log(response);
            element.data("deployed", true);
            element.removeData("regError");
            element.removeData("depError");
            element.removeClass("error-element");
            element.removeClass("success-element");
            element.addClass("deployed-element");
          },
          function(response) {
            console.log(response);
            element.data("depError", response.data ? response.data.globalMessage : response.status);
            element.removeClass("success-element");
            element.removeClass("deployed-element");
            element.addClass("error-element");
            vm.processing.deployed = false;
          });
      }

      function undeployComponents() {
        vm.processing = {};
        vm.processing.status = true;
        vm.processing.undeployed = true;
        vm.processing.finished = false;

        var undeployPromises = [];
        $(".jtk-node").each(function(index, element) {
          var $element = $(element);
          var type = $element.attr('class').toString().split(" ")[1];

          if (type == "actuator" && $element.data("deployed")) {
            var promise = undeploy(ENDPOINT_URI + "/deploy/actuator/" + $element.data("id"), $element);
            undeployPromises.push(promise);
          } else if (type == "sensor" && $element.data("deployed")) {
            var promise = undeploy(ENDPOINT_URI + "/deploy/sensor/" + $element.data("id"), $element);
            undeployPromises.push(promise);
          }
        });

        $q.all(undeployPromises).then(function() {
          if (undeployPromises.length === 0) {
            vm.processing.message = "No components to undeploy";
            vm.processing.success = false;
            processingTimeout();
          } else {
            saveModel().then(function(response) {
              if (vm.processing.undeployed && vm.processing.saved) {
                vm.processing.message = "Undeployment completed, model saved";
                vm.processing.success = true;
              } else if (vm.processing.undeployed && !vm.processing.saved) {
                vm.processing.message = "Undeployment completed, model saving error";
                vm.processing.success = false;
              } else if (!vm.processing.undeployed && vm.processing.saved) {
                vm.processing.message = "Undeployment error, model saved";
                vm.processing.success = false;
              } else if (!vm.processing.undeployed && !vm.processing.saved) {
                vm.processing.message = "Undeployment error, model saving error";
                vm.processing.success = false;
              }
              processingTimeout();
            });
          }
        });

        // $("#saveModelBtn").attr("disabled", false);
        // $("#clearCanvasBtn").attr("disabled", false);
        // $("#deleteModelBtn").attr("disabled", false);
        // $("#registerComponentsBtn").attr("disabled", false);
        // $("#deployComponentsBtn").attr("disabled", false);
        // $("#undeployComponentsBtn").attr("disabled", true);
      }

      function undeploy(component, element) {
        return ComponentService.undeploy(component).then(
          function(response) {
            console.log(response);
            element.data("deployed", false);
            element.removeData("depError");
            element.removeData("regError");
            element.removeClass("error-element");
            element.removeClass("deployed-element");
            element.addClass("success-element");
          },
          function(response) {
            console.log(response);
            element.data("depError", response.data ? response.data.globalMessage : response.status);
            element.removeClass("success-element");
            element.removeClass("deployed-element");
            element.addClass("error-element");
            vm.processing.undeployed = false;
          });
      }

      // function update() { // update deployment status
      //   vm.processing.status = true;
      //   ComponentService.isDeployed(vm.sensorDetailsCtrl.item._links.deploy.href)
      //     .then(
      //       function(deployed) {
      //         console.log('update: available, ' + deployed);
      //         vm.processing.status = false;
      //         vm.deployer.available = true;
      //         vm.deployer.deployed = deployed;
      //       },
      //       function(response) {
      //         console.log('update: unavailable');
      //         vm.processing.status = false;
      //         vm.deployer.available = false;
      //       });
      // }
      //
      // $scope.isCollapsedLog = false;

      function processingTimeout() {
        vm.processing.status = false;
        vm.processing.finished = true;
        $timeout(function() {
          vm.processing.finished = false;
        }, 3000);
      }

    });

  }

})();