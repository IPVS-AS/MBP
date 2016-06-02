/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mqttpysensor.deviceaddressmonitor.arping;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

/**
 *
 * @author rafae
 */
public class Arping {

    private String iprange;
    private Boolean usingStub;

    private static final String CURRENT_PATH = "./src/org/mqttpysensor"
            + "/deviceaddressmonitor/arping/";
    private static final String ARPING_PATH = CURRENT_PATH 
            + "arping.py";
    private static final String ARPINGSTUB_PATH = CURRENT_PATH 
            + "arpingstub.py";
    private static final int IP_ADDR_POS = 0;
    private static final int MAC_ADDR_POS = 1;

    public Arping(String iprange, Boolean useStub) throws IOException {
        this.iprange = iprange;
        this.usingStub = useStub;
        PythonInterpreter.initialize(System.getProperties(),
                System.getProperties(), new String[0]);
        System.out.println(new File(".").getCanonicalPath());
    }

    public Arping(String iprange) throws IOException {        
        this(iprange, false);
    }

    public Map getValues() {
        PythonInterpreter interpreter = new PythonInterpreter();
        if (this.usingStub) {
            interpreter.execfile(Arping.ARPINGSTUB_PATH);
        } else {
            interpreter.execfile(Arping.ARPING_PATH);
        }
        PyObject arping = interpreter.get("arping");
        PyObject result = arping.__call__(new PyString(this.iprange));
        return this.pyListToJavaMap((PyList) result);
    }

    private Map pyListToJavaMap(PyList result) {
        Map<String, String> m = new HashMap<>();
        for (Object o : result) {
            if (o instanceof PyList) {
                List tuple = (PyList) o;
                Object mac = tuple.get(MAC_ADDR_POS);
                Object ip = tuple.get(IP_ADDR_POS);
                if (mac instanceof PyString) {
                    mac = ((PyString) mac).getString();
                }
                if (ip instanceof PyString) {
                    ip = ((PyString) mac).getString();
                }
                m.put((String) mac, (String) ip);
            }
        }
        return m;
    }
}
