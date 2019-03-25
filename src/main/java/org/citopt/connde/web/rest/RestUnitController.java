package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.units.PredefinedQuantities;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.measure.unit.Unit;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestUnitController {

    @GetMapping(value = "/units")
    public ResponseEntity<List<PredefinedQuantities>> getSupportedQuantities() {
        //Get all enum objects as list
        List<PredefinedQuantities> quantitiesList = Arrays.asList(PredefinedQuantities.values());

        return new ResponseEntity<>(quantitiesList, HttpStatus.OK);
    }

    @GetMapping(value = "/units", params = {"compatible"})
    public ResponseEntity<List<PredefinedQuantities>> getSupportedCompatibleQuantities(
            @RequestParam("compatible") String compatibleUnit) {
        //Try to get compatible quantities
        try {
            List<PredefinedQuantities> quantitiesList = PredefinedQuantities.getCompatibleQuantities(compatibleUnit);
            return new ResponseEntity<>(quantitiesList, HttpStatus.OK);
        } catch (Exception e) {
            //Catch parsing exceptions
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/units", params = {"first", "second"})
    public ResponseEntity<Boolean> checkUnitsForCompatibility(@RequestParam("first") String firstUnitString,
                                                              @RequestParam("second") String secondUnitString) {
        Unit firstUnit, secondUnit;

        //Try to convert both strings to units
        try {
            firstUnit = Unit.valueOf(firstUnitString);
            secondUnit = Unit.valueOf(secondUnitString);
        } catch (Exception e) {
            //Catch parsing exceptions
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Check compatibility
        boolean compatible = firstUnit.isCompatible(secondUnit);

        return new ResponseEntity<>(compatible, HttpStatus.OK);
    }
}
