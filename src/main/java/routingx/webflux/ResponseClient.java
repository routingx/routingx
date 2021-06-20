package routingx.webflux;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import routingx.Response;
import routingx.json.JSON;
import routingx.model.Token;

public class ResponseClient extends RestTemplate {

	private final RestTemplate restTemplate;

	public ResponseClient(final RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public <V> Response<V> get(final String url, Class<V> responseType, final Object... uriVariables) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			SimpleExchangeContext.get().doOnNext(ctx -> {
				Token user = ctx.getToken();
				if (user != null) {
					headers.add(Token.ACCESS, user.getAccess());
					headers.add(Token.REFRESH, user.getRefresh());
				}
			}).subscribe();
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			String json = result.getBody();
			// String json = restTemplate.getForObject(url, String.class, uriVariables);
			return toJavaObject(responseType, json);
		} catch (Throwable ex) {
			return Response.er(ex.getMessage(), ex);
		}
	}

	public <V> Response<List<V>> getForList(final String url, Class<V> responseType, final Object... uriVariables) {
		try {
			String json = restTemplate.getForObject(url, String.class, uriVariables);
			return toJavaList(responseType, json);
		} catch (Throwable ex) {
			return Response.er(ex.getMessage(), ex);
		}
	}

	public <V> Response<V> post(final String url, final Object request, Class<V> responseType,
			final Object... uriVariables) {
		try {
			String json = restTemplate.postForObject(url, request, String.class, uriVariables);
			return toJavaObject(responseType, json);
		} catch (Throwable ex) {
			return Response.er(ex.getMessage(), ex);
		}
	}

	public <V> Response<List<V>> postForList(final String url, final Object request, Class<V> responseType,
			final Object... uriVariables) {
		try {
			String json = restTemplate.postForObject(url, request, String.class, uriVariables);
			return toJavaList(responseType, json);
		} catch (Throwable ex) {
			return Response.er(ex.getMessage(), ex);
		}
	}

	@SuppressWarnings("unchecked")
	private <V> Response<V> toJavaObject(Class<V> responseType, String json) {
		Response<V> res = JSON.parseObject(json, Response.class);
		if (res.getData() != null && res.getData().getClass().equals(responseType)) {
			return res;
		} else if (res.getData() != null) {
			res.setData(JSON.parseObject(res.getData(), responseType));
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	private <V> Response<List<V>> toJavaList(Class<V> responseType, String json) {
		Response<List<V>> res = JSON.parseObject(json, Response.class);
		if (res.getData() != null) {
			res.setData(JSON.parseList(res.getData(), responseType));
		}
		return res;
	}
}
