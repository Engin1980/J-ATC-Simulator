package exml.internal;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.utilites.ReflectionUtils;
import exml.loading.XLoadException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

//TODO remove if not used
public class SharedUtils {

  public static <T> Object convertListToArray(IList<Object> lst, Class<T> type) {
    Object ret = Array.newInstance(type.getComponentType(), lst.size());
    for (int i = 0; i < lst.size(); i++) {
      Object item = lst.get(i);
      Array.set(ret, i, item);
    }

    return ret;
  }

  public static IList<Object> convertArrayToList(Object array) {
    IList<Object> ret = new EList<>();

    for (int i = 0; i < Array.getLength(array); i++) {
      Object it = Array.get(array, i);
      ret.add(it);
    }

    return ret;
  }

  public static Field getField(Class<?> cls, String fieldName) {
    try {
      return ReflectionUtils.FieldUtils.getField(cls, fieldName);
    } catch (Exception e) {
      throw new XLoadException(sf("Failed to find field '%s' in type '%s'.", fieldName, cls), e, null);
    }
  }
}
