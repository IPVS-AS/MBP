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

/**
 *
 * @author rafaelkperes
 */
public class ScriptDeployer {

    public void deployScript(String script, String url, Integer port, String user, String key) throws UnknownHostException, IOException {

        Shell shell = new Shell.Safe(
                new SSH(
                        url, port,
                        user, key
                )
        );

        OutputStream stdout = new ByteArrayOutputStream();
        OutputStream stderr = new ByteArrayOutputStream();
        shell.exec(
                "cat > d.txt",
                new ByteArrayInputStream("hehehe".getBytes()),
                stdout,
                stderr
        );
    }

}
