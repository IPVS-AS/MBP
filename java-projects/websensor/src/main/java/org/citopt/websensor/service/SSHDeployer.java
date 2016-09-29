package org.citopt.websensor.service;

import com.jcabi.ssh.SSH;
import com.jcabi.ssh.Shell;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import org.citopt.websensor.domain.Script;
import org.springframework.stereotype.Component;

@Component
public class SSHDeployer {

    private static final String SCRIPTDIR = "/home/pi/scripts/";

    public static String key
            = "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIIEogIBAAKCAQEAyGALfW0RP//eXFfhKfVcQK8rCCxymWBduf0rmMmDApN50Kzv\n"
            + "ESS955Y8HWvTPGDwd0ny6rthWcbDRF2+2J2AsKa+UnrXamZ3PdOfIPmuCFSigiQd\n"
            + "fnjFk8Zg8sdtywBCBy2SHwq7QBsZME2Aztyx3L4k4lk2VK8w+2F9gCmAVxY+KLDN\n"
            + "Da5NsgVEe9xVvvzhwkmf86T6r4dhYmWPgzW30GkUh4vvBvozBbfa0YV/vj4f1DP0\n"
            + "U3l91wiUl96Ag0e7r2wsCuufW6Gs8Gy1IE/CpAbyrUxrH+yDoNFur0QP7qDiioRR\n"
            + "X7p+HpCdhl3qKKB6CeflpQOlKpx7Pj87QhL0LQIDAQABAoIBACzWWRva8RY6Ij7V\n"
            + "p1vlPJx41g9BKu+pQa/huAS7auaDq6mHWQOkDh6pXpBS1XTYWFbJJGNkRLd7I6zD\n"
            + "sXX1YJum5EW+mT+E6D/cf+o4FLpmferTPApV6hhUNtN8ztOzHhNPHjh2BUqmBa/q\n"
            + "V91yQxabMdO4lNDEVxiZSyUHpGFYAj4odQVJvGRG2502L0BKyYeMABmtZrKjaS5K\n"
            + "aahbL0Z2pkQ+gakEn+1cb/Rd2IDQhrA6EpacK9reoWydpUxP/MReQdeMU62rwqFe\n"
            + "TpEPc6ZS19XxWKyIhHHLiZl7qNcXkCOK64kEgvlark9miNj3JUf9P0OAmElRAtdM\n"
            + "PXP6Qf0CgYEA8mwnIyJ1atBsqgTySD6X+dTPtHUiSJ8euOtiqQTH8t2MTU06mZuA\n"
            + "8e7Fy45yxKSQ7w6uJA9UJs2Ru2vN6lC6zav0ri4LXhv2VAwJFkQKDv2fSR1lOAbk\n"
            + "/cKnwoWNSqda+lq+Bl7ZiLxSeviYbus+LcgIq3HyBVmcvKpIJRN/tYMCgYEA05kE\n"
            + "2fI5/dnyH1MvLCoSKkYp40uUwatnSDt07WSqa3SH5E/uz4lasFcgeJSmqOYpa3tw\n"
            + "/bqBXNlqWCWI6oNi/23Pv4mj69EFrfSf85IQcms8dGStdcin+9VmYSJpn+QPgia/\n"
            + "n4vm125CQrURmuE2r+oOcV3ShcpO1lS4AMs8MI8CgYBoFi3btRDrMuBlQ8hvYojI\n"
            + "WSpxVhXJTqDXTyHGZmofiiaSjkVJ7O25cwb0No5qhipAqnH0w6wjGQKokUoRgGYk\n"
            + "pt9g5h41YxYp0h0YtVAITbdVokxyeOtbVXfIWqVm12KFue57N8B5KDrV1+VDQrgo\n"
            + "2gl26266A1b73rUpTiz4VwKBgHgUtrQYyuBM9yLfyj1+AqELAGqFUf42j35mf4zZ\n"
            + "O/2PPC9NTXFpuZWpXDwR4CKpu4fLnevgE9nlaHxtkK3FskDSyLsiGWySSm7WDI/l\n"
            + "rH/Ca6SCHg5huTMpf9hP9zFN858g7k5UzsQjRmck6sDCXo6mfVvIqthSXzszCNkq\n"
            + "fRXxAoGARRp2fahKz31kUOVprVSK2UsH340fET43X3QlygyNI33J4V6tYUpTgCY7\n"
            + "dyBUmBHZKeZwJYYAtfkI4ACDCI0KEa6NdzAtwcwUgsR10fh6jGGBrKT88F4C5Xe1\n"
            + "8JinHG8VObUcB1S7+vmct88/ELxa+9CnJ/NbiYyDw0cuAxqWUWg=\n"
            + "-----END RSA PRIVATE KEY-----";

