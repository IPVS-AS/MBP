/* global app */

app.controller('DataModelListController',
    ['$scope', '$controller', '$q', 'dataModelList', 'addDataModel', 'deleteDataModel', 'DataModelService', 'OperatorService', 'NotificationService', 'HttpService',
        function ($scope, $controller, $q, dataModelList, addDataModel, deleteDataModel, DataModelService, OperatorService, NotificationService, HttpService) {
            var vm = this;

            vm.showDataModelTextBox = false;

            vm.treeNodes = "";

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                // Will be executed at start
            })();

            /**
             * [Public]
             * Converts a JSON string to a more pretty printed JSON representation
             * @param string
             * @returns {string}
             */
            vm.formatJSON = function formatJSON(string) {
                if (string != null) {
                    try {
                        return JSON.stringify(JSON.parse(string), null, 2);
                    } catch (e) {
                        return "Refresh page to view the example.";
                    }
                } else {
                    return "";
                }
            }

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain data model. It also
             * shows a list of all components that are affected by this deletion.
             *
             * @param data A data object that contains the id of the data model that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                var dataModelId = data.id;
                var dataModelName = "";

                //Determines the data model's name by checking all operators in the operator list
                for (var i = 0; i < dataModelList.length; i++) {
                    if (dataModelId === dataModelList[i].id) {
                        dataModelName = dataModelList[i].name;
                        break;
                    }
                }

                //Ask the server for all components that use this operator
                return DataModelService.getUsingOperators(data.id).then(function (result) {
                    var affectedWarning = "";

                    //If the list is not empty, create a message that contains the names of all affected components
                    if (result.length > 0) {

                        affectedWarning = "<br/><br/>The following operators are currently " +
                            "using this data model and <strong>might not work anymore after a deletion" +
                            "(as well as their using components)</strong>:<br/>";

                        //Iterate over all affected operators
                        for (var i = 0; i < result.length; i++) {
                            affectedWarning += "- ";
                            affectedWarning += result[i].name;
                            affectedWarning += "<br/>";
                        }
                    }

                    //Show the alert to the user and return the resulting promise
                    return Swal.fire({
                        title: 'Delete data model',
                        type: 'warning',
                        html: "Are you sure you want to delete the data model <strong>" +
                            dataModelName + "</strong>?" + affectedWarning,
                        showCancelButton: true,
                        confirmButtonText: 'Delete',
                        confirmButtonClass: 'bg-red',
                        focusConfirm: false,
                        cancelButtonText: 'Cancel'
                    });
                });
            }


            // expose controller ($controller will auto-add to $scope)
            angular.extend(vm, {
                treeNodes: vm.treeNodes,
                dataModelListCtrl: $controller('ItemListController as dataModelListCtrl',
                    {
                        $scope: $scope,
                        list: dataModelList,
                    }),
                addDataModelCtrl: $controller('AddItemController as addDataModelCtrl',
                    {
                        $scope: $scope,
                        entity: 'data-model',
                        addItem: function (data) {
                            //Extend request parameters for routines, their hashes and parameters
                            try {
                                data.treeNodes = JSON.parse(vm.treeNodes);
                            } catch (e) {
                                // TODO handle this
                            }
                            return addDataModel(data);
                        }
                    }),
                deleteDataModelCtrl: $controller('DeleteItemController as deleteDataModelCtrl',
                    {
                        $scope: $scope,
                        deleteItem: deleteDataModel,
                        confirmDeletion: confirmDelete
                    }),
            });

            // $watch 'addItem' result and add to 'itemList'
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.addDataModelCtrl.result;
                },
                function () {

                    //Callback
                    var data = vm.addDataModelCtrl.result;
                    if (data) {
                        //Close modal on success
                        $("#addDataModelModal").modal('toggle');

                        //Add new item to list
                        vm.dataModelListCtrl.pushItem(data);
                    }
                }
            );

            // $watch 'deleteItem' result and remove from 'itemList'
            $scope.$watch(
                function () {
                    // value being watched
                    return vm.deleteDataModelCtrl.result;
                },
                function () {
                    var id = vm.deleteDataModelCtrl.result;
                    vm.dataModelListCtrl.removeItem(id);
                }
            );

            //Enable popovers
            $(document).ready(function () {
                $('[data-toggle="popover"]').popover();
            });
        }
    ]
);
