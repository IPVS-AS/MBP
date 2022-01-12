package de.ipvs.as.mbp.service.deployment;

import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.settings.Settings;
import de.ipvs.as.mbp.service.deployment.demo.DemoDeployer;
import de.ipvs.as.mbp.service.deployment.ssh.SSHDeployer;
import de.ipvs.as.mbp.service.settings.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Dispatches the deployer component bean that is supposed to be used for the deployment of certain MBP components.
 * Thereby, it is also considered whether the demonstration mode is currently active.
 */
@org.springframework.stereotype.Component
public class DeployerDispatcher {

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private SSHDeployer sshDeployer;

    @Autowired
    private DemoDeployer demoDeployer;

    /**
     * Returns the currently suitable deployer component bean, depending on whether the demonstration mode is currently
     * active or not.
     *
     * @return The suitable deployer component bean
     */
    public IDeployer getDeployer() {
        //Retrieve settings from service
        Settings settings = settingsService.getSettings();

        //Check for demonstration mode
        if (settings.isDemoMode()) {
            //Demonstration mode is active, use demo deployer
            return demoDeployer;
        }

        //Use SSH deployer in all other cases
        return sshDeployer;
    }

    /**
     * Returns the deployer component bean that is supposed to be used for the deployment of a given MBP component.
     *
     * @param component The component to retrieve the suitable deployer component bean for
     * @return The suitable deployer component bean
     */
    public IDeployer getDeployer(Component component) {
        //Sanity check
        if (component == null) {
            throw new IllegalArgumentException();
        }

        //Currently no component-specific deployers exist, thus use the default ones
        return getDeployer();
    }
}
