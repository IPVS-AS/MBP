package org.citopt.connde.service.deploy;

import com.jcabi.ssh.SSH;
import com.jcabi.ssh.Shell;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;

/**
 * Objects of this class wrap SSH connection parameters and represent SSH sessions that can be used in order
 * to execute predefined shell commands on the remote machine.
 *
 * Created by Jan on 03.12.2018.
 */
public class SSHSession {
    //Default SSH port to use
    private static final int DEFAULT_PORT = 22;

    //Definitions of shell commands
    private static final String SHELL_CREATE_DIR = "sudo mkdir -p %s";
    private static final String SHELL_REMOVE_DIR = "sudo rm -rf %s";
    private static final String SHELL_CREATE_FILE = "sudo bash -c \"cat > %s/%s\"";
    private static final String SHELL_CREATE_FILE_BASE64 = "sudo bash -c \"base64 -d > %s/%s\"";
    private static final String SHELL_CHANGE_FILE_PERMISSIONS = "sudo chmod %s %s";
    private static final String SHELL_EXECUTE_SHELL_SCRIPT = "sudo bash %s %s";

    //Session parameters
    private String url;
    private int port;
    private String username;
    private String key;

    //Internal objects to maintain and use the ssh connection
    private Shell shell;
    private OutputStream stdout;
    private OutputStream stderr;

    /**
     * Creates a new SSH session object on the default port that wraps the corresponding connection parameters.
     * @param url The URL to connect to via SSH
     * @param username The user name to use on the target machine
     * @param key The private SSH key to use
     */
    public SSHSession(String url, String username, String key) {
        this(url, DEFAULT_PORT, username, key);
    }

    /**
     * Creates a new ssh session object that wraps the corresponding connection parameters.
     * @param url The URL to connect to via SSH
     * @param port The port to use (typically 22)
     * @param username The user name to use on the target machine
     * @param key The private SSH key to use
     */
    public SSHSession(String url, int port, String username, String key) {
        this.url = url;
        this.port = port;
        this.username = username;
        this.key = key;
    }

    public void executeShellScript(String filePath, String parametersString) throws IOException {
        checkConnectionState();

        //Build corresponding command
        String command = String.format(SHELL_EXECUTE_SHELL_SCRIPT, filePath, parametersString);

        //Create input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());

        //Execute command
        shell.exec(command, inputStream, stdout, stderr);
    }

    public void changeFilePermissions(String filePath, String permissions) throws IOException {
        checkConnectionState();

        //Build corresponding command
        String command = String.format(SHELL_CHANGE_FILE_PERMISSIONS, permissions, filePath);

        //Create input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());

        //Execute command
        shell.exec(command, inputStream, stdout, stderr);
    }

    public void createFileFromBase64(String dir, String fileName, String fileContent) throws IOException {
        checkConnectionState();

        //Build corresponding command
        String command = String.format(SHELL_CREATE_FILE_BASE64, dir, fileName);

        //Create input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());

        //Execute command
        shell.exec(command, inputStream, stdout, stderr);
    }

    public void createFile(String dir, String fileName, String fileContent) throws IOException {
        checkConnectionState();

        //Build corresponding command
        String command = String.format(SHELL_CREATE_FILE, dir, fileName);

        //Create input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());

        //Execute command
        shell.exec(command, inputStream, stdout, stderr);
    }

    public void removeDir(String path) throws IOException {
        checkConnectionState();

        //Build corresponding command
        String command = String.format(SHELL_REMOVE_DIR, path);

        //Create input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());

        //Execute command
        shell.exec(command, inputStream, stdout, stderr);
    }

    public void createDir(String path) throws IOException {
        checkConnectionState();

        //Build corresponding command
        String command = String.format(SHELL_CREATE_DIR, path);

        //Create input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());

        //Execute command
        shell.exec(command, inputStream, stdout, stderr);
    }

    public void connect() throws UnknownHostException {
        shell = new Shell.Safe(new SSH(url, port, username, key));
        stdout = new ByteArrayOutputStream();
        stderr = new ByteArrayOutputStream();
    }

    public void close() throws IOException {
        checkConnectionState();
        stdout.close();
        stderr.close();
        shell = null;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private void checkConnectionState(){
        if(shell == null){
            throw new IllegalStateException("No connection has been established yet.");
        }
    }
}
