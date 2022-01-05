/* global app */

/*
 * Controller for the settings page.
 */
app.controller('ExceptionLogController',
    ['$scope', 'exceptionLogs', 'LogService', 'NotificationService',
        function ($scope, exceptionLogs, LogService, NotificationService) {
            //Find DOM elements of exception logs table and stack trace modal
            const ELEMENT_LOGS_TABLE = $("#exception-logs-table");
            const ELEMENT_STACKTRACE_MODAL = $("#showStackTraceModal")

            //Store reference to the dataTable
            let logsDataTable = null;

            let vm = this;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Check if exception logs could be loaded
                if (exceptionLogs == null) {
                    NotificationService.notify("Could not load exception logs.", "error");
                }

                //Initialize data table and stack trace modal
                initDataTable();
                initStackTraceModal();
            })();

            /**
             * [Public]
             * Shows an alert that asks the user whether he really wants to delete all currently available exception
             * logs and if yes, performs a server request in order to do so.
             */
            function deleteExceptionLogs() {
                //Show alert
                Swal.fire({
                    title: 'Delete exception logs',
                    icon: 'warning',
                    html: "Are you sure you want to delete <strong>all</strong> exception logs?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete them',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                }).then((result) => {
                    //Check whether the user confirmed
                    if (!result.isConfirmed) {
                        return;
                    }

                    //Perform deletion server request
                    LogService.deleteExceptionLogs().then(function (response) {
                        //Success callback
                        NotificationService.notify('Deleted all exception logs successfully.', 'success');

                        //Clear table and redraw it
                        logsDataTable.clear().draw();
                    });
                });
            }

            /**
             * [Private]
             * Initializes the DataTable within the template page.
             */
            function initDataTable() {
                //Check whether table has already been initialized
                if (logsDataTable != null) {
                    return;
                }

                logsDataTable = ELEMENT_LOGS_TABLE.DataTable({
                    data: exceptionLogs,
                    serverSide: true,
                    searching: false,
                    ordering: false,
                    columns: [
                        {
                            data: 'time',
                            render: function (data, type, row) {
                                return new Date(data * 1000).toLocaleString('de-DE', {
                                    'year': 'numeric',
                                    'month': '2-digit',
                                    'day': '2-digit',
                                    'hour': '2-digit',
                                    'minute': '2-digit',
                                    'second': '2-digit'
                                }).replace(',', '');
                            }
                        },
                        {data: 'username'},
                        {data: 'exceptionType'},
                        {data: 'message'},
                        {data: 'rootCauseMessage'},
                        {
                            data: 'stackTrace',
                            render: function (data, type, row) {
                                //Create button for displaying a modal showing the stack trace
                                return '<button type="button" ' +
                                    'class="btn bg-blue btn-circle waves-effect waves-circle waves-float m-t-0" ' +
                                    'data-toggle="modal" data-target="#showStackTraceModal" ' +
                                    'data-backdrop="static" data-keyboard="false" ' +
                                    'data-stacktrace="' + data + '">' +
                                    '<i class="material-icons">remove_red_eye</i></button>';
                            }
                        }
                    ],
                    ajax: function (data, callback) {
                        //Calculate page number
                        let pageNumber = Math.floor(data.start / data.length);

                        //Perform request in order to retrieve the exception logs
                        LogService.getExceptionLogs(data.length, pageNumber, 'time,desc').then(function (response) {
                            //Trigger callback with the data from the response
                            callback({
                                draw: data.draw,
                                data: response.content,
                                recordsTotal: response.totalElements,
                                recordsFiltered: response.totalElements,
                            });
                        });
                    },
                    language: {
                        "decimal": "",
                        "emptyTable": "No logs available.",
                        "info": "Showing _START_ to _END_ of _TOTAL_ logs",
                        "infoEmpty": "Showing 0 to 0 of 0 logs",
                        "infoFiltered": "(filtered from _MAX_ total logs)",
                        "thousands": ".",
                        "lengthMenu": "Show _MENU_ logs",
                        "zeroRecords": "No matching logs found",
                    }
                });
            }

            /**
             * [Private]
             * Initializes the functionality of the stack trace modal dialog.
             */
            function initStackTraceModal() {
                ELEMENT_STACKTRACE_MODAL.on('show.bs.modal', function (e) {
                    //Retrieve stacktrace from the button that triggered the modal
                    let stackTrace = $(e.relatedTarget).data('stacktrace');

                    //Populate corresponding text area of the modal
                    $(e.currentTarget).find('.stacktrace-container').html(stackTrace);
                });
            }

            //Expose functions that are used externally
            angular.extend(vm, {
                deleteExceptionLogs: deleteExceptionLogs
            });
        }
    ]);
