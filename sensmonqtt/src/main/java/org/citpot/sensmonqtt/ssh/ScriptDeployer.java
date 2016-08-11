/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citpot.sensmonqtt.ssh;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;

import com.jcabi.ssh.Shell;
import com.jcabi.ssh.SSH;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import org.citopt.sensmonqtt.device.Script;

/**
 *
 * @author rafaelkperes
 */
public class ScriptDeployer {

    public void deployScript(String id, Collection<Script> scripts, String url, Integer port, String user, String key) throws UnknownHostException, IOException {

        Shell shell = new Shell.Safe(
                new SSH(
                        url, port,
                        user, key
                )
        );

        OutputStream stdout = new ByteArrayOutputStream();
        OutputStream stderr = new ByteArrayOutputStream();
        shell.exec(
                "mkdir ~/scripts/",
                new ByteArrayInputStream("".getBytes()),
                stdout,
                stderr
        );

        shell.exec(
                "mkdir ~/scripts/" + id,
                new ByteArrayInputStream("".getBytes()),
                stdout,
                stderr
        );

        for (Script script : scripts) {
            shell.exec(
                    "cat > ~/scripts/" + id + "/" + script.getName() + ".py",
                    new ByteArrayInputStream(script.getScript()),
                    stdout,
                    stderr
            );
        }
    }

}
