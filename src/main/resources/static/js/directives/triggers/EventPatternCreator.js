/* global app */

'use strict';

/**
 * Directive which allows the user to define event patterns based on components
 */
app.directive('eventPatternCreator', ['$interval', function ($interval) {

    /**
     * Linking function, glue code.
     *
     * @param scope Scope of the directive
     * @param element Elements of the directive
     * @param attrs Attributes of the directive
     */
    var link = function (scope, element, attrs) {

    };

    //Configure and expose the directive
    return {
        restrict: 'E', //Elements only
        template:
            '<div class="pattern-creator">' +
                '<div class="pattern-container">' +
                    '<div class="event"></div>' +
                    '<div class="event"></div>' +
                    '<div class="event"></div>' +
                '</div>' +
                '<div class="components-container">' +
                    '<div class="title"><span>Sensors</span></div>'+
                    '<div class="content">' +
                        '<div class="component"><i class="material-icons">settings_remote</i><span>TempSensor</span></div>' +
                        '<div class="component"><i class="material-icons">settings_remote</i><span>AccSensor</span></div>' +
                    '</div>' +
                '</div>'+
            '</div>'
        ,
        link: link,
        scope: {
            componentList: '@componentList'
        }
    };
}]);