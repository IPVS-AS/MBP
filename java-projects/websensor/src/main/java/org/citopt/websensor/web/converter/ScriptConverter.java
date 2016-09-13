package org.citopt.websensor.web.converter;

import org.citopt.websensor.domain.Script;
import org.citopt.websensor.repository.ScriptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ScriptConverter implements Converter<String, Script> {

    private static ScriptRepository scriptRepository;

    @Autowired
    public void setLocationRepository(ScriptRepository scriptRepository) {
        System.out.println("autowiring scriptRepository to scriptConverter");
        ScriptConverter.scriptRepository = scriptRepository;
    }
    
    @Override
    public Script convert(String id) {
        try {
            Script script = scriptRepository.findOne(id);
            return script;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
