package de.ipvs.as.mbp.domain;

import de.ipvs.as.mbp.DynamicBeanProvider;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Offers a static method for retrieving domain entities from a repository by their ID in a efficient manner. However,
 * this method should be used internally only, since no security or user checks are performed at all.
 */
public class SimpleEntityResolver {
    //Cache for repository beans
    private static final Map<Class<?>, MongoRepository<Object, String>> repositoryMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static Optional<Object> resolve(Class<?> repositoryClass, String entityId) {
        //Remember matching repository
        MongoRepository<Object, String> repository;

        //Check if repository is already known
        if (repositoryMap.containsKey(repositoryClass)) {
            //Retrieve repository from map
            repository = repositoryMap.get(repositoryClass);
        } else {
            //Get repository from bean provider
            repository = (MongoRepository<Object, String>) DynamicBeanProvider.get(repositoryClass);

            //Store repository in map for later usage
            repositoryMap.put(repositoryClass, repository);
        }

        //Retrieve desired entity from repository
        return repository.findById(entityId);
    }
}
