/* global app */

app.factory('DeviceService', function () {
    return {
        formatMacAddress: function (address) {
            if (address) {
                return address.match(/.{1,2}/g).join('-').toUpperCase();
            }
            return address;
        },
        normalizeMacAddress: function (address) {
            if (address) {
                norm = address.replace(new RegExp('-', 'g'), '');
                return norm.toLowerCase();
            }
            return address;
        }
    };
});