package de.ipvs.as.mbp.domain.component;

import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.service.deployment.DeployerDispatcher;
import de.ipvs.as.mbp.service.deployment.IDeployer;
import de.ipvs.as.mbp.service.validation.IDeleteValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ComponentDeleteValidator implements IDeleteValidator<Component> {

    @Autowired
    private DeployerDispatcher deployerDispatcher;

    @Override
    public void validateDeletable(Component component) {
        //Find suitable deployer component
        IDeployer deployer = deployerDispatcher.getDeployer();

        try {
            //Try to determine deployment status
            if (deployer.isComponentDeployed(component)) {
                //Component is deployed, notify the user
                throw new MBPException(HttpStatus.CONFLICT, "Component '" + component.getName() + "' cannot be deleted since it is still deployed.");
            }
        } catch (Exception ignored) {
            //Could not determine deployment status, delete anyway
        }
    }

}
