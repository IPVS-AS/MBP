package de.ipvs.as.mbp.domain.component;

import java.io.IOException;

import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.service.deploy.SSHDeployer;
import de.ipvs.as.mbp.service.validation.IDeleteValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @author Jakob Benz
 */
@Service
public class ComponentDeleteValidator implements IDeleteValidator<Component> {
	
	@Autowired
	private SSHDeployer sshDeployer;

	@Override
	public void validateDeletable(Component component) {
		try {
			if (sshDeployer.isComponentDeployed(component)) {
				throw new MBPException(HttpStatus.CONFLICT, "Component '" + component.getName() + "' cannot be deleted since it is still deployed.");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new MBPException(HttpStatus.INTERNAL_SERVER_ERROR, "Deployment status for component '" + component.getName() + "' could not be determined.");
		}
	}

}
