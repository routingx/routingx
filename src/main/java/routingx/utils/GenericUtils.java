package routingx.utils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import routingx.CustomException;
import routingx.data.Forbid;

@Slf4j
public class GenericUtils {

	public static <T> T newInstance(Class<T> clazz) {
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw CustomException.er("实例化错误", e);

		}
	}

	public static Class<?> getClass(String className) {
		try {
			return org.apache.commons.lang3.ClassUtils.getClass(className);
		} catch (Throwable e) {
			log.warn(e.getMessage());
			try {
				String packageName = CustomException.class.getPackageName();
				return org.apache.commons.lang3.ClassUtils.getClass(packageName + "." + className);
			} catch (Throwable ex) {
				log.warn(ex.getMessage());
				throw new RuntimeException(e);
			}
		}
	}

	public static Class<?> isClass(String className) {
		try {
			return org.apache.commons.lang3.ClassUtils.getClass(className);
		} catch (Throwable e) {
			log.warn(e.getMessage());
			try {
				String packageName = CustomException.class.getPackageName();
				return org.apache.commons.lang3.ClassUtils.getClass(packageName + "." + className);
			} catch (Throwable ex) {
				log.warn(ex.getMessage());
				throw new RuntimeException(e);
			}
		}
	}
	
	public static Class<?> getUniversalClass(String className) {
		Class<?> clazz = getClass(className);
		if (clazz.isAnnotationPresent(Forbid.class)) {
			throw new RuntimeException("Universal Controller Forbid");
		}
		return clazz;
	}

	public static Class<?> getParameterizedType(Class<?> src) {
		Class<?> calzz = src;
		List<Type> typeList = new ArrayList<>();
		while (typeList.size() == 0) {
			if (calzz.getGenericSuperclass() instanceof ParameterizedType) {
				Type[] types = ((ParameterizedType) calzz.getGenericSuperclass()).getActualTypeArguments();
				for (Type type : types) {
					typeList.add(type);
				}
			}
			calzz = calzz.getSuperclass();
		}
		return getClass(typeList.get(0));
	}

	/**
	 * Get the underlying class for a type, or null if the type is a variable type.
	 * 
	 * @param type the type
	 * @return the underlying class
	 */
	@SuppressWarnings("rawtypes")
	private static Class<?> getClass(Type type) {
		if (type instanceof Class) {
			return (Class) type;
		} else if (type instanceof ParameterizedType) {
			return getClass(((ParameterizedType) type).getRawType());
		} else if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type).getGenericComponentType();
			Class<?> componentClass = getClass(componentType);
			if (componentClass != null) {
				return Array.newInstance(componentClass, 0).getClass();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * This is a helper method to call a method on an Object with the given
	 * parameters. It is used for dispatching to specific DAOs that do not implement
	 * the GenericDAO interface.
	 */
	public static Object callMethod(Object object, String methodName, Object... args)
			throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Class<?>[] paramTypes = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null)
				throw new NullPointerException(
						"No arguments may be null when using callMethod(Object, String, Object...) because every argument is needed in order to determine the parameter types. Use callMethod(Object, String, Class<?>[], Object...) instead and specify parameter types.");

			paramTypes[i] = args[i].getClass();
		}

		return callMethod(object, methodName, paramTypes, args);
	}

	/**
	 * This is a helper method to call a method on an Object with the given
	 * parameters. It is used for dispatching to specific DAOs that do not implement
	 * the GenericDAO interface.
	 */
	public static Object callMethod(Object object, String methodName, Class<?>[] paramTypes, Object... args)
			throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Method method = getMethod(object.getClass(), methodName, paramTypes);
		if (method == null)
			throw new NoSuchMethodException("Method: " + methodName + " not found on Class: " + object.getClass());

		if (method.isVarArgs()) {
			// put variable arguments into array as last parameter
			Object[] allargs = new Object[method.getParameterTypes().length];
			Object[] vargs = (Object[]) Array.newInstance(
					method.getParameterTypes()[method.getParameterTypes().length - 1].getComponentType(),
					args.length - method.getParameterTypes().length + 1);

			System.arraycopy(args, 0, allargs, 0, method.getParameterTypes().length - 1);
			for (int i = 0; i < args.length - method.getParameterTypes().length + 1; i++) {
				vargs[i] = args[method.getParameterTypes().length - 1 + i];
			}
			allargs[method.getParameterTypes().length - 1] = vargs;

			return method.invoke(object, allargs);
		} else {

			return method.invoke(object, args);
		}
	}

	public static Method getMethod(Class<?> klass, String methodName, Class<?>... paramTypes) {

		List<Method> candidates = new ArrayList<Method>();

		// NOTE: getMethods() includes inherited methods
		outer: for (Method method : klass.getMethods()) {
			if (method.getName().equals(methodName)) {
				Class<?>[] methodParamTypes = method.getParameterTypes();
				if (paramTypes.length == methodParamTypes.length
						|| (method.isVarArgs() && paramTypes.length >= methodParamTypes.length - 1)) {
					// method has correct name and # of parameters

					if (method.isVarArgs()) {
						for (int i = 0; i < methodParamTypes.length - 1; i++) {
							if (paramTypes[i] != null && !methodParamTypes[i].isAssignableFrom(paramTypes[i])) {
								continue outer;
							}
						}
						if (methodParamTypes.length == paramTypes.length + 1) {
							// no param is specified for the optional vararg
							// spot
						} else if (methodParamTypes.length == paramTypes.length
								&& methodParamTypes[paramTypes.length - 1]
										.isAssignableFrom(paramTypes[paramTypes.length - 1])) {
							// an array is specified for the last param
						} else {
							Class<?> varClass = methodParamTypes[methodParamTypes.length - 1].getComponentType();
							for (int i = methodParamTypes.length - 1; i < paramTypes.length; i++) {
								if (paramTypes[i] != null && !varClass.isAssignableFrom(paramTypes[i])) {
									continue outer;
								}
							}
						}
					} else {
						for (int i = 0; i < methodParamTypes.length; i++) {
							if (paramTypes[i] != null && !methodParamTypes[i].isAssignableFrom(paramTypes[i])) {
								continue outer;
							}
						}
					}
					candidates.add(method);
				}
			}
		}

		if (candidates.size() == 0) {
			return null;
		} else if (candidates.size() == 1) {
			return candidates.get(0);
		} else {
			// There are several possible methods. Choose the most specific.

			// Throw away any var-args options.
			// Non var-args methods always beat var-args methods and we're going
			// to say that if we have two var-args
			// methods, we cannot choose between the two.
			Iterator<Method> itr = candidates.iterator();
			while (itr.hasNext()) {
				Method m = itr.next();
				if (m.isVarArgs()) {
					// the exception is if an array is actually specified as the
					// last parameter
					if (m.getParameterTypes().length != paramTypes.length
							|| !m.getParameterTypes()[paramTypes.length - 1]
									.isAssignableFrom(paramTypes[paramTypes.length - 1]))
						itr.remove();
				}
			}

			// If there are no candidates left, that means we had only var-args
			// methods, which we can't choose
			// between.
			if (candidates.size() == 0)
				return null;

			Method a = candidates.get(0);
			boolean ambiguous = false;

			for (int j = 1; j < candidates.size(); j++) {
				Method b = candidates.get(j);

				Class<?>[] aTypes = a.getParameterTypes();
				Class<?>[] bTypes = b.getParameterTypes();

				int aScore = 0, bScore = 0;
				// increment score if distance is greater for a given parameter
				for (int i = 0; i < aTypes.length; i++) {
					if (aTypes[i] != null) {
						int distA = getDist(aTypes[i], paramTypes[i]);
						int distB = getDist(bTypes[i], paramTypes[i]);
						if (distA > distB) {
							bScore++;
						} else if (distA < distB) {
							aScore++;
						} else if (distA == 1000) { // both are interfaces
													// if one interface extends the other, that
													// interface is lower in the hierarchy (more
													// specific) and wins
							if (!aTypes[i].equals(bTypes[i])) {
								if (aTypes[i].isAssignableFrom(bTypes[i]))
									bScore++;
								else if (bTypes[i].isAssignableFrom(aTypes[i]))
									aScore++;
							}
						}
					}
				}

				// lower score wins
				if (aScore == bScore) {
					ambiguous = true;
				} else if (bScore > aScore) {
					a = b; // b wins
					ambiguous = false;
				}
			}

			if (ambiguous)
				return null;

			return a;
		}
	}

	/**
	 * Greater dist is worse:
	 * <ol>
	 * <li>superClass = Object loses to all
	 * <li>If klass is not an interface, superClass is interface loses to all other
	 * classes
	 * <li>Closest inheritance wins
	 * </ol>
	 */
	private static int getDist(Class<?> superClass, Class<?> klass) {
		if (klass.isArray()) {
			if (superClass.isArray()) {
				superClass = superClass.getComponentType();
				klass = klass.getComponentType();
			} else {
				// superClass must be Object. An array fitting into an Object
				// must be more general than an array fitting into an Object[]
				// array.
				return 3000;
			}
		}

		if (superClass.equals(klass))
			return 0;
		if (superClass.equals(Object.class))
			return 2000; // specifying Object is always the most general
		if (superClass.isInterface()) {
			return 1000;
		}

		int dist = 0;
		while (true) {
			dist++;
			klass = klass.getSuperclass();
			if (superClass.equals(klass))
				return dist;
		}
	}
}
