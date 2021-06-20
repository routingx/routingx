package routingx.redis;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.BatchOptions;
import org.redisson.api.ExecutorOptions;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.LockOptions.BackOff;
import org.redisson.api.MapOptions;
import org.redisson.api.RAtomicDouble;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBatch;
import org.redisson.api.RBinaryStream;
import org.redisson.api.RBitSet;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBoundedBlockingQueue;
import org.redisson.api.RBucket;
import org.redisson.api.RBuckets;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RDeque;
import org.redisson.api.RDoubleAdder;
import org.redisson.api.RGeo;
import org.redisson.api.RHyperLogLog;
import org.redisson.api.RIdGenerator;
import org.redisson.api.RKeys;
import org.redisson.api.RLexSortedSet;
import org.redisson.api.RList;
import org.redisson.api.RListMultimap;
import org.redisson.api.RListMultimapCache;
import org.redisson.api.RLiveObjectService;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RLock;
import org.redisson.api.RLongAdder;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RPatternTopic;
import org.redisson.api.RPermitExpirableSemaphore;
import org.redisson.api.RPriorityBlockingDeque;
import org.redisson.api.RPriorityBlockingQueue;
import org.redisson.api.RPriorityDeque;
import org.redisson.api.RPriorityQueue;
import org.redisson.api.RQueue;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RReliableTopic;
import org.redisson.api.RRemoteService;
import org.redisson.api.RRingBuffer;
import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScript;
import org.redisson.api.RSemaphore;
import org.redisson.api.RSet;
import org.redisson.api.RSetCache;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RSetMultimapCache;
import org.redisson.api.RSortedSet;
import org.redisson.api.RStream;
import org.redisson.api.RTimeSeries;
import org.redisson.api.RTopic;
import org.redisson.api.RTransaction;
import org.redisson.api.RTransferQueue;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.RedissonRxClient;
import org.redisson.api.TransactionOptions;
import org.redisson.api.redisnode.BaseRedisNodes;
import org.redisson.api.redisnode.RedisNodes;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Sentinel;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ReflectionUtils;

public class Redisson implements RedissonClient {

	private static final String REDIS_PROTOCOL_PREFIX = "redis://";
	private static final String REDISS_PROTOCOL_PREFIX = "rediss://";

	private final RedissonProperties redissonProperties;

	private final RedisProperties redisProperties;

	private RedissonClient delegate;

	private RedissonReactiveClient reactiveClient;

	private RedissonConnectionFactory connectionFactory;

	private StringTemplate stringRedisTemplate;

	private RedisTemplate<Object, Object> redisTemplate;

	public Redisson(RedissonProperties redissonProperties, RedisProperties redisProperties) {
		this.redissonProperties = redissonProperties;
		this.redisProperties = redisProperties;
	}

	public void setConfig(String configInfo) {
		Config config = this.config(configInfo);
		if (delegate != null) {
			delegate.shutdown();
		}
		delegate = org.redisson.Redisson.create(config);
		reactiveClient = delegate.reactive();
		connectionFactory = new RedissonConnectionFactory(delegate);
		if (stringRedisTemplate == null) {
			stringRedisTemplate = new StringTemplate(getConnectionFactory());
		}
		stringRedisTemplate.setConnectionFactory(connectionFactory);
		if (redisTemplate == null) {
			redisTemplate = new RedisTemplate<Object, Object>();
		}
		redisTemplate.setConnectionFactory(connectionFactory);
	}

	@Override
	public RedissonReactiveClient reactive() {
		return reactiveClient;
	}

	public RedissonConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public RedisTemplate<Object, Object> redisTemplate() {
		if (redisTemplate == null) {
			redisTemplate = new RedisTemplate<Object, Object>();
			redisTemplate.setConnectionFactory(getConnectionFactory());
		}
		return redisTemplate;
	}

