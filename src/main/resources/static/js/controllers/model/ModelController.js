(function() {
  'use strict';

  angular
    .module('app')
    .controller('ModelController', ModelController);

  ModelController.$inject = ['$timeout'];

  function ModelController($timeout) {
    var vm = this;
    vm.clickedComponent = {};
    vm.elementIsDevice = false;
    vm.elementIsActuator = false;
    vm.elementIsSensor = false;

    // This project was done as a PoC for the WSO2 PC. This content is shared only for the learning purpose of the users.
    var endpointList = [];
    var sourcepointList = [];
    var _saveFlowchart, elementCount = 0;
    var jsPlumbInstance; //the jsPlumb jsPlumbInstance
    var properties = []; //keeps the properties of each element

    jsPlumb.ready(function() {
      var element = ""; //the element which will be appended to the canvas
      var clicked = false; //check whether an element from the palette was clicked

      jsPlumbInstance = window.jsp = jsPlumb.getInstance({
        // default drag options
        DragOptions: {
          cursor: 'pointer',
          zIndex: 2000
        },
        //the arrow overlay for the connection
        ConnectionOverlays: [
          ["Arrow", {
            location: 1,
            visible: true,
            id: "ARROW"
          }]
        ],
        Container: "canvas"
      });

      //define basic connection type
      var basicType = {
        connector: "StateMachine",
        paintStyle: {
          stroke: "#216477",
          strokeWidth: 4
        },
        hoverPaintStyle: {
          stroke: "blue"
        }
      };
      jsPlumbInstance.registerConnectionType("basic", basicType);

      //style for the connector
      var connectorPaintStyle = {
          strokeWidth: 4,
          stroke: "#61B7CF",
          joinstyle: "round",
          outlineColor: "white",
          outlineWidth: 2
        },

        //style for the connector hover
        connectorHoverStyle = {
          strokeWidth: 4,
          stroke: "#216477",
          outlineWidth: 2,
          outlineColor: "white"
        },
        endpointHoverStyle = {
          fill: "#216477",
          stroke: "#216477"
        },

        //the source endpoint definition from which a connection can be started
        sourceEndpoint = {
          endpoint: "Dot",
          paintStyle: {
            stroke: "#7AB02C",
            fill: "transparent",
            radius: 7,
            strokeWidth: 3
          },
          isSource: true,
          connector: ["Flowchart", {
            stub: [40, 60],
            gap: 5,
            cornerRadius: 5,
            alwaysRespectStubs: true
          }],
          connectorStyle: connectorPaintStyle,
          hoverPaintStyle: endpointHoverStyle,
          connectorHoverStyle: connectorHoverStyle,
          EndpointOverlays: [],
          maxConnections: -1,
          dragOptions: {},
          connectorOverlays: [
            ["Arrow", {
              location: 1,
              visible: true,
              id: "ARROW",
              direction: 1
            }]
          ]
        },

        //definition of the target endpoint the connector would end
        targetEndpoint = {
          endpoint: "Dot",
          paintStyle: {
            fill: "#7AB02C",
            radius: 9
          },
          maxConnections: -1,
          dropOptions: {
            hoverClass: "hover",
            activeClass: "active"
          },
          hoverPaintStyle: endpointHoverStyle,
          isTarget: true
        };

      function makeResizable(id) {
        $(id).resizable({
          resize: function(event, ui) {
            jsPlumbInstance.revalidate(ui.helper);
          },
          handles: "all"
        });
      }

      function makeDraggable(id, className, text) {
        $(id).draggable({
          helper: function() {
            return $("<div/>", {
              text: text,
              class: className
            });
          },
          stack: ".custom",
          revert: false
        });
      }

      makeDraggable("#startEv", "window start jtk-connected custom", "start");
      makeDraggable("#stepEv", "window step jtk-connected-step custom", "step");
      makeDraggable("#endEv", "window start jtk-connected-end custom", "end");
      makeDraggable("#room", "window room custom", "room");
      makeDraggable("#door", "window door custom", "door");
      makeDraggable("#defaultDevice", "window device default-device custom", "default device");
      makeDraggable("#defaultActuator", "window actuator default-actuator custom", "default actuator");
      makeDraggable("#defaultSensor", "window sensor default-sensor custom", "default sensor");

      $("#descEv").draggable({
        helper: function() {
          return createElement("");
        },
        stack: ".custom",
        revert: false
      });

      //make the editor canvas droppable
      $("#canvas").droppable({
        accept: ".window",
        drop: function(event, ui) {
          if (clicked) {
            clicked = false;
            elementCount++;
            var name = "Window" + elementCount;
            var id = "canvasWindow" + elementCount;
            element = createElement(id);
            drawElement(element, "#canvas", name);
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
          properties[0].top = y - 108;
          properties[0].left = x - 268;
        }
      });

      var properties;
      var clicked = false;

      function loadProperties(clsName, left, top, label, startpoints, endpoints, contenteditable) {
        properties = [];
        properties.push({
          left: left,
          top: top,
          clsName: clsName,
          label: label,
          startpoints: startpoints,
          endpoints: endpoints,
          contenteditable: contenteditable
        });
      }

      //load properties of a start element once the start element in the palette is clicked
      $('#startEv').mousedown(function() {
        loadProperties("window start custom jtk-node jtk-connected", "5em", "5em", "start", ["BottomCenter"],
          [], false);
        clicked = true;
      });

      //load properties of a step element once the step element in the palette is clicked
      $('#stepEv').mousedown(function() {
        loadProperties("window step custom jtk-node jtk-connected-step", "5em", "5em", "step",
          ["BottomCenter"], ["TopCenter"], true);
        clicked = true;
      });

      //load properties of a decision element once the decision element in the palette is clicked
      $('#descEv').mousedown(function() {
        loadProperties("window diamond custom jtk-node jtk-connected-step", "5em", "5em", "decision",
          ["LeftMiddle", "RightMiddle", "BottomCenter"], ["TopCenter"], true, 100, 100);
        clicked = true;
      });

      //load properties of a end element once the end element in the palette is clicked
      $('#endEv').mousedown(function() {
        loadProperties("window end custom jtk-node jtk-connected-end", "5em", "5em", "end",
          [], ["TopCenter"], false);
        clicked = true;
      });

      //load properties of a room element once the end element in the palette is clicked
      $('#room').mousedown(function() {
        loadProperties("window room custom jtk-node", "3em", "3em", "room",
          [], [], false);
        clicked = true;
      });

      //load properties of a door element once the end element in the palette is clicked
      $('#door').mousedown(function() {
        loadProperties("window door custom jtk-node", "5em", "5em", "door",
          [], [], false);
        clicked = true;
      });

      //load properties of a device element once the end element in the palette is clicked
      $('#defaultDevice').mousedown(function() {
        loadProperties("window device default-device custom jtk-node", "5em", "5em", "default device",
          ["LeftMiddle", "RightMiddle", "BottomCenter", "TopCenter"], [], false);
        clicked = true;
      });

      $('#defaultActuator').mousedown(function() {
        loadProperties("window actuator default-actuator custom jtk-node", "5em", "5em", "default actuator",
          [], ["LeftMiddle", "RightMiddle", "BottomCenter", "TopCenter"], false);
        clicked = true;
      });

      $('#defaultSensor').mousedown(function() {
        loadProperties("window sensor default-sensor custom jtk-node", "5em", "5em", "default sensor",
          [], ["LeftMiddle", "RightMiddle", "BottomCenter", "TopCenter"], false);
        clicked = true;
      });

      //create an element to be drawn on the canvas
      function createElement(id) {
        var elm = $('<div>').addClass(properties[0].clsName).attr('id', id);
        // if (properties[0].clsName.indexOf("diamond") > -1) {
        //   elm.outerWidth("100px");
        //   elm.outerHeight("100px");
        // }

        // The position to drop the element
        elm.css({
          'top': properties[0].top,
          'left': properties[0].left
        });

        // var strong = $('<strong>');
        // if (properties[0].clsName == "window diamond custom jtk-node jtk-connected-step") {
        //   elm.append("<i style='display: none; margin-left: -5px; margin-top: -50px' " +
        //     "class=\"fa fa-trash fa-lg close-icon desc-text\"><\/i>");
        //   var p = "<p style='line-height: 110%; margin-top: 25px' class='desc-text' contenteditable='true' " +
        //     "ondblclick='$(this).focus();'>" + properties[0].label + "</p>";
        //   strong.append(p);
        // } else if (properties[0].clsName == "window parallelogram step custom jtk-node jtk-connected-step") {
        //   elm.append("<i style='display: none' class=\"fa fa-trash fa-lg close-icon input-text\"><\/i>");
        //   var p = "<p style='line-height: 110%; margin-top: 25px' class='input-text' contenteditable='true' " +
        //     "ondblclick='$(this).focus();'>" + properties[0].label +
        //     "</p>";
        //   strong.append(p);
        // } else if (properties[0].contenteditable) {
        //   elm.append("<i style='display: none' class=\"fa fa-trash fa-lg close-icon\"><\/i>");
        //   var p = "<p style='line-height: 110%; margin-top: 25px' contenteditable='true' " +
        //     "ondblclick='$(this).focus();'>" + properties[0].label + "</p>";
        //   strong.append(p);
        // } else {
        //   elm.append("<i style='display: none' class=\"fa fa-trash fa-lg close-icon\"><\/i>");
        //   var p = $('<p>').text(properties[0].label);
        //   strong.append(p);
        // }


        elm.append("<i style='display: none' class=\"fa fa-trash fa-lg close-icon\"><\/i>");
        // elm.data("name", properties[0].label);
        return elm;
      }

      //add the endpoints for the elements
      var ep;
      var _addEndpoints = function(toId, sourceAnchors, targetAnchors) {
        for (var i = 0; i < sourceAnchors.length; i++) {
          var sourceUUID = toId + sourceAnchors[i];
          ep = jsPlumbInstance.addEndpoint("canvas" + toId, sourceEndpoint, {
            anchor: sourceAnchors[i],
            uuid: sourceUUID
          });
          sourcepointList.push(["canvas" + toId, ep]);
          ep.canvas.setAttribute("title", "Drag a connection from here");
          ep = null;
        }
        for (var j = 0; j < targetAnchors.length; j++) {
          var targetUUID = toId + targetAnchors[j];
          ep = jsPlumbInstance.addEndpoint("canvas" + toId, targetEndpoint, {
            anchor: targetAnchors[j],
            uuid: targetUUID
          });
          endpointList.push(["canvas" + toId, ep]);
          ep.canvas.setAttribute("title", "Drop a connection here");
          ep = null;
        }
      };


      function drawElement(element, canvasId, name) {
        $(canvasId).append(element);
        _addEndpoints(name, properties[0].startpoints, properties[0].endpoints);
        jsPlumbInstance.draggable(jsPlumbInstance.getSelector(".jtk-node"), {
          grid: [20, 20],
          filter: ".ui-resizable-handle"
        });
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

      $('#canvas').on('click', function(e) {
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
            vm.elementIsDevice = true;
            vm.clickedComponent.name = element.data("name");
            vm.clickedComponent.type = element.data("type");
            vm.clickedComponent.mac = element.data("mac");
            vm.clickedComponent.ip = element.data("ip");
            vm.clickedComponent.username = element.data("username");
            vm.clickedComponent.element = element;
          } else if (element.attr("class").indexOf("actuator") > -1) {
            vm.elementIsActuator = true;
            vm.clickedComponent.name = element.data("name");
            vm.clickedComponent.type = element.data("type");
            vm.clickedComponent.adapter = element.data("adapter");
            vm.clickedComponent.device = element.data("device");
            vm.clickedComponent.element = element;
          } else if (element.attr("class").indexOf("sensor") > -1) {
            vm.elementIsSensor = true;
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


      vm.saveModel = saveModel;

      function saveModel() {
        var totalCount = 0;
        if (elementCount > 0) {
          var nodes = [];

          //check whether the diagram has a start element
          var elm = $(".start.jtk-node");
          if (elm.length == 0) {
            alert("The flowchart diagram should have a start element");
          } else {
            $(".jtk-node").each(function(index, element) {
              totalCount++;
              var $element = $(element);
              var type = $element.attr('class').toString().split(" ")[1];
              if (type == "step" || type == "diamond" || type == "parallelogram") {
                nodes.push({
                  elementId: $element.attr('id'),
                  nodeType: type,
                  positionX: parseInt($element.css("left"), 10),
                  positionY: parseInt($element.css("top"), 10),
                  clsName: $element.attr('class').toString(),
                  label: $element.text(),
                  width: $element.outerWidth(),
                  height: $element.outerHeight()
                });
              } else {
                nodes.push({
                  elementId: $element.attr('id'),
                  nodeType: $element.attr('class').toString().split(" ")[1],
                  positionX: parseInt($element.css("left"), 10),
                  positionY: parseInt($element.css("top"), 10),
                  clsName: $element.attr('class').toString(),
                  label: $element.text()
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

            var flowchart = {};
            flowchart.nodes = nodes;
            flowchart.connections = connections;
            flowchart.numberOfElements = totalCount;
            alert(JSON.stringify(flowchart));
          }
        }
      }

    });

  }

})();