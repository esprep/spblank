package cn.ilongfei.rest;

import java.net.URI;
import java.util.List;

import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriComponentsBuilder;
import cn.ilongfei.entity.Recipe;
import cn.ilongfei.service.recipe.RecipeService;
import org.springside.modules.beanvalidator.BeanValidators;
import org.springside.modules.web.MediaTypes;

/**
 * Recipe的Restful API的Controller.
 * 
 * @author calvin
 */
@Controller
@RequestMapping(value = "/api/v1/recipe")
public class RecipeRestController {

	private static Logger logger = LoggerFactory.getLogger(RecipeRestController.class);

	@Autowired
	private RecipeService recipeService;

	@Autowired
	private Validator validator;

	@RequestMapping(method = RequestMethod.GET, produces = MediaTypes.JSON_UTF_8)
	@ResponseBody
	public List<Recipe> list() {
		return recipeService.getAllRecipe();
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaTypes.JSON_UTF_8)
	@ResponseBody
	public Recipe get(@PathVariable("id") Long id) {
		Recipe recipe = recipeService.getRecipe(id);
		if (recipe == null) {
			String message = "任务不存在(id:" + id + ")";
			logger.warn(message);
			throw new RestException(HttpStatus.NOT_FOUND, message);
		}
		return recipe;
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaTypes.JSON)
	@ResponseBody
	public ResponseEntity<?> create(@RequestBody Recipe recipe, UriComponentsBuilder uriBuilder) {
		// 调用JSR303 Bean Validator进行校验, 异常将由RestExceptionHandler统一处理.
		BeanValidators.validateWithException(validator, recipe);

		// 保存任务
		recipeService.saveRecipe(recipe);

		// 按照Restful风格约定，创建指向新任务的url, 也可以直接返回id或对象.
		Long id = recipe.getId();
		URI uri = uriBuilder.path("/api/v1/recipe/" + id).build().toUri();
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(uri);

		return new ResponseEntity(headers, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaTypes.JSON)
	public ResponseEntity<?> update(@RequestBody Recipe recipe) {
		// 调用JSR303 Bean Validator进行校验, 异常将由RestExceptionHandler统一处理.
		BeanValidators.validateWithException(validator, recipe);
		// 保存
		recipeService.saveRecipe(recipe);

		// 按Restful约定，返回204状态码, 无内容. 也可以返回200状态码.
		return new ResponseEntity(HttpStatus.NO_CONTENT);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") Long id) {
		recipeService.deleteRecipe(id);
	}
}
