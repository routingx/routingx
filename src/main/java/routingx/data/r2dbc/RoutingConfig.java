package routingx.data.r2dbc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
@ConfigurationProperties(RoutingConfig.PREFIX)
public class RoutingConfig {
	public static final String PREFIX = "r2dbc.datasource";
	private String name;
	private RoutingProperties[] properties;
}
