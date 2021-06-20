package routingx.redis;

import java.util.List;

import org.redisson.api.BatchOptions;
import org.redisson.api.ClusterNode;
import org.redisson.api.MapOptions;
import org.redisson.api.NodesGroup;
import org.redisson.api.RAtomicDoubleReactive;
import org.redisson.api.RAtomicLongReactive;
import org.redisson.api.RBatchReactive;
import org.redisson.api.RBinaryStreamReactive;
import org.redisson.api.RBitSetReactive;
import org.redisson.api.RBlockingDequeReactive;
import org.redisson.api.RBlockingQueueReactive;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RBucketsReactive;
import org.redisson.api.RCountDownLatchReactive;
import org.redisson.api.RDequeReactive;
import org.redisson.api.RGeoReactive;
import org.redisson.api.RHyperLogLogReactive;
import org.redisson.api.RIdGeneratorReactive;
import org.redisson.api.RKeysReactive;
import org.redisson.api.RLexSortedSetReactive;
import org.redisson.api.RListMultimapReactive;
import org.redisson.api.RListReactive;
import org.redisson.api.RLock;
import org.redisson.api.RLockReactive;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RMapReactive;
import org.redisson.api.RPatternTopicReactive;
import org.redisson.api.RPermitExpirableSemaphoreReactive;
import org.redisson.api.RQueueReactive;
import org.redisson.api.RRateLimiterReactive;
import org.redisson.api.RReadWriteLockReactive;
import org.redisson.api.RReliableTopicReactive;
import org.redisson.api.RRemoteService;
import org.redisson.api.RRingBufferReactive;
import org.redisson.api.RScoredSortedSetReactive;
import org.redisson.api.RScriptReactive;
import org.redisson.api.RSemaphoreReactive;
import org.redisson.api.RSetCacheReactive;
import org.redisson.api.RSetMultimapReactive;
import org.redisson.api.RSetReactive;
import org.redisson.api.RStreamReactive;
import org.redisson.api.RTimeSeriesReactive;
import org.redisson.api.RTopicReactive;
import org.redisson.api.RTransactionReactive;
import org.redisson.api.RTransferQueueReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.TransactionOptions;
import org.redisson.api.LockOptions.BackOff;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("deprecation")
@Setter
@Getter
public class RedissonReactive implements RedissonReactiveClient {

	private RedissonReactiveClient delegate;

	public RedissonReactive() {
	}

	@Override
	public <V> RTimeSeriesReactive<V> getTimeSeries(String name) {
		return delegate.getTimeSeries(name);
	}

	@Override
	public <V> RTimeSeriesReactive<V> getTimeSeries(String name, Codec codec) {
		return delegate.getTimeSeries(name, codec);
	}

	@Override
	public <K, V> RStreamReactive<K, V> getStream(String name) {
		return delegate.getStream(name);
	}

	@Override
	public <K, V> RStreamReactive<K, V> getStream(String name, Codec codec) {
		return delegate.getStream(name, codec);
	}

	@Override
	public RRateLimiterReactive getRateLimiter(String name) {
		return delegate.getRateLimiter(name);
	}

	@Override
	public RBinaryStreamReactive getBinaryStream(String name) {
		return delegate.getBinaryStream(name);
	}

	@Override
	public <V> RGeoReactive<V> getGeo(String name) {
		return delegate.getGeo(name);
	}

	@Override
	public <V> RGeoReactive<V> getGeo(String name, Codec codec) {
		return delegate.getGeo(name, codec);
	}

	@Override
	public <V> RSetCacheReactive<V> getSetCache(String name) {
		return delegate.getSetCache(name);
	}

	@Override
	public <V> RSetCacheReactive<V> getSetCache(String name, Codec codec) {
		return delegate.getSetCache(name, codec);
	}

	@Override
	public <K, V> RMapCacheReactive<K, V> getMapCache(String name, Codec codec) {
		return delegate.getMapCache(name);
	}

	@Override
	public <K, V> RMapCacheReactive<K, V> getMapCache(String name, Codec codec, MapOptions<K, V> options) {
		return delegate.getMapCache(name, codec, options);
	}

	@Override
	public <K, V> RMapCacheReactive<K, V> getMapCache(String name) {
		return delegate.getMapCache(name);
	}

