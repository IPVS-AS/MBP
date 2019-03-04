(function() {
  'use strict';

  angular
    .module('app')
    .controller('ModelController', ModelController);

  ModelController.$inject = ['ENDPOINT_URI', '$scope', '$timeout', '$controller',
    'ModelService', 'FlashService', 'ComponentService', 'DeviceService', 'CrudService', 'adapterList'
  ];

  function ModelController(ENDPOINT_URI, $scope, $timeout, $controller,
    ModelService, FlashService, ComponentService, DeviceService, CrudService, adapterList) {
    var vm = this;

    jsPlumb.ready(function() {
      var jsPlumbInstance;
      var canvasId = "#canvas";
      var elementCount = 0;
      var properties = {}; // keeps the properties of each element
      var element = ""; // the element which will be appended to the canvas
      var clicked = false; // true if an element from the palette was clicked
      vm.processing = false;
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
            elementCount++;
            var id = "canvasWindow" + elementCount;
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
          } else if (node.nodeType == "actuator" || node.nodeType == "sensor") {
            element.data("id", node.id);
            element.data("name", node.name);
            element.data("type", node.type);
            element.data("adapter", node.adapter);
            element.data("device", node.device);
            element.data("deviceId", node.deviceId);
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
          targetEndpoint.maxConnections = null;
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
            'outline': "2px solid red"
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
            vm.clickedComponent.name = element.data("name");
            vm.clickedComponent.type = element.data("type");
            vm.clickedComponent.mac = element.data("mac");
            vm.clickedComponent.ip = element.data("ip");
            vm.clickedComponent.username = element.data("username");
            vm.clickedComponent.rsaKey = element.data("rsaKey");
            vm.clickedComponent.element = element;
          } else if (element.attr("class").indexOf("actuator") > -1) {
            vm.clickedComponent.category = "ACTUATOR";
            vm.clickedComponent.name = element.data("name");
            vm.clickedComponent.type = element.data("type");
            vm.clickedComponent.adapter = element.data("adapter");
            vm.clickedComponent.device = element.data("device");
            vm.clickedComponent.element = element;
          } else if (element.attr("class").indexOf("sensor") > -1) {
            vm.clickedComponent.category = "SENSOR";
            vm.clickedComponent.name = element.data("name");
            vm.clickedComponent.type = element.data("type");
            vm.clickedComponent.adapter = element.data("adapter");
            vm.clickedComponent.device = element.data("device");
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
        elementCount = environment.numberOfElements;
      }

      function loadModels() {
        ModelService.GetModelsByUsername().then(function(response) {
          FlashService.Success("Model loaded!", false);
          console.log(response);
          vm.loadedModels = response.data;
        }, function(response) {
          FlashService.Error("Loading error!", false);
          console.log(response);
        });
      }

      function saveModel() {
        saveData();

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
              rsaKey: $element.data("rsaKey")
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
              deviceId: $element.data("deviceId")
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

        var model = {};
        model.value = JSON.stringify(environment);

        if (!vm.currentModel.name || vm.currentModel.name === "") {
          vm.currentModel.name = "New model";
        }
        model.name = vm.currentModel.name;

        console.log(model);

        ModelService.SaveModel(model).then(function(response) {
          FlashService.Success("Model saved!", false);
          console.log(response);
          vm.selectedOptionName = model.name;
          loadModels();
        }, function(response) {
          FlashService.Error("Saving error!", false);
          console.log(response);
        });

      }

      function deleteModel() {
        ModelService.DeleteModel(vm.currentModel.name).then(function(response) {
          FlashService.Success("Model deleted!", false);
          console.log(response);
          newModel();
        }, function(response) {
          FlashService.Error("Deletion error!", false);
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
        vm.currentModel = {};
        vm.selectedOptionName = "";
        clearCanvas();
        loadModels();
      }

      function registerComponents() {
        vm.processing = true;

        // First register devices
        $(".jtk-node").each(function(index, element) {
          var $element = $(element);
          var type = $element.attr('class').toString().split(" ")[1];

          if (type == "device") {
            var item = {};
            item.name = $element.data("name");
            item.componentType = $element.data("type");
            item.macAddress = DeviceService.normalizeMacAddress($element.data("mac"));
            item.ipAddress = $element.data("ip");
            item.username = $element.data("username");
            item.rsaKey = $element.data("rsaKey");
            register("devices", item, type, $element);
          }
        });

        // Then register actuators and sensors
        $(".jtk-node").each(function(index, element) {
          var $element = $(element);
          var type = $element.attr('class').toString().split(" ")[1];

          if (type == "actuator" || type == "sensor") {
            var item = {};
            item.name = $element.data("name");
            item.componentType = $element.data("type");
            item.adapter = ENDPOINT_URI + "/adapters/" + $element.data("adapter");
            item.device = ENDPOINT_URI + "/devices/" + $element.data("deviceId");
            register(type + "s", item, type, $element);
          }
        });

        $("#deployComponentsBtn").attr("disabled", false);
        vm.processing = false;
      }

      function register(category, item, type, element) {
        CrudService.addItem(category, item).then(
          function(response) {
            console.log(response);
            if (type == "actuator" || type == "sensor") {
              element.data("id", response.id);
            } else if (type == "device") {
              element.data("id", response.id);
              updateDeviceSA(element);
            }
          },
          function(response) {
            console.log(response);
          });
      }

      function deployComponents() {
        $(".jtk-node").each(function(index, element) {
          var $element = $(element);
          var type = $element.attr('class').toString().split(" ")[1];

          if (type == "actuator") {
            deploy(ENDPOINT_URI + "/deploy/actuator/" + $element.data("id"));
          } else if (type == "sensor") {
            deploy(ENDPOINT_URI + "/deploy/sensor/" + $element.data("id"));
          }
        });

        $("#saveModelBtn").attr("disabled", true);
        $("#clearCanvasBtn").attr("disabled", true);
        $("#deleteModelBtn").attr("disabled", true);
        $("#registerComponentsBtn").attr("disabled", true);
        $("#deployComponentsBtn").attr("disabled", true);
        $("#undeployComponentsBtn").attr("disabled", false);
      }

      function undeployComponents() {
        $(".jtk-node").each(function(index, element) {
          var $element = $(element);
          var type = $element.attr('class').toString().split(" ")[1];

          if (type == "actuator") {
            undeploy(ENDPOINT_URI + "/deploy/actuator/" + $element.data("id"));
          } else if (type == "sensor") {
            undeploy(ENDPOINT_URI + "/deploy/sensor/" + $element.data("id"));
          }
        });

        $("#saveModelBtn").attr("disabled", false);
        $("#clearCanvasBtn").attr("disabled", false);
        $("#deleteModelBtn").attr("disabled", false);
        $("#registerComponentsBtn").attr("disabled", false);
        $("#deployComponentsBtn").attr("disabled", false);
        $("#undeployComponentsBtn").attr("disabled", true);
      }

      // function update() { // update deployment status
      //   vm.processing = true;
      //   ComponentService.isDeployed(vm.sensorDetailsCtrl.item._links.deploy.href)
      //     .then(
      //       function(deployed) {
      //         console.log('update: available, ' + deployed);
      //         vm.processing = false;
      //         vm.deployer.available = true;
      //         vm.deployer.deployed = deployed;
      //       },
      //       function(response) {
      //         console.log('update: unavailable');
      //         vm.processing = false;
      //         vm.deployer.available = false;
      //       });
      // }
      //
      // $scope.isCollapsedLog = false;

      function deploy(component) {
        vm.processing = true;
        vm.parameterValues = [];

        ComponentService.deploy(vm.parameterValues, component)
          .then(
            function(response) {
              vm.processing = false;
              // vm.deployer.deployed = true;
              // vm.deployer.status = response.data;
              // vm.deployer.update();
              console.log(response.data);
            },
            function(response) {
              vm.processing = false;
              // vm.deployer.status = response.data;
              // vm.deployer.update();
              console.log(response.data);
            });
      }

      function undeploy(component) {
        vm.processing = true;
        ComponentService.undeploy(component)
          .then(
            function(response) {
              vm.processing = false;
              // vm.deployer.deployed = false;
              // vm.deployer.status = response.data;
              // vm.deployer.update();
              console.log(response.data);
            },
            function(response) {
              vm.processing = false;
              // vm.deployer.status = response.data;
              // vm.deployer.update();
              console.log(response.data);
            });
      }


    });

  }

})();