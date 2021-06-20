package routingx.json;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import routingx.model.SuperEntity;

@Configuration
public class JacksonConfig {

	@Bean
	public JavaTimeModule javaTimeModule() {
		// 解决LocalDateTime 时间格式问题
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		DateTimeFormatter localDateTimeFormat = DateTimeFormatter.ofPattern(SuperEntity.DATE_TIME_FORMAT);
		javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(localDateTimeFormat));
		javaTimeModule.addDeserializer(LocalDateTime.class, new JacksonLocalDateTimeDeserializer());
		javaTimeModule.addDeserializer(Date.class, new JacksonDateDeserializer());
		return javaTimeModule;
	}

	@Bean
	public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
		ObjectMapper om = builder.build();
		om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		om.configure(MapperFeature.USE_ANNOTATIONS, true);
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		om.setDateFormat(new SimpleDateFormat(SuperEntity.DATE_TIME_FORMAT));
		om.setTimeZone(TimeZone.getTimeZone(SuperEntity.TIMEZONE));
//		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);		
//		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

		JavaTimeModule javaTimeModule = javaTimeModule();
		om.registerModules(javaTimeModule);
		om.registerModules(new ParameterNamesModule());
		om.registerModules(ObjectMapper.findModules());
		JSON.setObjectMapper(om);
		return om;
	}

}
