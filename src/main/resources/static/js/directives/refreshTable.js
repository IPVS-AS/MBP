/* global app */

'use strict';

app.directive("refreshTable", function () {
    return {
        require: 'stTable',
        restrict: "A",
        link: function (scope, elem, attr, table) {
            var ref = attr.refreshTable;
            
            scope.$on(ref, function () {
                table.pipe(table.tableState());
            });
        }
    };
});