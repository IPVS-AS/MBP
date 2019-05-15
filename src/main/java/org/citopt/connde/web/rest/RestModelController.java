package org.citopt.connde.web.rest;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.model.Model;
import org.citopt.connde.repository.ModelRepository;
import org.citopt.connde.security.SecurityUtils;
import org.citopt.connde.web.rest.util.HeaderUtil;
import org.citopt.connde.web.rest.util.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for modeling tool.
 * @author Imeri Amil
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestModelController {
	
	@Autowired
	private ModelRepository modelRepository;
	
	/**
	 * POST /model : Saves the received model. 
	 * Checks first if current user has already a model with same name.
	 * If yes, the model value is overwritten.
	 * 
	 * @param model
	 *            The model to save
	 * @return the ResponseEntity with status 200 (OK) and the saved model
	 */
	@PostMapping("/model")
	public ResponseEntity<?> saveModel(@Valid @RequestBody Model model) {
		String username = SecurityUtils.getCurrentUserUsername();
		model.setUsername(username);
		
		Model newModel = null;
        Optional<Model> dbModel = modelRepository.findOneByNameAndUsername(model.getName(), model.getUsername());
        if (dbModel.isPresent() && !dbModel.get().getId().equals(model.getId())) {
			return ResponseEntity.badRequest()
					.headers(HeaderUtil.createFailureAlert("The name already exists", dbModel.get().getName())).body(null);
        } else {
    		newModel = modelRepository.save(model);
    		return ResponseEntity.ok().body(newModel);
        }
	}
	
	/**
	 * GET /models : Get all models of the current user.
	 *
	 * @return the ResponseEntity with status 200 (OK) and with body all models of current user
	 * @throws URISyntaxException 
	 */
	@GetMapping("/models")
	public ResponseEntity<List<Model>> getAllUserModels(Pageable pageable) throws URISyntaxException {
		String username = SecurityUtils.getCurrentUserUsername();
		Page<Model> page = modelRepository.findAllByUsername(username, pageable);
		List<Model> models = page.getContent().stream().collect(Collectors.toList());
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/models");
		return new ResponseEntity<>(models, headers, HttpStatus.OK);
	}
	
	/**
	 * DELETE /model : Delete the "name" model of the current user.
	 *
	 * @param name
	 *            The name of the model to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/models/{name}")
	public ResponseEntity<Void> deleteModel(@PathVariable String name) {
		String username = SecurityUtils.getCurrentUserUsername();
        modelRepository.findOneByNameAndUsername(name, username).ifPresent(model -> {
            modelRepository.delete(model);
        });
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
}
