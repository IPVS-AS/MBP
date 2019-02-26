/* global app */

'use strict';

app.controller('ItemDetailsController', ['$scope', 'item', function ($scope, item) {
        var vm = this;
        vm.item = item;
    }]);