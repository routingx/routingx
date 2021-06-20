package routingx.data.r2dbc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.r2dbc.convert.EntityRowMapper;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.data.relational.repository.query.RelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import routingx.CustomException;
import routingx.Response;
import routingx.dao.SQLUtils;
import routingx.dao.SimpleRepository;
import routingx.dao.SimpleRepositoryAbs;
import routingx.dao.SimpleRepositoryFactory;
import routingx.data.EntityMetaData;
import routingx.data.EntityProperties;
import routingx.data.ForeignKey;
import routingx.data.LinkDelete;
import routingx.data.UniqueIndex;
import routingx.data.EntityProperties.FieldType;
import routingx.json.JSON;
import routingx.model.IDEntity;
import routingx.model.Page;
import routingx.model.Token;
import routingx.utils.DateTimeUtils;
import routingx.utils.ObjectUtils;
import routingx.utils.ValidatorUtils;
import routingx.webflux.ResponseMono;
import routingx.webflux.SimpleExchangeContext;
import routingx.webflux.WebAbsContext;

@Slf4j
abstract class RepositoryImpl<T, ID extends Serializable> extends WebAbsContext implements SimpleRepositoryAbs {

	enum QueryEnum {
		COUNT, SELECT, UPDATE, INSERT, DELETE
	}

	private final static int MAX_LIMIT = 5000;
	private static final String NEWLINE = System.lineSeparator();
	private final SimpleRepositoryFactory factory;
	private final RelationalEntityInformation<T, ID> entityInfo;
	private final R2dbcEntityOperations entityOperations;
	private final RelationalPersistentEntity<?> persistentEntity;
	private final String entityName;
	private final String entityTable;
	protected final String entityId;
	protected final Class<T> entityClass;
	protected final EntityMetaData meta;

	public RepositoryImpl(SimpleRepositoryFactory factory, //
			RelationalEntityInformation<T, ID> entityInfo, //
			R2dbcEntityTemplate entityTemplate) {
		this.factory = factory;
		this.entityInfo = entityInfo;
		this.entityOperations = entityTemplate;
		this.entityClass = entityInfo.getJavaType();
		this.persistentEntity = entityOperations.getConverter().getMappingContext()
				.getRequiredPersistentEntity(entityClass);
		this.entityName = entityClass.getSimpleName();
		this.entityTable = entityInfo.getTableName().getReference();
		this.entityId = persistentEntity.getIdProperty().getName();
		this.meta = EntityMetaData.get(entityClass);
		if (log.isDebugEnabled()) {
			log.debug("{}({}) {}", entityName, entityTable, entityId);
		}
	}

	protected SimpleRepositoryFactory factory() {
		return factory;
	}

	@Override
	public String getEntityName() {
		return entityName;
	}

	@Override
	public String getEntityIdName() {
		return entityId;
	}

	private Mono<Boolean> validatorUpdate(Object update) {
		return context(ctx -> validator(ctx, update, true));
	}

	private Mono<Boolean> validatorInsert(Object insert) {
		ValidatorUtils.validatorAssert(insert);
		return context(ctx -> validator(ctx, insert, false));
	}

	private Mono<Boolean> validator(SimpleExchangeContext ctx, Object form, boolean update) {
		EntityProperties idProp = null;
		Serializable id = null;
		if (update) {
			idProp = meta.getId();
			id = (Serializable) ObjectUtils.readField(form, idProp.getField());
			if (id == null) {
				return Mono.error(CustomException.bq("id must not be null for update"));
			}
		}
		return validator(ctx, form, meta.getProperties(), id, idProp, update).doOnError(ex -> {
			log.error("{} {} ", form.getClass().getName(), ex.getMessage());
		});
	}

