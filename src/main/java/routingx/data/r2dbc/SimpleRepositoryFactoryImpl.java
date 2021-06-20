package routingx.data.r2dbc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import routingx.dao.SimpleRepository;
import routingx.dao.SimpleRepositoryFactory;

public class SimpleRepositoryFactoryImpl extends R2dbcRepositoryFactory implements SimpleRepositoryFactory {

	private final R2dbcEntityTemplate entityTemplate;
	private final Map<Class<?>, SimpleRepositoryImpl<?, Serializable>> repositoryMap = new HashMap<>();

	public SimpleRepositoryFactoryImpl(R2dbcEntityTemplate r2dbcEntityTemplate) {
		super(r2dbcEntityTemplate);
		this.entityTemplate = r2dbcEntityTemplate;
	}

	@Override
	public SimpleRepository<?, Serializable> get() {
		if (repositoryMap.keySet().isEmpty()) {
			return null;
		}
		return repositoryMap.get(repositoryMap.keySet().iterator().next());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> SimpleRepository<T, Serializable> get(Class<T> clazz) {
		if (repositoryMap.containsKey(clazz)) {
			return (SimpleRepository<T, Serializable>) repositoryMap.get(clazz);
		}
		SimpleRepositoryImpl<T, Serializable> repository = null;
		repository = new SimpleRepositoryImpl<>(this, getEntityInformation(clazz), entityTemplate);
		Assert.notNull(repository, clazz.getName() + " must be entity!");
		repositoryMap.put(clazz, repository);
		return repository;
	}

	@Override
	public <R> SimpleRepository<R, Serializable> get(R entity) {
		@SuppressWarnings("unchecked")
		Class<R> clazz = (Class<R>) ClassUtils.getUserClass(entity);
		return get(clazz);
	}

}
