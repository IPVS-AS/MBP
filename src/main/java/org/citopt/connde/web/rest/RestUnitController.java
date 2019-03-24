package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.units.PredefinedQuantities;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/units")
    public ResponseEntity<List<PredefinedQuantities>> getSupportedQuantities() {
        //Get all enum objects as list
        List<PredefinedQuantities> quantitiesList = Arrays.asList(PredefinedQuantities.values());

        return new ResponseEntity<>(quantitiesList, HttpStatus.OK);
    }
}
