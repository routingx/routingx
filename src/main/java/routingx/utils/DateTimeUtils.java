package routingx.utils;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

public class DateTimeUtils extends org.apache.commons.lang3.time.DateUtils {

	private static final String[] FORMATS = new String[] { "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss",
			"yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd" };

	private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static Date dateStart() {
		return dateStart(new Date(), 0);
	}

	public static Date dateStart(int day) {
		return dateStart(new Date(), day);
	}

	public static Date dateStart(Date date, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}

	public static Date dateStartHour(Date date, int hour) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, 0);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}

	public static Date dateEnd() {
		return dateEnd(new Date(), 0);
	}

	/**
	 * 当前时间+day天后的结束时间 0，yyyy-MM-dd 23:59:59
	 * 
	 * @param day
	 * @return
	 */
	public static Date dateEnd(int day) {
		return dateEnd(new Date(), day);
	}

	public static Date dateEnd(Date date, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		return calendar.getTime();
	}

	public static Date getFirstDayOfMonth(int month) {
		Calendar cal = Calendar.getInstance();
		// 设置月份
		cal.set(Calendar.MONTH, month - 1);
		// 获取某月最小天数
		int firstDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
		// 设置日历中月份的最小天数
		cal.set(Calendar.DAY_OF_MONTH, firstDay);
		// 格式化日期
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String firstDayOfMonth = sdf.format(cal.getTime()) + " 00:00:00";

		SimpleDateFormat formatter = new SimpleDateFormat(FORMAT);
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(firstDayOfMonth, pos);
		return strtodate;
	}

	/**
	 * 获得该月最后一天
	 *
	 */
	public static Date getLastDayOfMonth(int month) {
		Calendar cal = Calendar.getInstance();
		// 设置月份
		cal.set(Calendar.MONTH, month - 1);
		// 获取某月最大天数
		int lastDay = 0;
		// 2月的平年瑞年天数
		if (month == 2) {
			lastDay = cal.getLeastMaximum(Calendar.DAY_OF_MONTH);
		} else {
			lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		}
		// 设置日历中月份的最大天数
		cal.set(Calendar.DAY_OF_MONTH, lastDay);
		// 格式化日期
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String lastDayOfMonth = sdf.format(cal.getTime()) + " 23:59:59";

		SimpleDateFormat formatter = new SimpleDateFormat(FORMAT);
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(lastDayOfMonth, pos);
		return strtodate;
	}

	/**
	 * 时间过去了second
	 * 
	 * @param
	 * @return
	 */
	public static boolean passed(int second, Date date) {
		if (date != null) {
			return passed(date.getTime(), second * 1000);
		}
		return passed(0, second * 1000);
	}

	/**
	 * 时间没过去了second
	 *
	 * @param
	 * @return
	 */
	public static boolean notPassed(int second, Date date) {
		if (null == date) {
			return false;
		}
		if ((System.currentTimeMillis() - date.getTime()) > (second * 1000)) {
			return false;
		}
		return true;
	}

	/**
	 * 时间过去了timeMillis
	 * 
	 * @param
	 * @return
	 */
	public static boolean passed(Date date, long timeMillis) {
		if (date != null) {
			return passed(date.getTime(), timeMillis);
		}
		return passed(0, timeMillis);
	}

	/**
	 * 时间过去了timeMillis
	 * 
	 * @param
	 * @return
	 */
	public static boolean passed(long lastTimeMillis, long timeMillis) {
		return System.currentTimeMillis() - lastTimeMillis >= timeMillis;
	}

	public static final Date dateTime(final String format, final String ts) {
		try {
			return new SimpleDateFormat(format).parse(ts);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static final int yearMonthDayStr(Date date) {
		return Integer.parseInt(DateFormatUtils.format(date, "yyyyMMdd"));
	}

	public static final int yearMonthStr(Date date) {
		return Integer.parseInt(DateFormatUtils.format(date, "yyyyMM"));
	}

	public static Date asDate(LocalDate localDate) {
		return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}

	public static Date asDate(LocalDateTime localDateTime) {
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	public static LocalDate asLocalDate(Date date) {
		return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public static LocalDateTime asLocalDateTime(Date date) {
		return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	public static Date asDate(final String source) {
		try {
			return DateUtils.parseDate(source, FORMATS);
		} catch (Exception ex) {
			return null;
		}
	}

	public static LocalDateTime asLocalDateTime(final String source) {
		try {
			return asLocalDateTime(asDate(source));
		} catch (Exception ex) {
			return null;
		}
	}

	public static String format(final LocalDateTime localDateTime) {
		return DateFormatUtils.format(DateTimeUtils.asDate(localDateTime), FORMAT);
	}

	public static String format(final Date date) {
		return DateFormatUtils.format(date, FORMAT);
	}

}
