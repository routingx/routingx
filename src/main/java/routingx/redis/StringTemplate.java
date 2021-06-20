package routingx.redis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.extern.slf4j.Slf4j;
import routingx.CustomException;
import routingx.json.JSON;

@Slf4j
public class StringTemplate extends StringRedisTemplate {

	public StringTemplate(RedisConnectionFactory connectionFactory) {
		super(connectionFactory);
	}

	public String get(String key) {
		return opsForValue().get(key);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key, Class<T> claxx) {
		String json = opsForValue().get(key);
		if (claxx.equals(String.class)) {
			return (T) json;
		}
		if (StringUtils.isNotBlank(json)) {
			T o = JSON.parseObject(json, claxx);
			return o;
		}
		return null;
	}

	public <T> List<T> getList(String key, Class<T> claxx) {
		String json = opsForValue().get(key);
		if (StringUtils.isNotBlank(json)) {
			List<T> list = JSON.parseList(json, claxx);
			return list;
		} else {
			return new ArrayList<>();
		}
	}

	public <T> void set(String key, T value) {
		if (value != null && value.getClass().equals(String.class)) {
			opsForValue().set(key, (String) value);
		} else {
			opsForValue().set(key, JSON.toJSONString(value));
		}
	}

	public long getExpireSeconds(String key) {
		long se = getExpire(key, TimeUnit.SECONDS);
		return se;
	}

	public boolean setNX(String key, String value) {
		try {
			if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
				log.warn("key , value不能为空 - KEY:" + key + " VALUE:" + value);
				return false;
			}
			RedisCallback<Boolean> callback = new RedisCallback<Boolean>() {
				public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
					byte[] k = getStringSerializer().serialize(key);
					byte[] v = getStringSerializer().serialize(value);
					return redisConnection.setNX(k, v);
				}
			};
			boolean result = this.execute(callback);
			return result;
		} catch (Throwable e) {
			log.error("set nx KEY:" + key + " VALUE:" + value, e);
			return false;
		}
	}

	public boolean setNX(String key, String value, int seconds) {
		try {
			if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
				log.warn("key , value不能为空 - KEY:" + key + " VALUE:" + value);
				return false;
			}
			synchronized (key) {
				RedisCallback<Boolean> callback = new RedisCallback<Boolean>() {
					public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
						byte[] k = getStringSerializer().serialize(key);
						byte[] v = getStringSerializer().serialize(value);
						Boolean b = redisConnection.setNX(k, v);
						if (b && seconds > 0) {
							boolean exB = expire(key, seconds, TimeUnit.SECONDS);
							if (!exB) {
								delete(key);
								b = false;
							}
						}
						return b;
					}
				};
				boolean result = this.execute(callback);
				return result;
			}
		} catch (Throwable e) {
			log.error("set nx KEY:" + key + " VALUE:" + value, e);
			return false;
		}
	}

	public <T> void setex(String key, long expTimeSeconds, T value) {
		if (value != null && value.getClass().equals(String.class)) {
			opsForValue().set(key, (String) value, expTimeSeconds, TimeUnit.SECONDS);
		} else {
			opsForValue().set(key, JSON.toJSONString(value), expTimeSeconds, TimeUnit.SECONDS);
		}
	}

	public void delete(String... key) {
		if (key != null && key.length > 0) {
			if (key.length == 1) {
				delete(key[0]);
			} else {
				delete(Arrays.asList(key));
			}
		}
	}

	public <T> void hdel(String key, T value) {
		opsForHash().delete(key, value);
	}

	public boolean hset(String key, String item, Object value) {
		try {
			opsForHash().put(key, item, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public Object hget(String mastkey, String itemkey) {
		try {
			return opsForHash().get(mastkey, itemkey);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<String> range(String key, long start, long end) {
		return opsForList().range(key, start, end);
	}

	public <T> List<T> range(String key, Class<T> clazz) {
		List<T> clazzList = new ArrayList<>();
		List<String> jsons = opsForList().range(key, 0, opsForList().size(key));
		for (String text : jsons) {
			clazzList.add(JSON.parseObject(text, clazz));
		}
		return clazzList;
	}

	public <T> void lpush(String key, T value) {
		if (value != null && value.getClass().equals(String.class)) {
			opsForList().leftPush(key, (String) value);
		} else {
			opsForList().leftPush(key, JSON.toJSONString(value));
		}
	}

	/**
	 * 分布式加锁
	 *
	 * @param key          锁key
	 * @param milliSeconds 超时的时间戳
	 * @return boolean
	 */

	public boolean lock(final String key, final long milliSeconds) {
		try {
			if (StringUtils.isEmpty(key) || milliSeconds <= 0) {
				throw CustomException.bq("key不能为空" + " expires:" + milliSeconds);
			}
			RedisCallback<Boolean> callback = new RedisCallback<Boolean>() {
				public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
					byte[] k = getStringSerializer().serialize(key);
					byte[] v = getStringSerializer().serialize("OK");
					Boolean setNX = redisConnection.setNX(k, v);
					if (setNX) {
						boolean expire = expire(key, milliSeconds, TimeUnit.MILLISECONDS);
						if (!expire) {
							delete(key);
							setNX = false;
						}
					}
					return setNX;
				}
			};
			return execute(callback);
		} catch (Exception e) {
			log.error("分布式加锁异常：" + key, e);
		}
		return false;
	}

	/**
	 * 分布式解锁
	 *
	 * @param key 锁key
	 */

	public boolean unlock(String key) {
		boolean retFlag = false;
		try {
			RedisCallback<Boolean> callback = new RedisCallback<Boolean>() {
				public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
					byte[] lockKeyBytes = getStringSerializer().serialize(key);
					String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
					byte[] scriptBytes = getStringSerializer().serialize(script);
					Long result = redisConnection.eval(scriptBytes, ReturnType.INTEGER, 1, lockKeyBytes);
					return (result != null && result == 1L);
				}
			};
			return execute(callback);
		} catch (Exception e) {
			log.error("分布式解锁异常：" + key, e);
		}
		return retFlag;
	}

	public Long increment(String key) {
		try {
			return opsForValue().increment(key);
		} catch (Throwable ex) {
			log.error(ex + " " + ex.getMessage());
			return RandomUtils.nextLong(100, 1000000);
		}
	}

}