/* global app */

'use strict';

/**
 *
 *
 * @author Jan
 */
app.directive('unitInput', ['UnitService', function (UnitService) {

    var link = function (scope, element, attrs) {
        function loadUnits() {
            UnitService.getPredefinedUnits(scope.unitFilter).then(function (response) {
                scope.unitList = response.data;
            }, function () {
                scope.unitList = [];
            });
        }

        loadUnits();

        var suggestionCallback = function (unit) {
            scope.bindedModel = unit;
        };

        scope.suggestionCallback = suggestionCallback;
    };

    return {
        restrict: 'E',
        template: '<div class="input-group" style="margin-bottom:0;">' +
            '<input type="text" class="form-control" placeholder="Unit" ng-model="bindedModel">' +
            '<span class="input-group-btn">' +
            '<button type="button" class="btn bg-teal dropdown-toggle" data-toggle="dropdown">' +
            'Suggestions&nbsp;' +
            '<span class="caret"></span>' +
            '</button>' +
            '<ul class="dropdown-menu dropdown-scrollable" role="menu">' +
            '<li class="dropdown-header" ng-repeat-start="quantity in unitList">{{quantity.name}}</li>' +
            '<li><a href="#" ng-repeat="unit in quantity.units" ng-click="suggestionCallback(unit.format)">{{unit.name}}</a></li>' +
            '<li class="divider" ng-repeat-end></li>' +
            '</ul>' +
            '</span>' +
            '</div>',
        link: link,
        scope: {
            bindedModel: "=ngModel",
            unitFilter: "@unitFilter"
        }
    };
}]);