package org.citopt.connde.service.gamification;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.citopt.connde.constants.GamificationConstants;
import org.citopt.connde.domain.gamification.GamificationUser;
import org.citopt.connde.domain.user.Authority;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.GamificationUserRepository;
import org.citopt.connde.repository.UserRepository;
import org.citopt.connde.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import org.springframework.stereotype.Service;

/**
 * Service class for managing users.
 */
@Service
public class GamificationUserService {

    @Autowired
    private GamificationUserRepository gamificationUserRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;

    public GamificationUser createGamificationUser(GamificationUser GFUser) {
    	GamificationUser newGFUser = new GamificationUser();
    	newGFUser.setUser(GFUser.getUser());
    	newGFUser.setPoints(GFUser.getPoints());
  	  	newGFUser.setProgress(GFUser.getProgress());
  	    gamificationUserRepository.save(newGFUser);
  	    return newGFUser;
    }
    
    public GamificationUser createGamificationUser(User user, Integer points, String progress) {
    	GamificationUser newGFUser = new GamificationUser();
    	newGFUser.setUser(user);
    	newGFUser.setPoints(points);
  	  	newGFUser.setProgress(progress);
  	    gamificationUserRepository.save(newGFUser);
  	    return newGFUser;
    }
    
    public Optional<GamificationUser> getGamificationUserOfCurrentLoggedUser() {
    	//Get current user
    	User user = userService.getUserWithAuthorities();
    	Optional<GamificationUser> optionalGFUser = gamificationUserRepository.findByUserid(user.getId());
    	if (!optionalGFUser.isPresent()) {
    		GamificationUser newGFU = this.createGamificationUser(user, 0, GamificationConstants.InitialProgress);
    		optionalGFUser = Optional.of(newGFU);
    	}
    	
    	return optionalGFUser;
    }
    
    public void updateGamificationUser(String id, Integer points, String progress) {
        Optional.of(gamificationUserRepository
                .findOne(id))
                .ifPresent(GFuser -> {
                	GFuser.setPoints(points);
                	GFuser.setProgress(progress);
                	gamificationUserRepository.save(GFuser);
                });
    }

    public void deleteGamificationUser(String id) {
    	Optional.of(gamificationUserRepository
                .findOne(id))
                .ifPresent(GFuser -> {
                	gamificationUserRepository.delete(GFuser);
                });
    }
}