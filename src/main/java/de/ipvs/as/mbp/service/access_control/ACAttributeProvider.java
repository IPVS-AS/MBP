package de.ipvs.as.mbp.service.access_control;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;
import de.ipvs.as.mbp.domain.access_control.ACAccess;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAttributeNotAvailableException;
import de.ipvs.as.mbp.domain.access_control.ACAttributeValue;
import de.ipvs.as.mbp.domain.access_control.ACConditionSimpleAttributeArgument;
import de.ipvs.as.mbp.domain.access_control.ACEntityType;
import de.ipvs.as.mbp.domain.access_control.IACEntity;

/**
 * Service that provides additional attributes used for evaluating access requests.
 * 
 * @author Jakob Benz
 */
public class ACAttributeProvider {
	
	@SuppressWarnings("unchecked")
	public <T extends Comparable<T>> Optional<T> getValueForAttributeArgument(ACConditionSimpleAttributeArgument<T> attributeArgument, ACAccess access, ACAccessRequest request) throws ACAttributeNotAvailableException {
		// Retrieve entity associated with the attribute argument
		IACEntity associatedEntity = access.getEntityForType(attributeArgument.getKey().getEntityType());
		
		// 1) Check whether the corresponding attribute value can be found in the list of attributes from the access request (only if attribute of requesting entity)
		Optional<T> valueFromRequest = Optional.empty();
		if (attributeArgument.getKey().getEntityType() == ACEntityType.REQUESTING_ENTITY
				&& request.getContext().stream().anyMatch(a -> a.getKey().equals(attributeArgument.getKey()))) {
			valueFromRequest =  Optional.of((T) request.getContext().stream().filter(a -> a.getKey().getId().equals(attributeArgument.getKey().getId())).findFirst().get().getValue());
		}
		
		
		// 2) Check attribute value from records
		String valuePath = attributeArgument.getKey().getValueLookupPath();
		String[] valuePathSteps = valuePath.split("\\.");
		Optional<T> valueFromRecords = Optional.empty();
		try {
			// Get all attribute value candidate fields
			List<Field> fieldsWithAttributeAnnotation = FieldUtils.getFieldsListWithAnnotation(associatedEntity.getClass(), ACAttributeValue.class);
			for (Field field : fieldsWithAttributeAnnotation) {
				// NOTE: Currently only one-step value lookup paths are supported (more is not needed at the moment)
				
				// Match the field name against the first value path step
				if (field.getName().equals(valuePathSteps[0])) {					
					// Lookup the field via the lookup path
					Field currentField = field;
					// Make (private) field accessible (only) via reflection
					currentField.setAccessible(true);
					valueFromRecords = Optional.of((T) currentField.get(associatedEntity));
				}
			}
		} catch (Exception e) {
			return Optional.empty();
		}
		
		if (valueFromRequest.isPresent() && valueFromRecords.isPresent()) {
			if (valueFromRequest.get().equals(valueFromRecords.get())) {
				return valueFromRecords;
			} else {
				// Consider denying access directly from this point (e.g., via exception)
				return Optional.empty();
			}
		} else if (valueFromRequest.isPresent() ^ valueFromRecords.isPresent()) {
			return valueFromRecords.isPresent() ? valueFromRecords : valueFromRequest;
		} else {
			return Optional.empty();
		}
	}

}
