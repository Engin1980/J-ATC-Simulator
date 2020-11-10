package eng.newXmlUtils.implementations;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.ReflectionUtils;
import eng.newXmlUtils.*;
import eng.newXmlUtils.base.Deserializer;
import eng.newXmlUtils.base.InstanceFactory;
import eng.newXmlUtils.utils.XmlUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ObjectDeserializer implements Deserializer {

  private final static boolean MANAGE_ENUMS = true;

  private final IMap<String, Deserializer> customFieldDeserializers = new EMap<>();
  private Class<?> expectedClass;
  private boolean expectedTypeIncludingSubclasses;

  public ObjectDeserializer withCustomFieldDeserialization(String fieldName, Deserializer deserializer) {
    customFieldDeserializers.set(fieldName, deserializer);
    return this;
  }

  public ObjectDeserializer withIgnoredField(String fieldName) {
    this.customFieldDeserializers.set(fieldName, (e, v) -> null);
    return this;
  }

  public ObjectDeserializer withIgnoredFields(String... fieldNames) {
    for (String fieldName : fieldNames) {
      this.withIgnoredField(fieldName);
    }
    return this;
  }

  public ObjectDeserializer withIgnoredFields(Iterable<String> fieldNames) {
    for (String fieldName : fieldNames) {
      this.withIgnoredField(fieldName);
    }
    return this;
  }

  public ObjectDeserializer withValueTypValidation(Class<?> expectedValueClass, boolean includeSubclasses) {
    this.expectedClass = expectedValueClass;
    this.expectedTypeIncludingSubclasses = includeSubclasses;
    return this;
  }

  @Override
  public Object invoke(XElement e, XmlContext c) {
    Object ret;

    Class<?> type = XmlUtils.loadType(e);
    validateValueTypeIfRequired(type);

    ret = getInstance(type, c);

    IReadOnlyList<Field> fields = ReflectionUtils.ClassUtils.getFields(type);
    for (Field field : fields) {
      restoreField(e, ret, field, c);
    }

    return ret;
  }

  private void validateValueTypeIfRequired(Class<?> type) {
    if (expectedClass == null) return;

    if (expectedTypeIncludingSubclasses) {
      if (!this.expectedClass.isAssignableFrom(type))
        throw new EXmlException(sf("ObjectSerializer requires class/subclass of '%s', but got '%s'.", this.expectedClass, type));
    } else {
      if (!this.expectedClass.equals(type))
        throw new EXmlException(sf("ObjectSerializer requires class '%s', but got '%s'.", this.expectedClass, type));
    }
  }

  private Object getInstance(Class<?> type, XmlContext c) {
    InstanceFactory<?> instanceFactory = c.sdfManager.tryGetFactory(type);
    if (instanceFactory == null)
      instanceFactory = getPublicConstructorFactory(type);
    Object ret = instanceFactory.invoke();
    return ret;
  }

  private InstanceFactory<?> getPublicConstructorFactory(Class<?> type) {
    InstanceFactory<?> ret;
    try {
      Constructor<?> ctor = type.getConstructor();
      ret = (() -> {
        try {
          return ctor.newInstance(null);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
          throw new EXmlException(sf("Failed to invoke public parameter-less constructor for type '%s'.", type));
        }
      });
    } catch (NoSuchMethodException e) {
      throw new EXmlException(sf("Failed to get public parameter-less constructor for type '%s'.", type));
    }
    return ret;
  }

  private void restoreField(XElement e, Object v, Field field, XmlContext c) {
    XElement fieldElement = e.getChild(field.getName());
    Class<?> fieldType = XmlUtils.loadType(fieldElement);
    Deserializer deserializer = getDeserializer(field, fieldType, c);
    if (deserializer != null) {
      Object val = deserializer.invoke(fieldElement, c);
      setFieldValue(v, field, val);
    }
  }

  private Deserializer getDeserializer(Field field, Class<?> fieldType, XmlContext c) {
    Deserializer ret;
    if (this.customFieldDeserializers.containsKey(field.getName()))
      ret = this.customFieldDeserializers.get(field.getName());
    else if (MANAGE_ENUMS && fieldType.isEnum())
      ret = (e, ctx) -> Enum.valueOf((Class<Enum>) fieldType, e.getContent());
    else
      ret = c.sdfManager.getDeserializer(fieldType);
    return ret;
  }

  private void setFieldValue(Object object, Field field, Object value) {
    try {
      field.setAccessible(true);
      field.set(object, value);
      field.setAccessible(false);
    } catch (Exception ex) {
      throw new EXmlException(sf("Failed to set value '%s' ('%s') into '%s.%s'.",
              value, value == null ? "null" : value.getClass(),
              field.getDeclaringClass(),
              field.getName()));
    }
  }
}
