package access_control;

import org.citopt.connde.MBPApplication;
import org.citopt.connde.domain.access_control.ACAbstractCondition;
import org.citopt.connde.domain.access_control.ACArgumentFunction;
import org.citopt.connde.domain.access_control.ACCompositeCondition;
import org.citopt.connde.domain.access_control.ACConditionSimpleValueArgument;
import org.citopt.connde.domain.access_control.ACLogicalOperator;
import org.citopt.connde.domain.access_control.ACSimpleCondition;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.ACConditionRepository;
import org.citopt.connde.repository.ACEffectRepository;
import org.citopt.connde.repository.ACPolicyRepository;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.TestObjRepository;
import org.citopt.connde.repository.UserRepository;
import org.citopt.connde.service.access_control.ACPolicyEvaluationService;
import org.citopt.connde.util.C;
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
	private ACPolicyEvaluationService policyEvaluationService;
	
	@Autowired
	private ACPolicyRepository policyRepository;
	
	@Autowired
	private ACConditionRepository conditionRepository;
	
	@Autowired
	private ACEffectRepository effectRepository;
	
	@Test
	public void test0() throws JsonProcessingException {
		User admin = userRepository.findByUsername("admin").get();

		ACAbstractCondition sc1 = new ACSimpleCondition<Double>("Simple condition 1", "Desc SC 1", ACArgumentFunction.EQUALS, new ACConditionSimpleValueArgument<Double>(1D), new ACConditionSimpleValueArgument<Double>(1D), admin.getId());
		ACAbstractCondition sc2 = new ACSimpleCondition<Double>("Simple condition 2", "Desc SC 2", ACArgumentFunction.EQUALS, new ACConditionSimpleValueArgument<Double>(2D), new ACConditionSimpleValueArgument<Double>(2D), admin.getId());
		
		ACAbstractCondition cc1 = new ACCompositeCondition("Composite condition 1", "Desc CC 1", ACLogicalOperator.AND, C.listOf(sc1, sc2), admin.getId());
		
		conditionRepository.save(sc1);
		conditionRepository.save(sc2);
		conditionRepository.save(cc1);
	}
	
	@Test
	public void test1() throws JsonProcessingException {
		//5f589ddff6b51b0e096b09c0
		
//		List<ACPolicy> policies = policyRepository.findByIdAny(C.listOf("5f589ddff6b51b0e096b09c0", "5f589ddff6b51b0e096b09c1"));
//		List<ACPolicy> policies = policyRepository.findByAccessTypeMatchAll(C.listOf("READ", "UPDATE"));
//		List<ACPolicy> policies = policyRepository.findByIdAnyAndAccessTypeAll(C.listOf("5f589ddff6b51b0e096b09c0", "5f589ddff6b51b0e096b09c1"), C.listOf("READ"));
//		boolean exists = policyRepository.existsByIdAnyAndAccessTypeAll(C.listOf("5f589ddff6b51b0e096b09c0", "5f589ddff6b51b0e096b09c2"), C.listOf("READ, UPDATE"));
//		System.err.println(exists);
//		System.err.println(policies.size());
//		policies.forEach(p -> System.out.println(p.getName()));
		
		////
		
		// 5f218c7822424828a8275037 - admin
		
	}
	
	@Test
	public void test2() throws JsonProcessingException {
	}
	
	@Test
	public void test3() throws JsonProcessingException {
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
