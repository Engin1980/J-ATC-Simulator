package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Producer;
import eng.eSystem.validation.EAssert;

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

    public static <T> void restoreField(XElement element, T target, String fieldName, IMap<java.lang.Class<?>, Deserializer> customDeserializers, Deserializer defaultDeserializer, IMap<java.lang.Class<?>, Producer<?>> instanceProviders) {
      java.lang.reflect.Field field = getFieldByName(target.getClass(), fieldName);
      Deserializer deserializer = tryGetDeserializer(field.getType(), customDeserializers, defaultDeserializer);

      if (deserializer == null)
        throw new XmlUtilsException(sf("Unable to deserializer field '%s' of type '%s'. No matching deserializer found.",
                field.getName(), target.getClass()));

      restoreField(element, target, field, deserializer);
    }

    public static void restoreField(XElement element, Object target, String fieldName) {
      restoreField(element, target, fieldName, new EMap<>(), null, new EMap<>());
    }

    public static <T> void restoreFields(XElement element, T target, String... fieldNames) {
      restoreFields(element, target, new ESet<>(fieldNames), null, null, null);
    }

    public static <T> void restoreFields(XElement element, T target, IReadOnlySet<String> fieldNames, IMap<java.lang.Class<?>, Deserializer> customDeserializers, Deserializer defaultDeserializer, IMap<java.lang.Class<?>, Producer<?>> instanceProviders) {
      for (String fieldName : fieldNames) {
        restoreField(element, target, fieldName, customDeserializers, defaultDeserializer, instanceProviders);
      }
    }

    public static <T> T loadFieldValue(XElement source, String fieldName, java.lang.Class<T> targetClass) {
      Deserializer deserializer = tryGetDeserializer(targetClass, null, null);
      if (deserializer == null)
        throw new XmlUtilsException(sf("Unable to deserializer for type '%s'. No matching deserializer found.",
                targetClass));
      T ret = loadFieldValue(source, fieldName, deserializer);
      return ret;
    }

    public static <T> T loadFieldValue(XElement source, String fieldName, Deserializer deserializer) {
      EAssert.Argument.isNotNull(deserializer, "deserializer");

      XElement fieldElement = source.getChild(fieldName);

      Object tmp = deserializer.deserialize(fieldElement);
      T ret = (T) tmp;
      return ret;
    }

    public static IList<Object> loadFieldValues(XElement source, java.lang.Class<?> type, String ... fieldNames) {
      IList<Object> ret = new EList<>();
      for (String fieldName : fieldNames) {
        java.lang.reflect.Field field = getFieldByName(type, fieldName);
        Object val = loadFieldValue(source, fieldName, field.getType());
        ret.add(val);
      }
      return ret;
    }

    private static Deserializer tryGetDeserializer(java.lang.Class<?> type, IMap<java.lang.Class<?>, Deserializer> customDeserializers, Deserializer defaultDeserializer) {
      Deserializer deserializer = customDeserializers != null ? customDeserializers.tryGet(type) : null;
      if (deserializer == null)
        deserializer = XmlFieldHelper.tryGetDefaultDeserializer(type);
      if (deserializer == null)
        deserializer = defaultDeserializer;
      return deserializer;
    }

    private static java.lang.reflect.Field getFieldByName(java.lang.Class<?> type, String fieldName) {
      return ObjectUtils.getFields(type).getFirst(q -> q.getName().equals(fieldName));
    }

    private static <T> void restoreField(XElement sourceElement, T target, java.lang.reflect.Field field, Deserializer deserializer) {
      XElement fieldElement = sourceElement.getChild(field.getName());
      Object value = deserializer.deserialize(fieldElement);
      field.setAccessible(true);
      try {
        field.set(target, value);
      } catch (IllegalAccessException e) {
        throw new XmlUtilsException(sf("Failed to set value '%s' into field '%s' of '%s'.",
                value,
                field.getName(),
                target.getClass()), e);
      }
      field.setAccessible(false);
    }
  }
}
