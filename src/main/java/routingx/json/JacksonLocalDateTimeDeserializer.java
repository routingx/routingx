package routingx.json;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import lombok.extern.slf4j.Slf4j;
import routingx.utils.DateTimeUtils;

@Slf4j
class JacksonLocalDateTimeDeserializer extends LocalDateTimeDeserializer {

	private static final long serialVersionUID = 101963227214642567L;
	private static final String[] parsePatterns = new String[] { "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss",
			"yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd", "MM-dd-yyyy", "dd-MM-yyyy" };

	public JacksonLocalDateTimeDeserializer() {
		super(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	protected JacksonLocalDateTimeDeserializer(JacksonLocalDateTimeDeserializer base, Boolean leniency) {
		super(base, leniency);
	}

	@Override
	protected JacksonLocalDateTimeDeserializer withDateFormat(DateTimeFormatter formatter) {
		return new JacksonLocalDateTimeDeserializer();
	}

	@Override
	protected JacksonLocalDateTimeDeserializer withLeniency(Boolean leniency) {
		return new JacksonLocalDateTimeDeserializer(this, leniency);
	}

	@Override
	public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
		try {
			return super.deserialize(parser, context);
		} catch (IOException ex) {
			return deserialize(parser, context, ex);
		}
	}

	private LocalDateTime deserialize(JsonParser parser, DeserializationContext context, IOException ex)
			throws IOException {
		try {
			String source = parser.getText().trim();
			return DateTimeUtils.asLocalDateTime(DateTimeUtils.parseDate(source, parsePatterns));
		} catch (Throwable e) {
			log.warn(e.getMessage());
			throw ex;
		}
	}
}
