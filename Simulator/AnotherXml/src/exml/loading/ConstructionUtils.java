package exml.loading;

import exml.annotations.XConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

class ConstructionUtils {
  public static Object provideInstance(Class<?> type, XLoadContext ctx) {
    Object ret;
    if (type.equals(int.class) || type.equals(Integer.class))
      ret = Integer.MIN_VALUE;
    else if (type.equals(byte.class) || type.equals(Byte.class))
      ret = Byte.MIN_VALUE;
    else if (type.equals(short.class) || type.equals(Short.class))
      ret = Short.MIN_VALUE;
    else if (type.equals(long.class) || type.equals(Long.class))
      ret = Long.MIN_VALUE;
    else if (type.equals(float.class) || type.equals(Float.class))
      ret = Float.MIN_VALUE;
    else if (type.equals(double.class) || type.equals(Double.class))
      ret = Double.MIN_VALUE;
    else if (type.isEnum())
      ret = null;
    else
      ret = provideInstanceWithConstructor(type, ctx);

    return ret;
  }

  private static <T> T provideInstanceWithConstructor(Class<T> type, XLoadContext ctx) {
    // 0. try get factory
    // 1. get annotated constructor
    // 2. get public parameterless constructor

    T ret;

    if (ctx.factories.containsKey(type))
      ret = getInstanceViaFactory(type, ctx);
    else {
      Constructor<T> ctor;
      ctor = tryGetAnnotatedConstructor(type);
      if (ctor == null)
        ctor = tryGetPublicParameterlessConstructor(type);
      if (ctor == null)
        throw new XLoadException(sf("Unable to create an instance of '%s'.", type), ctx);
      ret = getInstanceViaConstructor(ctor, ctx);
    }
    return ret;
  }

  private static <T> T getInstanceViaFactory(Class<T> type, XLoadContext ctx) {
    T ret;
    try {
      ret = (T) ctx.factories.get(type).invoke();
    } catch (Exception e) {
      throw new XLoadException(sf("Failed to create a new instance of '%s' via custom factory.", type), e, ctx);
    }
    return ret;
  }

  private static <T> T getInstanceViaConstructor(Constructor<T> ctor, XLoadContext ctx) {
    T ret;

    Object[] params = new Object[ctor.getParameterCount()];
    for (int i = 0; i < ctor.getParameterCount(); i++) {
      params[i] = getDefaultParameterValue(ctor, i, ctx);
    }

    try {
      ctor.setAccessible(true);
      ret = ctor.newInstance(params);
      ctor.setAccessible(false);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new XLoadException(sf("Failed to create new instance of '%s' via constructor.", ctor.getDeclaringClass()), ctx);
    }

    return ret;
  }

  private static <T> Object getDefaultParameterValue(Constructor<T> ctor, int index, XLoadContext ctx) {
    Class<?> type = ctor.getParameters()[index].getType();
    Object ret;

    if (type.equals(int.class) || type.equals(Integer.class))
      ret = Integer.MIN_VALUE;
    else if (type.equals(byte.class) || type.equals(Byte.class))
      ret = Byte.MIN_VALUE;
    else if (type.equals(short.class) || type.equals(Short.class))
      ret = Short.MIN_VALUE;
    else if (type.equals(long.class) || type.equals(Long.class))
      ret = Long.MIN_VALUE;
    else if (type.equals(float.class) || type.equals(Float.class))
      ret = Float.MIN_VALUE;
    else if (type.equals(double.class) || type.equals(Double.class))
      ret = Double.MIN_VALUE;
    else if (type.equals(boolean.class))
      ret = false;
    else if (type.equals(Boolean.class))
      ret = null;
    else if (type.equals(char.class))
      ret = 0;
    else if (type.equals(Character.class))
      ret = null;
    else if (type.equals(XLoadContext.class))
      ret = ctx;
    else
      ret = null;

    return ret;
  }

  private static <T> Constructor<T> tryGetPublicParameterlessConstructor(Class<T> type) {
    Constructor<T>[] ctors = (Constructor<T>[]) type.getDeclaredConstructors();
    Optional<Constructor<T>> ret = Arrays.stream(ctors)
            .filter(q -> q.getParameterCount() == 0)
            .filter(q -> Modifier.isPublic(q.getModifiers()))
            .findFirst();
    return ret.isEmpty() ? null : ret.get();
  }

  private static <T> Constructor<T> tryGetAnnotatedConstructor(Class<T> type) {
    Constructor<T>[] ctors = (Constructor<T>[]) type.getDeclaredConstructors();
    Optional<Constructor<T>> ret = Arrays.stream(ctors).filter(q -> q.getAnnotation(XConstructor.class) != null).findFirst();
    return ret.isEmpty() ? null : ret.get();
  }
}
