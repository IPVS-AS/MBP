package access_control;

import java.util.ArrayList;
import java.util.List;

import org.citopt.connde.MBPApplication;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.access_control.ACAttribute;
import org.citopt.connde.domain.access_control.ACDataType;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.TestObjRepository;
import org.citopt.connde.repository.UserRepository;
import org.citopt.connde.util.Pages;
import org.citopt.connde.web.rest.RestDeviceController;
import org.citopt.connde.web.rest.RestTestObjController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author root
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MBPApplication.class)
public class Test1 {
	
	@Autowired
	private TestObjRepository testObjRepository;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private DeviceRepository dr;
	
	@Autowired
	private RestDeviceController restDeviceController;
	
	@Autowired
	private RestTestObjController restTestObjController;
	
	@Test
	public void test() throws JsonProcessingException {
//		List<ACAccessType> acs = new ArrayList<>();
//		acs.add(ACAccessType.READ);
//		acs.add(ACAccessType.UPDATE);
//		IACCondition c = new ACSimpleCondition<Double>(ACArgumentFunction.EQUALS, new ACConditionSimpleValueArgument<Double>(1D), new ACConditionSimpleValueArgument<Double>(1D));
//		IACEffect<Double> e = new ACDoubleAccuracyEffect(10, 5);
//		List<IACEffect<Double>> effects = new ArrayList<IACEffect<Double>>();
//		effects.add(e);
		
		int index = 104;
//		TestObj t = new TestObj();
//		t.i = index;
//		t.s = "s" + index;
//		t.policies.add(new ACPolicy<Double>("AC-" + index + "-1", 1, acs, c, effects));
//		t.policies.add(new ACPolicy<Double>("AC-" + index + "-2", 2, acs, c, effects));
//		t.policies.add(new ACPolicy<Double>("AC-" + index + "-3", 3, acs, c, effects));
//		testObjRepository.save(t);
		
		List<ACAccessType> accessType = new ArrayList<>();
//		accessType.add(ACAccessType.READ);
		accessType.add(ACAccessType.UPDATE);
//		testObjRepository.findX1(accessType).forEach(t -> System.out.println(t.i));
		String ownerId = "5f1e7f8015ad9129b866ea8a";
		List<String> ats = new ArrayList<>();
		ats.add(ACAccessType.READ.toString());
//		ats.add(ACAccessType.UPDATE.toString());
//		List<TestObj> result = testObjRepository.findByOwnerOrPolicyAccessTypeMatchAll(ownerId, ats, new PageRequest(0, 10));
//		List<TestObj> result = testObjRepository.findByPolicyAccessTypeMatchAll(ats);
//		List<TestObj> result = testObjRepository.findByOwner(ownerId, PageRequest.of(1, 20));
//		System.err.println(result.size());
//		result.forEach(t -> System.out.println(t.i));
		
		
		
		
		List<ACAttribute<? extends Comparable<?>>> attributes = new ArrayList<>();
    	attributes.add(new ACAttribute<String>(ACDataType.ALPHABETIC, "firstName", "Jakob"));
    	attributes.add(new ACAttribute<String>(ACDataType.ALPHABETIC, "lastName", "Benz"));
		ACAccessRequest r = new ACAccessRequest(attributes);
//		System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(r));
//		System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(restTestObjController.all1()));
//		System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(restTestObjController.all2(r)));
		
//		System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(dr.findAll(Pages.ALL)));
		
		System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(restDeviceController.all(Pages.ALL, r)));
		
	}
	
	public void print(Object o) {
		try {
			System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
