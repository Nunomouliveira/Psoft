package psoftProjectG10.userManagement.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import psoftProjectG10.exceptions.NotFoundException;
import psoftProjectG10.userManagement.api.Page;
import psoftProjectG10.userManagement.api.SearchUsersQuery;
import psoftProjectG10.userManagement.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@CacheConfig(cacheNames = "users")
public interface UserRepository extends UserRepoCustom, CrudRepository<User, Long> {

	@Override
	@CacheEvict(allEntries = true)
	<S extends User> List<S> saveAll(Iterable<S> entities);

	@Override
	@Caching(evict = { @CacheEvict(key = "#p0.id", condition = "#p0.id != null"),
			@CacheEvict(key = "#p0.username", condition = "#p0.username != null") })
	<S extends User> S save(S entity);


	@Override
	@Cacheable
	Optional<User> findById(Long objectId);


	@Cacheable
	default User getById(final Long id) {
		final Optional<User> maybeUser = findById(id);
		return maybeUser.filter(User::isEnabled).orElseThrow(() -> new NotFoundException(User.class, id));
	}

	@Cacheable
	default User getByUsername(final String username) {
		final Optional<User> maybeUser = findByUsername(username);
		return maybeUser.filter(User::isEnabled).orElseThrow(() -> new NotFoundException(User.class, username));
	}

	@Cacheable
	Optional<User> findByUsername(String username);


	boolean existsByUsername(String username);
}

interface UserRepoCustom {

	List<User> searchUsers(Page page, SearchUsersQuery query);
}

@RequiredArgsConstructor
class UserRepoCustomImpl implements UserRepoCustom {

	private final EntityManager em;

	@Override
	public List<User> searchUsers(final Page page, final SearchUsersQuery query) {

		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<User> cq = cb.createQuery(User.class);
		final Root<User> root = cq.from(User.class);
		cq.select(root);

		final List<Predicate> where = new ArrayList<>();
		if (StringUtils.hasText(query.getUsername())) {
			where.add(cb.equal(root.get("username"), query.getUsername()));
		}
		if (StringUtils.hasText(query.getFullName())) {
			where.add(cb.like(root.get("fullName"), "%" + query.getFullName() + "%"));
		}

		if (!where.isEmpty()) {
			cq.where(cb.or(where.toArray(new Predicate[0])));
		}

		cq.orderBy(cb.desc(root.get("createdAt")));

		final TypedQuery<User> q = em.createQuery(cq);
		q.setFirstResult((page.getNumber() - 1) * page.getLimit());
		q.setMaxResults(page.getLimit());

		return q.getResultList();
	}
}
