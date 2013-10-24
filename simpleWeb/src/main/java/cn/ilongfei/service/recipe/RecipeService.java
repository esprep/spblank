package cn.ilongfei.service.recipe;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.persistence.DynamicSpecifications;
import org.springside.modules.persistence.SearchFilter;
import org.springside.modules.persistence.SearchFilter.Operator;

import cn.ilongfei.entity.Recipe;
import cn.ilongfei.entity.Task;
import cn.ilongfei.repository.RecipeDao;

//Spring Bean的标识.
@Component
// 默认将类中的所有public函数纳入事务管理.
@Transactional
public class RecipeService {

	private RecipeDao recipeDao;

	public Recipe getRecipe(Long id) {
		return recipeDao.findOne(id);
	}

	public void saveRecipe(Recipe entity) {
		recipeDao.save(entity);
	}

	public void deleteRecipe(Long id) {
		recipeDao.delete(id);
	}

	public List<Recipe> getAllRecipe() {
		return (List<Recipe>) recipeDao.findAll();
	}

	public Page<Recipe> getUserRecipe(Long userId, Map<String, Object> searchParams, int pageNumber, int pageSize,
			String sortType) {
		PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, sortType);
		Specification<Recipe> spec = buildSpecification(userId, searchParams);
		
		return recipeDao.findAll(spec, pageRequest);
	}

	/**
	 * 创建分页请求.
	 */
	private PageRequest buildPageRequest(int pageNumber, int pagzSize, String sortType) {
		Sort sort = null;
		if ("auto".equals(sortType)) {
			sort = new Sort(Direction.DESC, "id");
		} else if ("title".equals(sortType)) {
			sort = new Sort(Direction.ASC, "title");
		}

		return new PageRequest(pageNumber - 1, pagzSize, sort);
	}

	/**
	 * 创建动态查询条件组合.
	 */
	private Specification<Recipe> buildSpecification(Long userId, Map<String, Object> searchParams) {
		Map<String, SearchFilter> filters = SearchFilter.parse(searchParams);
		filters.put("user.id", new SearchFilter("user.id", Operator.EQ, userId));
		Specification<Recipe> spec = DynamicSpecifications.bySearchFilter(filters.values(), Recipe.class);
		return spec;
	}

	@Autowired
	public void setRecipeDao(RecipeDao RecipeDao) {
		this.recipeDao = RecipeDao;
	}
}
