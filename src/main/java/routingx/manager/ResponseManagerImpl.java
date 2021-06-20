package routingx.manager;

import java.io.Serializable;
import java.util.List;

import reactor.core.publisher.Mono;
import routingx.CustomException;
import routingx.Response;
import routingx.service.UniversalAbsService;
import routingx.webflux.ResponseMono;
import routingx.webflux.WebAbsContext;

abstract class ResponseManagerImpl extends WebAbsContext implements ResponseManager {

	public ResponseManagerImpl() {
	}

	protected abstract UniversalAbsService access();
//
//	@Override
//	public Mono<Boolean> updateVerify(Object update) {
//		ValidatorUtils.validatorAssert(update);
//		return context(ctx -> verify(ctx, update, true));
//	}
//
//	@Override
//	public Mono<Boolean> insertVerify(Object insert) {
//		ValidatorUtils.validatorAssert(insert);
//		return context(ctx -> verify(ctx, insert, false));
//	}
//
//	private Mono<Boolean> verify(SimpleExchangeContext ctx, Object insertOrUpdate, boolean update) {
//		EntityMetaData metadata = EntityMetaData.get(insertOrUpdate.getClass());
//		EntityProperties idProp = null;
//		Serializable id = null;
//		if (update) {
//			idProp = metadata.getId();
//			id = (Serializable) ObjectUtils.readField(insertOrUpdate, idProp.getField());
//			if (id == null) {
//				return Mono.error(CustomException.bq("id must not be null for update"));
//			}
//		}
//		return verify(ctx, insertOrUpdate, metadata.getProperties(), id, idProp, update, metadata.isSqlDeleted());
//	}
//
//	private Mono<Boolean> verify(SimpleExchangeContext ctx //
//			, Object insertOrUpdate //
//			, Collection<EntityProperties> properties //
//			, Serializable id //
//			, EntityProperties idProp //
//			, boolean update //
//			, boolean sqlDeleted) {
//		Flux<Boolean> flux = Flux.fromIterable(properties).concatMap(prop -> {
//			return verify(ctx, insertOrUpdate, prop, id, idProp, update, sqlDeleted);
//		});
//		return flux.collectList().flatMap(boolList -> {
//			for (Boolean bool : boolList) {
//				if (!bool) {
//					return Mono.just(false);
//				}
//			}
//			return Mono.just(true);
//		});
//	}
//
//	private Mono<Boolean> verify(SimpleExchangeContext ctx //
//			, Object insertOrUpdate //
//			, EntityProperties prop //
//			, Serializable id //
//			, EntityProperties idProp //
//			, boolean update //
//			, boolean sqlDeleted) {
//		return verifyForeignKey(insertOrUpdate, prop)//
//				.flatMap(bool -> verifyUniqueIndex(ctx, insertOrUpdate, prop, id, idProp, update, sqlDeleted));
//	}
//
//	private Mono<Boolean> verifyForeignKey(Object insertOrUpdate, EntityProperties prop) {
//		ForeignKey fk = prop.getAnnotation(ForeignKey.class);
//		if (fk != null) {
//			Serializable fkValue = (Serializable) ObjectUtils.readField(insertOrUpdate, prop.getField());
//			if (fkValue == null) {
//				return Mono.error(CustomException.bq(prop.getName() + " must not be null "));
//			}
//			return access().existsById(fk.value(), fkValue).flatMap(bool -> {
//				if (!bool) {
//					String msg = prop.getName() + " Not Found from " + fk.value().getSimpleName() + "[" + fkValue + "]";
//					return Mono.error(CustomException.bq(msg));
//				} else {
//					return Mono.just(true);
//				}
//			});
//		}
//		return Mono.just(true);
//	}
//
//	private Mono<Boolean> verifyUniqueIndex(SimpleExchangeContext ctx //
//			, Object insertOrUpdate //
//			, EntityProperties prop //
//			, Serializable id //
//			, EntityProperties idProp //
//			, boolean update //
//			, boolean sqlDeleted) {
//		Token token = ctx.getToken();
//		UniqueIndex unique = prop.getAnnotation(UniqueIndex.class);
//		if (unique != null) {
//			Map<String, Object> params = new HashMap<>();
//			String[] fieldNames = unique.value();
//			for (String fieldName : fieldNames) {
//				if (StringUtils.isBlank(fieldName)) {
//					fieldName = prop.getName();
//				}
//				Object fieldValue = ObjectUtils.readField(insertOrUpdate, fieldName);
//				if (fieldValue == null) {
//					fieldValue = ObjectUtils.readField(token, fieldName);
//				}
//				params.put(fieldName, fieldValue);
//			}
//			Object fieldValue = ObjectUtils.readField(insertOrUpdate, prop.getField());
//			if (fieldValue == null) {
//				return Mono.error(CustomException.bq(prop.getName() + " must not be null "));
//			}
//			if (sqlDeleted) {
//				params.put(SuperEntity.DELETED_COLUMN, Arrays.asList(true, false));
//			}
//			return find8Map(insertOrUpdate.getClass(), params).flatMap(list -> {
//				if (update) {
//					if (list.size() > 0) {
//						Object idValue = ObjectUtils.readField(list.get(0), idProp.getField());
//						if (!idValue.equals(id)) {
//							return Mono.error(
//									CustomException.bq(prop.getName() + " [" + fieldValue + "] already exists "));
//						}
//					}
//				} else {
//					if (list.size() > 0) {
//						return Mono.error(CustomException.bq(prop.getName() + " [" + fieldValue + "] already exists "));
//					}
//				}
//				return Mono.just(true);
//			});
//		}
//		return Mono.just(true);
//
//	}

	@Override
	public Mono<Response<Integer>> deleteById(Class<?> clazz, String id) {
		return removeById(clazz, id).flatMap(r -> {
			if (r > 0) {
				return ResponseMono.ok(r, "删除成功");
			} else {
				return Mono.error(CustomException.er("删除失败"));
			}
		});
	}

	@Override
	public <T> Mono<Response<Integer>> deleteByIds(Class<T> clazz, List<Serializable> ids) {
		return removeByIds(clazz, ids).flatMap(size -> ResponseMono.ok(size));
	}

	@Override
	public <T> Mono<Response<List<T>>> findAllResponse(Class<T> clazz) {
		return findAll(clazz).flatMap(list -> ResponseMono.ok(list));
	}

	@Override
	public <T> Mono<Response<T>> findResponse(Class<T> clazz, String id) {
		return findById(clazz, id)//
				.flatMap(r -> ResponseMono.ok(r))//
				.defaultIfEmpty(Response.bq("找不到数据"));
	}

	@Override
	public <T> Mono<Response<List<T>>> findResponse(T entity) {
		return find(entity).flatMap(data -> ResponseMono.ok(data));
	}

	@Override
	public <T> Mono<Response<T>> insertResponse(T entity) {
		return insert(entity)//
				.flatMap(r -> ResponseMono.ok(r))//
				.defaultIfEmpty(Response.er("添加数据响应空值"));
	}

	@Override
	public <T> Mono<Response<T>> updateResponse(T entity) {
		return update(entity)//
				.flatMap(r -> ResponseMono.ok(r))//
				.defaultIfEmpty(Response.er("添加数据响应空值"));
	}

}
