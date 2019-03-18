/* global app */

/**
 * Notification service that enables the creation of temporary user notifications with different types.
 */
app.factory('NotificationService', function () {

    //Private
    var typeList = {
        success: {
            icon: 'check',
            btType: 'success'
        },
        error:{
            icon: 'close',
            btType: 'danger'
        }
    };

    return {
        /**
         * Creates a new temporary notification and displays it on the user interface.
         * @param message The message to display
         * @param type The type of the notification (see typeList)
         * @param settings Optional settings of the notification (see http://bootstrap-notify.remabledesigns.com)
         */
        notify: function (message, type, settings) {
            //Handle optional settings parameter
            if (typeof settings === 'undefined') {
                settings = {};
            }

            //Get corresponding type object
            var chosenType = typeList[type];

            //Create options object
            var options = {};
            options.message = message;
            options.title = '<i style="font-size: 16px" class="material-icons">' + chosenType.icon + '</i>';

            //Expand settings
            settings.placement = {
                from: 'bottom',
                align: 'right'
            };
            settings.delay = 5000;
            settings.allow_dismiss = true;
            settings.type = chosenType.btType;

            //Show notification
            $.notify(options, settings);
        }
    };
});