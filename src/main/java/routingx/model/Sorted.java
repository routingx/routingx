package routingx.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Sorted {

	public static final Direction DEFAULT_DIRECTION = Direction.ASC;

	private final List<Order> orders;

	public Sorted() {
		orders = new ArrayList<>();
	}

	protected Sorted(Direction direction, List<String> properties) {
		if (properties == null || properties.isEmpty()) {
			throw new IllegalArgumentException("You have to provide at least one property to sort by!");
		}
		this.orders = properties.stream() //
				.map(it -> new Order(direction, it)) //
				.collect(Collectors.toList());
	}

	protected Sorted(List<Order> orders) {
		this.orders = orders;
	}

	public Sorted and(Sorted sort) {
		Assert.notNull(sort, "Sort must not be null!");
		ArrayList<Order> these = new ArrayList<>(this.orders);
		for (Order order : sort.getOrders()) {
			these.add(order);
		}
		return Sorted.by(these);
	}

	public static Sorted unsorted() {
		return new Sorted();
	}

	@JsonIgnore
	public org.springframework.data.domain.Sort getSort() {
		if (orders != null && orders.size() > 0) {
			List<org.springframework.data.domain.Sort.Order> these = new ArrayList<>();
			orders.sort(Comparator.comparing(Sorted.Order::getOrder));
			for (Order order : orders) {
				these.add(new org.springframework.data.domain.Sort.Order(order.getDirection(), order.getProperty()));
			}
			return org.springframework.data.domain.Sort.by(these);
		} else {
			return org.springframework.data.domain.Sort.unsorted();
		}
	}

	public static Sorted by(Direction direction, String... properties) {

		Assert.notNull(direction, "Direction must not be null!");
		Assert.notNull(properties, "Properties must not be null!");
		Assert.isTrue(properties.length > 0, "At least one property must be given!");

		return Sorted.by(Arrays.stream(properties)//
				.map(it -> new Order(direction, it))//
				.collect(Collectors.toList()));
	}

	public static Sorted by(List<Order> orders) {

		Assert.notNull(orders, "Orders must not be null!");

		return orders.isEmpty() ? Sorted.unsorted() : new Sorted(orders);
	}

	public static Sorted by(Order... orders) {

		Assert.notNull(orders, "Orders must not be null!");

		return new Sorted(Arrays.asList(orders));
	}

	@Setter
	@Getter
	public static class Order {
		private Direction direction;
		private String property;
		private int order;

		public Order() {
		}

		public Order(Direction direction, String property, int order) {
			this.direction = direction;
			this.property = property;
			this.order = order;
		}

		public Order(Direction direction, String property) {
			this(direction, property, 0);
		}

		public static Order by(String property) {
			return new Order(DEFAULT_DIRECTION, property);
		}

		public static Order asc(String property) {
			return new Order(Direction.ASC, property);
		}

		public static Order desc(String property) {
			return new Order(Direction.DESC, property);
		}
	}
}
