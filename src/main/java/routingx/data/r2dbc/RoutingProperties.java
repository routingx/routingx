package routingx.data.r2dbc;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

/**
 * 连接参数
 * 
 * @author peixere
 *
 */
@Setter
@Getter
public class RoutingProperties {

	private String id;

	private String group;

	private String[] patterns;

	private String url = "r2dbcs:mysql://127.0.0.1:3306/test";

	private String username;

	private String password;

	private RoutingPool pool;

	public Object lookupKey() {
		if (id == null) {
			id = UUID.randomUUID().toString();
		}
		return id;
	}
}