	@Override
	public <K, V> RMapCacheReactive<K, V> getMapCache(String name, MapOptions<K, V> options) {
		return delegate.getMapCache(name, options);
	}

	@Override
	public <V> RBucketReactive<V> getBucket(String name) {
		return delegate.getBucket(name);
	}

	@Override
	public <V> RBucketReactive<V> getBucket(String name, Codec codec) {
		return delegate.getBucket(name, codec);
	}

	@Override
	public RBucketsReactive getBuckets() {
		return delegate.getBuckets();
	}

	@Override
	public RBucketsReactive getBuckets(Codec codec) {
		return delegate.getBuckets(codec);
	}

	@Override
	public <V> RHyperLogLogReactive<V> getHyperLogLog(String name) {
		return delegate.getHyperLogLog(name);
	}

	@Override
	public <V> RHyperLogLogReactive<V> getHyperLogLog(String name, Codec codec) {
		return delegate.getHyperLogLog(name, codec);
	}

	@Override
	public <V> RListReactive<V> getList(String name) {
		return delegate.getList(name);
	}

	@Override
	public <V> RListReactive<V> getList(String name, Codec codec) {
		return delegate.getList(name, codec);
	}

	@Override
	public <K, V> RListMultimapReactive<K, V> getListMultimap(String name) {
		return delegate.getListMultimap(name);
	}

	@Override
	public <K, V> RListMultimapReactive<K, V> getListMultimap(String name, Codec codec) {
		return delegate.getListMultimap(name, codec);
	}

	@Override
	public <K, V> RMapReactive<K, V> getMap(String name) {
		return delegate.getMap(name);
	}

	@Override
	public <K, V> RMapReactive<K, V> getMap(String name, MapOptions<K, V> options) {
		return delegate.getMap(name, options);
	}

	@Override
	public <K, V> RMapReactive<K, V> getMap(String name, Codec codec) {
		return delegate.getMap(name, codec);
	}

	@Override
	public <K, V> RMapReactive<K, V> getMap(String name, Codec codec, MapOptions<K, V> options) {
		return delegate.getMap(name, codec, options);
	}

	@Override
	public <K, V> RSetMultimapReactive<K, V> getSetMultimap(String name) {
		return delegate.getSetMultimap(name);
	}

	@Override
	public <K, V> RSetMultimapReactive<K, V> getSetMultimap(String name, Codec codec) {
		return delegate.getSetMultimap(name, codec);
	}

	@Override
	public RSemaphoreReactive getSemaphore(String name) {
		return delegate.getSemaphore(name);
	}

	@Override
	public RPermitExpirableSemaphoreReactive getPermitExpirableSemaphore(String name) {
		return delegate.getPermitExpirableSemaphore(name);
	}

	@Override
	public RLockReactive getLock(String name) {
		return delegate.getLock(name);
	}

	@Override
	public RLockReactive getMultiLock(RLock... locks) {
		return delegate.getMultiLock(locks);
	}

	@Deprecated
	@Override
	public RLockReactive getRedLock(RLock... locks) {
		return delegate.getRedLock(locks);
	}

	@Override
	public RLockReactive getFairLock(String name) {
		return delegate.getFairLock(name);
	}

	@Override
	public RReadWriteLockReactive getReadWriteLock(String name) {
		return delegate.getReadWriteLock(name);
	}

	@Override
	public <V> RSetReactive<V> getSet(String name) {
		return delegate.getSet(name);
	}

	@Override
	public <V> RSetReactive<V> getSet(String name, Codec codec) {
		return delegate.getSet(name);
	}

	@Override
	public <V> RScoredSortedSetReactive<V> getScoredSortedSet(String name) {
		return delegate.getScoredSortedSet(name);
	}

	@Override
	public <V> RScoredSortedSetReactive<V> getScoredSortedSet(String name, Codec codec) {
		return delegate.getScoredSortedSet(name, codec);
	}

	@Override
	public RLexSortedSetReactive getLexSortedSet(String name) {
		return delegate.getLexSortedSet(name);
	}

	@Override
	public RTopicReactive getTopic(String name) {
		return delegate.getTopic(name);
	}

	@Override
	public RTopicReactive getTopic(String name, Codec codec) {
		return delegate.getTopic(name, codec);
	}

	@Override
	public RReliableTopicReactive getReliableTopic(String name) {
		return delegate.getReliableTopic(name);
	}

