/* global app */

/*
 * Controller for the index main page.
 */
app.controller('IndexController',
    ['$scope',
        function ($scope) {
            var vm = this;

            //public
            function getDate() {
                return new Date();
            }

            //expose
            angular.extend(vm, {
                getDate: getDate
            });

        }
    ]
);

