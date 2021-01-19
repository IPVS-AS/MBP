package de.ipvs.as.mbp.web.rest.event_handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public void testStarted(TestDetails testDetails)  {

        List<Sensor> sensorArray = new ArrayList<>();

        List<String> f = testDetails.getType();


        if(testDetails.getType().size() > 0){
            // checks if the sensors for the test are registered. If not this will be done in the following, if the specific operators are existing.
            for (String sensType : testDetails.getType()) {
                Sensor sensor = sensorRepository.findByName(sensType).get();
                sensorArray.add(sensor);
            }
        }


        // checks if the testing actuator is registered
        //  Actuator actuator = actuatorRepository.findByName("TestingActuator");

        testDetails.setSensor(sensorArray);
    }


    /**
     * Called in case a test is supposed to be deleted. This method then takes care of deleting all
     * test reports that are associated with this test.
     *
     * @param test The sensor that is supposed to be deleted

    @HandleAfterDelete
    public void afterSensorDelete(TestDetails test) {
        String testId = test.getId();

        if (testDetails.isPdfExists()){
            Path pathTestReport = Paths.get(test.getPathPDF());
            Path pathDiagram = Paths.get(pathTestReport.getParent().toString(), testId + ".gif");

            try {
                Files.delete(pathTestReport);
                Files.delete(pathDiagram);
                response = new ResponseEntity<>("Test report successfully deleted", HttpStatus.OK);
            } catch (NoSuchFileException x) {
                response = new ResponseEntity<>("Test report doesn't exist.", HttpStatus.NOT_FOUND);
            } catch (IOException x) {
                response = new ResponseEntity<>(x, HttpStatus.CONFLICT);
            }
        } else {
            response = new ResponseEntity<>("No available Test report for this Test.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

     */
}

