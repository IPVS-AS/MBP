package org.citopt.connde.service.deploy;

import com.jcabi.ssh.SSH;
import com.jcabi.ssh.Shell;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Objects of this class wrap SSH connection parameters and represent SSH sessions that can be used in order
 * to execute predefined shell commands on the remote device.
 *
 * @author Jan
 */
public class SSHSession {
    //Default SSH port to use
    private static final int DEFAULT_PORT = 22;

    //Definitions of shell commands
    private static final String SHELL_COMMAND_TEST = "test 5 -gt 2 && echo \"true\" || echo \"false\"";
    private static final String SHELL_CREATE_DIR = "mkdir -p %s";
    private static final String SHELL_REMOVE_DIR = "rm -rf %s";
    private static final String SHELL_CREATE_FILE = "bash -c \"cat > %s/%s\"";
    private static final String SHELL_CREATE_FILE_BASE64 = "bash -c \"base64 -d > %s/%s\"";
    private static final String SHELL_CHANGE_FILE_PERMISSIONS = "chmod %s %s";
    private static final String SHELL_EXECUTE_SHELL_SCRIPT = "bash %s%s";

    private static final String SHELL_PREFIX_SUDO_PASSWORD = "echo \"%s\" | sudo -S ";

    //Session parameters
    private String url;
    private int port;
    private String username;
    private String password;
    private String key;

    //Internal objects to maintain and use the ssh connection
    private Shell shell;
    private OutputStream stdOutStream;
    private OutputStream stdErrStream;

    /**
     * Creates a new SSH session object on the default port that wraps the corresponding connection parameters.
     *
     * @param url      The URL to connect to via SSH
     * @param username The username to use on the target device
     * @param password The password which is required for executing sudo commands
     * @param key      The private SSH key to use
     */
    public SSHSession(String url, String username, String password, String key) {
        this(url, DEFAULT_PORT, username, password, key);
    }

    /**
     * Creates a new ssh session object that wraps the corresponding connection parameters.
     *
     * @param url      The URL to connect to via SSH
     * @param port     The port to use (typically 22)
     * @param username The username to use on the target device
     * @param password The password which is required for executing sudo commands
     * @param key      The private SSH key to use
     */
    public SSHSession(String url, int port, String username, String password, String key) {
        this.url = url;
        this.port = port;
        this.username = username;
        this.password = password;
        this.key = key;
    }

    /**
     * Checks if it is possible to run basic commands by using the SSH session.
     *
     * @return True, if commands can be executed successfully; false otherwise
     */
    public boolean isCommandExecutable() {
        checkConnectionState();

        //Create input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());

        //Execute command
        try {
            executeShellCommand(SHELL_COMMAND_TEST, inputStream);
        } catch (IOException e) {
            return false;
        }

        //Retrieve return value and check if it is correct
        String returnValue = stdOutStream.toString().toLowerCase();
        return returnValue.contains("true");
    }

    /**
     * Executes a shell script that is located on the remote device with command line parameters.
     *
     * @param filePath   The path to the script to execute
     * @param parameters Command line parameters to pass
     * @throws IOException In case of an I/O issue
     */
    public void executeShellScript(String filePath, String... parameters) throws IOException {
        checkConnectionState();

        //Build string that contains all parameters separated by whitespaces
        StringBuilder parametersString = new StringBuilder();

        for (String parameter : parameters) {
            parametersString.append(" ");
            parametersString.append(parameter);
        }

        //Build corresponding command
        String command = String.format(SHELL_EXECUTE_SHELL_SCRIPT, filePath, parametersString.toString());

        //Create input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());

        //Execute command
        executeShellCommand(command, inputStream);
    }

    /**
     * Changes the permissions of a file on the remote device.
     *
     * @param filePath    The path to the file to change
     * @param permissions The permissions to set (e.g. +x)
     * @throws IOException In case of an I/O issue
     */
    public void changeFilePermissions(String filePath, String permissions) throws IOException {
        checkConnectionState();

        //Build corresponding command
        String command = String.format(SHELL_CHANGE_FILE_PERMISSIONS, permissions, filePath);

        //Create input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());

        //Execute command
        executeShellCommand(command, inputStream);
    }

