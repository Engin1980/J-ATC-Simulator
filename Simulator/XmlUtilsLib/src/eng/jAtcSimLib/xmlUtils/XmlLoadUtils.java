package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlySet;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Producer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class XmlLoadUtils {

  public static class Class {

    public static <T> T provideInstance(java.lang.Class<T> type) {
      T ret = provideInstance(type, null);
      return ret;
    }

    public static <T> T provideInstance(java.lang.Class<T> type, IMap<java.lang.Class<?>, Producer<?>> instanceProviders) {
      T ret;
      if (instanceProviders != null && instanceProviders.containsKey(type))
        try {
          ret = (T) instanceProviders.get(type).invoke();
        } catch (Exception e) {
          throw new XmlUtilsException(sf("Custom instance-provider for type '%s' provided, but invoked an error.", type.getName()), e);
        }
      else {
        try {
          Constructor<T> ctor = type.getConstructor();
          ret = ctor.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
          throw new XmlUtilsException(sf("Failed to create an instance using public parameterless constructor of type '%s'.", type.getName()), e);
        }
      }

      return ret;
    }
  }

  public static class Field {

    public static void restoreField(XElement element, Object target, String fieldName, Parser parser) {
      restoreField(element, target, fieldName, parser.toDeserializer());
    }

    public static void restoreField(XElement element, Object target, String fieldName, Deserializer deserializer) {
      java.lang.reflect.Field field = getFieldByName(target.getClass(), fieldName);
      restoreField(element, target, field, deserializer);
    }

    public static <T> void restoreField(XElement element, T ret, String fieldName, IMap<java.lang.Class<?>, Deserializer> customDeserializers, Deserializer defaultDeserializer, IMap<java.lang.Class<?>, Producer<?>> instanceProviders) {
      java.lang.reflect.Field field = getFieldByName(ret.getClass(), fieldName);
      Deserializer deserializer = customDeserializers.tryGet(field.getType());
      if (deserializer == null)
        deserializer = XmlFieldHelper.tryGetDefaultDeserializer(field.getType());
      if (deserializer == null)
        deserializer = defaultDeserializer;

      restoreField(element, ret, field, deserializer);
    }

    public static void restoreField(XElement element, Object target, String fieldName) {
      restoreField(element, target, fieldName, new EMap<>(), null, new EMap<>());
    }

    public static <T> void restoreFields(XElement element, T ret, IReadOnlySet<String> fieldNames, IMap<java.lang.Class<?>, Deserializer> customDeserializers, Deserializer defaultDeserializer, IMap<java.lang.Class<?>, Producer<?>> instanceProviders) {
      for (String fieldName : fieldNames) {
        restoreField(element, ret, fieldName, customDeserializers, defaultDeserializer, instanceProviders);
      }
    }

    private static java.lang.reflect.Field getFieldByName(java.lang.Class<?> type, String fieldName) {
      return ObjectUtils.getFields(type).getFirst(q -> q.getName().equals(fieldName));
    }

    private static <T> void restoreField(XElement sourceElement, T targetObject, java.lang.reflect.Field field, Deserializer deserializer) {
      XElement fieldElement = sourceElement.getChild(field.getName());
      Object value = deserializer.deserialize(fieldElement, field.getType());
      field.setAccessible(true);
      try {
        field.set(targetObject, value);
      } catch (IllegalAccessException e) {
        throw new XmlUtilsException(sf("Failed to set value '%s' into field '%s' of '%s'.",
                value,
                field.getName(),
                targetObject.getClass()), e);
      }
      field.setAccessible(false);
    }
  }
}
