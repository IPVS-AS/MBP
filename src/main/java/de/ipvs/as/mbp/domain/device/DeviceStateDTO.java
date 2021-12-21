package de.ipvs.as.mbp.domain.device;

public class DeviceStateDTO {
    private final String content;

    public DeviceStateDTO(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
