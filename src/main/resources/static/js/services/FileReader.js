/* global app */

'use strict';

app.factory("FileReader",
    ["$q", function ($q) {
        let onLoad = function (reader, deferred, scope, file) {
            return function () {
                scope.$apply(function () {
                    deferred.resolve({
                        name: file.name,
                        content: reader.result
                    });
                });
            };
        };

        let onError = function (reader, deferred, scope) {
            return function () {
                scope.$apply(function () {
                    deferred.reject(reader.result);
                });
            };
        };

        let onProgress = function (reader, scope) {
            return function (event) {
                scope.$broadcast("fileProgress",
                    {
                        total: event.total,
                        loaded: event.loaded
                    });
            };
        };

        let getReader = function (deferred, scope, file) {
            let reader = new FileReader();
            reader.onload = onLoad(reader, deferred, scope, file);
            reader.onerror = onError(reader, deferred, scope);
            reader.onprogress = onProgress(reader, scope);
            return reader;
        };

        let readAsText = function (file, scope) {
            let deferred = $q.defer();

            let reader = getReader(deferred, scope, file);
            reader.readAsText(file);

            return deferred.promise;
        };

        let readMultipleAsText = function (files, scope) {
            let promises = [];

            angular.forEach(files, function (file) {
                promises.push(readAsText(file, scope));
            });

            return $q.all(promises);
        };

        let readAsDataURL = function (file, scope) {
            let deferred = $q.defer();

            let reader = getReader(deferred, scope, file);
            reader.readAsDataURL(file);

            return deferred.promise;
        };

        let readMultipleAsDataURL = function (files, scope) {
            let promises = [];

            angular.forEach(files, function (file) {
                promises.push(readAsDataURL(file, scope));
            });

            return $q.all(promises);
        };

        /**
         * [Public]
         * Reads a file from the user's disk as MD5 hash string.
         *
         * @param file The file to read
         * @param scope Current angular scope
         * @returns {*} A promise for reading and hashing the file
         */
        let readAsMD5Hash = function (file, scope) {
            let deferred = $q.defer();

            //Setup reader
            let reader = getReader(deferred, scope, file);

            //Read file as binary string
            reader.readAsBinaryString(file);

            //Return promise for the reading process and chain the hash generation
            return deferred.promise.then(function (result) {
                return CryptoJS.MD5(result.content).toString();
            });
        };

        /**
         * [Public]
         * Reads multiple files from the user's disk as MD5 hash strings.
         *
         * @param files The files to read
         * @param scope Current angular scope
         * @returns {*} A promise for reading and hashing the files
         */
        let readMultipleAsMD5Hash = function (files, scope) {
            //Array for all generated promises
            let promises = [];

            //Iterate over all files and read the hashes
            angular.forEach(files, function (file) {
                //Push promise to array
                promises.push(readAsMD5Hash(file, scope));
            });

            //Return promise containing all file promises
            return $q.all(promises);
        };

        return {
            readAsText: readAsText,
            readMultipleAsText: readMultipleAsText,
            readAsDataURL: readAsDataURL,
            readMultipleAsDataURL: readMultipleAsDataURL,
            readAsMD5Hash: readAsMD5Hash,
            readMultipleAsMD5Hash: readMultipleAsMD5Hash
        };
    }]);

