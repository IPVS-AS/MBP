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

      makeDraggable("#room", "window room custom");
      makeDraggable("#door", "window door custom");
      makeDraggable("#defaultDevice", "window device default-device custom");
      makeDraggable("#defaultActuator", "window actuator default-actuator custom");
      makeDraggable("#defaultSensor", "window sensor default-sensor custom");

      //make the editor canvas droppable
      $(canvasId).droppable({
        accept: ".window",
        drop: function(event, ui) {
          if (clicked) {
            clicked = false;
            elementIdCount++;
            var id = "canvasWindow" + elementIdCount;
            element = createElement(id, undefined);
            drawElement(element);
            element = "";
          }
        }
      });

      //take the x, y coordinates of the current mouse position
      var x, y;
      $(document).on("mousemove", function(event) {
        x = event.pageX;
        y = event.pageY;
        if (clicked) {
          properties.top = y - 108;
          properties.left = x - 268;
        }
      });

      // Temporary saved properties of clicked element in palette
      // The data is used to create the element on drop
      function loadProperties(clsName) {
        properties = {};
        properties.clsName = clsName;
        clicked = true;
      }

      //load properties of a room element once the end element in the palette is clicked
      $('#room').mousedown(function() {
        loadProperties("window room custom jtk-node");
      });

      //load properties of a door element once the end element in the palette is clicked
      $('#door').mousedown(function() {
        loadProperties("window door custom jtk-node");
      });

      //load properties of a device element once the end element in the palette is clicked
      $('#defaultDevice').mousedown(function() {
        loadProperties("window device default-device custom jtk-node");
      });

      $('#defaultActuator').mousedown(function() {
        loadProperties("window actuator default-actuator custom jtk-node");
      });

      $('#defaultSensor').mousedown(function() {
        loadProperties("window sensor default-sensor custom jtk-node");
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
          if (properties.clsName.indexOf("room") > -1) {
            element.outerWidth("250px");
            element.outerHeight("250px");
          }
          // Add connection square on device
          if (properties.clsName.indexOf("device") > -1) {
            element.append("<div class=\"ep\"></div>");
          }
        }

        element.append("<i style='display: none' class=\"fa fa-trash fa-lg close-icon\"><\/i>");
        return element;
      }

      function drawElement(element) {
        $(canvasId).append(element);
        jsPlumbInstance.draggable(jsPlumbInstance.getSelector(".jtk-node"), {
          filter: ".ui-resizable-handle"
        });
        addEndpoints(element);
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
        if ($(this).attr("class").indexOf("room") > -1) {
          $(this).css({
            'outline': "2px solid #4863A0"
          });
        }

      });

      $(canvasId).on('click', function(e) {
        saveData();
      });

      $(document).on("click", ".close-icon", function() {
        var element = $(this).parent();
        $timeout(function() {
          vm.clickedComponent = {};
          jsPlumbInstance.remove(element);
        });
      });

      // Add device name to sensor or actuator when a connection was created
      jsPlumbInstance.bind("connection", function(info) {
        var source = $(info.source);
        var target = $(info.target);
        if (target.attr("class").indexOf("device") == -1) {
          target.data("device", source.data("name"));
          target.data("deviceId", source.data("id"));
        }
      });

      // Remove device name from sensor or actuator when a connection is removed
      jsPlumbInstance.bind("connectionDetached", function(info) {
        var target = $(info.target);
        if (target.attr("class").indexOf("device") == -1) {
          target.removeData("device");
          target.removeData("deviceId");
        }
      });

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
        ModelService.GetModelsByUsername().then(function(response) {
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
            nodes.push({
              nodeType: type,
              elementId: $element.attr('id'),
              clsName: $element.attr('class').toString(),
              positionX: parseInt($element.css("left"), 10),
              positionY: parseInt($element.css("top"), 10),
              width: $element.outerWidth(),
              height: $element.outerHeight(),
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
          } else {
            nodes.push({
              nodeType: type,
              elementId: $element.attr('id'),
              clsName: $element.attr('class').toString(),
              positionX: parseInt($element.css("left"), 10),
              positionY: parseInt($element.css("top"), 10),
              width: $element.outerWidth(),
              height: $element.outerHeight(),
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

        if (!vm.currentModel.name || vm.currentModel.name === "") {
          vm.currentModel.name = "New model";
        }
        model.name = vm.currentModel.name;

        console.log(model);

        return ModelService.SaveModel(model).then(function(response) {
          if (savingIndividual) {
            vm.processing.message = "Model saved";
            vm.processing.success = true;
            vm.processing.status = false;
            vm.processing.finished = true;
          }
          console.log(response);
          vm.selectedOptionName = model.name;
          loadModels();
        }, function(response) {
          if (savingIndividual) {
            vm.processing.message = "Model saving error";
            vm.processing.success = false;
            vm.processing.status = false;
            vm.processing.finished = true;
          }
          vm.processing.saved = false;
          console.log(response);
        });

      }

      function deleteModel() {
        vm.processing = {};
        vm.processing.status = true;
        vm.processing.finished = false;
        ModelService.DeleteModel(vm.currentModel.name).then(function(response) {
          vm.processing.message = vm.currentModel.name + " deleted";
          vm.processing.success = true;
          vm.processing.status = false;
          vm.processing.finished = true;
          console.log(response);
          newModel();
        }, function(response) {
          vm.processing.message = "Deletion error";
          vm.processing.success = false;
          vm.processing.status = false;
          vm.processing.finished = true;
          console.log(response);
        });
      }

      function clearCanvas() {
        jsPlumbInstance.empty("canvas");
        $timeout(function() {
          vm.clickedComponent = {};
        });
      }

      function newModel() {
        elementIdCount = 0;
        vm.currentModel = {};
        vm.selectedOptionName = "";
        clearCanvas();
        loadModels();
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
            var promise = register("devices", item, type, $element);
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
              var promise = register(type + "s", item, type, $element);
              actuatorSensorPromises.push(promise);
            }
          });

          $q.all(actuatorSensorPromises).then(function() {
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
              vm.processing.status = false;
              vm.processing.finished = true;
            });
          });
        });
      }

      function register(category, item, type, element) {
        return CrudService.addItem(category, item).then(
          function(response) {
            console.log(response);
            if (type == "actuator" || type == "sensor") {
              element.data("id", response.id);
            } else if (type == "device") {
              element.data("id", response.id);
              updateDeviceSA(element);
            }
            element.removeData("regError");
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
            vm.processing.status = false;
            vm.processing.finished = true;
          });
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
            vm.processing.status = false;
            vm.processing.finished = true;
          });
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

    });

  }

})();