	private Mono<Boolean> validator(SimpleExchangeContext ctx //
			, Object insertOrUpdate //
			, Collection<EntityProperties> properties //
			, Serializable id //
			, EntityProperties idProp //
			, boolean update) {
		return Flux.fromIterable(properties).concatMap(prop -> {
			return validator(ctx, insertOrUpdate, prop, id, idProp, update);
		}).collectList().flatMap(boolList -> {
			for (Boolean bool : boolList) {
				if (!bool) {
					return Mono.just(false);
				}
			}
			return Mono.just(true);
		});
	}

	private Mono<Boolean> validator(SimpleExchangeContext ctx //
			, Object form //
			, EntityProperties prop //
			, Serializable id //
			, EntityProperties idProp //
			, boolean update) {
		return validatorFk(form, prop, update)//
				.flatMap(bool -> validatorUik(ctx, form, prop, id, idProp, update));
	}

	private Mono<Boolean> validatorFk(Object form, EntityProperties prop, boolean update) {
		ForeignKey fk = prop.getAnnotation(ForeignKey.class);
		if (fk != null) {
			Serializable fkValue = (Serializable) ObjectUtils.readField(form, prop.getField());
			if (fkValue == null) {
				if (update) {
					return Mono.just(true);
				}
				return Mono.error(CustomException.bq(prop.getName() + " must not be null "));
			}
			return factory().get(fk.value()).existsById(fkValue).flatMap(bool -> {
				if (!bool) {
					String msg = prop.getName() + " Not Found " + form.toString() + " FK from "
							+ fk.value().getSimpleName() + "[" + fkValue + "]";
					return Mono.error(CustomException.bq(msg));
				} else {
					return Mono.just(true);
				}
			});
		}
		return Mono.just(true);
	}

	private Mono<Boolean> validatorUik(SimpleExchangeContext ctx //
			, Object form //
			, EntityProperties prop //
			, Serializable id //
			, EntityProperties idProp //
			, boolean update) {
		Token token = ctx.getToken();
		UniqueIndex unique = prop.getAnnotation(UniqueIndex.class);
		if (unique != null) {
			Map<String, Object> params = new HashMap<>();
			String[] fieldNames = unique.value();
			for (String fieldName : fieldNames) {
				if (StringUtils.isBlank(fieldName)) {
					fieldName = prop.getName();
				}
				Object fieldValue = ObjectUtils.readField(form, fieldName);
				if (fieldValue == null) {
					fieldValue = ObjectUtils.readField(token, fieldName);
				}
				params.put(fieldName, fieldValue);
			}
			Object fieldValue = ObjectUtils.readField(form, prop.getField());
			if (fieldValue == null) {
				if (update) {
					return Mono.just(true);
				}
				return Mono.error(CustomException.bq(prop.getName() + " must not be null "));
			}
			if (meta.getDeleted() != null) {
				params.put(meta.getDeleted().getColumn(), null);
			}
			return factory().get(form.getClass()).find8Map(params).flatMap(list -> {
				if (update) {
					if (list.size() > 0) {
						Object idValue = ObjectUtils.readField(list.get(0), idProp.getField());
						if (!idValue.equals(id)) {
							return Mono.error(
									CustomException.bq(prop.getName() + " [" + fieldValue + "] already exists "));
						}
					}
				} else {
					if (list.size() > 0) {
						return Mono.error(CustomException.bq(prop.getName() + " [" + fieldValue + "] already exists "));
					}
				}
				return Mono.just(true);
			});
		}
		return Mono.just(true);
	}

	protected boolean setIdEntity(T entityToInsert) {
		if (entityToInsert instanceof IDEntity) {
			String id = ((IDEntity) entityToInsert).getId();
			if (StringUtils.isBlank(id)) {
				((IDEntity) entityToInsert).setId(IDEntity.nextId());
				return true;
			}
		}
		return false;
	}

