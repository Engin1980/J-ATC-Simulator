package eng.jAtcSimLib.xmlUtils.serializers;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.StringUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSimLib.xmlUtils.*;

import java.lang.reflect.Field;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class SimpleObjectSerializer<T> implements Serializer<T> {

  public static <T> SimpleObjectSerializer<T> createFor(Class<T> type, boolean includeAllFieldsAutomatically) {
    SimpleObjectSerializer<T> ret = new SimpleObjectSerializer<>(type);
    if (includeAllFieldsAutomatically)
      ret = ret.includeFields(ObjectUtils.getFields(type).select(q -> q.getName()));
    return ret;
  }

  private final Class<? extends T> expectedClass;
  private final ISet<String> includedFieldNames = new ESet<>();
  private final IMap<Class<?>, Formatter<?>> customFormatters = new EMap<>();
  private final IMap<Class<?>, Serializer<?>> customSerializers = new EMap<>();

  private SimpleObjectSerializer(Class<? extends T> expectedClass) {
    this.expectedClass = expectedClass;
  }

  public SimpleObjectSerializer<T> excludeFields(ICollection<String> excludedFieldNames) {
    ISet<Field> invalidFields = ObjectUtils.getFields(this.expectedClass)
            .where(q -> excludedFieldNames.contains(q.getName()) == false).toSet();
    if (invalidFields.isEmpty() == false)
      throwInvalidFieldsException(invalidFields);

    excludedFieldNames.forEach(q -> this.includedFieldNames.tryRemove(q));

    return this;
  }

  public SimpleObjectSerializer<T> includeFields(ICollection<String> includedFieldNames) {
    ISet<Field> invalidFields = ObjectUtils.getFields(this.expectedClass)
            .where(q -> includedFieldNames.contains(q.getName()) == false).toSet();
    if (invalidFields.isEmpty() == false)
      throwInvalidFieldsException(invalidFields);

    this.includedFieldNames.addMany(includedFieldNames);

    return this;
  }

  @Override
  public void invoke(XElement targetElement, Object value) {
    if (value == null)
      XmlSaveUtils.saveNullIntoElementContent(targetElement);
    else {
      EAssert.isTrue(
              this.expectedClass.isAssignableFrom(value.getClass()),
              sf("This SimpleObjectSerializer expects type '%s', but got '%s'.",
                      this.expectedClass, value.getClass()));
      XmlSaveUtils.Field.storeFields(targetElement,
              value, includedFieldNames.toArray(String.class),
              customFormatters, customSerializers);
    }
  }

  public <TType> SimpleObjectSerializer<T> useFormatter(Class<TType> clazz, Formatter<TType> formatter) {
    this.customFormatters.set(clazz, formatter);
    return this;
  }

  public SimpleObjectSerializer<T> useFormatters(IMap<Class<?>, Formatter<?>> formatters) {
    this.customFormatters.setMany(formatters);
    return this;
  }

  public <TType> SimpleObjectSerializer<T> useSerializer(Class<TType> clazz, Serializer<TType> serializer) {
    this.customSerializers.set(clazz, serializer);
    return this;
  }

  public SimpleObjectSerializer<T> useSerializers(IMap<Class<?>, Serializer<?>> serializers) {
    this.customSerializers.setMany(serializers);
    return this;
  }

  private void throwInvalidFieldsException(ISet<Field> invalidFields) {
    throw new XmlUtilsException("Unable to create SimpleObjectSerializer. " +
            "The following explicitly specified fields are missing" +
            sf("in the type %s: %s.", this.expectedClass.getName(), StringUtils.join(",", invalidFields.select(q -> q.getName()))));
  }
}