	public StringTemplate stringRedisTemplate() {
		if (stringRedisTemplate == null) {
			stringRedisTemplate = new StringTemplate(getConnectionFactory());
			stringRedisTemplate.setConnectionFactory(getConnectionFactory());
		}
		return stringRedisTemplate;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	private Config config(String configInfo) {
		if (StringUtils.isNotBlank(configInfo)) {
			redissonProperties.setConfig(configInfo);
		}
		Config config = null;
		Method clusterMethod = ReflectionUtils.findMethod(RedisProperties.class, "getCluster");
		Method timeoutMethod = ReflectionUtils.findMethod(RedisProperties.class, "getTimeout");
		Object timeoutValue = ReflectionUtils.invokeMethod(timeoutMethod, redisProperties);
		int timeout;
		if (null == timeoutValue) {
			timeout = 10000;
		} else if (!(timeoutValue instanceof Integer)) {
			Method millisMethod = ReflectionUtils.findMethod(timeoutValue.getClass(), "toMillis");
			timeout = ((Long) ReflectionUtils.invokeMethod(millisMethod, timeoutValue)).intValue();
		} else {
			timeout = (Integer) timeoutValue;
		}

		if (redissonProperties.getConfig() != null) {
			try {
				config = Config.fromYAML(redissonProperties.getConfig());
			} catch (IOException e) {
				try {
					config = Config.fromJSON(redissonProperties.getConfig());
				} catch (IOException e1) {
					throw new IllegalArgumentException("Can't parse config", e1);
				}
			}
		} else if (redisProperties.getSentinel() != null) {
			Method nodesMethod = ReflectionUtils.findMethod(Sentinel.class, "getNodes");
			Object nodesValue = ReflectionUtils.invokeMethod(nodesMethod, redisProperties.getSentinel());

			String[] nodes;
			if (nodesValue instanceof String) {
				nodes = convert(Arrays.asList(((String) nodesValue).split(",")));
			} else {
				nodes = convert((List<String>) nodesValue);
			}

			config = new Config();
			config.useSentinelServers().setMasterName(redisProperties.getSentinel().getMaster())
					.addSentinelAddress(nodes).setDatabase(redisProperties.getDatabase()).setConnectTimeout(timeout)
					.setPassword(redisProperties.getPassword());
		} else if (clusterMethod != null && ReflectionUtils.invokeMethod(clusterMethod, redisProperties) != null) {
			Object clusterObject = ReflectionUtils.invokeMethod(clusterMethod, redisProperties);
			Method nodesMethod = ReflectionUtils.findMethod(clusterObject.getClass(), "getNodes");
			List<String> nodesObject = (List<String>) ReflectionUtils.invokeMethod(nodesMethod, clusterObject);
			String[] nodes = convert(nodesObject);

			config = new Config();
			config.useClusterServers().addNodeAddress(nodes).setConnectTimeout(timeout)
					.setPassword(redisProperties.getPassword());
		} else {
			config = new Config();
			String prefix = REDIS_PROTOCOL_PREFIX;
			Method method = ReflectionUtils.findMethod(RedisProperties.class, "isSsl");
			if (method != null && (Boolean) ReflectionUtils.invokeMethod(method, redisProperties)) {
				prefix = REDISS_PROTOCOL_PREFIX;
			}

			config.useSingleServer().setAddress(prefix + redisProperties.getHost() + ":" + redisProperties.getPort())
					.setConnectTimeout(timeout).setDatabase(redisProperties.getDatabase())
					.setPassword(redisProperties.getPassword());
		}
		return config;
	}

	private String[] convert(List<String> nodesObject) {
		List<String> nodes = new ArrayList<String>(nodesObject.size());
		for (String node : nodesObject) {
			if (!node.startsWith(REDIS_PROTOCOL_PREFIX) && !node.startsWith(REDISS_PROTOCOL_PREFIX)) {
				nodes.add(REDIS_PROTOCOL_PREFIX + node);
			} else {
				nodes.add(node);
			}
		}
		return nodes.toArray(new String[nodes.size()]);
	}

	@Override
	public <V> RTimeSeries<V> getTimeSeries(String name) {
		return delegate.getTimeSeries(name);
	}

	@Override
	public <V> RTimeSeries<V> getTimeSeries(String name, Codec codec) {
		return delegate.getTimeSeries(name, codec);
	}

	@Override
	public <K, V> RStream<K, V> getStream(String name) {
		return delegate.getStream(name);
	}

	@Override
	public <K, V> RStream<K, V> getStream(String name, Codec codec) {
		return delegate.getStream(name, codec);
	}

	@Override
	public RRateLimiter getRateLimiter(String name) {
		return delegate.getRateLimiter(name);
	}

	@Override
	public RBinaryStream getBinaryStream(String name) {
		return delegate.getBinaryStream(name);
	}

	@Override
	public <V> RGeo<V> getGeo(String name) {
		return delegate.getGeo(name);
	}

	@Override
	public <V> RGeo<V> getGeo(String name, Codec codec) {
		return delegate.getGeo(name, codec);
	}

	@Override
	public <V> RSetCache<V> getSetCache(String name) {
		return delegate.getSetCache(name);
	}

	@Override
	public <V> RSetCache<V> getSetCache(String name, Codec codec) {
		return delegate.getSetCache(name, codec);
	}

	@Override
	public <K, V> RMapCache<K, V> getMapCache(String name, Codec codec) {
		return delegate.getMapCache(name);
	}

	@Override
	public <K, V> RMapCache<K, V> getMapCache(String name, Codec codec, MapOptions<K, V> options) {
		return delegate.getMapCache(name, codec, options);
	}

	@Override
	public <K, V> RMapCache<K, V> getMapCache(String name) {
		return delegate.getMapCache(name);
	}

	@Override
	public <K, V> RMapCache<K, V> getMapCache(String name, MapOptions<K, V> options) {
		return delegate.getMapCache(name, options);
	}

	@Override
	public <V> RBucket<V> getBucket(String name) {
		return delegate.getBucket(name);
	}

	@Override
	public <V> RBucket<V> getBucket(String name, Codec codec) {
		return delegate.getBucket(name, codec);
	}

	@Override
	public RBuckets getBuckets() {
		return delegate.getBuckets();
	}

	@Override
	public RBuckets getBuckets(Codec codec) {
		return delegate.getBuckets(codec);
	}

	@Override
	public <V> RHyperLogLog<V> getHyperLogLog(String name) {
		return delegate.getHyperLogLog(name);
	}

	@Override
	public <V> RHyperLogLog<V> getHyperLogLog(String name, Codec codec) {
		return delegate.getHyperLogLog(name, codec);
	}

	@Override
	public <V> RList<V> getList(String name) {
		return delegate.getList(name);
	}

	@Override
	public <V> RList<V> getList(String name, Codec codec) {
		return delegate.getList(name, codec);
	}

	@Override
	public <K, V> RListMultimap<K, V> getListMultimap(String name) {
		return delegate.getListMultimap(name);
	}

	@Override
	public <K, V> RListMultimap<K, V> getListMultimap(String name, Codec codec) {
		return delegate.getListMultimap(name, codec);
	}

	@Override
	public <K, V> RListMultimapCache<K, V> getListMultimapCache(String name) {
		return delegate.getListMultimapCache(name);
	}

	@Override
	public <K, V> RListMultimapCache<K, V> getListMultimapCache(String name, Codec codec) {
		return delegate.getListMultimapCache(name, codec);
	}

	@Override
	public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, LocalCachedMapOptions<K, V> options) {
		return delegate.getLocalCachedMap(name, options);
	}

