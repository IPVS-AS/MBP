package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.units.PredefinedQuantities;
import org.citopt.connde.domain.units.PredefinedUnit;
import org.citopt.connde.domain.units.UnitConversion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.measure.Measure;
import javax.measure.quantity.Quantity;
import javax.measure.unit.ProductUnit;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.util.Arrays;
import java.util.List;

import static javax.measure.unit.NonSI.HOUR;
import static javax.measure.unit.SI.*;

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

    @GetMapping(value = "/units/factor", params = {"startUnit", "targetUnit"})
    public ResponseEntity<UnitConversion> getUnitConversionFactor(
            @RequestParam("startUnit") String startUnitString,
            @RequestParam("targetUnit") String targetUnitString) {

        try {
            //Try to parse the nit strings and to create unit objects
            Unit startUnit = Unit.valueOf(startUnitString);
            Unit targetUnit = Unit.valueOf(targetUnitString);

            //Create conversion object from both units
            UnitConversion conversion = new UnitConversion(startUnit, targetUnit);

            return new ResponseEntity<>(conversion, HttpStatus.OK);
        } catch (Exception e) {
            //Catch parsing exceptions
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
