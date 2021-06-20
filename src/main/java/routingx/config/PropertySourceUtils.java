package routingx.config;

import static java.util.Collections.unmodifiableMap;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;

class PropertySourceUtils {

	public static Map<String, Object> getSubProperties(ConfigurableEnvironment environment, String prefix) {
		return getSubProperties(environment.getPropertySources(), environment, prefix);
	}

	public static Map<String, Object> getSubProperties(PropertySources propertySources, String prefix) {
		return getSubProperties(propertySources, new PropertySourcesPropertyResolver(propertySources), prefix);
	}

	public static Map<String, Object> getSubProperties(PropertySources propertySources,
			PropertyResolver propertyResolver, String prefix) {

		Map<String, Object> subProperties = new LinkedHashMap<String, Object>();

		String normalizedPrefix = normalizePrefix(prefix);

		Iterator<PropertySource<?>> iterator = propertySources.iterator();

		while (iterator.hasNext()) {
			PropertySource<?> source = iterator.next();
			for (String name : getPropertyNames(source)) {
				if (!subProperties.containsKey(name) && name.startsWith(normalizedPrefix)) {
					String subName = name.substring(normalizedPrefix.length());
					if (!subProperties.containsKey(subName)) { // take first one
						Object value = source.getProperty(name);
						if (value instanceof String) {
							// Resolve placeholder
							value = propertyResolver.resolvePlaceholders((String) value);
						}
						subProperties.put(subName, value);
					}
				}
			}
		}

		return unmodifiableMap(subProperties);
	}

	public static String normalizePrefix(String prefix) {
		return prefix.endsWith(".") ? prefix : prefix + ".";
	}

	public static final String[] EMPTY_STRING_ARRAY = {};

	public static String[] getPropertyNames(PropertySource<?> propertySource) {
		String[] propertyNames = propertySource instanceof EnumerablePropertySource
				? ((EnumerablePropertySource<?>) propertySource).getPropertyNames()
				: null;

		if (propertyNames == null) {
			propertyNames = EMPTY_STRING_ARRAY;
		}

		return propertyNames;
	}
}