	@Override
	public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, Codec codec,
			LocalCachedMapOptions<K, V> options) {
		return delegate.getLocalCachedMap(name, options);
	}

	@Override
	public <K, V> RMap<K, V> getMap(String name) {
		return delegate.getMap(name);
	}

	@Override
	public <K, V> RMap<K, V> getMap(String name, MapOptions<K, V> options) {
		return delegate.getMap(name, options);
	}

	@Override
	public <K, V> RMap<K, V> getMap(String name, Codec codec) {
		return delegate.getMap(name, codec);
	}

	@Override
	public <K, V> RMap<K, V> getMap(String name, Codec codec, MapOptions<K, V> options) {
		return delegate.getMap(name, codec, options);
	}

	@Override
	public <K, V> RSetMultimap<K, V> getSetMultimap(String name) {
		return delegate.getSetMultimap(name);
	}

	@Override
	public <K, V> RSetMultimap<K, V> getSetMultimap(String name, Codec codec) {
		return delegate.getSetMultimap(name, codec);
	}

	@Override
	public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(String name) {
		return delegate.getSetMultimapCache(name);
	}

	@Override
	public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(String name, Codec codec) {
		return delegate.getSetMultimapCache(name, codec);
	}

	@Override
	public RSemaphore getSemaphore(String name) {
		return delegate.getSemaphore(name);
	}

	@Override
	public RPermitExpirableSemaphore getPermitExpirableSemaphore(String name) {
		return delegate.getPermitExpirableSemaphore(name);
	}

	@Override
	public RLock getLock(String name) {
		return delegate.getLock(name);
	}

	@Override
	public RLock getMultiLock(RLock... locks) {
		return delegate.getMultiLock(locks);
	}

	@Deprecated
	@Override
	public RLock getRedLock(RLock... locks) {
		return delegate.getRedLock(locks);
	}

	@Override
	public RLock getFairLock(String name) {
		return delegate.getFairLock(name);
	}

	@Override
	public RReadWriteLock getReadWriteLock(String name) {
		return delegate.getReadWriteLock(name);
	}

	@Override
	public <V> RSet<V> getSet(String name) {
		return delegate.getSet(name);
	}

	@Override
	public <V> RSet<V> getSet(String name, Codec codec) {
		return delegate.getSet(name);
	}

	@Override
	public <V> RSortedSet<V> getSortedSet(String name) {
		return delegate.getSortedSet(name);
	}

	@Override
	public <V> RSortedSet<V> getSortedSet(String name, Codec codec) {
		return delegate.getSortedSet(name, codec);
	}

	@Override
	public <V> RScoredSortedSet<V> getScoredSortedSet(String name) {
		return delegate.getScoredSortedSet(name);
	}

	@Override
	public <V> RScoredSortedSet<V> getScoredSortedSet(String name, Codec codec) {
		return delegate.getScoredSortedSet(name, codec);
	}

	@Override
	public RLexSortedSet getLexSortedSet(String name) {
		return delegate.getLexSortedSet(name);
	}

	@Override
	public RTopic getTopic(String name) {
		return delegate.getTopic(name);
	}

	@Override
	public RTopic getTopic(String name, Codec codec) {
		return delegate.getTopic(name, codec);
	}

	@Override
	public RReliableTopic getReliableTopic(String name) {
		return delegate.getReliableTopic(name);
	}

	@Override
	public RReliableTopic getReliableTopic(String name, Codec codec) {
		return delegate.getReliableTopic(name, codec);
	}

	@Override
	public RPatternTopic getPatternTopic(String pattern) {
		return delegate.getPatternTopic(pattern);
	}

	@Override
	public RPatternTopic getPatternTopic(String pattern, Codec codec) {
		return delegate.getPatternTopic(pattern, codec);
	}

	@Override
	public <V> RQueue<V> getQueue(String name) {
		return delegate.getQueue(name);
	}

	@Override
	public <V> RTransferQueue<V> getTransferQueue(String name) {
		return delegate.getTransferQueue(name);
	}

	@Override
	public <V> RTransferQueue<V> getTransferQueue(String name, Codec codec) {
		return delegate.getTransferQueue(name, codec);
	}

	@Override
	public <V> RDelayedQueue<V> getDelayedQueue(RQueue<V> destinationQueue) {
		return delegate.getDelayedQueue(destinationQueue);
	}

	@Override
	public <V> RQueue<V> getQueue(String name, Codec codec) {
		return delegate.getQueue(name, codec);
	}

	@Override
	public <V> RRingBuffer<V> getRingBuffer(String name) {
		return delegate.getRingBuffer(name);
	}

	@Override
	public <V> RRingBuffer<V> getRingBuffer(String name, Codec codec) {
		return delegate.getRingBuffer(name, codec);
	}

	@Override
	public <V> RPriorityQueue<V> getPriorityQueue(String name) {
		return delegate.getPriorityQueue(name);
	}

	@Override
	public <V> RPriorityQueue<V> getPriorityQueue(String name, Codec codec) {
		return delegate.getPriorityQueue(name, codec);
	}

	@Override
	public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(String name) {
		return delegate.getPriorityBlockingQueue(name);
	}

	@Override
	public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(String name, Codec codec) {
		return delegate.getPriorityBlockingQueue(name, codec);
	}

	@Override
	public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(String name) {
		return delegate.getPriorityBlockingDeque(name);
	}

	@Override
	public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(String name, Codec codec) {
		return delegate.getPriorityBlockingDeque(name, codec);
	}

	@Override
	public <V> RPriorityDeque<V> getPriorityDeque(String name) {
		return delegate.getPriorityDeque(name);
	}

	@Override
	public <V> RPriorityDeque<V> getPriorityDeque(String name, Codec codec) {
		return delegate.getPriorityDeque(name, codec);
	}

	@Override
	public <V> RBlockingQueue<V> getBlockingQueue(String name) {
		return delegate.getBlockingQueue(name);
	}

	@Override
	public <V> RBlockingQueue<V> getBlockingQueue(String name, Codec codec) {
		return delegate.getBlockingQueue(name, codec);
	}

	@Override
	public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String name) {
		return delegate.getBoundedBlockingQueue(name);
	}

	@Override
	public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String name, Codec codec) {
		return delegate.getBoundedBlockingQueue(name, codec);
	}

	@Override
	public <V> RDeque<V> getDeque(String name) {
		return delegate.getDeque(name);
	}

	@Override
	public <V> RDeque<V> getDeque(String name, Codec codec) {
		return delegate.getDeque(name, codec);
	}

	@Override
	public <V> RBlockingDeque<V> getBlockingDeque(String name) {
		return delegate.getBlockingDeque(name);
	}

	@Override
	public <V> RBlockingDeque<V> getBlockingDeque(String name, Codec codec) {
		return delegate.getBlockingDeque(name, codec);
	}

	@Override
	public RAtomicLong getAtomicLong(String name) {
		return delegate.getAtomicLong(name);
	}

	@Override
	public RAtomicDouble getAtomicDouble(String name) {
		return delegate.getAtomicDouble(name);
	}

	@Override
	public RLongAdder getLongAdder(String name) {
		return delegate.getLongAdder(name);
	}

	@Override
	public RDoubleAdder getDoubleAdder(String name) {
		return delegate.getDoubleAdder(name);
	}

	@Override
	public RCountDownLatch getCountDownLatch(String name) {
		return delegate.getCountDownLatch(name);
	}

	@Override
	public RBitSet getBitSet(String name) {
		return delegate.getBitSet(name);
	}

	@Override
	public <V> RBloomFilter<V> getBloomFilter(String name) {
		return delegate.getBloomFilter(name);
	}

	@Override
	public <V> RBloomFilter<V> getBloomFilter(String name, Codec codec) {
		return delegate.getBloomFilter(name);
	}

	@Override
	public RIdGenerator getIdGenerator(String name) {
		return delegate.getIdGenerator(name);
	}

	@Override
	public RScript getScript() {
		return delegate.getScript();
	}

	@Override
	public RScript getScript(Codec codec) {
		return delegate.getScript(codec);
	}

	@Override
	public RScheduledExecutorService getExecutorService(String name) {
		return delegate.getExecutorService(name);
	}

	@Override
	public RScheduledExecutorService getExecutorService(String name, ExecutorOptions options) {
		return delegate.getExecutorService(name, options);
	}

	@Override
	public RScheduledExecutorService getExecutorService(String name, Codec codec) {
		return delegate.getExecutorService(name, codec);
	}

	@Override
	public RScheduledExecutorService getExecutorService(String name, Codec codec, ExecutorOptions options) {
		return delegate.getExecutorService(name, codec, options);
	}

	@Override
	public RRemoteService getRemoteService() {
		return delegate.getRemoteService();
	}

	@Override
	public RRemoteService getRemoteService(Codec codec) {
		return delegate.getRemoteService(codec);
	}

	@Override
	public RRemoteService getRemoteService(String name) {
		return delegate.getRemoteService(name);
	}

	@Override
	public RRemoteService getRemoteService(String name, Codec codec) {
		return delegate.getRemoteService(name, codec);
	}

	@Override
	public RTransaction createTransaction(TransactionOptions options) {
		return delegate.createTransaction(options);
	}

	@Override
	public RBatch createBatch(BatchOptions options) {
		return delegate.createBatch(options);
	}

	@Override
	public RBatch createBatch() {
		return delegate.createBatch();
	}

	@Override
	public RKeys getKeys() {
		return delegate.getKeys();
	}

	@Override
	public RLiveObjectService getLiveObjectService() {
		return delegate.getLiveObjectService();
	}

	@Override
	public void shutdown() {
		if (delegate != null) {
			delegate.shutdown();
		}

	}

	@Override
	public void shutdown(long quietPeriod, long timeout, TimeUnit unit) {
		if (delegate != null) {
			delegate.shutdown(quietPeriod, timeout, unit);
		}
	}

	@Override
	public Config getConfig() {
		return delegate.getConfig();
	}

	@Override
	public <T extends BaseRedisNodes> T getRedisNodes(RedisNodes<T> nodes) {
		return delegate.getRedisNodes(nodes);
	}

	@Deprecated
	@Override
	public org.redisson.api.NodesGroup<org.redisson.api.Node> getNodesGroup() {
		return delegate.getNodesGroup();
	}

	@Deprecated
	@Override
	public org.redisson.api.ClusterNodesGroup getClusterNodesGroup() {
		return delegate.getClusterNodesGroup();
	}

	@Override
	public boolean isShutdown() {
		return delegate.isShutdown();
	}

	@Override
	public boolean isShuttingDown() {
		return delegate.isShuttingDown();
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public RLock getSpinLock(String name) {
		return delegate.getSpinLock(name);
	}

	@Override
	public RLock getSpinLock(String name, BackOff backOff) {
		return delegate.getSpinLock(name, backOff);
	}

	@Override
	public RedissonRxClient rxJava() {
		return delegate.rxJava();
	}

}
