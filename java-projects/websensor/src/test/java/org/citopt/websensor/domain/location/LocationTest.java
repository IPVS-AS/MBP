package org.citopt.websensor.domain.location;

import org.citopt.websensor.domain.Location;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class LocationTest {
    
    private Location instance;
    
    private static final String name = "name";
    private static final String description = "description";

    public LocationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        this.instance = new Location(name, description);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetId() {
        System.out.println("getId");
        ObjectId result = instance.getId();
        assertNull(result);
    }

    @Test
    public void testGetName() {
        System.out.println("getName");
        String expResult = name;
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetDescription() {
        System.out.println("getDescription");
        String expResult = description;
        String result = instance.getDescription();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetDescription() {
        System.out.println("setDescription");
        String new_description = "new description";
        instance.setDescription(new_description);
        String expResult = new_description;
        String result = instance.getDescription();
        assertEquals(expResult, result);
    }

    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        Location another = new Location(name, description);
        int expResult = another.hashCode();
        int result = instance.hashCode();
        assertEquals(expResult, result);
        
        another = new Location("another " + name, description);
        expResult = another.hashCode();
        assertNotEquals(expResult, result);
        
        another = new Location(name, "another " + description);
        expResult = another.hashCode();
        assertNotEquals(expResult, result);
    }

    @Test
    public void testEquals() {
        System.out.println("equals");
        Object obj = null;
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        
        obj = new Location(name, description);
        expResult = true;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        
        obj = new Location(name, "another " + description);
        expResult = true;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        
        obj = new Location("another " + name, description);
        expResult = false;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        
        obj = new Location("another " + name, "another " + description);
        expResult = false;
        result = instance.equals(obj);
        assertEquals(expResult, result);
    }

}