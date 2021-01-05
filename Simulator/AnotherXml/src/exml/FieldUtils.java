package exml;

import eng.eSystem.collections.ISet;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class FieldUtils {
  public static void saveFieldItems(Object obj, String itemsFieldName,  Class<?> itemType, XElement elm, XContext ctx) {
    Field field = getField(obj.getClass(), itemsFieldName);
    Object tmp = getFieldValue(obj, field);
    Iterable<?> items = (Iterable<?>) tmp;

    XElement itemsElement = ctx.saver.saveItems(items, itemType, itemsFieldName);
    elm.addElement(itemsElement);
  }

  static ISet<String> getRemainingFields(Class<?> cls, ISet<String> usedFields) {
    ISet<String> ret = getAllFieldsToPersist(cls);
    ret.tryRemoveMany(usedFields);
    return ret;
  }

  static void saveField(Object obj, String fieldName, XElement elm, XContext ctx) {
    XElement fieldElement = saveField(obj, fieldName, ctx);
    elm.addElement(fieldElement);
  }

  private static XElement saveField(Object obj, String fieldName, XContext ctx) {
    Field field = getField(obj.getClass(), fieldName);
    Object value = getFieldValue(obj, field);

    XElement fieldElement = new XElement(field.getName());
    ctx.saver.saveObject(value, fieldElement);
    if (value != null && TypeUtils.isTypeSame(value.getClass(), field.getType()) == false)
      fieldElement.setAttribute("__type", value.getClass().getName());
    return fieldElement;
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

  private static Field getField(Class<? extends Object> cls, String fieldName) {
    try {
      return ReflectionUtils.FieldUtils.getField(cls, fieldName);
    } catch (Exception e) {
      throw new SimPersistenceExeption(sf("Failed to find field '%s' in type '%s'.", fieldName, cls), e);
    }
  }

  private static ISet<String> getAllFieldsToPersist(Class<?> cls) {
    ISet<String> ret = ReflectionUtils.ClassUtils.getFields(cls)
            .where(q -> Modifier.isStatic(q.getModifiers()) == false)
            .select(q -> q.getName())
            .toSet();
    return ret;
  }
}
