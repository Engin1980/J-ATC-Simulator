package eng.newXmlUtils.implementations;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.ReflectionUtils;
import eng.newXmlUtils.EXmlException;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.base.Formatter;
import eng.newXmlUtils.base.Serializer;
import eng.newXmlUtils.utils.InternalXmlUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ObjectSerializer implements Serializer {

  private static final String OBJECT_SERIALIZER_VALUE_SET = "__OBJECT_SERIALIZER_VALUE_SET";

  private final IMap<String, Serializer> customFieldSerializers = new EMap<>();
  private Class<?> expectedClass;
  private boolean expectedTypeIncludingSubclasses;

  public ObjectSerializer withCustomFieldSerializer(String fieldName, Serializer serializer) {
    customFieldSerializers.set(fieldName, serializer);
    return this;
  }

  public <T> ObjectSerializer withCustomFieldFormatter(String fieldName, Formatter<T> formatter) {
    customFieldSerializers.set(fieldName, formatter.toSerializer());
    return this;
  }

  public ObjectSerializer withIgnoredFields(String... fieldNames) {
    for (String fieldName : fieldNames) {
      this.customFieldSerializers.set(fieldName, null);
    }
    return this;
  }

  public ObjectSerializer withIgnoredFields(Iterable<String> fieldNames) {
    for (String fieldName : fieldNames) {
      this.customFieldSerializers.set(fieldName, null);
    }
    return this;
  }

  public ObjectSerializer withValueClassCheck(Class<?> expectedValueClass, boolean includeSubclasses) {
    this.expectedClass = expectedValueClass;
    this.expectedTypeIncludingSubclasses = includeSubclasses;
    return this;
  }

  public ObjectSerializer withValueClassCheck(Class<?> expectedValueClass) {
    return this.withValueClassCheck(expectedValueClass, false);
  }

  @Override
  public void invoke(XElement element, Object value, XmlContext xmlContext) {
    validateExpectedTypeIfRequired(value);
    preCyclicSerializationCheck(value, xmlContext);

    IReadOnlyList<Field> fields = ReflectionUtils.ClassUtils.getFields(value.getClass()).where(q -> !Modifier.isStatic(q.getModifiers()));

    for (Field field : fields) {
      if (field.getName().equals("this$0")) continue; // ignores internal field of inner class
      storeField(element, value, field, xmlContext);
    }

    InternalXmlUtils.saveType(element, value);

    postCyclicSerializationCheck(value, xmlContext);
  }

  private void postCyclicSerializationCheck(Object value, XmlContext xmlContext) {
    ISet<Object> objectSerializerSet = (ISet<Object>) xmlContext.values.get(OBJECT_SERIALIZER_VALUE_SET);
    objectSerializerSet.remove(value);
  }

  private void preCyclicSerializationCheck(Object value, XmlContext xmlContext) {
    ISet<Object> objectSerializerSet = (ISet<Object>) xmlContext.values.getOrSet(OBJECT_SERIALIZER_VALUE_SET, () -> new ESet<>());
    if (objectSerializerSet.contains(value))
      throw  new EXmlException(sf("Object-serializer in cyclic serialization of '%s' ('%s').", value, value.getClass()));
    else
      objectSerializerSet.add(value);
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
      deleteTypeAttributeIfNotRequired(field.getType(), fieldElement);
      e.addElement(fieldElement);
    }
  }

  private void deleteTypeAttributeIfNotRequired(Class<?> type, XElement fieldElement) {
    if (fieldElement.hasAttribute(InternalXmlUtils.TYPE_NAME) && type.getName().equals(fieldElement.getAttribute(InternalXmlUtils.TYPE_NAME)))
      fieldElement.removeAttribute(InternalXmlUtils.TYPE_NAME);
  }

  private Serializer getSerializer(Field field, Object fieldValue, XmlContext c) {
    Serializer ret;
    if (customFieldSerializers.containsKey(field.getName()))
      ret = customFieldSerializers.get(field.getName());
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
