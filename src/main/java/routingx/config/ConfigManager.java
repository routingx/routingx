package routingx.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

import lombok.extern.slf4j.Slf4j;
import routingx.listener.GenericListener;

@Component
@Slf4j
public class ConfigManager {

	private final NacosProperties properties;

	private final Environment env;

	private final String appName;

	private final String envName;

	public ConfigManager(@Autowired Environment env, @Autowired NacosProperties properties) {
		this.env = env;
		this.properties = properties;
		this.appName = this.env.getProperty("spring.application.name");
		String[] profiles = env.getActiveProfiles();
		if (profiles != null && profiles.length > 0) {
			this.envName = profiles[0];
		} else {
			this.envName = "";
		}
		if (log.isDebugEnabled()) {
			log.debug("appName [{}] envName [{}]", appName, envName);
		}
	}

	public void global(final String id, GenericListener<String> listener) {
		register(id, true, listener);
	}

	public void register(final String id, GenericListener<String> listener) {
		register(id, false, listener);
	}

	private void register(final String id, boolean global, GenericListener<String> listener) {
		String dataId = global ? id : (appName + "-" + id);
		ConfigListener configListener = null;
		if (StringUtils.isBlank(envName)) {
			configListener = ConfigListener.of(listener, dataId, properties.getGroup());
		} else {
			configListener = ConfigListener.of(listener, dataId, envName + "-" + properties.getGroup());
		}
		try {
			String content = null;
			if (properties.isEnabled()) {
				ConfigService configService = NacosFactory
						.createConfigService(properties.assembleConfigServiceProperties());
				content = configService.getConfig(dataId, properties.getGroup(), 5000);
				configService.addListener(configListener.getDataId(), configListener.getGroup(), configListener);
				if (log.isDebugEnabled()) {
					log.debug("listener [{}] OK", configListener.toString());
				}
			}
			configListener.receiveConfigInfo(content);
		} catch (NacosException e) {
			log.error(String.format("listener [%s]", configListener.toString()));
		}
	}

}