	protected void setSuperEntity(T entity, Token token) {
		if (meta.getUpdated() != null) {
			ObjectUtils.writeField(entity, meta.getUpdated().getName(), meta.getUpdated().getDefaultValue());
		}
		if (meta.getCreated() != null) {
			ObjectUtils.writeField(entity, meta.getCreated().getName(), meta.getCreated().getDefaultValue());
		}
		String account = token != null ? token.getAccount() : "-";
		if (meta.getUpdater() != null) {
			ObjectUtils.writeField(entity, meta.getUpdater().getName(), account);
		}
		if (meta.getCreater() != null) {
			ObjectUtils.writeField(entity, meta.getCreater().getName(), account);
		}
		if (meta.getUserId() != null) {
			ObjectUtils.writeFieldIfNull(entity, meta.getUserId().getName(), token.getUserId());
		}
		setTenantId(entity, token);
	}

	protected void setTenantId(T entity, Token token) {
		if (meta.getTenantId() != null) {
			ObjectUtils.writeFieldIfNull(entity, meta.getTenantId().getName(), token.getTenantId());
		}
	}

	protected String getTenantId(Token token) {
		if (token != null) {
			return token.getTenantId();
		} else {
			log.warn("getTenantId {} token must not be null!", getEntityName());
			return null;
		}
	}

	protected Query where(Criteria where, Token token) {
		where = whereSqlDelete(where);
		where = whereTenantId(where, token);
		if (where == null) {
			where = Criteria.empty();
		}
		return Query.query(where);
	}

	protected Query whereThenSort(Criteria where, Token token) {
		return querySort(where(where, token));
	}

	private Criteria whereSqlDelete(Criteria where) {
		try {
			if (meta.getDeleted() != null) {
				Object value = meta.getDeleted().getDefaultValue();
				if (meta.getDeleted().getFieldType().equals(FieldType.NUMERIC)) {
					if (where == null) {
						where = Criteria.where(meta.getDeleted().getColumn()).greaterThanOrEquals(value);
					} else {
						where = where.and(meta.getDeleted().getColumn()).greaterThanOrEquals(value);
					}
				} else {
					if (where == null) {
						where = Criteria.where(meta.getDeleted().getColumn()).is(value);
					} else {
						where = where.and(meta.getDeleted().getColumn()).is(value);
					}
				}
			}
		} catch (Throwable e) {
			log.error("", e);
		}
		return where;
	}

	private Criteria whereTenantId(Criteria where, Token token) {
		if (meta.getTenantId() == null) {
			return where;
		}
		if (!SQLUtils.contains(where, meta.getTenantId().getName(), meta.getTenantId().getColumn())//
				&& StringUtils.isNotBlank(getTenantId(token))) {
			if (where == null) {
				where = Criteria.where(meta.getTenantId().getColumn()).is(getTenantId(token));
			} else {
				where = where.and(meta.getTenantId().getColumn()).is(getTenantId(token));
			}
		}
		return where;
	}

	protected Query whereMap(Map<String, Object> whereMap, Token token) {
		Criteria where = null;
		if (whereMap != null) {
			for (String column : whereMap.keySet()) {
				where = append(where, column, whereMap.get(column));
			}
		}
		return where(where, token);
	}

	protected Criteria wherePage(Criteria criteria, Page page) {
		if (page == null) {
			return criteria;
		}
		if (meta.getCreated() != null && page.getStartTime() != null && page.getEndTime() != null) {
			String start = DateTimeUtils.format(page.getStartTime());
			String end = DateTimeUtils.format(page.getEndTime());
			if (criteria == null) {
				criteria = Criteria.where(meta.getCreated().getColumn()).between(start, end);
			} else {
				criteria = criteria.and(meta.getCreated().getColumn()).between(start, end);
			}
		}
		if (!CollectionUtils.isEmpty(page.getParams())) {
			for (String column : page.getParams().keySet()) {
				Object value = page.getParams().get(column);
				if (value != null) {
					criteria = append(criteria, column, value);
				}
			}
		}
		return criteria;
	}

	protected ID getId(T entity) {
		return entityInfo.getRequiredId(entity);
	}

