package routingx.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.CriteriaDefinition;

import routingx.Note;
import routingx.data.EntityMetaData;
import routingx.data.EntityProperties;
import routingx.json.JSON;
import routingx.model.IDEntity;
import routingx.model.Page;

public class SQLUtils {

	private static final String FROM = " FROM ";
	private static final String ORDER_BY = " ORDER BY ";
	private static final String LIMIT = " LIMIT ";
	private static final String LINE_SEPARATOR = System.lineSeparator();
	private static final String LINE = "\r\n";
	private static final String LIMIT_OFFSET = "OFFSET";
	private static final String LIMIT_SIZE = "LIMIT";
	private static final String LIKE = "%&LIKE;%:";

	public static String isLike(String value) {
		return LIKE + value;
	}

	public static String like(Object value) {
		if (value instanceof String) {
			String v = ((String) value);
			if (v.startsWith(SQLUtils.LIKE)) {
				return "%" + v.replaceFirst(SQLUtils.LIKE, "") + "%";
			}
		}
		return null;
	}

	public static String format(String sql) {
		while (sql.indexOf(LINE_SEPARATOR) > 0) {
			sql = sql.replaceAll(LINE_SEPARATOR, " ");
		}
		while (sql.indexOf(LINE) > 0) {
			sql = sql.replaceAll(LINE, " ");
		}
		while (sql.indexOf("  ") > 0) {
			sql = sql.replaceAll("  ", " ");
		}
		return sql;
	}

	public static Map<String, Object> toSqlParams(Object entity) {
		Map<String, Object> params = null;
		if (entity instanceof IDEntity) {
			Page page = ((IDEntity) entity).getPage();
			params = page.getParams();
		}
		if (params == null) {
			params = new HashMap<>();
		}
		Map<String, Object> map = JSON.parseMap(entity);
		EntityMetaData metadata = EntityMetaData.get(entity.getClass());
		for (EntityProperties prop : metadata.getProperties()) {
			if (!params.containsKey(prop.getColumn())) {
				Object value = map.get(prop.getName());
				if (value != null) {
					params.put(prop.getColumn(), value);
				}
			}
		}
		map.clear();
		map = null;
		return params;
	}

	@Note("生成占位符条件SQL")
	public static String where(String table, Map<String, Object> whereMap) {
		StringBuilder where = new StringBuilder();
		for (String column : whereMap.keySet()) {
			Object value = whereMap.get(column);
			if (value != null && value.getClass().isArray()) {
				where.append("AND " + table + "." + column + " in(:" + column + ")");
			} else if (value instanceof Collection<?>) {
				where.append("AND " + table + "." + column + " in(:" + column + ")");
			} else {
				where.append("AND " + table + "." + column + " = :" + column);
			}
			where.append(LINE);
		}
		return where.toString();
	}

	public static String toCount(String sql) {
		return toCount(sql, null);
	}

	public static String toCount(String sql, String countBy) {
		sql = format(sql);
		String tmp = sql.toUpperCase();
		int endIndex = tmp.lastIndexOf(ORDER_BY);
		if (endIndex <= 0) {
			endIndex = tmp.lastIndexOf(LIMIT);
		}
		if (endIndex <= 0) {
			endIndex = sql.length();
		}
		String sqlCount = "SELECT count(" + (StringUtils.isNotBlank(countBy) ? countBy : "*") + ") "
				+ sql.substring(tmp.indexOf(FROM), endIndex);
		return sqlCount;
	}

	public static String limit(String sql, Page page) {
		String tmp = sql.toUpperCase();
		if (!page.containsKey(LIMIT_OFFSET)) {
			page.put(LIMIT_OFFSET, page.offset());
		}
		if (!page.containsKey(LIMIT_SIZE)) {
			page.put(LIMIT_SIZE, page.limit());
		}
		if (tmp.lastIndexOf(LIMIT) > 0) {
			return sql;
		}
		sql += " limit :" + LIMIT_OFFSET + ",:" + LIMIT_SIZE;
		return sql;
	}

	/**
	 * 查询条件中不包括字段
	 * 
	 * @param where   查询条件
	 * @param columns 字段
	 * @return
	 */
	public static boolean contains(Criteria where, String... columns) {
		if (where == null) {
			return false;
		}
		for (String column : columns) {
			Criteria previous = where;
			while (previous != null) {
				if (previous.getColumn().getReference().equals(column)) {
					return true;
				}
				previous = previous.getPrevious();
			}
		}
		if (where.isGroup()) {
			List<CriteriaDefinition> group = where.getGroup();
			for (CriteriaDefinition cd : group) {
				for (String column : columns) {
					if (cd.getColumn().getReference().equals(column)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
