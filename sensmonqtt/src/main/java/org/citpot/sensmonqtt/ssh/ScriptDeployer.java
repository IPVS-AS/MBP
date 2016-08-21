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
import java.util.List;
import java.util.Map;
import org.citopt.sensmonqtt.device.Pin;
import org.citopt.sensmonqtt.device.Script;

/**
 *
 * @author rafaelkperes
 */
public class ScriptDeployer {

    private static final String SCRIPTDIR = "/home/pi/scripts/";

    public void deployScript(String id, List<Pin> pinset, Script script, String url, Integer port, String mqtt, String user, String key) throws UnknownHostException, IOException {

        String dir = SCRIPTDIR + id;
        String servicename = "s" + id;

        Shell shell = new Shell.Safe(
                new SSH(
                        url, port,
                        user, key
                )
        );

        OutputStream stdout = new ByteArrayOutputStream();
        OutputStream stderr = new ByteArrayOutputStream();
        shell.exec(
                "mkdir -p " + SCRIPTDIR,
                new ByteArrayInputStream("".getBytes()),
                stdout,
                stderr
        );

        shell.exec(
                "mkdir -p " + dir,
                new ByteArrayInputStream("".getBytes()),
                stdout,
                stderr
        );

        List<Map<Script.ScriptIndex, String>> scripts = script.getScript();

        for (Map<Script.ScriptIndex, String> s : scripts) {
            String name = s.get(Script.ScriptIndex.NAME);
            String content = s.get(Script.ScriptIndex.CONTENT);
            shell.exec(
                    "cat > " + dir + "/" + name,
                    new ByteArrayInputStream(content.getBytes()),
                    stdout,
                    stderr
            );
        }

        String service = script.getService();
        service = service.replace("%dir%", dir);
        service = service.replace("%id%", id);
        service = service.replace("%mqtturl%", mqtt);
        String pins = "";
        for (Pin p : pinset) {
            pins += p.getArg() + ",";            
        }
        service = service.replace("%pinset%", pins);

        System.out.println(service);

        shell.exec(
                "sudo bash -c  \"cat > /etc/init/" + servicename + ".conf\"",
                new ByteArrayInputStream(service.getBytes()),
                stdout,
                stderr
        );

        shell.exec(
                "sudo initctl reload-configuration",
                new ByteArrayInputStream("".getBytes()),
                stdout,
                stderr
        );

        shell.exec(
                "sudo service " + servicename + " start",
                new ByteArrayInputStream("".getBytes()),
                stdout,
                stderr
        );

        System.out.println(stdout);
        System.out.println(stderr);
    }

}
