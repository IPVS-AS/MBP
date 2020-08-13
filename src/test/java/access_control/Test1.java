package access_control;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.citopt.connde.MBPApplication;
import org.citopt.connde.domain.access_control.ACAccess;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.access_control.ACArgumentFunction;
import org.citopt.connde.domain.access_control.ACAttribute;
import org.citopt.connde.domain.access_control.ACConditionSimpleAttributeArgument;
import org.citopt.connde.domain.access_control.ACConditionSimpleValueArgument;
import org.citopt.connde.domain.access_control.ACDataType;
import org.citopt.connde.domain.access_control.ACDoubleAccuracyEffect;
import org.citopt.connde.domain.access_control.ACEntityType;
import org.citopt.connde.domain.access_control.ACPolicy;
import org.citopt.connde.domain.access_control.ACSimpleCondition;
import org.citopt.connde.domain.access_control.IACCondition;
import org.citopt.connde.domain.access_control.IACEffect;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.TestObjRepository;
import org.citopt.connde.repository.UserRepository;
import org.citopt.connde.service.access_control.ACPolicyEvaluationService;
import org.citopt.connde.util.C;
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
 * @author Jakob Benz
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
	private DeviceRepository deviceRepository;
	
	@Autowired
	private RestDeviceController restDeviceController;
	
	@Autowired
	private RestTestObjController restTestObjController;
	
	@Autowired
	private ACPolicyEvaluationService policyEvaluationService;
	
	
	@Test
	public void test0() throws JsonProcessingException {
		Device device = deviceRepository.findById("5f32421db50095677bd137e4").get();
		
		List<ACAccessType> acs = new ArrayList<>();
		acs.add(ACAccessType.READ);
		IACCondition c = new ACSimpleCondition<String>("C1", ACArgumentFunction.EQUALS, new ACConditionSimpleAttributeArgument<>(ACEntityType.REQUESTING_ENTITY, "a1"), new ACConditionSimpleValueArgument<String>("v1"));
		List<IACEffect<?>> effects = new ArrayList<>();
		effects.add(new ACDoubleAccuracyEffect("Test Effect 1", 10, 5));
		ACPolicy p = new ACPolicy("P1", 1, acs, c, effects, null);

		device.getAccessControlPolicies().add(p);
		device = deviceRepository.save(device);
	}
	
	@Test
	public void test1() throws JsonProcessingException {
		ACAccessRequest request = new ACAccessRequest();
		request.getContext().add(new ACAttribute(ACDataType.ALPHABETIC, "firstName", "Jakob"));
		request.getContext().add(new ACAttribute(ACDataType.ALPHABETIC, "a1", "v1"));
		
		User user = userRepository.findOneByUsername("test1").get();
		String adminUserId = "5f218c7822424828a8275037";
		
//		final ACAccess access = new ACAccess(ACAccessType.READ, user, device);
//		device.getAccessControlPolicies().forEach(p -> {
//			System.err.println(policyEvaluationService.evaluate(p, access, request));
//		});
	}
	
	@Test
	public void test2() throws JsonProcessingException {
//		List<ACAccessType> acs = new ArrayList<>();
//		acs.add(ACAccessType.READ);
//		acs.add(ACAccessType.UPDATE);
//		IACCondition c = new ACSimpleCondition<Double>(ACArgumentFunction.EQUALS, new ACConditionSimpleValueArgument<Double>(1D), new ACConditionSimpleValueArgument<Double>(1D));
//		IACEffect<Double> e = new ACDoubleAccuracyEffect(10, 5);
//		List<IACEffect<Double>> effects = new ArrayList<IACEffect<Double>>();
//		effects.add(e);
		
//		TestObj t = new TestObj();
//		t.i = index;
//		t.s = "s" + index;
//		t.policies.add(new ACPolicy<Double>("AC-" + index + "-1", 1, acs, c, effects));
//		t.policies.add(new ACPolicy<Double>("AC-" + index + "-2", 2, acs, c, effects));
//		t.policies.add(new ACPolicy<Double>("AC-" + index + "-3", 3, acs, c, effects));
//		testObjRepository.save(t);
		
		System.out.println("1111");
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
