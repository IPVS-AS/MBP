package org.citopt.connde.service.access_control;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.citopt.connde.domain.access_control.ACAccess;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAttributeNotAvailableException;
import org.citopt.connde.domain.access_control.ACAttributeValue;
import org.citopt.connde.domain.access_control.ACConditionSimpleAttributeArgument;
import org.citopt.connde.domain.access_control.ACEntityType;
import org.citopt.connde.domain.access_control.IACEntity;

/**
 * Service that provides additional attributes used for evaluating access requests.
 * 
 * @author Jakob Benz
 */
public class ACAttributeProvider {
	
	@SuppressWarnings("unchecked")
	public <T extends Comparable<T>> Optional<T> getValueForAttributeArgument(ACConditionSimpleAttributeArgument<T> attributeArgument, ACAccess access, ACAccessRequest request) throws ACAttributeNotAvailableException {
		String filename = "/Users/jakob/Desktop/log2.txt";
		if (new File(filename).exists()) {
			new File(filename).delete();
		}
		try {
			FileWriter fw = new FileWriter(filename);
			try { fw.write("1: " + attributeArgument.getKey() + "\n"); } catch (Exception e) { e.printStackTrace(); }
			try { fw.write("2: " + attributeArgument.getKey().getId() + "\n"); } catch (Exception e) { e.printStackTrace(); }
			try { fw.write("3: " + attributeArgument.getEntityType() + "\n"); } catch (Exception e) { e.printStackTrace(); }
			try { fw.write("4: " + access.getRequestingEntity().getId() + "\n"); } catch (Exception e) { e.printStackTrace(); }
			for (int i = 0; i < request.getContext().size(); i++) {				
				try { fw.write("5: " + request.getContext().get(i).getKey() + "\n"); } catch (Exception e) { e.printStackTrace(); }
				try { fw.write("5: " + request.getContext().get(i).getValue() + "\n"); } catch (Exception e) { e.printStackTrace(); }
			}
			fw.flush();
		
		
		
		
		
		// Retrieve entity associated with the attribute argument
		IACEntity associatedEntity = access.getEntityForType(attributeArgument.getEntityType());
		
		// 1) Check whether the corresponding attribute value can be found in the list of attributes from the access request (only if attribute of requesting entity)
		Optional<T> valueFromRequest = Optional.empty();
		if (attributeArgument.getKey().getEntityType() == ACEntityType.REQUESTING_ENTITY
				&& request.getContext().stream().anyMatch(a -> a.getKey().equals(attributeArgument.getKey()))) {
			valueFromRequest =  Optional.of((T) request.getContext().stream().filter(a -> a.getKey().getId().equals(attributeArgument.getKey().getId())).findFirst().get().getValue());
		}
		
		try { fw.write("6: " + valueFromRequest + "\n"); } catch (Exception e) { e.printStackTrace(); }
		
		// 2) Check attribute value from records
		String valuePath = attributeArgument.getKey().getValueLookupPath();
		String[] valuePathSteps = valuePath.split("\\.");
		Optional<T> valueFromRecords = Optional.empty();
		try { fw.write("9.00: " + valuePath + "\n"); } catch (Exception e) { e.printStackTrace(); }
		for (String valuePathStep : valuePathSteps) {			
			try { fw.write("9.0: " + valuePathStep + "\n"); } catch (Exception e) { e.printStackTrace(); }
		}
		try {
			// Get all attribute value candidate fields
			List<Field> fieldsWithAttributeAnnotation = FieldUtils.getFieldsListWithAnnotation(associatedEntity.getClass(), ACAttributeValue.class);
			topLevelFieldLoop: for (Field field : fieldsWithAttributeAnnotation) {
				try { fw.write("9.1: " + field.getName() + "\n"); } catch (Exception e) { e.printStackTrace(); }
				// Match the field name against the first value path step
				if (field.getName().equals(valuePathSteps[0])) {					
					// Lookup the field via the lookup path
					Field currentField = field;
					// Make (private) field accessible (only) via reflection
					currentField.setAccessible(true);
					
					// Iterate over the value lookup path until the value is found
					Object tempValue = null;
					for (String valuePathStep : valuePathSteps) {
						tempValue = currentField.get(associatedEntity);
						try {
							// Try to go one step further in the lookup path
							currentField = tempValue.getClass().getDeclaredField(valuePathStep);
						} catch (NoSuchFieldException nsfe) {
							try { fw.write("9.1: " + "No field!" + "\n"); } catch (Exception e) { e.printStackTrace(); }
							// No such field exists -> continue with the next attribute field in the associated entity
							continue topLevelFieldLoop;
						}
					}
					
					// Make (private) field accessible (only) via reflection
					currentField.setAccessible(true);
					valueFromRecords = Optional.of((T) currentField.get(tempValue));
				}
			}
		} catch (Exception e) {
			fw.close();
			try { fw.write("7: " + e.getMessage() + "\n"); } catch (Exception e1) { e1.printStackTrace(); }
			return Optional.empty();
		}
		
		try { fw.write("8: " + valueFromRecords + "\n"); } catch (Exception e) { e.printStackTrace(); }
		fw.close();
		
		if (valueFromRequest.isPresent() && valueFromRecords.isPresent()) {
			if (valueFromRequest.get().equals(valueFromRecords.get())) {
				return valueFromRecords;
			} else {
				// TODO: Consider denying access directly from this point (e.g., via exception)
				return Optional.empty();
			}
		} else if (valueFromRequest.isPresent() ^ valueFromRecords.isPresent()) {
			return valueFromRecords.isPresent() ? valueFromRecords : valueFromRequest;
		} else {
			return Optional.empty();
		}
		
		
		} catch (Exception e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

}
