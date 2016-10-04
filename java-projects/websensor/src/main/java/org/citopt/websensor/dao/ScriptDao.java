package org.citopt.websensor.dao;

import java.util.List;
import org.bson.types.ObjectId;
import org.citopt.websensor.domain.Script;
import org.citopt.websensor.repository.ScriptRepository;
import org.citopt.websensor.web.exception.IdNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

public class ScriptDao {
    
    @Autowired
    private ScriptRepository repository;
    
    public Script find(ObjectId id) throws IdNotFoundException {
        Script result = repository.findOne(id.toString());
        if(result == null) {
            throw new IdNotFoundException();
        }
        return result;
    }

    public List<Script> findAll() {
        return repository.findAll();
    }

    public Script insert(Script script) {
        return repository.insert(script);
    }

    public Script save(Script script) {
        return repository.save(script);
    }

    public void delete(ObjectId id) {
        repository.delete(id.toString());
    }
}
