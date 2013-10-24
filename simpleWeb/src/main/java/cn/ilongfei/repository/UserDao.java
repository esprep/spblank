package cn.ilongfei.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import cn.ilongfei.entity.User;

public interface UserDao extends PagingAndSortingRepository<User, Long> {
	User findByLoginName(String loginName);
}
