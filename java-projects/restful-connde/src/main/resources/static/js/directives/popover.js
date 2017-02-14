/* global app */

'use strict';

app.directive('popover', ['$location', function (location) {
        return {
            restrict: 'A',
            scope: {
                title: '@popoverTitle',
                text: '@text'
            },
            link: function (scope, element, attrs, controller) {
                scope.title;
                scope.text;
            }
        };
    }]);

