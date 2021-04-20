/* global app */

'use strict';

/**
 * Directive for an interactive tree input method for data models.
 */
app.directive('dmInput', [function () {

    /**
     * Linking function, glue code
     *
     * @param scope Scope of the directive
     * @param element Elements of the directive
     * @param attrs Attributes of the directive
     */
    var link = function (scope, element, attrs) {

        /**
         * [public]
         * Deletes all child nodes of one tree node.
         * @param data - The tree node data
         */
        scope.delete = function (data) {
            data.nodes = [];
            scope.parseToDataModelJson();
        };

        /**
         * [public]
         * Adds one child node to the passed tree node.
         * @param data - The tree node data to which a tree node should be added as children
         */
        scope.add = function (data) {
            var post = data.nodes.length + 1;
            var newName = data.name + '-' + post;
            data.nodes.push({name: newName, type: "double", expanded: true, nodes: []});
            scope.parseToDataModelJson();
        };

        /**
         * [public]
         * Collapses/Expands the view of one tree node which makes his childs visible/unvisible.
         * Also, the edit mode will be set to false for all children.
         *
         * @param data The tree node data
         */
        scope.collapseExpand = function(data) {
            data.expanded = !data.expanded;
            scope.parseToDataModelJson();
            if (data.expanded === false) {
                scope.recursivelySetEditToFalse(data);
            }
        }

        /**
         * [public]
         * Opens the edit mode of one node which makes input boxes visible.
         * @param data
         */
        scope.edit = function (data) {
            data.edit = !data.edit;
        }

        /**
         * [public]
         * Actions, when the type of one node is changed. Makes sure that the data model constraints
         * are still correctly applied after type change.
         * @param data - Tree node
         */
        scope.typeChanged = function (data) {
            if (data.type === "array") {
                if (data.nodes.length > 1) {
                    data.nodes.splice(1);
                } else if (data.nodes.length === 0) {
                    scope.add(data);
                }
            } else if (data.type === "object") {
                data.dimension = undefined;
            } else {
                // Primitive type
                data.nodes = [];
                data.dimension = undefined;
            }
            scope.parseToDataModelJson();
        }

        scope.parseToDataModelJson = function () {
            var listToAdd = [];
            addChildNode(listToAdd, {name: ""}, scope.tree[0]);
            scope.bindedModel = JSON.stringify(listToAdd, null, '\t');
        }

        var addChildNode = function (listToAdd, parent, nextChild) {
            var node = {
                name: nextChild.name,
                description: nextChild.description,
                type: nextChild.type,
                dimension: nextChild.dimension,
                unit: nextChild.unit,
                parent: parent.name,
                children: []
            };

            for (var i = 0; i < nextChild.nodes.length; i++) {
                node.children.push(nextChild.nodes[i].name);
                addChildNode(listToAdd, nextChild, nextChild.nodes[i]);
            }


            listToAdd.push(node);
        }

        scope.recursivelySetEditToFalse = function(data) {
            data.edit = false;
            if (data.nodes.length >= 1) {
                for (var i = 0; i < data.nodes.length; i++) {
                    scope.recursivelySetEditToFalse(data.nodes[i]);
                }
            }
        }

        scope.tree = [{name: "RootObj", expanded: true, type: "object", nodes: [], isRoot: true, edit: false,
        dimension: undefined, description: "", unit: ""}];
    };

    //Configure and expose the directive
    return {
        restrict: 'E', //Elements only
        templateUrl: 'templates/data-model-input.html',
        link: link,
        scope: {
            bindedModel: "=ngModel"
        }
    };
}]);