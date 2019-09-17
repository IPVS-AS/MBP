/* global app */

'use strict';

/**
 * Directive for approving and disapproving users in terms of a certain user entity.
 */
app.directive('userApproval', ['$timeout', 'UserApprovalService', function ($timeout, UserApprovalService) {

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
            '<div>' +
            '<ul class="list-group">' +
            '<li class="list-group-item">First user' +
            '<button type="button" class="btn btn-xs bg-red waves-effect" style="margin-top:-5px;float:right;"><i class="material-icons">delete</i></button>' +
            '</li>' +
            '<li class="list-group-item">Second user' +
            '<button type="button" class="btn btn-xs bg-red waves-effect" style="margin-top:-5px;float:right;"><i class="material-icons">delete</i></button>' +
            '</li>' +
            '<li class="list-group-item">Third user' +
            '<button type="button" class="btn btn-xs bg-red waves-effect" style="margin-top:-5px;float:right;"><i class="material-icons">delete</i></button>' +
            '</li>' +
            '<li class="list-group-item"><div class="form-group" style="margin-bottom:0;"><div class="form-line">' +
            '<input type="text" class="form-control" style="height:25px;" placeholder="Add username...">' +
            '</div></div>' +
            '<button type="button" class="btn btn-success btn-block m-t-5 waves-effect">Share</button>'+
            '</li>' +
            '</div>',
        link: link,
        scope: {}
    };
}]);