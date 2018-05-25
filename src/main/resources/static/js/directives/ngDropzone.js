///* global app */
//
//'use strict';
//
//Dropzone.autoDiscover = false;
//
//app.directive('ngDropzone', ['$parse', function ($parse) {
//        return {
//            restrict: 'A',
//            link: function (scope, element, attrs) {
//
//                var modelGetter = $parse(attrs.ngModel);
//
//                var isMultiple = attrs.multiple;
//                var modelSetter = modelGetter.assign;
//
//                var noOfFiles = isMultiple ? 5 : 1;
//
//                var config = {
//                    url: '',
//                    maxFilesize: 1000,
//                    paramName: modelGetter,
//                    maxThumbnailFilesize: noOfFiles,
//                    parallelUploads: noOfFiles,
//                    autoProcessQueue: false
//                };
//
//                var eventHandlers = {
//                    'addedfile': function (file) {
//                        console.log(file);
//                        console.log(this.files);
//
//                        if (this.files[noOfFiles] !== null) {
//                            this.removeFile(this.files[0]);
//                        }
//
//                        if (isMultiple) {
//                            modelSetter(scope, this.files);
//                        } else {
//                            modelSetter(scope, file);
//                        }
//
////                        scope.$apply(function () {
////                            scope.fileAdded = true;
////                        });
//                    }
//
//                };
//
//
//                var dropzone = Dropzone(element[0], config);
//
//                angular.forEach(eventHandlers, function (handler, event) {
//                    dropzone.on(event, handler);
//                });
//
//                scope.processDropzone = function () {
//                    dropzone.processQueue();
//                };
//
//                scope.resetDropzone = function () {
//                    dropzone.removeAllFiles();
//                };
//            }
//        };
//    }]);
//
//
