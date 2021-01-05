package exml;

import eng.eSystem.collections.ISet;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.ReflectionUtils;

import java.lang.reflect.Field;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class FieldUtils {
  public static ISet<String> getRemainingFields(Class<?> cls, ISet<String> usedFields) {
    ISet<String> ret = getAllFieldsToPersist(cls);
    ret.tryRemoveMany(usedFields);
    return ret;
  }

  public static void saveField(ISimPersistable obj, String remainingFieldName, XElement elm, XmlContext ctx) {
    Field field = getField(obj.getClass(), remainingFieldName);
    Object value = getFieldValue(obj, field);

  }

  private static Object getFieldValue(Object obj, Field field) {
    Object ret;

    try {
      field.setAccessible(true);
      ret = field.get(obj);
      field.setAccessible(false);
    } catch (IllegalAccessException e) {
      throw new SimPersistenceExeption(sf("Failed to load field '%s' value from '%s'.", field.getName(), obj.getClass()), e);
    }
    return ret;
  }

  private static Field getField(Class<? extends ISimPersistable> cls, String fieldName) {
    try {
      return ReflectionUtils.FieldUtils.getField(cls, fieldName);
    } catch (Exception e) {
      throw new SimPersistenceExeption(sf("Failed to find field '%s' in type '%s'.", fieldName, cls), e);
    }
  }

  private static ISet<String> getAllFieldsToPersist(Class<?> cls) {
    ISet<String> ret = ReflectionUtils.ClassUtils.getFields(cls)
            .select(q -> q.getName())
            .toSet();
    return ret;
  }
}
