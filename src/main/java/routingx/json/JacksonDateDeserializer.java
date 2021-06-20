package routingx.json;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer;

import lombok.extern.slf4j.Slf4j;
import routingx.utils.DateTimeUtils;

@Slf4j
class JacksonDateDeserializer extends DateDeserializer {

	private static final long serialVersionUID = 101963227214642567L;
	private static final String[] parsePatterns = new String[] { "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss",
			"yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd", "MM-dd-yyyy", "dd-MM-yyyy" };

	public JacksonDateDeserializer() {
	}

	public JacksonDateDeserializer(DateDeserializer base, DateFormat df, String formatString) {
		super(base, df, formatString);
	}

	@Override
	protected JacksonDateDeserializer withDateFormat(DateFormat df, String formatString) {
		return new JacksonDateDeserializer(this, df, formatString);
	}

	@Override
	public Date deserialize(JsonParser parser, DeserializationContext context) throws IOException {
		try {
			return super.deserialize(parser, context);
		} catch (IOException ex) {
			return deserialize(parser, context, ex);
		}
	}

	private Date deserialize(JsonParser parser, DeserializationContext context, IOException ex) throws IOException {
		try {
			String source = parser.getText().trim();
			return DateTimeUtils.parseDate(source, parsePatterns);
		} catch (Throwable e) {
			log.warn(e.getMessage());
			throw ex;
		}
	}
}
