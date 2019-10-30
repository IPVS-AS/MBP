/* global app */

'use strict';

/**
 * Directive for approving and disapproving users in terms of a certain user entity.
 */
app.directive('userApproval', ['$timeout', 'UserApprovalService', 'NotificationService',
    function ($timeout, UserApprovalService, NotificationService) {

        const CLASS_USERNAME_INPUT = "username-input";
        const CLASS_APPROVAL_BUTTON = "approval-button";

        /**
         * Linking function, glue code.
         *
         * @param scope Scope of the directive
         * @param element Elements of the directive
         * @param attrs Attributes of the directive
         */
        let link = function (scope, element, attrs) {
            //Find input elements
            let usernameInput = element.find('.' + CLASS_USERNAME_INPUT);
            let approvalButton = element.find('.' + CLASS_APPROVAL_BUTTON);

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

            //Click listener for approval button
            approvalButton.on('click', function () {
                //Read query string from input
                let queryString = usernameInput.val().trim();

                //Sanity check
                if(!queryString){
                    NotificationService.notify('Invalid username.', 'success');
                    return;
                }

                //Execute removal request
                UserApprovalService.approveUser(scope.categoryName, scope.entityId, queryString)
                    .then(function (response) {
                        //Clear input and Notify user
                        usernameInput.val('');
                        NotificationService.notify('The entity was shared.', 'success');
                    }, function (response) {
                        //Check response code
                        switch (response.status) {
                            case 400:
                                NotificationService.notify('The entity is already shared with the user.', 'error');
                                break;
                            case 403:
                                NotificationService.notify('Not authorized to share the entity.', 'error');
                                break;
                            case 404:
                                NotificationService.notify('User or entity not found.', 'error');
                                break;
                            default:
                                NotificationService.notify('Entity could not be shared.', 'error');
                                break;
                        }
                    });
            });
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
                '<ul class="list-group">' +
                '<li class="list-group-item">First user' +
                '<button type="button" class="btn btn-xs bg-red waves-effect" style="margin-top:-5px;float:right;"><i class="material-icons">delete</i></button>' +
                '</li>' +
                '<li class="list-group-item">Second user' +
                '<button type="button" class="btn btn-xs bg-red waves-effect" style="margin-top:-5px;float:right;"><i class="material-icons">delete</i></button>' +
                '</li>' +
                '</ul>' +
                '<div class="form-group" style="margin-bottom:0;">' +
                '<div class="form-line">' +
                '<input type="text" class="form-control ' + CLASS_USERNAME_INPUT + '" style="height:25px;" placeholder="Add username...">' +
                '</div></div>' +
                '<button type="button" class="btn btn-success btn-block m-t-5 waves-effect ' + CLASS_APPROVAL_BUTTON + '">Share</button>' +
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