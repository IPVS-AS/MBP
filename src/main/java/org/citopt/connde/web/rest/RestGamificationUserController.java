package org.citopt.connde.web.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.gamification.GamificationUser;
import org.citopt.connde.repository.GamificationUserRepository;
import org.citopt.connde.service.gamification.GamificationUserService;
import org.citopt.connde.web.rest.util.HeaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Controller for managing gamification users.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Gamification User entities"}, description = "Gamification User management and CRUD for these entities")
public class RestGamificationUserController {

    @Autowired
    private GamificationUserRepository gamificationUserRepository;

    @Autowired
    private GamificationUserService gamificationUserService;

    /**
     * POST /gamificationUser : Creates a new gamification user.
     * <p>
     * Creates a new GF user if not already created for the username.
     *
     * @param user The gamification user to create
     * @return the ResponseEntity with status 201 (Created) and with body the
     * new gamification user, or with status 400 (Bad Request) if the username
     * already has a gamification user object
     * @throws URISyntaxException If the Location URI syntax is incorrect
     */
    @PostMapping("/gamificationUser")
    @ApiOperation(value = "Creates a new gamification user entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "Username already has a gamification user")})
    public ResponseEntity<?> createGamificationUser(@Valid @RequestBody @ApiParam(value = "The gamification user to create", required = true) GamificationUser gamificationUser) throws URISyntaxException {
        if (gamificationUserRepository.findByUserid(gamificationUser.getUser().getId()).isPresent()) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("User already has Gamification User", gamificationUser.getUser().getUsername())).body(null);
        } else {
            GamificationUser newGFUser = gamificationUserService.createGamificationUser(gamificationUser);
            return ResponseEntity.created(new URI("/api/gamificationUsers/" + newGFUser.getUser().getUsername()))
                    .headers(HeaderUtil.createAlert("Gamification User registered successfully", newGFUser.getUser().getUsername()))
                    .body(newGFUser);
        }
    }
    
    /**
     * GET /gamificationUser : get the gamification user object of current logged user.
     *
     * @return the ResponseEntity with status 200 (OK) and with body the
     * gamification user, or with status 404 (Not Found)
     */
    @GetMapping("/gamificationUser")
    @ApiOperation(value = "Returns a gamification user entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 404, message = "Gamification user not found")})
    public ResponseEntity<GamificationUser> getGamificationUserByUsername() {
        return gamificationUserService.getGamificationUserOfCurrentLoggedUser()
                .map(gamificationUser -> new ResponseEntity<>(gamificationUser, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * PUT /gamificationUser : Updates an existing Gamification User.
     *
     * @param GFUser The gamification user to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated
     * gamification user, or with status 400 (Bad Request) - RODRIGO: do we need this?
     * or with status 500 (Internal Server Error) if the gamification user
     * couldn't be updated
     */
    @PutMapping("/gamificationUser")
    @ApiOperation(value = "Updates an existing gamification user entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "ERROR?"), @ApiResponse(code = 500, message = "Gamification User could not be updated")})
    public ResponseEntity<GamificationUser> updateGamificationUser(@RequestBody @ApiParam(value = "The gamification user to update", required = true) GamificationUser GFuser) {
        /*Optional<GamificationUser> existingGFUser = userRepository.findOneByUsername(user.getUsername().toLowerCase());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(user.getId()))) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("Username already in use", user.getUsername())).body(null);
        }*/
    	gamificationUserService.updateGamificationUser(GFuser.getId(), GFuser.getPoints(), GFuser.getProgress());

        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Gamification User updated successfully", GFuser.getUser().getUsername()))
                .body(GFuser);
    }
    
    /**
     * GET /gamificationQuestDB : get the gamification quest database objects.
     *
     * @return the quest DB with status 200 (OK) and with body the
     * objects in JSON, or with status 404 (Not Found)
     */
    @GetMapping("/gamificationQuestDB")
    @ApiOperation(value = "Returns the gamification quest database", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 404, message = "Gamification quest DB not found")})
    public ResponseEntity<String> getGamificationQuestDB() {
    	StringBuilder fileContent = new StringBuilder();
        try {
        	BufferedReader bufferedReader = null;
            bufferedReader = new BufferedReader(new FileReader(new File("C:\\Gamification\\questDB.json")));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                fileContent.append(line);
            }
        } catch (IOException e) {
        	return new ResponseEntity<String>("Quest DB not found", HttpStatus.NOT_FOUND);
        }
    	        
        return new ResponseEntity<String>(fileContent.toString(), HttpStatus.OK);
    }
}
