package de.ipvs.as.mbp.util;

import java.io.ByteArrayOutputStream;

public class CommandOutput {
    private final ByteArrayOutputStream stderr;
    private final ByteArrayOutputStream stdout;
    private int exitCode;

    public CommandOutput() {
        this.stderr = new ByteArrayOutputStream();
        this.stdout = new ByteArrayOutputStream();
    }

    public ByteArrayOutputStream getStderrStream() {
        return stderr;
    }

    public ByteArrayOutputStream getStdoutStream() {
        return stdout;
    }

    public String getStderr() {
        return stderr.toString();
    }

    public String getStdout() {
        return stdout.toString();
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }
}
