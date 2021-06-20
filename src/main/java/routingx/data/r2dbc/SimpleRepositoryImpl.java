package routingx.data.r2dbc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.data.relational.repository.query.RelationalEntityInformation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import routingx.Response;
import routingx.dao.SQLUtils;
import routingx.dao.SimpleRepository;
import routingx.dao.SimpleRepositoryFactory;
import routingx.model.IDEntity;
import routingx.model.Page;
import routingx.model.Token;
import routingx.webflux.ResponseMono;

@Transactional(readOnly = true)
class SimpleRepositoryImpl<T, ID extends Serializable> extends RepositoryImpl<T, ID> //
		/* extends SimpleR2dbcRepository<T, ID> */
		implements SimpleRepository<T, ID> {

	public SimpleRepositoryImpl(SimpleRepositoryFactory factory, //
			RelationalEntityInformation<T, ID> entityInfo, //
			R2dbcEntityTemplate entityTemplate) {
		super(factory, entityInfo, entityTemplate);
	}

	@Override
	@Transactional
	public Mono<Integer> remove(T objectToremove) {
		Assert.notNull(objectToremove, "Object to remove must not be null!");
		return removeById(getId(objectToremove));
	}

	@Override
	@Transactional
	public Mono<Integer> removeAll() {
		return token(token -> {
			if (meta.getDeleted() != null) {
				return removeAll(token);
			} else {
				return delete(where(null, token));
			}
		});
	}

	@Override
	@Transactional
	public Mono<Integer> removeAll(List<? extends T> iterable) {
		Assert.notNull(iterable, "The iterable of Id's must not be null!");
		List<ID> ids = new ArrayList<>();
		for (T e : iterable) {
			ids.add(getId(e));
		}
		return removeByIds(ids);
	}

	@Transactional
	@Override
	public Mono<Integer> removeById(ID id) {
		Assert.notNull(id, "Id must not be null!");
		if (meta.getDeleted() != null) {
			return removeLinkById(id);
		} else {
			return deleteLinkById(id);
		}
	}

	@Override
	@Transactional
	public Mono<Integer> removeByIds(List<ID> ids) {
		if (ids.isEmpty()) {
			return Mono.just(0);
		}
		Assert.notEmpty(ids, "ids to removeByIds must not be null!");
		return Flux.fromIterable(ids)//
				.concatMap(this::removeById)//
				.collectList().flatMap(intList -> {
					int count = 0;
					for (Integer num : intList) {
						count += num;
					}
					return Mono.just(count);
				});
	}

	@Override
	@Transactional
	public Mono<T> insert(T entityToInsert) {
		return token(token -> insert(entityToInsert, token));
	}

	@Override
	@Transactional
	public Mono<List<T>> insert(List<? extends T> entitysToInsert) {
		Assert.notEmpty(entitysToInsert, "entitys to insert must not be null!");
		return Flux.fromIterable(entitysToInsert).concatMap(this::insert).collectList();
	}

	@Override
	@Transactional
	public Mono<T> update(T entityToUpdate) {
		Assert.notNull(entityToUpdate, "entityToUpdate to update must not be null!");
		// return validatorUpdate(entityToUpdate).flatMap(bool -> token(token ->
		// update(entityToUpdate, token)));
		return token(token -> update(entityToUpdate, token));
	}

	@Override
	@Transactional
	public Mono<List<T>> update(List<? extends T> entitysToUpdate) {
		Assert.notEmpty(entitysToUpdate, "entitys to update must not be null!");
		Flux<T> flux = Flux.fromIterable(entitysToUpdate).concatMap(this::update);
		return flux.collectList();
	}

	@Override
	@Transactional
	public Mono<Integer> updateIfNull(String column, Object value) {
		Update update = Update.update(column, value);
		Criteria where = Criteria.where(column).isNull();
		return update(update, Query.query(where)).flatMap(rowsUpdated -> Mono.just(rowsUpdated));
	}

	private <S extends T> Mono<Boolean> isNew(S value) {
		if (setIdEntity(value)) {
			return Mono.just(true);
		}
		ID id = getId(value);
		if (id != null) {
			return existsById(id).flatMap(e -> Mono.just(!e));
		} else {
			return Mono.just(true);
		}
	}

	@Override
	@Transactional
	public Mono<T> save(T objectToSave) {
		Assert.notNull(objectToSave, "Object to save must not be null!");
		return token(token -> {
			setSuperEntity(objectToSave, token);
			return isNew(objectToSave).flatMap(bool -> {
				if (bool) {
					return insert(objectToSave);
				}
				return update(objectToSave);
			});
		});
	}

	@Override
	public Mono<List<T>> save(List<? extends T> entitysToSave) {
		Assert.notNull(entitysToSave, "entitysToSave to saveOrUpdateAll must not be null!");
		return Flux.fromIterable(entitysToSave).concatMap(this::save).collectList();
	}

	@Override
	public Mono<Boolean> existsById(ID id) {
		return findById(id).flatMap(e -> Mono.just(e != null)).defaultIfEmpty(false);
	}

	@Override
	public Mono<Boolean> exists(T entity) {
		Query query = query(entity);
		return findOne(query).flatMap(one -> {
			return Mono.justOrEmpty(one != null);
		}).defaultIfEmpty(false);
	}

	@Override
	public Mono<List<T>> existsFind(T entity) {
		Query query = query(entity);
		return find(query);
	}

	@Override
	public Mono<List<T>> find(T entity) {
		return token(token -> find(entity, token));
	}

	private Mono<List<T>> find(T entity, Token token) {
		Page page = null;
		Query query = query(entity, token);
		if (entity instanceof IDEntity) {
			page = ((IDEntity) entity).getPage();
			if (page != null && page.getSort() != null) {
				query = query.sort(page.getSort().getSort());
			}
		}
		query = querySort(query);
		return select(query);
	}

	@Override
	public Mono<List<T>> find(Query query) {
		return select(query);
	}

	@Override
	public Mono<List<T>> find(String column, Object value) {
		Assert.notNull(column, "column to findOne must not be null!");
		Assert.notNull(value, "Value must not be null!");
		return token(token -> {
			Criteria where = null;
			if (value != null && value.getClass().isArray()) {
				where = Criteria.where(column).in((Object[]) value);
			} else if (value instanceof Collection<?>) {
				where = Criteria.where(column).in((Collection<?>) value);
			} else {
				String like = SQLUtils.like(value);
				if (like != null) {
					where = Criteria.where(column).like(like);
				} else {
					where = Criteria.where(column).is(value);
				}
			}
			return select(whereThenSort(where, token));
		});
	}

	@Override
	public Mono<List<T>> findIds(String column, Object value) {
		Assert.notNull(column, "column to findOne must not be null!");
		Assert.notNull(value, "Value must not be null!");
		return token(token -> {
			Criteria where = null;
			if (value != null && value.getClass().isArray()) {
				where = Criteria.where(column).in((Object[]) value);
			} else if (value instanceof Collection<?>) {
				where = Criteria.where(column).in((Collection<?>) value);
			} else {
				String like = SQLUtils.like(value);
				if (like != null) {
					where = Criteria.where(column).like(like);
				} else {
					where = Criteria.where(column).is(value);
				}
			}
			Query query = whereThenSort(where, token);
			query = query.columns(entityId);
			return select(query);
		});
	}

	@Override
	public Mono<List<T>> findAll() {
		return token(token -> find(whereThenSort(null, token)));
	}

	@Override
	public Mono<List<T>> findAll(Sort sort) {
		return token(token -> find(querySort(where(null, token).sort(sort))));
	}

	@Override
	public Mono<T> findById(ID id) {
		Assert.notNull(id, "Id must not be null!");
		return selectOne(Query.query(Criteria.where(entityId).is(id)));
	}

	@Override
	public Mono<T> findOne(T entity) {
		return token(token -> findOne(entity, token));
	}

	private Mono<T> findOne(T entity, Token token) {
		Query query = query(entity, token);
		query = querySort(query);
		return findOne(query);
	}

	@Override
	public Mono<T> findOne(Query query) {
		query = querySort(query);
		query.offset(0);
		query.limit(1);
		return select(query).flatMap(list -> {
			return list.size() > 0 ? Mono.just(list.get(0)) : Mono.empty();
		});
	}

	@Override
	public Mono<T> findOne(String column, Object value) {
		return token(token -> findOne(column, value, token));
	}

	private Mono<T> findOne(String column, Object value, Token token) {
		Assert.notNull(column, "column to findOne must not be null!");
		Criteria where = null;
		if (value.getClass().isArray()) {
			where = Criteria.where(column).in((Object[]) value);
		} else if (value instanceof Collection<?>) {
			where = Criteria.where(column).in((Collection<?>) value);
		} else {
			where = Criteria.where(column).is(value);
		}
		return findOne(where(where, token));
	}

	@Override
	public Mono<List<T>> find8Map(Map<String, Object> whereMap) {
		Assert.notNull(whereMap, "whereMap to find must not be null!");
		return token(token -> {
			return find8Map(whereMap, token);
		});
	}

	private Mono<List<T>> find8Map(Map<String, Object> whereMap, Token token) {
		return select(querySort(whereMap(whereMap, token)));
	}

	@Override
	public Mono<Long> count(T entity) {
		return token(token -> count(entity, token));
	}

	private Mono<Long> count(T entity, Token token) {
		return count(query(entity, token));
	}

	@Override
	public Mono<Long> countAll() {
		return token(token -> count(where(null, token)));
	}

	@Override
	public Mono<Response<List<T>>> page(T entity) {
		return token(token -> page(entity, token));
	}

	private Mono<Response<List<T>>> page(T entity, Token token) {
		Assert.notNull(entity, "entity to page must not be null!");
		Query query = query(entity, token);
		final Page page;
		if (entity instanceof IDEntity) {
			page = ((IDEntity) entity).page();
		} else {
			page = new Page();
		}
		return count(query)//
				.flatMap(total -> {
					page.setTotal(total);
					if (total > 0) {
						Query q = query;
						if (page.getSort() != null) {
							q = q.sort(page.getSort().getSort());
						}
						q = querySort(q);
						q = q.offset(page.offset());
						q = q.limit(page.limit());
						return find(q).flatMap(list -> {
							return ResponseMono.ok(page, list);
						});
					} else {
						return ResponseMono.ok(page, Collections.emptyList());
					}
				});
	}

	@Override
	public Mono<Response<List<T>>> page(Page page) {
		Assert.notNull(page, "whereMap to find must not be null!");
		return token(token -> page(page, token));
	}

	private Mono<Response<List<T>>> page(Page page, Token token) {
		Criteria where = wherePage(null, page);
		Query query = where(where, token);
		return count(query)//
				.flatMap(total -> {
					page.setTotal(total);
					if (total > 0) {
						Query q = query;
						if (page.getSort() != null) {
							q = q.sort(page.getSort().getSort());
						}
						q = querySort(q);
						q = q.offset(page.offset());
						q = q.limit(page.limit());
						return find(q).flatMap(list -> {
							return ResponseMono.ok(page, list);
						});
					} else {
						return ResponseMono.ok(page, Collections.emptyList());
					}
				});
	}
}
