/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.database;

import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.citopt.sensmonqtt.device.Device;
import org.citopt.sensmonqtt.device.Location;
import org.citopt.sensmonqtt.device.Pin;
import org.citopt.sensmonqtt.device.Script;
import org.citopt.sensmonqtt.device.Sensor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

/**
 *
 * @author rafaelkperes
 */
public class DataServiceTest {

    static DataService ds;

    public DataServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws UnknownHostException {
        MongoClient mc = MorphiaUtil.getMongoClient();
        ds = new DataService(MorphiaUtil.getDatastore(mc, "DataServiceTest"));
    }

    @AfterClass
    public static void tearDownClass() throws UnknownHostException {
        MorphiaUtil.getMongoClient().dropDatabase("DataServiceTest");
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCase01() {
        // test if id is null
        Device d = new Device("AA:BB:CC:DD");
        assertNull(d.getId());
        ds.storeDevice(d);
        assertNotNull(d.getId());

        // get device
        Device d2 = ds.getDevice(d.getId());
        assertEquals(d, d2);
        
        // query device
        Device d3 = ds.queryDevice()
                .field("macAddress").equal("AA:BB:CC:DD")
                .iterator().next();
        assertEquals(d, d3);
        assertEquals(d.getId(), d3.getId());
        
        // register pin
        Pin p = new Pin("A0", "0");
        assertNull(p.getId());
        ds.storePin(p);
        assertNotNull(p.getId());
        
        // get all pins
        List<Pin> result = ds.queryPin().asList();
        List<Pin> expResult = new ArrayList();
        expResult.add(p);
        assertEquals(expResult, result);
    }

}
