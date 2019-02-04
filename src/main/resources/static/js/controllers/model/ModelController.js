(function() {
  'use strict';

  angular
    .module('app')
    .controller('ModelController', ModelController);

  ModelController.$inject = ['$timeout', 'ModelService', 'FlashService'];

  function ModelController($timeout, ModelService, FlashService) {
    var vm = this;

    jsPlumb.ready(function() {
      var jsPlumbInstance;
      var canvasId = "#canvas";
      var elementCount = 0;
      var properties = {}; // keeps the properties of each element
      var element = ""; // the element which will be appended to the canvas
      var clicked = false; // true if an element from the palette was clicked
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
      vm.registerComponent = registerComponent;
      vm.registerComponents = registerComponents;
      vm.deployComponents = deployComponents;
      vm.undeployComponents = undeployComponents;

      (function initController() {
        vm.loadModels();
      })();

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
            label: "FOO",
            id: "label",
            cssClass: "aLabel"
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

      // bind a click listener to each connection; the connection is deleted on click
      jsPlumbInstance.bind("click", jsPlumbInstance.deleteConnection);

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
            element.data("name", node.name);
            element.data("type", node.type);
            element.data("mac", node.mac);
            element.data("ip", node.ip);
            element.data("username", node.username);
          } else if (node.nodeType == "actuator" || node.nodeType == "sensor") {
            element.data("name", node.name);
            element.data("type", node.type);
            element.data("adapter", node.adapter);
            element.data("device", node.device);
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
          jsPlumbInstance.makeSource(element, sourceEndpoint);
          jsPlumbInstance.makeTarget(element, targetEndpoint);
        } else if (type == "actuator" || type == "sensor") {
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
        jsPlumbInstance.remove($(this).parent().attr("id"));
      });

      function loadData(element) {
        $timeout(function() {
          if (element.attr("class").indexOf("device") > -1) {
            vm.clickedComponent.category = "DEVICE";
            vm.clickedComponent.name = element.data("name");
            vm.clickedComponent.type = element.data("type");
            vm.clickedComponent.mac = element.data("mac");
            vm.clickedComponent.ip = element.data("ip");
            vm.clickedComponent.username = element.data("username");
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
          } else if (element.attr("class").indexOf("actuator") > -1) {
            element.data("name", vm.clickedComponent.name);
            element.data("type", vm.clickedComponent.type);
            element.data("adapter", vm.clickedComponent.adapter);
            element.data("device", vm.clickedComponent.device);
          } else if (element.attr("class").indexOf("sensor") > -1) {
            element.data("name", vm.clickedComponent.name);
            element.data("type", vm.clickedComponent.type);
            element.data("adapter", vm.clickedComponent.adapter);
            element.data("device", vm.clickedComponent.device);
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
          jsPlumbInstance.connect({
            source: connection.sourceId,
            target: connection.targetId,
            type: "basic"
          });
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
              name: $element.data("name"),
              type: $element.data("type"),
              mac: $element.data("mac"),
              ip: $element.data("ip"),
              username: $element.data("username")
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
              name: $element.data("name"),
              type: $element.data("type"),
              adapter: $element.data("adapter"),
              device: $element.data("device")
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
            targetId: connection.target.id
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

      function registerComponent() {

      }

      function registerComponents() {

      }

      function deployComponents() {

      }

      function undeployComponents() {

      }


    });

  }

})();