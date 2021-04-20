package de.ipvs.as.mbp.web.rest.event_handler;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.repository.SensorRepository;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;


/**
 * Event handler for operations that are performed on tests.
 */
@Component
@RepositoryEventHandler
public class TestDetailsEventHandler {

    @Autowired
    private SensorRepository sensorRepository;

    /**
     * Called in case an test is supposed to be created. This method checks, if the required components are registered.
     * If the TestDevice does not exist, it will be automatically created. Sensor/Actuator Simulators will be automatically registered, if the operators are existing.
     *
     * @param testDetails Test with details to be created
     */
    @HandleBeforeCreate

    public void testStarted(TestDetails testDetails) {


        List<Sensor> sensorArray = new ArrayList<>();

        if (testDetails.getType().size() > 0) {
            // checks if the sensors for the test are registered. If not this will be done in the following, if the specific operators are existing.
            for (String sensType : testDetails.getType()) {
                Sensor sensor = sensorRepository.findByName(sensType).get();
                sensorArray.add(sensor);
            }
        }

        testDetails.setSensor(sensorArray);
    }





}