	@Override
	public RReliableTopicReactive getReliableTopic(String name, Codec codec) {
		return delegate.getReliableTopic(name, codec);
	}

	@Override
	public RPatternTopicReactive getPatternTopic(String pattern) {
		return delegate.getPatternTopic(pattern);
	}

	@Override
	public RPatternTopicReactive getPatternTopic(String pattern, Codec codec) {
		return delegate.getPatternTopic(pattern, codec);
	}

	@Override
	public <V> RQueueReactive<V> getQueue(String name) {
		return delegate.getQueue(name);
	}

	@Override
	public <V> RTransferQueueReactive<V> getTransferQueue(String name) {
		return delegate.getTransferQueue(name);
	}

	@Override
	public <V> RTransferQueueReactive<V> getTransferQueue(String name, Codec codec) {
		return delegate.getTransferQueue(name, codec);
	}

	@Override
	public <V> RQueueReactive<V> getQueue(String name, Codec codec) {
		return delegate.getQueue(name, codec);
	}

	@Override
	public <V> RRingBufferReactive<V> getRingBuffer(String name) {
		return delegate.getRingBuffer(name);
	}

	@Override
	public <V> RRingBufferReactive<V> getRingBuffer(String name, Codec codec) {
		return delegate.getRingBuffer(name, codec);
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
	public void shutdown() {
		if (delegate != null) {
			delegate.shutdown();
		}

	}

	@Override
	public Config getConfig() {
		return delegate.getConfig();
	}

	@Deprecated
	@Override
	public org.redisson.api.NodesGroup<org.redisson.api.Node> getNodesGroup() {
		return delegate.getNodesGroup();
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
	public RCountDownLatchReactive getCountDownLatch(String name) {
		return delegate.getCountDownLatch(name);
	}

	@Override
	public <V> List<RBucketReactive<V>> findBuckets(String pattern) {
		return delegate.findBuckets(pattern);
	}

	@Override
	public RIdGeneratorReactive getIdGenerator(String name) {
		return delegate.getIdGenerator(name);
	}

	@Override
	public <V> RBlockingQueueReactive<V> getBlockingQueue(String name) {
		return delegate.getBlockingQueue(name);
	}

	@Override
	public <V> RBlockingQueueReactive<V> getBlockingQueue(String name, Codec codec) {
		return delegate.getBlockingQueue(name, codec);
	}

	@Override
	public <V> RBlockingDequeReactive<V> getBlockingDeque(String name) {
		return delegate.getBlockingDeque(name);
	}

	@Override
	public <V> RBlockingDequeReactive<V> getBlockingDeque(String name, Codec codec) {
		return delegate.getBlockingDeque(name);
	}

	@Override
	public <V> RDequeReactive<V> getDeque(String name) {
		return delegate.getDeque(name);
	}

	@Override
	public <V> RDequeReactive<V> getDeque(String name, Codec codec) {
		return delegate.getDeque(name, codec);
	}

	@Override
	public RAtomicLongReactive getAtomicLong(String name) {
		return delegate.getAtomicLong(name);
	}

	@Override
	public RAtomicDoubleReactive getAtomicDouble(String name) {
		return delegate.getAtomicDouble(name);
	}

	@Override
	public RBitSetReactive getBitSet(String name) {
		return delegate.getBitSet(name);
	}

	@Override
	public RScriptReactive getScript() {
		return delegate.getScript();
	}

	@Override
	public RScriptReactive getScript(Codec codec) {
		return delegate.getScript(codec);
	}

	@Override
	public RTransactionReactive createTransaction(TransactionOptions options) {
		return delegate.createTransaction(options);
	}

	@Override
	public RBatchReactive createBatch(BatchOptions options) {
		return delegate.createBatch(options);
	}

	@Override
	public RBatchReactive createBatch() {
		return delegate.createBatch();
	}

	@Override
	public RKeysReactive getKeys() {
		return delegate.getKeys();
	}

	@Override
	public NodesGroup<ClusterNode> getClusterNodesGroup() {
		return delegate.getClusterNodesGroup();
	}

	@Override
	public RLockReactive getSpinLock(String name) {
		return delegate.getSpinLock(name);
	}

	@Override
	public RLockReactive getSpinLock(String name, BackOff backOff) {
		return delegate.getSpinLock(name, backOff);
	}
}
