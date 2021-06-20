package routingx.manager;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiOperation;
import reactor.core.publisher.Mono;
import routingx.Response;
import routingx.utils.GenericUtils;
import routingx.webflux.WebAbsContext;

public abstract class AbsGenericController<T, ID extends Serializable> extends WebAbsContext {
	@SuppressWarnings("unchecked")
	private final Class<T> clazz = (Class<T>) GenericUtils.getParameterizedType(this.getClass());

	@Autowired
	private ResponseManager access;

	private ResponseManager access() {
		return access;
	}

	public AbsGenericController() {
	}

//	public AbsGenericController(GenericManager<T, ID> access) {
//		this.access = access;
//	}

//	public Mono<Boolean> verifyInsert(Object insert) {
//		return access().insertVerify(insert);
//	}
//
//	public Mono<Boolean> verifyUpdate(Object update) {
//		return access().updateVerify(update);
//	}

	@ApiOperation(value = "批量删除")
	@PostMapping(name = "批量删除", value = "/delete")
	public Mono<Response<Integer>> deleteByIds(@RequestBody List<Serializable> ids) {
		return access().deleteByIds(clazz, ids);
	}

	@ApiOperation(value = "删除")
	@GetMapping(name = "删除", value = "/delete/{id}")
	public Mono<Response<Integer>> deleteById(@PathVariable String id) {
		return access().findById(clazz, id).flatMap(entity -> {
			return access().deleteById(clazz, id);
		}).defaultIfEmpty(Response.bq("找不到要删除的数据"));
	}

	@ApiOperation(value = "添加")
	@PostMapping(name = "添加", value = "/insert")
	public Mono<Response<T>> insert(@RequestBody T entity) {
		return access().insertResponse(entity);

	}

	@ApiOperation(value = "修改")
	@PostMapping(name = "修改", value = "/update")
	public Mono<Response<T>> update(@RequestBody T entity) {
		return access().updateResponse(entity);
	}

	@ApiOperation(value = "查询所有")
	@GetMapping(name = "查询所有", value = "/find-all")
	public Mono<Response<List<T>>> findAll() {
		return access().findAllResponse(clazz);
	}

	@ApiOperation(value = "对象明细")
	@GetMapping(name = "对象明细", value = "/find/{id}")
	public Mono<Response<T>> find(@PathVariable String id) {
		return access().findResponse(clazz, id);
	}

	@ApiOperation(value = "查询列表")
	@PostMapping(name = "查询列表", value = "/find")
	public Mono<Response<List<T>>> find(@RequestBody T entity) {
		return access().findResponse(entity);
	}

	@ApiOperation(value = "查询分页")
	@PostMapping(name = "查询分页", value = "/page")
	public Mono<Response<List<T>>> page(@RequestBody T entity) {
		return access().page(entity);
	}

}
