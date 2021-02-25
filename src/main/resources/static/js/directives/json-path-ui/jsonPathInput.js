/* global app */

'use strict';

/**
 * Directive which creates an input ui element for json paths. The input
 * method adapts to the currently selected json path and the required
 * modifications like array dimensions. It is possible to bind the
 * final input to extern variables as well as to display error
 * messages in the case of invalid input.
 *
 * @author Tim
 */
app.directive('jsonPathInput', ['UnitService', '$compile', function (UnitService, $compile) {

    /**
     * Linking function, glue code
     *
     * @param scope Scope of the directive
     * @param element Elements of the directive
     * @param attrs Attributes of the directive
     */
    var link = function (scope, element, attrs) {

        element.bind('focus', function() {

        })


        // List of bindings for the dynamically created array dimension input fields
        scope.arrInputBinding = [];

        scope.selectedBinding = null;

        // The jsonPath which should be edited
        var pathItemToEdit = {};

        // Counts the number of currently used wildcards.
        // For a valid input numberOfWildcards == numberOfNeededWildcards must be true
        var currentNumberOfWildcards = 0;

        // Fragments of the json path which must be put together with the user array index input
        var jsonPathSplittedForArrInput = [];

        /**
         * [public]
         * Actions when a json path item is selected by the user.
         *
         * @param pathItem Object with all the necessary json path information.
         */
        scope.onSelectPathItem = function () {
            var pathItem = JSON.parse(scope.selectedBinding);

            // Remove all the old input fields
            removeInputFields();
            console.log(scope.fieldCollectionIdInput);
            console.log(scope.pathType);
            scope.fieldCollectionIdInput = scope.pathType;

            // Get a copy of the path item to edit things etc.
            pathItemToEdit = JSON.parse(JSON.stringify(pathItem));

            // Update the number of needed wildcards and currently used wildcards
            scope.currentNumberOfWildcards = 0;

            // Split the string in pieces by # so that in between the inputs are to be considered
            var tmpSplittedPath = pathItemToEdit.path.split("#");
            // The convention for this string list is that in between of two array entries an array dimension must be added
            jsonPathSplittedForArrInput = [];

            var oddCount = 0;
            for (var i = 0; i < tmpSplittedPath.length; i++) {
                // If the index is odd then this is is the array index which must be adapted, therefore only add even indices
                if (i % 2 === 0) {
                    jsonPathSplittedForArrInput.push(tmpSplittedPath[i]);
                } else {
                    var newArrBinding = {
                        arrayDimension: parseInt(tmpSplittedPath[i]),
                        input: "",
                        inputOptions: [],
                    }
                    // Check if we need wildcards, if yes add * as input option
                    console.log("XX " + scope.numberOfNeededWildcards);
                    if (parseInt(scope.numberOfNeededWildcards) > 0) {
                        newArrBinding.inputOptions.push("*");
                    }
                    // Add as many input options as big the dimension of the array is
                    for (var arrayDimensionCount = 0; arrayDimensionCount < newArrBinding.arrayDimension; arrayDimensionCount++) {
                        newArrBinding.inputOptions.push("" + arrayDimensionCount);
                    }
                    scope.arrInputBinding.push(newArrBinding);
                }
            }
            // Call the method to update the bindings even if no array dimension inputs are necessary
            scope.onDimensionInput();
        }
        /**
         * [private]
         * Removes all currently added input fields.
         */
        var removeInputFields = function () {
            // Reset the bindings list
            scope.arrInputBinding = [];
        }

        /**
         * [public]
         * Actions when the array index input of the currently selected json path
         * changes. Put the new json path together.
         */
        scope.onDimensionInput = function () {
            scope.fieldCollectionIdInput = scope.pathType;
            var editJsonPath = "";
            for (var i = 0; i < jsonPathSplittedForArrInput.length; i++) {
                editJsonPath += jsonPathSplittedForArrInput[i];
                if (i !== jsonPathSplittedForArrInput.length - 1) {
                    editJsonPath += scope.arrInputBinding[i].input;
                }
            }
            pathItemToEdit.path = editJsonPath;
            scope.bindedModel = pathItemToEdit;
        }
    };

    //Configure and expose the directive
    return {
        restrict: 'E', //Elements only
        templateUrl: 'templates/json-path-input-template.html',
        link: link,
        scope: {
            /*
            ng-model variable for reading the user input
             */
            bindedModel: "=ngModel",

            fieldCollectionIdInput: "=fieldCollectionIdInput",
            pathType: "@pathType",

            /*
            All json path options which should be considered by this input method
             */
            jsonPathList: "=jsonPathList",
            /*
             * Number of arrays which should be considered by the json path input. E.g. if number = 2 then at least two
             * fields must have the input *. If number = 0 then all array dimensions must be inputted without wildcards (*)
             */
            numberOfNeededWildcards: "@numberOfNeededWildcards"
        }
    };
}]);