	protected Query query(T entity) {
		Criteria criteria = null;
		Page page = null;
		if (entity instanceof IDEntity) {
			page = ((IDEntity) entity).getPage();
		}
		Map<String, Object> params = page != null ? page.getParams() : Collections.emptyMap();
		for (EntityProperties prop : meta.getProperties()) {
			if (!params.containsKey(prop.getColumn())) {
				Object value = ObjectUtils.readField(entity, prop.getName());
				criteria = append(criteria, prop.getColumn(), value);
			}
		}
		if (page != null) {
			criteria = wherePage(criteria, page);
		}
		Query query;
		if (criteria != null) {
			query = Query.query(criteria);
		} else {
			query = Query.empty();
		}
		return query;
	}

	protected Query query(T entity, Token token) {
		if (meta.getDeleted() != null) {
			ObjectUtils.writeField(entity, meta.getDeleted().getColumn(), meta.getDeleted().getDefaultValue());
		}
		this.setTenantId(entity, token);
		return query(entity);
	}

	protected Query querySort(Query query) {
		if (query.getSort() == null || query.getSort().isEmpty() || query.getSort().isUnsorted()) {
			query = query.sort(meta.getSort());
		}
		return query;
	}

	/**
	 * 逻辑删除
	 * 
	 * @param deleteColumn
	 * @param token
	 * @return
	 */
	protected Mono<Integer> removeAll(Token token) {
		Query query = where(null, token);
		EntityProperties deleted = meta.getDeleted();
		Update update = Update.update(deleted.getColumn(), deleted.getDeletedValue());
		if (meta.getUpdated() != null) {
			update = update.set(meta.getUpdated().getColumn(), meta.getUpdated().getDefaultValue());
		}
		if (meta.getUpdater() != null) {
			update = update.set(meta.getUpdater().getColumn(), token.getUserId());
		}
		Assert.notNull(update, "The update of columns must not be null!");
		Assert.hasText(query.toString(), "The where of columns must not be null!");
		return update(update, query);
	}

	/**
	 * 逻辑删除
	 * 
	 * @param deleteColumn
	 * @param token
	 * @param ids
	 * @return
	 */
	private Mono<Integer> removeById(Token token, ID id) {
		Criteria where = Criteria.where(entityId).is(id);
		EntityProperties deleted = meta.getDeleted();
		Update update = Update.update(deleted.getColumn(), deleted.getDeletedValue());
		if (meta.getUpdated() != null) {
			update = update.set(meta.getUpdated().getColumn(), meta.getUpdated().getDefaultValue());
		}
		if (meta.getUpdater() != null) {
			update = update.set(meta.getUpdater().getColumn(), token.getUserId());
		}
		return update(update, Query.query(where));
	}

	/**
	 * 逻辑删除
	 * 
	 * @param id
	 * @return
	 */
	protected Mono<Integer> removeLinkById(ID id) {
		List<LinkDelete> linkDeleteList = meta.getLinkDeletes();
		if (linkDeleteList.size() > 0) {
			return Flux.fromIterable(linkDeleteList)//
					.concatMap(linkDelete -> deleteLink(id, linkDelete))//
					.collectList().flatMap(intList -> {
						return token(token -> removeById(token, id));
					});
		} else {
			return token(token -> removeById(token, id));
		}
	}

	/**
	 * 物理删除
	 * 
	 * @param id
	 * @return
	 */
	private Mono<Integer> deleteById(ID id) {
		Criteria where = Criteria.where(entityId).is(id);
		Query query = Query.query(where);
		if (log.isInfoEnabled()) {
			log.info("{}{} DELETE FROM {} {}", entityName, NEWLINE, entityTable, where);
		}
		return entityOperations.delete(query, entityClass);
	}

	/**
	 * 物理删除
	 * 
	 * @param id
	 * @return
	 */
	protected Mono<Integer> deleteLinkById(ID id) {
		List<LinkDelete> linkDeleteList = meta.getLinkDeletes();
		if (linkDeleteList.size() > 0) {
			return Flux.fromIterable(linkDeleteList)//
					.concatMap(linkDelete -> deleteLink(id, linkDelete))//
					.collectList().flatMap(intList -> {
						return deleteById(id);
					});
		} else {
			return deleteById(id);
		}
	}

