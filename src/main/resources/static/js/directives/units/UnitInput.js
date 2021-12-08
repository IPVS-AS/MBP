/* global app */

'use strict';

/**
 * Directive which creates a input for physical units and a button that offers related
 * suggestions to the user.
 *
 * @author Jan
 */
app.directive('unitInput', ['UnitService', function (UnitService) {

    /**
     * Linking function, glue code
     *
     * @param scope Scope of the directive
     * @param element Elements of the directive
     * @param attrs Attributes of the directive
     */
    var link = function (scope, element, attrs) {
        /**
         * Loads a list of predefined units (suggestions) from the server into a array that is available in scope.
         */
        function loadUnits() {
            //Execute server request
            UnitService.getPredefinedUnits(scope.unitFilter).then(function (response) {
                //Success, set array
                scope.unitList = response;
            }, function () {
                //Failure, no suggestions
                scope.unitList = [];
            });
        }

        //Load units from server
        loadUnits();

        /**
         * Function that is called when the user selects a unit from the suggestion list. It then
         * updates the input according to the user's choice.
         *
         * @param unit The unit the user selected
         */
        var suggestionCallback = function (unit) {
            //Update input model, so that the suggestion is displayed in the input
            scope.bindedModel = unit;
        };

        //Make callback function available
        scope.suggestionCallback = suggestionCallback;

        // Logic for the ng-change functionality, if the model changes
        scope.$watch('bindedModel', function() {
            if (scope.changeCallback) {
                scope.changeCallback();
            }
        });
    };

    //Configure and expose the directive
    return {
        restrict: 'E', //Elements only
        template: '<div class="input-group" style="margin-bottom:0;">' +
            '<input type="text" class="form-control" placeholder="Unit" ng-model="bindedModel">' +
            '<span class="input-group-btn">' +
            '<button type="button" class="btn bg-mbp-blue dropdown-toggle" data-toggle="dropdown">' +
            'Suggestions&nbsp;' +
            '<span class="caret"></span>' +
            '</button>' +
            '<ul class="dropdown-menu dropdown-scrollable" role="menu">' +
            '<input disable-auto-close type="search" ng-model="searchFilter" class="units-search-box" placeholder="Search..."/>' +
            '<li class="dropdown-header" ng-repeat-start="quantity in unitList | filter: searchFilter">{{quantity.name}}</li>' +
            '<li><a href="#" ng-repeat="unit in quantity.units" ng-click="suggestionCallback(unit.format)">{{unit.name}}</a></li>' +
            '<li class="divider" ng-repeat-end></li>' +
            '</ul>' +
            '</span>' +
            '</div>',
        link: link,
        //Take and connect the ng-model angular variable and a unit filter from outside
        scope: {
            /*
            ng-model variable for reading the user input
             */
            bindedModel: "=ngModel",
            /*
            (Optional)
            unit-filter variable that holds a unit string. If set, only units are displayed
            as suggestions which are probably compatible with the given unit
             */
            unitFilter: "@unitFilter",
            /*
            (Optional)
            Function callback for actions when the ng-model changes.
             */
            changeCallback: "&ngChange"
        }
    };
}]);