    /**
     * Creates a file on the remote device from a base64 encoded string.
     *
     * @param dirPath     The path to the directory in which the file is supposed to be created
     * @param fileName    The name of the file to create
     * @param fileContent The file content as base64 encoded string
     * @throws IOException In case of an I/O issue
     */
    public void createFileFromBase64(String dirPath, String fileName, String fileContent) throws IOException {
        checkConnectionState();

        //Build corresponding command
        String command = String.format(SHELL_CREATE_FILE_BASE64, dirPath, fileName);

        //Create input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());

        //Execute command
        executeShellCommand(command, inputStream);
    }

    /**
     * Creates a file on the remote device from a plain content string.
     *
     * @param dirPath     The path to the directory in which the file is supposed to be created
     * @param fileName    The name of the file to create
     * @param fileContent The file content as plain string
     * @throws IOException In case of an I/O issue
     */
    public void createFile(String dirPath, String fileName, String fileContent) throws IOException {
        checkConnectionState();

        //Build corresponding command
        String command = String.format(SHELL_CREATE_FILE, dirPath, fileName);

        //Create input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());

        //Execute command
        executeShellCommand(command, inputStream);
    }

    /**
     * Removes a directory and its contents on the remote device.
     *
     * @param path The path to the directory to remove
     * @throws IOException In case of an I/O issue
     */
    public void removeDir(String path) throws IOException {
        checkConnectionState();

        //Build corresponding command
        String command = String.format(SHELL_REMOVE_DIR, path);

        //Create input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());

        //Execute command
        executeShellCommand(command, inputStream);
    }

    /**
     * Creates a directory on the remote device.
     *
     * @param path The path to the directory in which the directory is supposed to be created
     * @throws IOException In case of an I/O issue
     */
    public void createDir(String path) throws IOException {
        checkConnectionState();

        //Build corresponding command
        String command = String.format(SHELL_CREATE_DIR, path);

        //Create input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());

        //Execute command
        executeShellCommand(command, inputStream);
    }

    /**
     * Establishes the SSH connection with the parameters that were set previously.
     *
     * @throws IOException In case of an I/O issue
     */
    public void connect() throws IOException {
        //Create new safe shell instance
        shell = new Shell.Safe(new SSH(url, port, username, key));

        //Create corresponding streams for further usage
        stdOutStream = new ByteArrayOutputStream();
        stdErrStream = new ByteArrayOutputStream();
    }

    /**
     * Closes the SSH connection.
     *
     * @throws IOException In case of an I/O issue
     */
    public void close() throws IOException {
        checkConnectionState();
        stdOutStream.close();
        stdErrStream.close();
        shell = null;
    }

    /**
     * Returns the url to connect to via SSH.
     *
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the url to connect to via SSH.
     *
     * @param url The url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the port to use for the SSH connection.
     *
     * @return The port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port to use for the SSH connection.
     *
     * @param port The port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns the username to use on the target device.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username to use on the target device.
     *
     * @param username The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the private SSH key to use for the SSH connection.
     *
     * @return The key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the private SSH key to use for the SSH connection.
     *
     * @param key The key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the standard output stream of the session.
     *
     * @return The output stream
     */
    public OutputStream getStdOutStream() {
        return stdOutStream;
    }

    /**
     * Returns the standard error stream of the session.
     *
     * @return The error stream
     */
    public OutputStream getStdErrStream() {
        return stdErrStream;
    }

    private int executeShellCommand(String command, ByteArrayInputStream inputStream) throws IOException {
        command = addCommandPrefix(command);
        return shell.exec(command, inputStream, stdOutStream, stdErrStream);
    }

    private String addCommandPrefix(String command) {
        //Check if password is available
        if ((password == null) || password.isEmpty()) {
            return "sudo " + command;
        }

        return String.format(SHELL_PREFIX_SUDO_PASSWORD, password) + command;
    }

    /**
     * Checks whether the SSH connection is already established and throws an exception if this is not the case.
     */
    private void checkConnectionState() {
        if (shell == null) {
            throw new IllegalStateException("No connection has been established yet.");
        }
    }
}