	/**
	 * 删除关联表数据
	 * 
	 * @param id
	 * @param linkDelete
	 * @return
	 */
	private Mono<Integer> deleteLink(Serializable fkId, LinkDelete linkDelete) {
		SimpleRepository<?, Serializable> repository = factory().get(linkDelete.value());
		final String idName = repository.getEntityIdName();
		return repository.findIds(linkDelete.column(), fkId).flatMap(link -> {
			if (log.isInfoEnabled()) {
				log.info("{}{} {} {}", getEntityName(), NEWLINE, linkDelete.value().getName(), JSON.format(link));
			}
			List<Serializable> ids = new ArrayList<>();
			for (Object entity : link) {
				Object idValue = ObjectUtils.readField(entity, idName);
				ids.add((Serializable) idValue);
			}
			if (CollectionUtils.isEmpty(ids)) {
				return Mono.just(0);
			}
			return repository.removeByIds(ids);
		});
	}

	protected Criteria updateByVersion(T entityToUpdate) {
		Criteria where = null;
		if (persistentEntity.hasVersionProperty()) {
			RelationalPersistentProperty versionProperty = persistentEntity.getVersionProperty();
			PersistentPropertyAccessor<?> propertyAccessor = persistentEntity.getPropertyAccessor(entityToUpdate);
			Object version = propertyAccessor.getProperty(versionProperty);
			if (version == null) {
				where = Criteria.where(versionProperty.getColumnName().getReference()).isNull();
			} else {
				where = Criteria.where(versionProperty.getColumnName().getReference()).is(version);
			}
			ConversionService conversionService = entityOperations.getConverter().getConversionService();
			long newVersionValue = 1L;
			if (version != null) {
				newVersionValue = conversionService.convert(version, Long.class) + 1;
			}
			ObjectUtils.writeField(entityToUpdate, versionProperty.getField(), newVersionValue);
		}
		return where;
	}

	protected Mono<? extends T> updateMissing(T entity) {

		return Mono.error(persistentEntity.hasVersionProperty()
				? new OptimisticLockingFailureException(updateMissingOptimisticLocking(entity))
				: new TransientDataAccessResourceException(updateMissingTransient(entity)));
	}

	private String updateMissingOptimisticLocking(T entity) {
		return String.format("Failed to update entity [%s(%s)]. Version does not match for row with Id [%s].",
				persistentEntity.getName(), //
				persistentEntity.getTableName(), //
				persistentEntity.getIdentifierAccessor(entity).getIdentifier());
	}

	private String updateMissingTransient(T entity) {
		return String.format("Failed to update entity [%s(%s)]. Row with Id [%s] does not exist.",
				persistentEntity.getName(), //
				persistentEntity.getTableName(), //
				persistentEntity.getIdentifierAccessor(entity).getIdentifier());
	}

	protected Criteria append(Criteria where, String column, Object value) {
		if (value == null || StringUtils.isBlank(value.toString())) {
			return where;
		}
		if (value != null && value.getClass().isArray()) {
			if (where == null) {
				where = Criteria.where(column).in((Object[]) value);
			} else {
				where = where.and(column).in((Object[]) value);
			}
		} else if (value instanceof Collection<?>) {
			if (where == null) {
				where = Criteria.where(column).in((Collection<?>) value);
			} else {
				where = where.and(column).in((Collection<?>) value);
			}
		} else {
			String like = SQLUtils.like(value);
			if (like != null) {
				if (where == null) {
					where = Criteria.where(column).like(like);
				} else {
					where = where.and(column).like(like);
				}
			} else if (where == null) {
				where = Criteria.where(column).is(value);
			} else {
				where = where.and(column).is(value);
			}
		}
		return where;
	}

