package cn.ilongfei.web.recipe;

import java.util.Map;

import javax.servlet.ServletRequest;
import javax.validation.Valid;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import cn.ilongfei.entity.Recipe;
import cn.ilongfei.entity.User;
import cn.ilongfei.service.account.ShiroDbRealm.ShiroUser;
import cn.ilongfei.service.recipe.RecipeService;
import org.springside.modules.web.Servlets;

import com.google.common.collect.Maps;

/**
 * Recipe管理的Controller, 使用Restful风格的Urls:
 * 
 * List page : GET /recipe/
 * Create page : GET /recipe/create
 * Create action : POST /recipe/create
 * Update page : GET /recipe/update/{id}
 * Update action : POST /recipe/update
 * Delete action : GET /recipe/delete/{id}
 * 
 * @author calvin
 */
@Controller
@RequestMapping(value = "/recipe")
public class RecipeController {

	private static final String PAGE_SIZE = "3";

	private static Map<String, String> sortTypes = Maps.newLinkedHashMap();
	static {
		sortTypes.put("auto", "自动");
		sortTypes.put("title", "标题");
	}

	@Autowired
	private RecipeService recipeService;

	@RequestMapping(method = RequestMethod.GET)
	public String list(@RequestParam(value = "page", defaultValue = "1") int pageNumber,
			@RequestParam(value = "page.size", defaultValue = PAGE_SIZE) int pageSize,
			@RequestParam(value = "sortType", defaultValue = "auto") String sortType, Model model,
			ServletRequest request) {
		Map<String, Object> searchParams = Servlets.getParametersStartingWith(request, "search_");
		Long userId = getCurrentUserId();

		Page<Recipe> recipes = recipeService.getUserRecipe(userId, searchParams, pageNumber, pageSize, sortType);

		model.addAttribute("recipes", recipes);
		model.addAttribute("sortType", sortType);
		model.addAttribute("sortTypes", sortTypes);
		// 将搜索条件编码成字符串，用于排序，分页的URL
		model.addAttribute("searchParams", Servlets.encodeParameterStringWithPrefix(searchParams, "search_"));

		return "recipe/recipeList";
	}

	@RequestMapping(value = "create", method = RequestMethod.GET)
	public String createForm(Model model) {
		model.addAttribute("recipe", new Recipe());
		model.addAttribute("action", "create");
		return "recipe/recipeForm";
	}

	@RequestMapping(value = "create", method = RequestMethod.POST)
	public String create(@Valid Recipe newRecipe, RedirectAttributes redirectAttributes) {
		User user = new User(getCurrentUserId());
		newRecipe.setUser(user);

		recipeService.saveRecipe(newRecipe);
		redirectAttributes.addFlashAttribute("message", "创建任务成功");
		return "redirect:/recipe/";
	}

	@RequestMapping(value = "update/{id}", method = RequestMethod.GET)
	public String updateForm(@PathVariable("id") Long id, Model model) {
		model.addAttribute("recipe", recipeService.getRecipe(id));
		model.addAttribute("action", "update");
		return "recipe/recipeForm";
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	public String update(@Valid @ModelAttribute("recipe") Recipe recipe, RedirectAttributes redirectAttributes) {
		recipeService.saveRecipe(recipe);
		redirectAttributes.addFlashAttribute("message", "更新任务成功");
		return "redirect:/recipe/";
	}

	@RequestMapping(value = "delete/{id}")
	public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
		recipeService.deleteRecipe(id);
		redirectAttributes.addFlashAttribute("message", "删除任务成功");
		return "redirect:/recipe/";
	}

	/**
	 * 所有RequestMapping方法调用前的Model准备方法, 实现Struts2 Preparable二次部分绑定的效果,先根据form的id从数据库查出Recipe对象,再把Form提交的内容绑定到该对象上。
	 * 因为仅update()方法的form中有id属性，因此仅在update时实际执行.
	 */
	@ModelAttribute
	public void getRecipe(@RequestParam(value = "id", defaultValue = "-1") Long id, Model model) {
		if (id != -1) {
			model.addAttribute("recipe", recipeService.getRecipe(id));
		}
	}

	/**
	 * 取出Shiro中的当前用户Id.
	 */
	private Long getCurrentUserId() {
		ShiroUser user = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		return user.id;
	}
}
