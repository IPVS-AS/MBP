package access_control;

import java.util.ArrayList;
import java.util.List;

import org.citopt.connde.MBPApplication;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.access_control.TestObj;
import org.citopt.connde.repository.TestObjRepository;
import org.citopt.connde.repository.UserEntityRepository;
import org.citopt.connde.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
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
		
//		ExampleMatcher matcher = ExampleMatcher
//				.matching()
//				.withIgnorePaths("_id")
//				.withIgnorePaths("policies.name")
//				.withIgnorePaths("policies.priority")
//				.withIgnorePaths("policies.condition")
//				.withIgnorePaths("policies.effects")
//				.withMatcher("policies", GenericPropertyMatchers.contains());
//		TestObj probe = new TestObj();
//		probe.policies.add(new ACPolicy<Double>(null, -1, acs, null, null));
		List<ACAccessType> accessType = new ArrayList<>();
//		accessType.add(ACAccessType.READ);
		accessType.add(ACAccessType.UPDATE);
//		testObjRepository.findX1(accessType).forEach(t -> System.out.println(t.i));
		
//		List<TestObj> objs = testObjRepository.findAll();
//		objs.forEach(o -> o.setOwner(userRepository.findOne("5f1e7f8015ad9129b866ea8a")));
//		objs.forEach(o -> testObjRepository.save(o));
		
		
//		PageRequest pageRequest = new PageRequest(0, 10);
//		Page<TestObj> page = testObjRepository.findAll(pageRequest);
//		page.forEach(t -> System.out.println(t.s));
		
//		User user = new User();
//		user.setUsername("admin");
//		user.setPassword("admin");
//		user.setFirstName("admin");
//		user.setLastName("admin");
//		user = userRepository.save(user);
//		System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(user));
		
//		System.err.println(testObjRepository.findAll().size());
//		testObjRepository.findAll().forEach(this::print);
		
		String ownerId = "5f1e7f8015ad9129b866ea8a";
		List<String> ats = new ArrayList<>();
		ats.add(ACAccessType.READ.toString());
//		ats.add(ACAccessType.UPDATE.toString());
//		List<TestObj> result = testObjRepository.findByOwnerOrPolicyAccessTypeMatchAll(ownerId, ats, new PageRequest(0, 10));
//		List<TestObj> result = testObjRepository.findByPolicyAccessTypeMatchAll(ats);
		List<TestObj> result = testObjRepository.findByOwner(ownerId, new PageRequest(1, 20));
		System.err.println(result.size());
		result.forEach(t -> System.out.println(t.i));
		
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
