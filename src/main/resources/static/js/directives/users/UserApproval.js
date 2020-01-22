/* global app */

'use strict';

/**
 * Directive for approving and disapproving users in terms of a certain user entity.
 */
app.directive('userApproval', ['$timeout', 'UserApprovalService', 'CrudService', 'NotificationService',
    function ($timeout, UserApprovalService, CrudService, NotificationService) {

        const APPROVE_ERROR_MESSAGES = {
            400: 'The entity is already shared with this user.',
            403: 'Not authorized to share the entity.',
            404: 'User could not be found.'
        };

        const APPROVE_DEFAULT_ERROR_MESSAGE = 'Entity could not be shared.';

        /**
         * Linking function, glue code.
         *
         * @param scope Scope of the directive
         * @param element Elements of the directive
         * @param attrs Attributes of the directive
         */
        let link = function (scope, element, attrs) {
            //Find input elements
            let usernameInput = element.find('input');

            //Stores the list of approved users
            scope.approvedUsers = [];

            //Initialize scope variables
            scope.username = "";
            scope.errorMessage = false;
            scope.loading = false;

            //Enable auto-completion for username input
            usernameInput.easyAutocomplete({
                url: function (queryString) {
                    return UserApprovalService.buildUsersSearchURL(queryString);
                },
                getValue: "username",
                list: {
                    match: {
                        enabled: true
                    }
                }
            });

            //Expose approving function
            scope.approveUser = function () {
                //Get username from input
                let username = scope.username.trim();

                //Sanity check
                if (!username) {
                    scope.errorMessage = 'No user selected.';
                    return;
                }

                //Execute approve request
                UserApprovalService.approveUser(scope.categoryName, scope.entityId, username)
                    .then(function (response) {
                        //Clear input and error message and notify user
                        scope.username = "";
                        scope.errorMessage = false;
                        NotificationService.notify('The entity was shared successfully.', 'success');

                        //Update approved users
                        scope.loadApprovedUsers();
                    }, function (response) {
                        //Set suitable error message and notify user
                        scope.errorMessage = APPROVE_ERROR_MESSAGES[response.status] || APPROVE_DEFAULT_ERROR_MESSAGE;
                        NotificationService.notify('Failed to share the entity.', 'error');
                    });
            };

            //Expose disapproving function
            scope.disapproveUser = function (username) {
                //Sanity check
                if (!username) {
                    return;
                }

                //Execute disapproval request
                UserApprovalService.disapproveUser(scope.categoryName, scope.entityId, username)
                    .then(() => {
                        //Notify user and update approved users
                        NotificationService.notify('The user was disapproved successfully.', 'success');
                        scope.loadApprovedUsers();
                    }, () => NotificationService.notify('Failed to disapprove the user.', 'error'));
            };

            //Expose function for updating the list of approved users
            scope.loadApprovedUsers = function () {
                scope.loading = true;
                CrudService.fetchSpecificItem(scope.categoryName, scope.entityId).then(data => {
                        //Sanity check
                        if (!data) {
                            NotificationService.notify("Could not load approved users.", "error");
                            return;
                        }

                        //Update
                        scope.approvedUsers = data.approvedUsers;
                        scope.loading = false;
                    },
                    () => {
                        NotificationService.notify("Could not load approved users.", "error");
                        scope.loading = false;
                    });
            };

            //Initially load the approved users
            scope.loadApprovedUsers();
        };

        //Configure and expose the directive
        return {
            restrict: 'E', //Elements only
            template:
                '<div class="modal fade" id="approval-model-{{entityId}}" tabindex="-1" role="dialog">' +
                '<div class="modal-dialog" role="document">' +
                '<div class="modal-content">' +
                '<div class="modal-header">' +
                '<h5 class="modal-title">Manage shared users' +
                '<button type="button" class="close" data-dismiss="modal" aria-label="Close">' +
                '<span aria-hidden="true">&times;</span>' +
                '</button>' +
                '</h5>' +
                '</div>' +
                '<div class="modal-body">' +
                '<ul ng-show="!loading" class="list-group">' +
                '<li ng-repeat="user in approvedUsers" class="list-group-item">' +
                '<i class="material-icons" style="vertical-align: middle;">person</i>&nbsp;{{user.username}}' +
                '<button ng-click="disapproveUser(user.username)" type="button" class="btn btn-xs bg-red waves-effect"' +
                ' style="margin-top:-5px;float:right;"><i class="material-icons">delete</i>' +
                '</button>' +
                '</li>' +
                '</ul>' +
                '<div ng-show="loading" style="margin-bottom: 20px; width: 100%; text-align: center;">' +
                '<div class="preloader">' +
                '<div class="spinner-layer pl-teal">' +
                '<div class="circle-clipper left">' +
                '<div class="circle">' +
                '</div></div>' +
                '<div class="circle-clipper right">' +
                '<div class="circle">' +
                '</div></div></div></div></div>' +
                '<form ng-submit="approveUser()">' +
                '<div class="form-group" style="margin-bottom:0;" ng-class="{\'has-error\' : errorMessage }">' +
                '<div class="form-line"  ng-class="{\'focused error\' : errorMessage }">' +
                '<input ng-model="username" type="text" class="form-control" style="height:25px;" placeholder="Add username...">' +
                '</div>' +
                '<span class="help-block" ng-show="errorMessage">{{errorMessage}}</span>' +
                '</div>' +
                '<button type="submit" class="btn btn-success btn-block m-t-5 waves-effect">Share</button>' +
                '</form>' +
                '</div>' +
                '<div class="modal-footer">' +
                '<button type="button" class="btn btn-secondary m-t-0 waves-effect" data-dismiss="modal">Close</button>' +
                '</div></div></div></div>' +
                '<button type="submit" class="btn btn-warning m-t-0 waves-effect" data-toggle="modal" ' +
                'data-target="#approval-model-{{entityId}}" data-backdrop="static" data-keyboard="false">Manage users' +
                '</button>',
            link: link,
            scope: {
                entityId: "@entityId",
                categoryName: "@categoryName"
            }
        };
    }]);