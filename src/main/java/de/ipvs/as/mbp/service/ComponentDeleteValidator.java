package de.ipvs.as.mbp.service;

import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.service.deploy.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ComponentDeleteValidator implements IDeleteValidator<Component> {

    @Autowired
    private SSHDeployer sshDeployer;

    @Override
    public void validateDeletable(Component component) {
        try {
            //Try to determine deployment status
            if (sshDeployer.isComponentDeployed(component)) {
                //Component is deployed, notify the user
                throw new MBPException(HttpStatus.CONFLICT, "Component '" + component.getName() + "' cannot be deleted since it is still deployed.");
            }
        } catch (IOException ignored) {
            //Could not determine deployment status, delete anyway
        }
    }

}
