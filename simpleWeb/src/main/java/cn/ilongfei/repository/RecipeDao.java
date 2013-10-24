package cn.ilongfei.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ilongfei.entity.Recipe;
import cn.ilongfei.entity.Task;

public interface RecipeDao extends PagingAndSortingRepository<Recipe, Long>, JpaSpecificationExecutor<Recipe> {
	Recipe findByName(String name);
}
