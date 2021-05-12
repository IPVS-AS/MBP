
/**
 * Provides services for managing default components.
 */
app.factory('DefaultComponentsService', [
    function () {

        // Constant list of the components that can be included in the test
        const SIMULATOR_LIST = {
            TESTING_DEVICE: 'TESTING_Device',
            ACTUATOR: 'TESTING_Actuator',
            TEMPERATURE: 'TESTING_TemperatureSensor',
            TEMPERATURE_PL: 'TESTING_TemperatureSensorPl',
            HUMIDITY: 'TESTING_HumiditySensor',
            HUMIDITY_PL: 'TESTING_HumiditySensorPl',
        };

        /**
         * Returns the number of components without the for the user invisible testing components
         *
         * @param componentList
         * @return {*}
         */
        function getListWoSimulators(componentList) {
            let tempComponentList = componentList;

            angular.forEach(SIMULATOR_LIST, function (value) {
                componentList.some(function (component) {
                    if (component.name === value || component.name.includes("RERUN_")) {
                        const index = tempComponentList.indexOf(component);
                        if(index !== -1){
                            tempComponentList.splice(index,1)
                        }
                    }
                });

            });
            return tempComponentList.length;

        }

        //Expose public methods
        return {
            getListWoSimulators:getListWoSimulators
        }
    }]);
