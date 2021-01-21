package exml;

import eng.eSystem.collections.IMap;
import eng.eSystem.collections.ISet;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.ReflectionUtils;
import exml.annotations.XIgnored;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class FieldUtils {

  public static <K, V> void saveFieldEntries(Object obj, String entriesFieldName, Class<K> keyType, Class<V> valueType, XElement elm, XContext ctx) {
    Field field = getField(obj.getClass(), entriesFieldName);
    Object tmp = getFieldValue(obj, field);
    Iterable<Map.Entry<K, V>> entries;
    if (tmp instanceof Map) {
      Map<K, V> map = (Map<K, V>) tmp;
      entries = map.entrySet();
    } else if (tmp instanceof IMap) {
      IMap<K,V> map = (IMap<K,V>) tmp;
      entries = map.getEntries();
    } else
      throw new SimPersistenceExeption("Unsupported map type to save entries: " + tmp.getClass());

    XElement itemsElement = ctx.saver.saveEntries(entries, keyType, valueType, entriesFieldName);
    elm.addElement(itemsElement);
  }

  public static void saveFieldItems(Object obj, String itemsFieldName, Class<?> itemType, XElement elm, XContext ctx) {
    Field field = getField(obj.getClass(), itemsFieldName);
    Object tmp = getFieldValue(obj, field);
    Iterable<?> items = (Iterable<?>) tmp;

    XElement itemsElement = ctx.saver.saveItems(items, itemType, itemsFieldName);
    elm.addElement(itemsElement);
  }

  public static void loadField(Object obj, String fieldName, XElement elm, XContext ctx) {
    Field field = getField(obj.getClass(), fieldName);
    XElement fieldElement = elm.getChild(fieldName);

    Object value = ctx.loader.loadObject(fieldElement, field.getType());

    field.setAccessible(true);
    try {
      field.set(obj, value);
    } catch (IllegalAccessException e) {
      throw new SimPersistenceExeption(sf("Unable to set field '%s.%s'.", obj.getClass().getName(), fieldName), e);
    }
    field.setAccessible(false);
  }

  public static void loadFieldItems(Object obj, String itemsFieldName, Object itemsContainer, Class<?> itemType, XElement elm, XContext ctx) {
    Field field = getField(obj.getClass(), itemsFieldName);
    XElement itemsElement = elm.getChild(itemsFieldName);

    ctx.loader.loadItems(itemsElement, itemsContainer, itemType);
    setFieldValue(obj, field, itemsContainer);
  }

  private static void setFieldValue(Object obj, Field field, Object val) {
    try {
      field.setAccessible(true);
      field.set(obj, val);
      field.setAccessible(false);
    } catch (IllegalAccessException e) {
      throw new SimPersistenceExeption(sf("Failed to set value '%s' into '%s.%s'.", val, obj.getClass().getName(), field.getName()), e);
    }
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
      fieldElement.setAttribute(Constants.TYPE_ATTRIBUTE, value.getClass().getName());
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
            .where(q -> q.getAnnotationsByType(XIgnored.class).length == 0)
            .where(q -> q.getName().equals(Constants.INNER_CLASS_REFERENCE_FIELD_NAME) == false)
            .select(q -> q.getName())
            .toSet();
    return ret;
  }
}