	protected Mono<T> insert(T entityToInsert, Token token) {
		Assert.notNull(entityToInsert, "entityToInsert to update must not be null!");
		setIdEntity(entityToInsert);
		ObjectUtils.setDefaultValue(entityToInsert);
		setSuperEntity(entityToInsert, token);
		if (log.isDebugEnabled()) {
			log.debug("{}{}{}", getEntityIdName(), NEWLINE, JSON.format(entityToInsert));
		}
		// return entityOperations.insert(entityToInsert);
		return validatorInsert(entityToInsert).flatMap(bool -> entityOperations.insert(entityToInsert));
	}

	protected Mono<T> update(T entityToUpdate, Token token) {
		return validatorUpdate(entityToUpdate).flatMap(bool -> updatexe(entityToUpdate, token));
	}

	private Mono<T> updatexe(T entityToUpdate, Token token) {
		setSuperEntity(entityToUpdate, token);
		Criteria where = updateByVersion(entityToUpdate);
		EntityMetaData meta = EntityMetaData.get(entityToUpdate.getClass());
		ID id = getId(entityToUpdate);
		Assert.notNull(id, "The entityToUpdate of ID must not be null!");
		if (where == null) {
			where = Criteria.where(entityId).is(id);
		} else {
			where = where.and(entityId).is(id);
		}
		Update update = null;
		for (EntityProperties prop : meta.getProperties()) {
			String column = prop.getColumn();
			Object value = ObjectUtils.readField(entityToUpdate, prop.getName());
			if (value != null && prop.isUpdatable() && !prop.isVersion()) {
				if (update == null) {
					update = Update.update(column, value);
				} else {
					update = update.set(column, value);
				}
			}
		}
		Assert.notNull(update, "The entityToUpdate of columns must not be null!");
		return update(update, Query.query(where))//
				.flatMap(rowsUpdated -> rowsUpdated == 0 ? updateMissing(entityToUpdate) : Mono.just(entityToUpdate));
	}

	/**
	 * 
	 * @param query
	 * @param delete 1.delete 2.count 3.select
	 */
	protected void showSql(Query query, QueryEnum queryEnum) {
		if (log.isInfoEnabled()) {
			StringBuilder sql = new StringBuilder();
			sql.append(entityName);
			sql.append(NEWLINE);
			switch (queryEnum) {
			case DELETE:
				sql.append("DELETE ");
				break;
			case COUNT:
				sql.append("SELECT count(*) ");
				break;
			case SELECT:
				sql.append("SELECT ");
				if (query.getColumns().size() > 0) {
					sql.append(JSON.format(query.getColumns()));
				} else {
					sql.append("* ");
				}
				break;
			default:
				break;
			}
			sql.append("FROM ");
			sql.append(entityTable);
			if (!query.getCriteria().isEmpty()) {
				sql.append(NEWLINE);
				sql.append(" WHERE ");
				sql.append(query.getCriteria().get().toString());
			}
			if (query.isSorted()) {
				sql.append(NEWLINE);
				sql.append(" ORDER BY ");
				sql.append(query.getSort().toString().replaceAll(":", ""));
			}
			long offset = query.getOffset();
			int limit = query.getLimit();
			if (offset >= 0) {
				sql.append(" limit " + offset);
			}
			if (limit >= 0) {
				sql.append(", " + limit);
			}
			log.info(sql.toString());
		}
	}

	protected Mono<Integer> delete(Query query) {
		Assert.notNull(query, "query must not be null!");
		showSql(query, QueryEnum.DELETE);
		return entityOperations.delete(query, entityClass);
	}

	protected Mono<Integer> update(Update update, Query query) {
		Assert.notNull(update, "update must not be null!");
		Assert.notNull(query, "query must not be null!");
		if (log.isInfoEnabled()) {
			log.info("{}{} UPDATE {} {} WHERE {}", entityName, NEWLINE, entityTable, update, query.getCriteria().get());
		}
		return entityOperations.update(entityClass).matching(query).apply(update);
	}

