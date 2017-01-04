function formatMacAddress(address) {
    if (address) {
        return address.match(/.{1,2}/g).join('-').toUpperCase();
    }
    return address;
}

function normalizeMacAddress(address) {
    if (address) {
        norm = address.replace(new RegExp('-', 'g'), '');
        return norm.toLowerCase();
    }
    return address;
}

function Device($q, RetrieverService, uriRestDevices) {
    return {
        formatMacAddress: formatMacAddress,
        normalizeMacAddress: normalizeMacAddress,
        getDevices: function () {
            return RetrieverService.get(uriRestDevices)
                    .then(function (data) {
                        devices = data._embedded.devices;
                        for (var i in devices) {
                            devices[i].formattedMacAddress =
                                    formatMacAddress(devices[i].macAddress);
                        }
                        //data._embedded.devices = devices;
                        return devices;
                    }, function (response) {
                        return $q.reject(response);
                    });
        }, add: function (data) {
            return RetrieverService.post(uriRestDevices, data)
                    .then(function (data) {
                        return data;
                    }, function (response) {
                        return $q.reject(response);
                    });
        }
    };
}

angular.module('app')
        .factory('Device', ['$q', 'RetrieverService', 'uriRestDevices', Device]);