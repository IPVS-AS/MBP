package org.citopt.websensor.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class IndexControllerTest {

    @Configuration
    static class IndexControllerTestConfiguration {
        
        @Bean
        public IndexController indexController() {
            return new IndexController();
        }
    }
    
    @Autowired
    private IndexController indexController;

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testViewIndex() {
        System.out.println("viewIndex");
        String expResult = "index";
        String result = indexController.viewIndex(null);
        assertEquals(expResult, result);
    }

}
