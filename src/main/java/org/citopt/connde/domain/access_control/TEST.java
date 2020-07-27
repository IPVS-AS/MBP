package org.citopt.connde.domain.access_control;

import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class TEST {
	
	private static final Logger LOGGER = Logger.getLogger(TEST.class.getName());
	
	public static class TestC {
		public String s = "TestC";
		int x = 0;
		int y = 1;
	}
	
	@Bean
	public TestC test() {
		LOGGER.info("Test1");
		return new TestC();
	}

}
