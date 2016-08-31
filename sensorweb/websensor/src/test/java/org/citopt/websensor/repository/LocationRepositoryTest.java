package org.citopt.websensor.repository;

import java.util.ArrayList;
import java.util.List;
import org.citopt.websensor.RootTestConfiguration;
import org.citopt.websensor.domain.Location;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RootTestConfiguration.class})
public class LocationRepositoryTest {

    @Autowired
    LocationRepository repository;

    private static final String NAME = "primeiro";
    private static final String DESCRIPTION = "huehuehue";
    private static final Location LOCATION = new Location(NAME, DESCRIPTION);

    private static final String ANOTHER_NAME = "segundo";
    private static final String ANOTHER_DESCRIPTION = "jajajaja";
    private static final Location ANOTHER_LOCATION = new Location(ANOTHER_NAME,
        ANOTHER_DESCRIPTION);

    public LocationRepositoryTest() {
    }

    @Before
    public void setUp() {
        repository.deleteAll();

        repository.save(LOCATION);
        repository.save(ANOTHER_LOCATION);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFindByName() {
        System.out.println("findByName");
        String name = NAME;
        Location expResult = LOCATION;
        Location result = repository.findByName(name);
        System.out.println(result);
        assertEquals(expResult, result);
    }

    @Test
    public void testFindByDescription() {
        System.out.println("findByDescription");
        String description = ANOTHER_DESCRIPTION;
        List<Location> expResult = new ArrayList<>();
        expResult.add(ANOTHER_LOCATION);
        List<Location> result = repository.findByDescription(description);
        System.out.println(result);
        assertEquals(expResult, result);
    }

}