    public static String pubkey
            = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDIYAt9bRE//95cV+Ep9VxArysILH"
            + "KZYF25/SuYyYMCk3nQrO8RJL3nljwda9M8YPB3SfLqu2FZxsNEXb7YnYCwpr5Setdq"
            + "Znc9058g+a4IVKKCJB1+eMWTxmDyx23LAEIHLZIfCrtAGxkwTYDO3LHcviTiWTZUrz"
            + "D7YX2AKYBXFj4osM0Nrk2yBUR73FW+/OHCSZ/zpPqvh2FiZY+DNbfQaRSHi+8G+jMF"
            + "t9rRhX++Ph/UM/RTeX3XCJSX3oCDR7uvbCwK659boazwbLUgT8KkBvKtTGsf7IOg0W"
            + "6vRA/uoOKKhFFfun4ekJ2GXeoooHoJ5+WlA6UqnHs+PztCEvQt pi@raspberrypi";

    public static String getScriptDir(String id) {
        return SCRIPTDIR + id;
    }

    public static String parseService(String service, Map<String, String> values) {
        for (String k : values.keySet()) {
            service = service.replace(k, values.get(k));
        }
        return service;
    }

    public void deployScript(String id, String url, Integer port, String user,
            String key, String mqtt, Script script, String pinset) throws UnknownHostException, IOException {
        String sid = "s" + id;
        String dir = SCRIPTDIR + sid + "/";
        String servicename = sid;

        Shell shell = new Shell.Safe(
                new SSH(
                        url, port,
                        user, key
                )
        );

        OutputStream stdout = new ByteArrayOutputStream();
        OutputStream stderr = new ByteArrayOutputStream();

        // creates routine dir
        shell.exec(
                "mkdir -p " + dir,
                new ByteArrayInputStream("".getBytes()),
                stdout,
                stderr
        );
        System.out.println(stdout);
        System.out.println(stderr);

        String routine = new String(script.getRoutine().getContent());

        // copies routine        
        shell.exec(
                "sudo bash -c  \"cat > " + dir + script.getRoutine().getName() + "\"",
                new ByteArrayInputStream(routine.getBytes()),
                stdout,
                stderr
        );
        System.out.println(stdout);
        System.out.println(stderr);

        String service = new String(script.getService().getContent());
        System.out.println(service);

        Map<String, String> serviceParser = new HashMap<>();
        serviceParser.put("${dir}", dir);
        serviceParser.put("${id}", id);
        serviceParser.put("${mqtturl}", mqtt);
        serviceParser.put("${pinset}", pinset);
        service = parseService(service, serviceParser);
        System.out.println(service);

        // copies service        
        shell.exec(
                "sudo bash -c  \"cat > /etc/init/" + servicename + ".conf\"",
                new ByteArrayInputStream(service.getBytes()),
                stdout,
                stderr
        );
        System.out.println(stdout);
        System.out.println(stderr);

        // reloads service config
        shell.exec(
                "sudo initctl reload-configuration",
                new ByteArrayInputStream("".getBytes()),
                stdout,
                stderr
        );
        System.out.println(stdout);
        System.out.println(stderr);

        try {
            shell.exec(
                    "sudo service " + servicename + " stop",
                    new ByteArrayInputStream("".getBytes()),
                    stdout,
                    stderr
            );
        } catch (Exception e) {
        }

        // starts service
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
