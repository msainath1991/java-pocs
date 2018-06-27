package com.github.diegopacheco.sandbox.java.service.test.v1x;

import org.junit.jupiter.api.Test;

import com.github.diegopacheco.sandbox.java.service.Version;
import com.github.diegopacheco.sandbox.java.service.test.DataServiceTestConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataServiceImplV1x {
	
	@Test	
	public void testWrongVersion(){
		assertThrows(IllegalArgumentException.class, ()->  
			DataServiceTestConfig.getDataService().getCurrentDate(null) );
	}
	
	@Test	
	public void test1xVersionOK(){
			String result = DataServiceTestConfig.getDataService().getCurrentDate(new Version("1.x"));
			assertNotNull(result);
			assertTrue(!"".equals(result));
	}
	
}
