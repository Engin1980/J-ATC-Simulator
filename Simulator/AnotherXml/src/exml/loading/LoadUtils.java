package exml.loading;

import eng.eSystem.collections.ESet;
import eng.eSystem.collections.ISet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

class LoadUtils {

  public static <T> Object loadEnum(String value, Class<T> type) {
    Method parseMethod;
    Object ret;
    ISet<Method> methods = new ESet<>(type.getDeclaredMethods());
    parseMethod = methods
            .where(q -> q.getName().equals("parse"))
            .where(q -> Modifier.isPublic(q.getModifiers()) && Modifier.isStatic(q.getModifiers()))
            .where(q -> q.getParameterCount() == 1 && q.getParameters()[0].getType().equals(String.class))
            .tryGetFirst();
    if (parseMethod != null) {
      try {
        ret = parseMethod.invoke(null, value);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new XLoadException(
                sf("Failed to 'parse' Enum type '%s' from value '%s'", type.getName(), value), e, null);
      }
    } else
      ret = Enum.valueOf((Class<Enum>) type, value);
    return ret;
  }
}