	protected Mono<List<T>> select(Query query) {
		Assert.notNull(query, "query must not be null!");
		if (query.getLimit() < 0 || query.getLimit() > MAX_LIMIT) {
			query = query.limit(MAX_LIMIT);
		}
		if (query.getOffset() < 0) {
			query = query.offset(0);
		}
		showSql(query, QueryEnum.SELECT);
		return entityOperations.select(query, entityClass).collectList();
	}

	protected Mono<T> selectOne(Query query) {
		Assert.notNull(query, "query must not be null!");
		showSql(query, QueryEnum.SELECT);
		return entityOperations.selectOne(query, entityClass);
	}

	@Override
	public Mono<Long> count(Query query) {
		showSql(query, QueryEnum.COUNT);
		return entityOperations.count(query, entityClass);
	}

	@Transactional
	@Override
	public Mono<Integer> execute(String sql, Map<String, Object> params) {
		GenericExecuteSpec execute = nativeExecuteSpec(sql, params);
		return execute.fetch().rowsUpdated();
	}

	@Override
	public <R> Mono<R> nativeOne(String sql, Map<String, Object> params, Class<R> resultType) {
		Assert.notNull(resultType, "resultType to page must not be null!");
		GenericExecuteSpec execute = nativeExecuteSpec(sql, params);
		// return execute.as(resultType).fetch().one();
		EntityRowMapper<R> rowMapper = new EntityRowMapper<>(resultType, entityOperations.getConverter());
		return execute.map(rowMapper).one();
	}

	@Override
	public <R> Mono<List<R>> nativeList(String sql, Map<String, Object> params, Class<R> resultType) {
		Assert.notNull(resultType, "resultType to page must not be null!");
		GenericExecuteSpec execute = nativeExecuteSpec(sql, params);
		EntityRowMapper<R> rowMapper = new EntityRowMapper<>(resultType, entityOperations.getConverter());
		return execute.map(rowMapper).all().collectList();
	}

	@Override
	public Mono<Long> nativeCount(String sql, Map<String, Object> params) {
		GenericExecuteSpec execute = nativeExecuteSpec(sql, params);
		return execute.fetch().one().flatMap(map -> {
			if (map.keySet().size() == 0) {
				return Mono.just(0L);
			}
			return Mono.just((Long) map.get(map.keySet().iterator().next()));
		}).switchIfEmpty(Mono.just(0L));
	}

	@Override
	public <R> Mono<Response<List<R>>> nativePage(String sql, Page page, Class<R> resultType) {
		Assert.notNull(sql, "sql to page must not be null!");
		Assert.notNull(page, "params to page must not be null!");
		Assert.notNull(resultType, "resultType to page must not be null!");
		String sqlCount = SQLUtils.toCount(sql);
		return nativeCount(sqlCount, page.getParams())//
				.flatMap(total -> {
					page.setTotal(total);
					if (total > 0) {
						final String query = SQLUtils.limit(sql, page);
						return nativeList(query, page.getParams(), resultType).flatMap(list -> {
							return ResponseMono.ok(page, list);
						});
					} else {
						return ResponseMono.ok(page, Collections.emptyList());
					}
				});
	}

	private GenericExecuteSpec nativeExecuteSpec(String sql, Map<String, Object> params) {
		Assert.notNull(sql, "sql to page must not be null!");
		if (log.isInfoEnabled()) {
			log.info("{}{}{}{}{}", entityName, NEWLINE, sql, NEWLINE, JSON.format(params));
		}
		if (params == null || params.size() == 0) {
			return entityOperations.getDatabaseClient().sql(sql);
		}
		GenericExecuteSpec execute = entityOperations.getDatabaseClient().sql(sql);
		for (String parameterName : params.keySet()) {
			final Object value = params.get(parameterName);
			execute = execute.bind(parameterName, value);
		}
		return execute;
	}

}
