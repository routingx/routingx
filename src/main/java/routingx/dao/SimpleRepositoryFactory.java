package routingx.dao;

import java.io.Serializable;

public interface SimpleRepositoryFactory {

	SimpleRepository<?, Serializable> get();

	<T> SimpleRepository<T, Serializable> get(Class<T> clazz);

	<R> SimpleRepository<R, Serializable> get(R entity);

}