package routingx;

import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@ApiModel(value = "一对")
public class Pair<S, T> {

	private final @NonNull S first;
	private final @NonNull T second;

	public static <S, T> Pair<S, T> of(S first, T second) {
		return new Pair<>(first, second);
	}
}
