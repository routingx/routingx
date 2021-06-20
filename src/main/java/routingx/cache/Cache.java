package routingx.cache;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import routingx.Note;
import routingx.ThreadExecutor;

@Setter
@Getter
@Slf4j
public class Cache<K, V> {

	private final Map<K, CacheValue> cache = new LinkedHashMap<>();

	private Loading<K, V> loading;

	@Note("过期时长")
	private long expireAfter;

	private Long lastClearTime = System.currentTimeMillis();

	public Cache(long expireAfterMillis) {
		this.expireAfter = expireAfterMillis;
	}

	public long getExpireAfter() {
		return expireAfter;
	}

	public void setExpireAfter(long expireAfter) {
		this.expireAfter = expireAfter;
	}

	public void remove(K key) {
		cache.remove(key);
	}

	public Mono<V> get(K key, V defaultValue) {
		final CacheValue cacheValue = cache.get(key);
		clear();
		if (cacheValue != null) {
			if (cacheValue.isExpired()) {
				return loading.load(key, defaultValue).map(v -> {
					if (v != null) {
						cacheValue.setValue(v);
					}
					return v;
				});
			}
			return Mono.justOrEmpty(cacheValue.value);
		} else {
			final CacheValue cv = new CacheValue();
			return loading.load(key, defaultValue).map(v -> {
				if (v != null) {
					cv.setValue(v);
					cache.put(key, cv);
				}
				return v;
			});
		}
	}

	public Mono<V> get(K key) {
		return get(key, null);
	}

	private void clearRun() {
		try {
			synchronized (lastClearTime) {
				if (System.currentTimeMillis() - lastClearTime >= expireAfter) {
					lastClearTime = System.currentTimeMillis();
				} else {
					return;
				}
			}
			List<K> keys = new ArrayList<>(cache.keySet());
			for (K key : keys) {
				CacheValue cv = cache.get(key);
				if (cv == null || cv.isExpired()) {
					cache.remove(key);
				}
			}
		} catch (Throwable ex) {
			log.warn(ex.getMessage());
		}
	}

	private void clear() {
		if (System.currentTimeMillis() - lastClearTime >= expireAfter) {
			ThreadExecutor.execute(this::clearRun);
		}
	}

	public interface Loading<K, V> extends EventListener {
		Mono<V> load(K key, V defaultValue);
	}

	class CacheValue {

		private V value;

		private long valueTime;

		public CacheValue() {
		}

		public void setValue(V value) {
			this.value = value;
			this.valueTime = System.currentTimeMillis();
		}

		boolean isExpired() {
			return System.currentTimeMillis() - valueTime >= expireAfter;
		}
	}
}
