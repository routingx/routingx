package routingx.cache;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import routingx.ThreadExecutor;

@Slf4j
public class Cached<K, V> {

	public interface Loading<K, V> extends EventListener {
		V load(K key);
	}

	private Map<K, CacheValue> cache = new LinkedHashMap<>();

	private final Loading<K, V> loading;

	private final long expireAfter;
	private Long lastClearTime = System.currentTimeMillis();

	public Cached(long expireAfterMillis, Loading<K, V> loading) {
		this.expireAfter = expireAfterMillis;
		this.loading = loading;
	}

	public void remove(K key) {
		cache.remove(key);
	}

	public V get(K key) {
		final CacheValue cacheValue = cache.get(key);
		clear();
		if (cacheValue != null) {
			if (cacheValue.isExpired()) {
				V v = loading.load(key);
				if (v != null) {
					cacheValue.setValue(v);
				}
				return v;
			}
			return cacheValue.value;
		} else {
			final CacheValue cv = new CacheValue();
			V v = loading.load(key);
			if (v != null) {
				cv.setValue(v);
				cache.put(key, cv);
			}
			return v;
		}
	}

	private void clear() {
		if (System.currentTimeMillis() - lastClearTime >= expireAfter) {
			ThreadExecutor.execute(this::clearRun);
		}
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
