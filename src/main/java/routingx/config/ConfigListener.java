package routingx.config;

import java.util.concurrent.Executor;

import com.alibaba.nacos.api.config.listener.Listener;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import routingx.listener.GenericListener;

@ToString
@Getter
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ConfigListener implements Listener {

	private final @NonNull String dataId;
	private final @NonNull String group;
	private final @NonNull GenericListener<String> listener;

	private String configInfo;

	public static ConfigListener of(GenericListener<String> listener, String dataId, String group) {
		return new ConfigListener(dataId, group, listener);
	}

	@Override
	public Executor getExecutor() {
		return null;
	}

	@Override
	public void receiveConfigInfo(String config) {
		try {
			if (log.isDebugEnabled()) {
				log.info(String.format("receive [group=%s, dataId=%s] %s", group, dataId, config));
			}
			if (config != null && config.equals(configInfo)) {
				return;
			}
			configInfo = config;
			listener.event(config);
		} catch (Exception e) {
			log.error(String.format("receive [group=%s, dataId=%s]", group, dataId), e);
		}
	}
}
