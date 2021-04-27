package de.ipvs.as.mbp.domain.settings;

/**
 * Model class holding information about the running MBP app instance and the environment in which it is operated.
 */
public class MBPInfo {
    private String version = "";
    private String commitID = "";
    private String commitTime = "";
    private String buildTime = "";
    private String branch = "";
    private BrokerLocation brokerLocation = BrokerLocation.LOCAL;
    private boolean demoMode = false;

    public MBPInfo() {

    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCommitID() {
        return commitID;
    }

    public void setCommitID(String commitID) {
        this.commitID = commitID;
    }

    public String getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(String commitTime) {
        this.commitTime = commitTime;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public BrokerLocation getBrokerLocation() {
        return brokerLocation;
    }

    public void setBrokerLocation(BrokerLocation brokerLocation) {
        this.brokerLocation = brokerLocation;
    }

    public boolean isDemoMode() {
        return demoMode;
    }

    public void setDemoMode(boolean demoMode) {
        this.demoMode = demoMode;
    }
}
