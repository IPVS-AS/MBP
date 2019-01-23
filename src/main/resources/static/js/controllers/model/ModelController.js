(function() {
  'use strict';

  angular
    .module('app')
    .controller('ModelController', ModelController);

  ModelController.$inject = ['$timeout', 'ModelService', 'FlashService'];

  function ModelController($timeout, ModelService, FlashService) {
    var vm = this;

    (function initController() {
      // Load models
      ModelService.GetModelsByUsername().then(function(response) {
        FlashService.Success("Model loaded!", false);
        console.log(response);
      }, function(response) {
        FlashService.Error("Loading error!", false);
        console.log(response);
      });
    })();

    jsPlumb.ready(function() {
      var jsPlumbInstance;
      var canvasId = "#canvas";
      var endpointList = [];
      var sourcepointList = [];
      var elementCount = 0;
      var properties = {}; // keeps the properties of each element
      var element = ""; // the element which will be appended to the canvas
      var clicked = false; // true if an element from the palette was clicked
      vm.clickedComponent = {};
      vm.saveModel = saveModel;
      vm.deleteModel = deleteModel;
      vm.clearCanvas = clearCanvas;
      vm.registerComponent = registerComponent;
      vm.registerComponents = registerComponents;
      vm.deployComponents = deployComponents;
      vm.undeployComponents = undeployComponents;

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
            id: "label"
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

      // //the source endpoint definition from which a connection can be started
      // sourceEndpoint = {
      //   endpoint: "Dot",
      //   paintStyle: {
      //     stroke: "#7AB02C",
      //     fill: "transparent",
      //     radius: 7,
      //     strokeWidth: 3
      //   },
      //   isSource: true,
      //   connector: ["Flowchart", {
      //     stub: [40, 60],
      //     gap: 5,
      //     cornerRadius: 5,
      //     alwaysRespectStubs: true
      //   }],
      //   connectorStyle: {
      //       strokeWidth: 4,
      //       stroke: "#61B7CF",
      //       joinstyle: "round",
      //       outlineColor: "white",
      //       outlineWidth: 2
      //     },
      //   hoverPaintStyle: {
      //     fill: "#216477",
      //     stroke: "#216477"
      //   },
      //   connectorHoverStyle: {
      //     strokeWidth: 4,
      //     stroke: "#216477",
      //     outlineWidth: 2,
      //     outlineColor: "white"
      //   },
      //   EndpointOverlays: [],
      //   maxConnections: -1,
      //   dragOptions: {},
      //   connectorOverlays: [
      //     ["Label", {
      //       location: 1,
      //       visible: true,
      //       id: "label",
      //       direction: 1
      //     }]
      //   ]
      // },

      var targetEndpoint = {
        dropOptions: {
          hoverClass: "dragHover"
        },
        anchor: "Continuous",
        allowLoopback: false
      };

      // //definition of the target endpoint the connector would end
      // targetEndpoint = {
      //   endpoint: "Dot",
      //   paintStyle: {
      //     fill: "#7AB02C",
      //     radius: 9
      //   },
      //   maxConnections: -1,
      //   dropOptions: {
      //     hoverClass: "hover",
      //     activeClass: "active"
      //   },
      //   hoverPaintStyle: endpointHoverStyle,
      //   isTarget: true
      // };

      // bind a click listener to each connection; the connection is deleted on click
      jsPlumbInstance.bind("click", jsPlumbInstance.deleteConnection);

      function makeResizable(id) {
        $(id).resizable({
          resize: function(event, ui) {
            jsPlumbInstance.revalidate(ui.helper);
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
            element = createElement(id);
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
        properties.clsName = clsName;
      }

      //load properties of a room element once the end element in the palette is clicked
      $('#room').mousedown(function() {
        loadProperties("window room custom jtk-node");
        clicked = true;
      });

      //load properties of a door element once the end element in the palette is clicked
      $('#door').mousedown(function() {
        loadProperties("window door custom jtk-node");
        clicked = true;
      });

      //load properties of a device element once the end element in the palette is clicked
      $('#defaultDevice').mousedown(function() {
        loadProperties("window device default-device custom jtk-node");
        clicked = true;
      });

      $('#defaultActuator').mousedown(function() {
        loadProperties("window actuator default-actuator custom jtk-node");
        clicked = true;
      });

      $('#defaultSensor').mousedown(function() {
        loadProperties("window sensor default-sensor custom jtk-node");
        clicked = true;
      });

      //create an element to be drawn on the canvas
      function createElement(id) {
        var element = $('<div>').addClass(properties.clsName).attr('id', id);
        // The position to drop the element
        element.css({
          'top': properties.top,
          'left': properties.left
        });

        if (properties.clsName.indexOf("room") > -1) {
          element.outerWidth("250px");
          element.outerHeight("250px");
        }

        if (properties.clsName.indexOf("device") > -1) {
          element.append("<div class=\"ep\"></div>");
        }
        element.append("<i style='display: none' class=\"fa fa-trash fa-lg close-icon\"><\/i>");
        return element;
      }

      function addEndpoints(element) {
        var type = element.attr('class').toString().split(" ")[1];
        if (type == "device") {
          jsPlumbInstance.makeSource(element, sourceEndpoint);
        } else if (type == "actuator" || type == "sensor") {
          jsPlumbInstance.makeTarget(element, targetEndpoint);
        }
      };

      // //add the endpoints for the elements
      // var ep;
      //
      // function addEndpoints(toId, sourceAnchors, targetAnchors) {
      //   for (var i = 0; i < sourceAnchors.length; i++) {
      //     var sourceUUID = toId + sourceAnchors[i];
      //     ep = jsPlumbInstance.addEndpoint("canvas" + toId, sourceEndpoint, {
      //       anchor: sourceAnchors[i],
      //       uuid: sourceUUID
      //     });
      //     sourcepointList.push(["canvas" + toId, ep]);
      //     ep.canvas.setAttribute("title", "Drag a connection from here");
      //     ep = null;
      //   }
      //   for (var j = 0; j < targetAnchors.length; j++) {
      //     var targetUUID = toId + targetAnchors[j];
      //     ep = jsPlumbInstance.addEndpoint("canvas" + toId, targetEndpoint, {
      //       anchor: targetAnchors[j],
      //       uuid: targetUUID
      //     });
      //     endpointList.push(["canvas" + toId, ep]);
      //     ep.canvas.setAttribute("title", "Drop a connection here");
      //     ep = null;
      //   }
      // };


      function drawElement(element) {
        $(canvasId).append(element);
        // addEndpoints(name, properties[0].startpoints, properties[0].endpoints);
        jsPlumbInstance.draggable(jsPlumbInstance.getSelector(".jtk-node"), {
          filter: ".ui-resizable-handle"
        });
        addEndpoints(element);
        makeResizable(".custom");
      }






      $(document).on("click", ".custom", function() {
        loadData($(this));

        var marginLeft = $(this).outerWidth() + 8 + "px";
        $(".close-icon").prop("title", "Delete the element");
        $(this).find("i").css({
          'margin-left': marginLeft,
          'margin-top': "-10px"
        }).show();

        // if ($(this).attr("class").indexOf("diamond") == -1) {
        //   var marginLeft = $(this).outerWidth() + 8 + "px";
        //   $(".close-icon").prop("title", "Delete the element");
        //   $(this).find("i").css({
        //     'margin-left': marginLeft,
        //     'margin-top': "-10px"
        //   }).show();
        // } else {
        //   $(this).find("i").css({
        //     'margin-left': "35px",
        //     'margin-top': "-40px"
        //   }).show();
        // }

      });

      $(canvasId).on('click', function(e) {
        saveData();

        $(".jtk-node").css({
          'outline': "none"
        });
        $(".close-icon").hide();
        if (e.target.nodeName == "P") {
          e.target.parentElement.parentElement.style.outline = "4px solid red";
        } else if (e.target.nodeName == "STRONG") {
          e.target.parentElement.style.outline = "4px solid red";
        } else if (e.target.getAttribute("class") != null && e.target.getAttribute("class").indexOf("jtk-node") > -1) { //when clicked the step, decision or i/o elements
          e.target.style.outline = "4px solid red";
        }
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
      }


      function loadModel(model) {
        var environment = JSON.parse(model.value);
        $.each(environment.nodes, function(index, node) {
          // loadProperties(node.id...)
          // createElement
          // drawElement
        });

      }



      function saveModel() {
        saveData();

        var totalCount = 0;

        if (elementCount == 0) {
          alert("The blueprint should have at least one element");
        } else {
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
              connectionId: connection.id,
              sourceUUId: connection.endpoints[0].getUuid(),
              targetUUId: connection.endpoints[1].getUuid()
            });
          });

          var environment = {};
          environment.nodes = nodes;
          environment.connections = connections;
          environment.numberOfElements = totalCount;

          var model = {};
          model.value = JSON.stringify(environment);
          model.name = "House";

          console.log(model.value);

          ModelService.SaveModel(model).then(function(response) {
            FlashService.Success("Model saved!", false);
            console.log(response);
          }, function(response) {
            FlashService.Error("Saving error!", false);
            console.log(response);
          });
        }
      }

      function deleteModel() {
        clearCanvas();
        ModelService.DeleteModel("House").then(function(response) {
          FlashService.Success("Model deleted!", false);
          console.log(response);
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