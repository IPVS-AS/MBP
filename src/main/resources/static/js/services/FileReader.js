/* global app */

'use strict';

app.factory("FileReader",
    ["$q", function ($q) {
        var onLoad = function (reader, deferred, scope, file) {
            return function () {
                scope.$apply(function () {
                    deferred.resolve({
                        name: file.name,
                        content: reader.result
                    });
                });
            };
        };

        var onError = function (reader, deferred, scope) {
            return function () {
                scope.$apply(function () {
                    deferred.reject(reader.result);
                });
            };
        };

        var onProgress = function (reader, scope) {
            return function (event) {
                scope.$broadcast("fileProgress",
                    {
                        total: event.total,
                        loaded: event.loaded
                    });
            };
        };

        var getReader = function (deferred, scope, file) {
            var reader = new FileReader();
            reader.onload = onLoad(reader, deferred, scope, file);
            reader.onerror = onError(reader, deferred, scope);
            reader.onprogress = onProgress(reader, scope);
            return reader;
        };

        var readAsText = function (file, scope) {
            var deferred = $q.defer();

            var reader = getReader(deferred, scope, file);
            reader.readAsText(file);

            return deferred.promise;
        };

        var readMultipleAsText = function (files, scope) {
            var promises = [];

            angular.forEach(files, function (file) {
                promises.push(readAsText(file, scope));
            });

            return $q.all(promises);
        };

        var readAsDataURL = function (file, scope) {
            var deferred = $q.defer();

            var reader = getReader(deferred, scope, file);
            reader.readAsDataURL(file);

            return deferred.promise;
        };

        var readMultipleAsDataURL = function (files, scope) {
            var promises = [];

            angular.forEach(files, function (file) {
                promises.push(readAsDataURL(file, scope));
            });

            return $q.all(promises);
        };

        return {
            readAsText: readAsText,
            readMultipleAsText: readMultipleAsText,
            readAsDataUrl: readAsDataURL,
            readMultipleAsDataURL: readMultipleAsDataURL
        };
    }]);

