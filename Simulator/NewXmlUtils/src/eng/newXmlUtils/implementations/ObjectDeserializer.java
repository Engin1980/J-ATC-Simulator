package eng.newXmlUtils.implementations;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Consumer2;
import eng.eSystem.utilites.ReflectionUtils;
import eng.newXmlUtils.EXmlException;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.base.Deserializer;
import eng.newXmlUtils.base.InstanceFactory;
import eng.newXmlUtils.utils.InternalObjectUtils;
import eng.newXmlUtils.utils.InternalXmlUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ObjectDeserializer<T> implements Deserializer {

  private static final String PRESERVED_VALUE = "__PRESERVED_VALUE";
  private final IMap<String, Deserializer> customFieldDeserializers = new EMap<>();
  private Class<?> expectedClass;
  private boolean expectedTypeIncludingSubclasses;
  private InstanceFactory<T> instanceFactory = null;
  private final ISet<String> preservedFields = new ESet<>();
  private Consumer2<T, XmlContext> afterLoadAction = null;
  private Consumer2<T, XmlContext> beforeLoadAction = null;

  @Override
  public Object invoke(XElement e, XmlContext c) {
    Object ret;

    Class<?> type = InternalXmlUtils.loadType(e);
    validateValueTypeIfRequired(type);

    ret = getInstance(type, c);

    IReadOnlyList<Field> fields = ReflectionUtils.ClassUtils.getFields(type).where(q -> !Modifier.isStatic(q.getModifiers()));

    if (this.beforeLoadAction != null)
      this.beforeLoadAction.invoke((T) ret, c);

    for (Field field : fields) {
      preserveKeptFieldIfRequired(field, ret, c);
      restoreField(e, ret, field, c);
    }

    if (this.afterLoadAction != null)
      this.afterLoadAction.invoke((T) ret, c);

    return ret;
  }

  public ObjectDeserializer<T> withAfterLoadAction(Consumer2<T, XmlContext> afterLoadAction) {
    this.afterLoadAction = afterLoadAction;
    return this;
  }

  public ObjectDeserializer<T> withBeforeLoadAction(Consumer2<T, XmlContext> beforeLoadAction) {
    this.beforeLoadAction = beforeLoadAction;
    return this;
  }

  public ObjectDeserializer<T> withCustomFieldDeserialization(String fieldName, Deserializer deserializer) {
    customFieldDeserializers.set(fieldName, deserializer);
    return this;
  }

  public ObjectDeserializer<T> withIgnoredFields(Iterable<String> fieldNames) {
    for (String fieldName : fieldNames) {
      this.customFieldDeserializers.set(fieldName, null);
    }
    return this;
  }

  public ObjectDeserializer<T> withIgnoredFields(String... fieldNames) {
    for (String fieldName : fieldNames) {
      this.customFieldDeserializers.set(fieldName, null);
    }
    return this;
  }

  public ObjectDeserializer<T> withInstanceFactory(InstanceFactory<T> instanceFactory) {
    this.instanceFactory = instanceFactory;
    return this;
  }

  public Deserializer withPreservedFields(String... fieldName) {
    this.preservedFields.addMany(fieldName);
    return this;
  }

  public ObjectDeserializer<T> withValueTypValidation(Class<?> expectedValueClass, boolean includeSubclasses) {
    this.expectedClass = expectedValueClass;
    this.expectedTypeIncludingSubclasses = includeSubclasses;
    return this;
  }

  private void preserveKeptFieldIfRequired(Field field, Object o, XmlContext c) {
    if (this.preservedFields.contains(field.getName())) {
      c.values.set(PRESERVED_VALUE, ReflectionUtils.FieldUtils.get(o, field.getName()));
    }
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
    InstanceFactory<?> instanceFactory = this.instanceFactory;
    if (instanceFactory == null && c.values.containsKey(PRESERVED_VALUE)) {
      Object tmp = c.values.get(PRESERVED_VALUE);
      instanceFactory = (cc) -> tmp;
      c.values.remove(PRESERVED_VALUE);
    }
    if (instanceFactory == null)
      instanceFactory = c.sdfManager.tryGetFactory(type);
    if (instanceFactory == null)
      instanceFactory = InternalObjectUtils.tryGetPublicConstructorFactory(type);
    if (instanceFactory == null)
      instanceFactory = InternalObjectUtils.tryGetAnnotatedConstructorFactory(type);
    if (instanceFactory == null)
      throw new EXmlException(sf("Failed to find a way how to instantiate '%s'.", type));
    Object ret;
    try {
      ret = instanceFactory.invoke(c);
    } catch (Exception e) {
      throw new EXmlException(sf("Failed to create a new instance of '%s'.", type), e);
    }
    return ret;
  }

  private void restoreField(XElement e, Object v, Field field, XmlContext c) {
    if (customFieldDeserializers.containsKey(field.getName()) && customFieldDeserializers.get(field.getName()) == null)
      return; // skipped as ignored

    XElement fieldElement;
    try {
      fieldElement = e.getChild(field.getName());
    } catch (Exception exception) {
      throw new EXmlException(sf("Failed to find element for '%s.%s'.", v.getClass().getName(), field.getName()));
    }


    if (fieldElement.getContent().equals(InternalXmlUtils.NULL_CONTENT))
      setFieldValue(v, field, null);
    else {
      Class<?> fieldType = InternalXmlUtils.tryLoadType(fieldElement);
      if (fieldType == null) {
        fieldType = field.getType();
        InternalXmlUtils.saveType(fieldElement, field.getType());
      }
      Deserializer deserializer = getDeserializer(field, fieldType, c);

      if (deserializer != null) {
        Object fieldValue = deserializer.invoke(fieldElement, c);
        setFieldValue(v, field, fieldValue);
      }
    }
  }

  private Deserializer getDeserializer(Field field, Class<?> fieldType, XmlContext c) {
    Deserializer ret;
    if (this.customFieldDeserializers.containsKey(field.getName()))
      ret = this.customFieldDeserializers.get(field.getName());
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
