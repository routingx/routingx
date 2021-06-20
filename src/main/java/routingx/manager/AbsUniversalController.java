package routingx.manager;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import reactor.core.publisher.Mono;
import routingx.Response;
import routingx.json.JSON;
import routingx.utils.GenericUtils;
import routingx.webflux.WebAbsContext;

public abstract class AbsUniversalController extends WebAbsContext {

	@Resource
	private UniversalManager access;

	protected UniversalManager access() {
		return access;
	}

	@PostMapping(name = "批量删除", value = "/delete")
	public Mono<Response<Integer>> deleteByIds(@PathVariable String className, @RequestBody List<Serializable> ids) {
		Class<?> clazz = GenericUtils.getUniversalClass(className);
		return access().deleteByIds(clazz, ids);
	}

	@GetMapping(name = "删除", value = "/delete/{id}")
	public Mono<Response<Integer>> deleteById(@PathVariable String className, @PathVariable String id) {
		Class<?> clazz = GenericUtils.getUniversalClass(className);
		return access().deleteById(clazz, id);
	}

	@PostMapping(name = "添加", value = "/insert")
	public Mono<Response<Object>> insert(@PathVariable String className, @RequestBody Map<String, Object> map) {
		Class<?> clazz = GenericUtils.getUniversalClass(className);
		Object entity = JSON.parseObject(map, clazz);
		return access().insertResponse(entity);
	}

	@PostMapping(name = "修改", value = "/update")
	public Mono<Response<Object>> update(@PathVariable String className, @RequestBody Map<String, Object> map) {
		Class<?> clazz = GenericUtils.getUniversalClass(className);
		Object entity = JSON.parseObject(map, clazz);
		return access().updateResponse(entity);
	}

	@GetMapping(name = "查询对象", value = "/find/{id}")
	public Mono<?> find(@PathVariable String className, @PathVariable String id) {
		Class<?> clazz = GenericUtils.getUniversalClass(className);
		return access().findResponse(clazz, id);
	}

	@GetMapping(name = "查询所有", value = "/find-all")
	public <T> Mono<?> findAll(@PathVariable String className) {
		Class<?> clazz = GenericUtils.getUniversalClass(className);
		return access().findAll(clazz);
	}

	@PostMapping(name = "查询列表", value = "/find")
	public Mono<?> find(@PathVariable String className, @RequestBody Map<String, Object> map) {
		Class<?> clazz = GenericUtils.getUniversalClass(className);
		Object entity = JSON.parseObject(map, clazz);
		return access().findResponse(entity);
	}

	@PostMapping(name = "查询分页", value = "/page")
	public Mono<?> page(@PathVariable String className, @RequestBody Map<String, Object> map) {
		Class<?> clazz = GenericUtils.getUniversalClass(className);
		Object entity = JSON.parseObject(map, clazz);
		return access().page(entity);
	}
}
