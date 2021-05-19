package de.ipvs.as.mbp.repository;

import de.ipvs.as.mbp.domain.settings.Settings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for saving and loading application-wide settings.
 */
@Repository
public interface SettingsRepository extends MongoRepository<Settings, String> {

}