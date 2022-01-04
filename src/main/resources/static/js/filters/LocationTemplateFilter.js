/* global app */

/**
 * Allows the filtering of location templates by location requirement operators.
 */
app.filter('byLocationTemplateType', function () {
    return function (locationTemplates, operator) {
        //Object for mapping operators to location template types
        let typeMapping = {
            'described_by': ['Informal'],
            'at_location': ['Point'],
            'in_area': ['Circle', 'Polygon']
        }

        //Sanity checks
        if ((!Array.isArray(locationTemplates)) || (!operator) || (!typeMapping.hasOwnProperty(operator))) {
            return [];
        }

        //Create result array
        let results = [];

        //Iterate over all given location templates
        for (let i = 0; i < locationTemplates.length; i++) {
            //Get current location template
            let template = locationTemplates[i];

            //Check if a valid type was provided for this template
            if (!template.hasOwnProperty("type")) continue;

            //Compare type of the template against the given operator
            if (typeMapping[operator].includes(template.type)) {
                //Add template to result array
                results.push(template);
            }
        }

        // Return results
        return results;
    }
});
