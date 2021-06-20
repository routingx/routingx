package routingx.utils;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormat extends SimpleDateFormat {
	private static final long serialVersionUID = 1L;

	private String pattern;

	public DateFormat(String pattern) {
		super(pattern);
		this.pattern = pattern;
	}

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
		return super.format(date, toAppendTo, fieldPosition);
	}

	@Override
	public Date parse(String source, ParsePosition pos) {
		Date date = null;
		try {
			date = super.parse(source, pos);
		} catch (Exception e) {
			DateTimeUtils.asDate(source);
		}
		return date;
	}

	@Override
	public Object clone() {
		return new DateFormat(pattern);
	}
}
