package routingx.data.r2dbc;

import lombok.Getter;
import lombok.Setter;
import routingx.Note;

/**
 * 连接参数
 * 
 * @author peixere
 *
 */
@Setter
@Getter
public class RoutingPool {

	@Note("启动时连接池大小，默认10")
	private int initialSize;

	@Note("连接池最大大小，默认10")
	private int maxSize;

	@Note("最长空闲时间(秒)。默认30分钟")
	private long maxIdleTime;

	@Note("请求连接的重试次数，默认为1")
	private int acquireRetry = 1;

	@Note("获取连接的最长时间(秒)")
	private long maxAcquireTime;

	@Note("创建连接的超时时间(秒)，默认不会超时")
	private long maxCreateConnectionTime = 30;

	private String validationQuery = "select 1";

	public RoutingPool() {

	}

}
