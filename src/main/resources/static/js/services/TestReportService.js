/* global app */

/**
 * Provides services for managing tests.
 */
app.factory('TestReportService', ['HttpService', '$http', '$resource', '$q', 'ENDPOINT_URI', 'NotificationService',
    function (HttpService, $http, $resource, $q, ENDPOINT_URI, NotificationService) {

        const vm = this;


        /**
         * [Public]
         *
         * Performs a server request in order to start the current test (in case it has been stopped before).
         * @param testId The id of the test to be started
         */
        function generateReport(table2) {
            console.log(table2)
            var doc = new jsPDF("p", "mm", "a4");

            doc.setFontSize(40);
            doc.setFontSize(18);
            doc.text(18, 25, "Test-Report");
            doc.addImage(table2, 'PNG', 15, 60, 180, 200, "dc", "NONE", 0);

            let data = [{id: 1, name: "Peter"}, {id: 2, name: "Chris"}];
            doc.table(20, 30, data);
            doc.save('TestReport.pdf');


        }

        //Expose public methods
        return {
            generateReport: generateReport,


        }
    }
]);