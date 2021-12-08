package de.ipvs.as.mbp.domain.data_model;

import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree;
import de.ipvs.as.mbp.domain.visualization.repo.ValueLogPathObject;
import de.ipvs.as.mbp.domain.visualization.repo.VisMappingInfo;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.stream.Collectors;

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

        // Do not change the order of the following statements!
        DataModelTree tree = new DataModelTree(entity.getTreeNodes());
        entity.setJSONExample(tree.getJSONExample());
        entity.setJsonPathsToLeafNodes(tree.getLeafNodes().stream().map(
                node -> new ValueLogPathObject().setName(node.getName()).setDimension(node.getSize()).setType(node.getType().getName())
                        .setPath(node.getInternPathToNode()).setUnit(node.getUnit())).collect(Collectors.toList()));
        entity.setPossibleVisMappings((ArrayList<VisMappingInfo>) tree.getAllPossibleVisualizationsMappings());
    }
}
