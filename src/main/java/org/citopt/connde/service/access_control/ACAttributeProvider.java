package org.citopt.connde.service.access_control;

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
		// Retrieve entity associated with the attribute argument
		IACEntity associatedEntity = access.getEntityForType(attributeArgument.getEntityType());
		
		// 1) Check whether the corresponding attribute value can be found in the list of attributes from the access request (only if attribute of requesting entity)
		Optional<T> valueFromRequest = Optional.empty();
		if (attributeArgument.getEntityType() == ACEntityType.REQUESTING_ENTITY
				&& request.getContext().stream().anyMatch(a -> a.getKey().equals(attributeArgument.getKey()))) {
			valueFromRequest =  Optional.of((T) request.getContext().stream().filter(a -> a.getKey().equals(attributeArgument.getKey())).findFirst().get().getValue());
		}
		
		// 2) Check attribute value from records
		String valuePath = attributeArgument.getKey().getValueLookupPath();
		String[] valuePathSteps = valuePath.split("\\.");
		Optional<T> valueFromRecords = Optional.empty();
		try {
			// Get all attribute value candidate fields
			List<Field> fieldsWithAttributeAnnotation = FieldUtils.getFieldsListWithAnnotation(associatedEntity.getClass(), ACAttributeValue.class);
			topLevelFieldLoop: for (Field field : fieldsWithAttributeAnnotation) {
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
			return Optional.empty();
		}
		
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
	}

}
