package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlySet;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ToDoException;
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

    public static void restoreField(XElement element, Object target, String fieldName, Deserializer parser) {
      throw new ToDoException();
    }

    public static <T> void restoreField(XElement element, T ret, String fieldName, IMap<java.lang.Class<?>, Deserializer> customDeserializers, Deserializer defaultDeserializer, IMap<java.lang.Class<?>, Producer<?>> instanceProviders) {
      java.lang.reflect.Field field = ObjectUtils.getFields(ret.getClass()).getFirst(q -> q.getName().equals(fieldName));
      Deserializer deserializer = customDeserializers.tryGet(field.getType());
      if (deserializer == null)
        deserializer = XmlFieldHelper.tryGetDefaultDeserializer(field.getType());
      if (deserializer == null)
        deserializer = defaultDeserializer;

      Object value = deserializer.deserialize(element, field.getType());
      field.setAccessible(true);
      field.set(ret, value);
      field.setAccessible(false);
    }

    public static <T> void restoreFields(XElement element, T ret, IReadOnlySet<String> fieldNames, IMap<java.lang.Class<?>, Deserializer> customDeserializers, Deserializer defaultDeserializer, IMap<java.lang.Class<?>, Producer<?>> instanceProviders) {
      for (String fieldName : fieldNames) {
        restoreField(element, ret, fieldName, customDeserializers, defaultDeserializer, instanceProviders);
      }
    }
  }
}
