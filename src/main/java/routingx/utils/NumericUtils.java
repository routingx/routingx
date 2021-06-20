package routingx.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import routingx.Note;

public class NumericUtils {

	public static final int ZERO = 0;
	public static final int ONE = 1;
	public static final int TWO = 2;
	public static final int THREE = 3;

	public static int parseInt(String str) {
		return parseInt(str, 0);
	}

	public static int parseInt(String str, int defaultValue) {
		try {
			return Integer.parseInt(str);
		} catch (Throwable ex) {
			return defaultValue;
		}
	}

	public static boolean equals(Integer x, Integer y) {
		if (x == null) {
			x = 0;
		}
		if (y == null) {
			y = 0;
		}
		return x.intValue() == y.intValue();
	}

	public static boolean equals(Long x, Long y) {
		if (x == null) {
			x = 0L;
		}
		if (y == null) {
			y = 0L;
		}
		return x.longValue() == y.longValue();
	}

	public static boolean equals(Short x, Short y) {
		if (x == null) {
			x = 0;
		}
		if (y == null) {
			y = 0;
		}
		return x.shortValue() == y.shortValue();
	}

	public static boolean equals(Byte x, Byte y) {
		if (x == null) {
			x = 0;
		}
		if (y == null) {
			y = 0;
		}
		return x.byteValue() == y.byteValue();
	}

	public static boolean equals(Float x, Float y) {
		if (x == null) {
			x = 0F;
		}
		if (y == null) {
			y = 0F;
		}
		return x.floatValue() == y.floatValue();
	}

	public static boolean equals(Double x, Double y) {
		if (x == null) {
			x = 0D;
		}
		if (y == null) {
			y = 0D;
		}
		return x.doubleValue() == y.doubleValue();
	}

	public static boolean equals(BigDecimal x, BigDecimal y) {
		if (x == null) {
			x = BigDecimal.ZERO;
		}
		if (y == null) {
			y = BigDecimal.ZERO;
		}
		x = scale2(x);
		y = scale2(y);
		return x.equals(y);
	}

	public static BigDecimal scale2(long val) {
		return scale2(BigDecimal.valueOf(val));
	}

	public static BigDecimal scale2(float val) {
		return scale2(BigDecimal.valueOf(val * 100).setScale(2, RoundingMode.DOWN).divide(BigDecimal.valueOf(100)));
	}

	public static BigDecimal scale2(double val) {
		return scale2(BigDecimal.valueOf(val));
	}

	public static BigDecimal scale2(BigDecimal bigDecimal) {
		return bigDecimal.setScale(2, RoundingMode.DOWN);
	}

	/**
	 * a / b 保留两位小数
	 * 
	 * @param bigDecimal
	 * @return
	 */
	public static BigDecimal divide2(BigDecimal a, BigDecimal b) {
		if (BigDecimal.ZERO.equals(b)) {
			b = scale2(1);
		}
		return a.divide(b, 2, RoundingMode.DOWN);
	}

	/**
	 * a / b 保留两位小数
	 * 
	 * @param bigDecimal
	 * @return
	 */
	public static BigDecimal divide2(BigDecimal a, double b) {
		if (b == 0) {
			b = 1;
		}
		return divide2(a, BigDecimal.valueOf(b));
	}

	/**
	 * a / b 保留两位小数
	 * 
	 * @param bigDecimal
	 * @return
	 */
	public static BigDecimal divide2(BigDecimal a, long b) {
		if (b == 0) {
			b = 1;
		}
		return divide2(a, BigDecimal.valueOf(b));
	}

	/**
	 * a * b 保留两位小数
	 * 
	 * @param bigDecimal
	 * @return
	 */
	public static BigDecimal multiply2(BigDecimal a, BigDecimal b) {
		return scale2(a.multiply(b));
	}

	/**
	 * a * b 保留两位小数
	 * 
	 * @param bigDecimal
	 * @return
	 */
	public static BigDecimal multiply2(BigDecimal a, double b) {
		return multiply2(a, BigDecimal.valueOf(b));
	}

	/**
	 * a * b 保留两位小数
	 * 
	 * @param bigDecimal
	 * @return
	 */
	public static BigDecimal multiply2(BigDecimal a, long b) {
		return multiply2(a, BigDecimal.valueOf(b));
	}

	/**
	 * a < B ? true:false
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean less(BigDecimal a, BigDecimal b) {
		if (a == null || b == null) {
			return false;
		}
		return a.compareTo(b) == -1;
	}

	/**
	 * a > B ? true:false
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean greater(BigDecimal a, BigDecimal b) {
		if (a == null || b == null) {
			return false;
		}
		return a.compareTo(b) == 1;
	}

	/**
	 * a >= B ? true:false
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean gte(BigDecimal a, BigDecimal b) {
		if (a == null || b == null) {
			return false;
		}
		return a.compareTo(b) != -1;
	}

	/**
	 * a >= B ? true:false
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean gte(Double a, Double b) {
		if (a == null) {
			a = 0D;
		}
		if (b == null) {
			b = 0D;
		}
		return a >= b;
	}

	/**
	 * a > B ? true:false
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean gt(Double a, Double b) {
		if (a == null) {
			a = 0D;
		}
		if (b == null) {
			b = 0D;
		}
		return a > b;
	}

	/**
	 * a < B ? true:false
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean less(Double a, Double b) {
		if (a == null) {
			a = 0D;
		}
		if (b == null) {
			b = 0D;
		}
		return a < b;
	}

	/**
	 * a > B ? true:false
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean gt(Long a, Long b) {
		if (a == null) {
			a = 0L;
		}
		if (b == null) {
			b = 0L;
		}
		return a > b;
	}

	/**
	 * a >= B ? true:false
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean gte(Integer a, Integer b) {
		if (a == null) {
			a = 0;
		}
		if (b == null) {
			b = 0;
		}
		return a >= b;
	}

	@Note("求两个最大公约数")
	public static long commonDivisor(long n, long m) {
		// 辗转相除是用大的除以小的。如果n<m，第一次相当n与m值交换
		while (n % m != 0) {
			long temp = n % m;
			n = m;
			m = temp;
		}
		return m;
	}

	@Note("求两个数最小公倍数")
	public static long commonMultiple(long n, long m) {
		return n * m / commonDivisor(n, m);
	}

	@Note("求多个数的最小公倍数")
	public static long commonMultiple(Long[] nums) {
		long value = nums[0];
		for (int i = 1; i < nums.length; i++) {
			value = commonMultiple(value, nums[i]);
		}
		return value;
	}

	@Note("求多个数的最小公倍数")
	public static long commonMultiple(long[] nums) {
		long value = nums[0];
		for (int i = 1; i < nums.length; i++) {
			value = commonMultiple(value, nums[i]);
		}
		return value;
	}

}
