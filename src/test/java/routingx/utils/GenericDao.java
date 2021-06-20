package routingx.utils;

import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenericDao<T, ID> extends HashMap<ID, T> implements GenericIDao<T, ID> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1343341895781458511L;
	private final Class<T> clazz;

	public GenericDao(Class<T> clazz) {
		this.clazz = clazz;
	}

	@SuppressWarnings("unchecked")
	public GenericDao() {
		clazz = (Class<T>) GenericUtils.getParameterizedType(this.getClass());
		log.debug(clazz.toString());
	}
}
