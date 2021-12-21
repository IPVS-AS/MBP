package de.ipvs.as.mbp.service.deployment.ssh;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import com.jcabi.ssh.SshByPassword;

/**
 * Objects of this class wrap SSH connection parameters and represent SSH sessions that can be used in order
 * to execute predefined shell commands on the remote device.
 */
public class SSHSession {
    //Default SSH port to use
    public static final int DEFAULT_PORT = 22;

    //Definitions of shell commands
    private static final String SHELL_TEST_AVAILABILITY = "test 5 -gt 2 && echo \"true\" || echo \"false\"";
    private static final String SHELL_TEST_SUDO_PW_REQUIRED = "sudo -n echo \"success\"";
    private static final String SHELL_CREATE_DIR = "mkdir -p %s";
    private static final String SHELL_REMOVE_DIR = "rm -rf %s";
    private static final String SHELL_CREATE_FILE = "bash -c \"cat > %s/%s\"";
    private static final String SHELL_CREATE_FILE_BASE64 = "bash -c \"base64 -d > %s/%s\"";
    private static final String SHELL_CHANGE_FILE_PERMISSIONS = "chmod %s %s";
    private static final String SHELL_EXECUTE_SHELL_SCRIPT = "bash %s%s";
    private static final String SHELL_TEST_DIR_EXISTS = "[ -d \"%s\" ] && echo true || echo false";
    private static final String SHELL_GENERATE_HASH = "md5sum %s | grep '^[^[:space:]]*' -o";

    private static final String SHELL_PREFIX_SUDO_PASSWORD = "sudo -S ";

    //Session parameters
    private String url;
    private int port;
    private String username;
    private final String password;
    private String key;

    //Remembers whether a sudo password is required
    private boolean passwordRequired = false;

    //Internal objects to maintain and use the ssh connection
    private Shell shell;
    private ByteArrayOutputStream stdOutStream;
    private ByteArrayOutputStream stdErrStream;

