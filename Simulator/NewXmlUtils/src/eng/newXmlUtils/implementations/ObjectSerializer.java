package eng.newXmlUtils.implementations;

import eng.newXmlUtils.EXmlException;
import eng.newXmlUtils.base.Serializer;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.utils.XmlUtils;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.ReflectionUtils;

import java.lang.reflect.Field;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ObjectSerializer implements Serializer {

  private final static boolean MANAGE_ENUMS = true;

  private final IMap<String, Serializer> customFieldSerializers = new EMap<>();
  private Class<?> expectedClass;
  private boolean expectedTypeIncludingSubclasses;

  public ObjectSerializer withCustomFieldSerialization(String fieldName, Serializer serializer) {
    customFieldSerializers.set(fieldName, serializer);
    return this;
  }

  public ObjectSerializer withIgnoredField(String fieldName) {
    this.customFieldSerializers.set(fieldName, null);
    return this;
  }

  public ObjectSerializer withIgnoredFields(String... fieldNames) {
    for (String fieldName : fieldNames) {
      this.withIgnoredField(fieldName);
    }
    return this;
  }

  public ObjectSerializer withIgnoredFields(Iterable<String> fieldNames) {
    for (String fieldName : fieldNames) {
      this.withIgnoredField(fieldName);
    }
    return this;
  }

  public ObjectSerializer withValueTypValidation(Class<?> expectedValueClass, boolean includeSubclasses) {
    this.expectedClass = expectedValueClass;
    this.expectedTypeIncludingSubclasses = includeSubclasses;
    return this;
  }

  @Override
  public void invoke(XElement element, Object value, XmlContext xmlContext) {
    validateExpectedTypeIfRequired(value);

    IReadOnlyList<Field> fields = ReflectionUtils.ClassUtils.getFields(value.getClass());

    for (Field field : fields) {
      storeField(element, value, field, xmlContext);
    }

    XmlUtils.saveType(element, value);
  }

  private void validateExpectedTypeIfRequired(Object value) {
    if (expectedClass == null) return;
    if (value == null) return;

    if (expectedTypeIncludingSubclasses) {
      if (!this.expectedClass.isAssignableFrom(value.getClass()))
        throw new EXmlException(sf("ObjectSerializer requires class/subclass of '%s', but got '%s'.", this.expectedClass, value.getClass()));
    } else {
      if (!this.expectedClass.equals(value.getClass()))
        throw new EXmlException(sf("ObjectSerializer requires class '%s', but got '%s'.", this.expectedClass, value.getClass()));
    }
  }

  private void storeField(XElement e, Object v, Field field, XmlContext c) {
    Object fieldValue = getFieldValue(v, field);
    Serializer serializer = getSerializer(field, fieldValue, c);
    if (serializer != null) {
      XElement fieldElement = new XElement(field.getName());
      serializer.invoke(fieldElement, fieldValue, c);
      e.addElement(fieldElement);
    }
  }

  private Serializer getSerializer(Field field, Object fieldValue, XmlContext c) {
    Serializer ret;
    if (customFieldSerializers.containsKey(field.getName()))
      ret = customFieldSerializers.get(field.getName());
    else if (MANAGE_ENUMS && fieldValue.getClass().isEnum())
      ret = (e, v, ctx) -> e.setContent(v.toString());
    else
      ret = c.sdfManager.getSerializer(fieldValue);
    return ret;
  }

  private Object getFieldValue(Object v, Field field) {
    Object ret;

    try {
      field.setAccessible(true);
      ret = field.get(v);
      field.setAccessible(false);
    } catch (IllegalAccessException e) {
      throw new EXmlException(sf("Failed to get value of '%s.%s'.", field.getClass().getName(), field.getName()));
    }

    return ret;
  }
}
