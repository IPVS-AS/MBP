package org.citopt.websensor.web.converter;

import org.citopt.websensor.domain.Device;
import org.citopt.websensor.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DeviceConverter implements Converter<String, Device> {

    private static DeviceRepository deviceRepository;

    @Autowired
    public void setLocationRepository(DeviceRepository locationRepository) {
        System.out.println("autowiring deviceRepository to deviceConverter");
        DeviceConverter.deviceRepository = locationRepository;
    }
    
    @Override
    public Device convert(String id) {
        try {
            Device device = deviceRepository.findOne(id);
            return device;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
