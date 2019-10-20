/* global app */

'use strict';

app.directive('menuItem', ['$location', function (location) {
    return {
        restrict: 'A',
        link: function (scope, element, attrs, controller) {
            var path = attrs.href;

            //Sanity check
            if (!path) {
                return;
            }

            if (path.startsWith('#')) {
                path = path.substring(1); // skip '#'
            }
            if (!path.startsWith('/')) {
                path = '/' + path; // add '/'
            }
            if (path === "/./") {
                path = "/";
            }

            scope.location = location;
            scope.$watch('location.path()', function (newPath) {
                if (path === newPath) {
                    element.addClass('active');
                } else {
                    element.removeClass('active');
                }
            });
        }
    };
}]);