    /**
     * Creates a new SSH session object on the default port that wraps the corresponding connection parameters.
     *
     * @param url      The URL to connect to via SSH
     * @param username The username to use on the target device
     * @param password The password which is required for executing sudo commands
     * @param key      The private SSH key to use
     */
    protected SSHSession(String url, String username, String password, String key) {
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
    protected SSHSession(String url, int port, String username, String password, String key) {
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
    public synchronized boolean isCommandExecutable() {
        checkConnectionState();

        //Reset output stream of session
        resetStdOutStream();

        //Execute command
        try {
            executeShellCommand(SHELL_TEST_AVAILABILITY);
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
    public synchronized void executeShellScript(String filePath, String... parameters) throws IOException {
        checkConnectionState();

        //Build string that contains all parameters separated by whitespaces
        StringBuilder parametersString = new StringBuilder();

        for (String parameter : parameters) {
            parametersString.append(" ");
            parametersString.append(parameter);
        }

        //Build corresponding command
        String command = String.format(SHELL_EXECUTE_SHELL_SCRIPT, filePath, parametersString.toString());

        //Execute command
        executeShellCommand(command);
    }

    /**
     * Changes the permissions of a file on the remote device.
     *
     * @param filePath    The path to the file to change
     * @param permissions The permissions to set (e.g. +x)
     * @throws IOException In case of an I/O issue
     */
    public synchronized void changeFilePermissions(String filePath, String permissions) throws IOException {
        checkConnectionState();

        //Build corresponding command
        String command = String.format(SHELL_CHANGE_FILE_PERMISSIONS, permissions, filePath);

        //Execute command
        executeShellCommand(command);
    }

    /**
     * Creates a file on the remote device from a base64 encoded string.
     *
     * @param dirPath     The path to the directory in which the file is supposed to be created
     * @param fileName    The name of the file to create
     * @param fileContent The file content as base64 encoded string
     * @throws IOException In case of an I/O issue
     */
    public synchronized void createFileFromBase64(String dirPath, String fileName, String fileContent) throws IOException {
        checkConnectionState();

        //Build corresponding command
        String command = String.format(SHELL_CREATE_FILE_BASE64, dirPath, fileName);

        //Execute command
        executeShellCommand(command, fileContent);
    }

    /**
     * Creates a file on the remote device from a plain content string.
     *
     * @param dirPath     The path to the directory in which the file is supposed to be created
     * @param fileName    The name of the file to create
     * @param fileContent The file content as plain string
     * @throws IOException In case of an I/O issue
     */
    public synchronized void createFile(String dirPath, String fileName, String fileContent) throws IOException {
        checkConnectionState();

        //Build corresponding command
        String command = String.format(SHELL_CREATE_FILE, dirPath, fileName);

        //Execute command
        executeShellCommand(command, fileContent);
    }

    /**
     * Removes a directory and its contents on the remote device.
     *
     * @param path The path to the directory to remove
     * @throws IOException In case of an I/O issue
     */
    public synchronized void removeDir(String path) throws IOException {
        checkConnectionState();

        //Build corresponding command
        String command = String.format(SHELL_REMOVE_DIR, path);

        //Execute command
        executeShellCommand(command);
    }

    /**
     * Creates a directory on the remote device.
     *
     * @param path The path to the directory in which the directory is supposed to be created
     * @throws IOException In case of an I/O issue
     */
    public synchronized void createDir(String path) throws IOException {
        checkConnectionState();

        //Build corresponding command
        String command = String.format(SHELL_CREATE_DIR, path);

        //Execute command
        executeShellCommand(command);
    }

    /**
     * Checks whether a directory at a given path exists.
     *
     * @param path The path of the directory to check
     * @return True, if the directory exists; false otherwise
     * @throws IOException In case of an I/O issue
     */
    public synchronized boolean dirExists(String path) throws IOException {
        checkConnectionState();

        //Remove trailing slashes from path
        path = path.replaceAll("/$", "");

        //Build corresponding command
        String command = String.format(SHELL_TEST_DIR_EXISTS, path);

        //Reset output stream of session
        resetStdOutStream();

        //Execute command
        executeShellCommand(command);

        //Retrieve return value and check its value
        String returnValue = stdOutStream.toString().toLowerCase();
        return returnValue.contains("true");
    }

    /**
     * Returns the MD5 hash of a certain file on the remote device.
     *
     * @param filePath The the to hash
     * @return The MD5 hash of the file
     * @throws IOException In case of an I/O issue
     */
    public String generateHashOfFile(String filePath) throws IOException {
        checkConnectionState();

        //Build corresponding command
        String command = String.format(SHELL_GENERATE_HASH, filePath);

        //Reset output stream of session
        resetStdOutStream();

        //Execute command
        executeShellCommand(command);

        //Retrieve the resulting hash from the stream
        return stdOutStream.toString().trim().toLowerCase();
    }

    /**
     * Rests the stdout stream of this SSH session.
     */
    public synchronized void resetStdOutStream() {
        this.stdOutStream.reset();
    }

    /**
     * Rests the stderr stream of this SSH session.
     */
    public synchronized void resetStdErrStream() {
        this.stdErrStream.reset();
    }

    /**
     * Establishes the SSH connection with the parameters that were set previously.
     *
     * @throws IOException In case of an I/O issue
     */
    protected synchronized void connect() throws IOException {
        //Create new safe shell instance
        if (key != null){
          shell = new Shell.Safe(new Ssh(url, port, username, key));
        }
        else{
          shell = new SshByPassword(url, port, username, password);
        }
        //Create corresponding streams for further usage
        stdOutStream = new ByteArrayOutputStream();
        stdErrStream = new ByteArrayOutputStream();

        //Remember whether a password is required
        passwordRequired = isSudoPasswordRequired();
    }

    /**
     * Executes a shell command with sudo permissions via the currently active SSH session. The password that
     * was provided to this session will be used in order to execute sudo. However, if no password is available,
     * it will try to execute sudo without password.
     *
     * @param command The shell command to execute via SSH
     * @return The integer return value of the
     * @throws IOException In case of an I/O issue
     */
    private synchronized int executeShellCommand(String command) throws IOException {
        //Wrap and delegate
        return executeShellCommand(command, null);
    }

    /**
     * Executes a shell command with sudo permissions via the currently active SSH session. The password that
     * was provided to this session will be used in order to execute sudo. However, if no password is available,
     * it will try to execute sudo without password. It is possible to provide a string that should be
     * injected into the input stream of the target process and may be required for the execution of the command.
     *
     * @param command           The shell command to execute via SSH
     * @param inputStreamString The sting to inject into the input stream
     * @return The integer return value of the
     * @throws IOException In case of an I/O issue
     */
    private synchronized int executeShellCommand(String command, String inputStreamString) throws IOException {
        checkConnectionState();

        //Ensure valid input stream string
        if (inputStreamString == null) {
            inputStreamString = "";
        }

        //Check if password is available and required
        if ((password == null) || password.isEmpty() || !(passwordRequired)) {
            //No password, try to use sudo without one
            command = "sudo " + command;
        } else {
            //Extend command for sudo with password and provide password via input stream
            command = SHELL_PREFIX_SUDO_PASSWORD + command;
            inputStreamString = password + "\n" + inputStreamString;
        }

        //Create corresponding input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream(inputStreamString.getBytes());

        //Execute shell command remotely
        return shell.exec(command, inputStream, stdOutStream, stdErrStream);
    }

    /**
     * Checks whether a sudo password is actually required in order to run sudo commands within the current session.
     *
     * @return True, if a sudo password is required; false otherwise
     * @throws IOException In case of an I/O issue
     */
    private synchronized boolean isSudoPasswordRequired() throws IOException {
        checkConnectionState();

        //Try to execute the corresponding test command without password and check if it fails
        try {
            shell.exec(SHELL_TEST_SUDO_PW_REQUIRED, new ByteArrayInputStream("".getBytes()), stdOutStream, stdErrStream);
        } catch (IllegalArgumentException e) {
            //Execution failed, sudo password needed
            return true;
        }

        //Flush output stream
        stdOutStream.flush();

        //Execution did not fail, no password needed
        return false;
    }

    /**
     * Checks whether the SSH connection is already established and throws an exception if this is not the case.
     */
    private void checkConnectionState() {
        if (shell == null) {
            throw new IllegalStateException("No connection has been established yet.");
        }
    }

    /**
     * Returns whether the SSH session is currently active.
     *
     * @return True, if the session is active; false otherwise
     */
    public boolean isActive() {
        return shell != null;
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
    protected void setUrl(String url) {
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
    protected void setPort(int port) {
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
    protected void setUsername(String username) {
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
    protected void setKey(String key) {
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
}
