package org.citopt.connde.service.access_control;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.util.Strings;
import org.citopt.connde.domain.access_control.ACAccess;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAttributeKey;
import org.citopt.connde.domain.access_control.ACAttributeNotAvailableException;
import org.citopt.connde.domain.access_control.ACAttributeValue;
import org.citopt.connde.domain.access_control.ACConditionSimpleAttributeArgument;
import org.citopt.connde.domain.access_control.ACConditionSimpleValueArgument;
import org.citopt.connde.domain.access_control.ACSimpleCondition;
import org.citopt.connde.domain.access_control.IACConditionArgument;
import org.citopt.connde.domain.access_control.IACEntity;

/**
 * Evaluator for simple {@link ACSimpleCondition conditions}.
 * 
 * @param <T> the data type of the simple condition arguments.
 * @author Jakob Benz
 */
public class ACSimpleConditionEvaluator<T extends Comparable<T>> extends ACAbstractConditionEvaluator<ACSimpleCondition<T>> {
	
	public ACSimpleConditionEvaluator() {
		// TODO Auto-generated constructor stub
	}
	
 	@Override
	public boolean evaluate(ACSimpleCondition<T> condition, ACAccess access, ACAccessRequest request) {
		Optional<T> leftValue = getValueForArgument(condition.getLeft(), access, request);
		Optional<T> rightValue = getValueForArgument(condition.getRight(), access, request);
		
		// Non-present (null) values are currently not supported
		if (!leftValue.isPresent()  || !rightValue.isPresent()) {
			return false;
		}
		
		// Compare arguments using the specified function and return result
		return condition.getFunction().apply(leftValue.get(), rightValue.get());
	}
	
	/**
	 * TODO: Comment
	 * 
	 * @param argument
	 * @param access
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Optional<T> getValueForArgument(IACConditionArgument argument, ACAccess access, ACAccessRequest request) {
		if (argument instanceof ACConditionSimpleAttributeArgument<?>) {
			try {
				return getValueForAttributeArgument((ACConditionSimpleAttributeArgument<T>) argument, access, request);
			} catch (ACAttributeNotAvailableException e) {
				return Optional.empty();
			}
		} else if (argument instanceof ACConditionSimpleValueArgument<?>) {
			return Optional.of((T) ((ACConditionSimpleValueArgument<T>) argument).getValue());
		} else {
			return null;
		}
	}
	
	/**
	 * TODO: Comment
	 * 
	 * @param attributeArgument
	 * @param access
	 * @param request
	 * @return
	 * @throws ACAttributeNotAvailableException
	 */
	@SuppressWarnings("unchecked")
	private Optional<T> getValueForAttributeArgument(ACConditionSimpleAttributeArgument<T> attributeArgument, ACAccess access, ACAccessRequest request) throws ACAttributeNotAvailableException {
		// Retrieve entity associated with the attribute argument
		IACEntity associatedEntity = access.getEntityForType(attributeArgument.getEntityType());
		
		// 1) Check whether the corresponding attribute value can be found in the list of attributes from the access request
		if (request.getContext().stream().anyMatch(a -> a.getKey().equals(attributeArgument.getKey()))) {
			return Optional.of((T) request.getContext().stream().filter(a -> a.getKey().equals(attributeArgument.getKey())).findFirst().get().getValue());
		}
		
		// 2) Check whether the corresponding attribute value can be found in the requesting entity itself
		try {
			// Get all attribute value candidate fields
			List<Field> fieldsWithAttributeAnnotation = FieldUtils.getFieldsListWithAnnotation(associatedEntity.getClass(), ACAttributeValue.class);
			for (Field field : fieldsWithAttributeAnnotation) {
				// Get the ACAttributeValue annotation from the field
				ACAttributeValue attribute = field.getAnnotation(ACAttributeValue.class);
				// Get attribute key (defaults to the field name)
				String key = !Strings.isNullOrEmpty(attribute.keyString()) ? attribute.keyString() : (attribute.key() == null ? field.getName() : attribute.key().getKey());
				
				// Check whether the current field is the attribute we are looking for
				if (attributeArgument.getKey().equals(key)) {
					// Get the lookup path for attribute value (could be a property of a nested object)
					String valuePath = !Strings.isNullOrEmpty(attribute.valueLookupPath()) ? attribute.valueLookupPath() : (attribute.key() == null ? field.getName() : attribute.key().getValueLookupPath());
					String[] valuePathSteps = valuePath.split(".");
					
					if (Strings.isNullOrEmpty(valuePath)) {						
						// Return the field value
						return Optional.of((T) field.get(associatedEntity));
					} else {
						// Lookup the field via the lookup path
						Field currentField = field;
						for (String valuePathStep : valuePathSteps) {
							Object tempValue = currentField.get(associatedEntity);
							currentField = tempValue.getClass().getField(valuePathStep);
						}
						return Optional.of((T) currentField.get(associatedEntity));
						// TODO: TEST TEST TEST !!!
					}
				}
			}
			
			// No matching field found in the associated entity
			return Optional.empty();
		} catch (Exception e) {
			throw new ACAttributeNotAvailableException(e.getMessage(), e);
		}
	}

}
