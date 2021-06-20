package routingx;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Item implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String key;
	private String value;
	private String name;

	public Item() {

	}

	public Item(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public Item(String key, String value, String name) {
		this.key = key;
		this.value = value;
		this.name = name;
	}
}
