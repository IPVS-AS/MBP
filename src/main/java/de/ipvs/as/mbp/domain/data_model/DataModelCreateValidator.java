package de.ipvs.as.mbp.domain.data_model;

import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTreeNode;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.stereotype.Service;

/**
 * Validator for DataModel trees. Criterias, amongst other things: Non-cyclic, connected, primitive data fields
 * as leaves, objects have at least one child, arrays have exactly one child
 */
@Service
public class DataModelCreateValidator implements ICreateValidator<DataModel> {

    @Override
    public void validateCreatable(DataModel entity) {

        //Sanity check
        if (entity == null) {
            throw new EntityValidationException("The entity is invalid.");
        }

        //Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create, because some fields are invalid.");

        //Check name
        if (Validation.isNullOrEmpty(entity.getName())) {
            exception.addInvalidField("name", "The name must not be empty.");
        }

        //Check treeNode array
        if (entity.getTreeNodes() == null || entity.getTreeNodes().size() <= 0) {
            exception.addInvalidField("treeNodes", "The data model must not be empty.");
        }

        //Throw exception if there are invalid fields so far
        if (exception.hasInvalidFields()) {
            throw exception;
        }

        DataModelTree tree = new DataModelTree(entity.getTreeNodes());
        entity.setJSONExample(tree.getJSONExample());
        entity.setPossibleVisMappings(tree.getPossibleVisualizationMappings());
        System.out.println("test");
    }
}
