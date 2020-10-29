package eng.jAtcSimLib.xmlUtils.serializers;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.StringUtils;
import eng.jAtcSimLib.xmlUtils.*;

import java.lang.reflect.Field;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class SimpleObjectSerializer<T> implements Serializer<T> {

  public static <T> SimpleObjectSerializer<T> createFor(Class<T> type) {
    SimpleObjectSerializer<T> ret = new SimpleObjectSerializer<>(type);
    return ret;
  }

  public static <T> SimpleObjectSerializer<T> create() {
    SimpleObjectSerializer<T> ret = new SimpleObjectSerializer<>(null);
    return ret;
  }

  private final Class<? extends T> expectedClass;
  private boolean generateIncludedFields = true;
  private boolean validateFieldNames = true;
  private boolean storeType = false;
  private ISet<String> includedFieldNames = new ESet<>();
  private ISet<String> excludedFieldNames = new ESet<>();
  private final IMap<Class<?>, Formatter<?>> customFormatters = new EMap<>();
  private final IMap<Class<?>, Serializer<?>> customSerializers = new EMap<>();

  private SimpleObjectSerializer(Class<? extends T> expectedClass) {
    this.expectedClass = expectedClass;
  }

  public SimpleObjectSerializer<T> excludeFields(ICollection<String> excludedFieldNames) {
    excludedFieldNames.forEach(q -> this.includedFieldNames.tryRemove(q));
    return this;
  }

  public SimpleObjectSerializer<T> includeFields(Class<?> type) {
    IReadOnlySet<String> fieldNames = ObjectUtils.getFields(type).select(q -> q.getName());
    this.includeFields(fieldNames);
    return this;
  }

  public SimpleObjectSerializer<T> includeFields(ICollection<String> includedFieldNames) {
    this.generateIncludedFields = false;
    this.includedFieldNames.addMany(includedFieldNames);
    return this;
  }

  @Override
  public void invoke(XElement targetElement, Object value) {
    if (value == null)
      XmlSaveUtils.saveNullIntoElementContent(targetElement);
    else {
      if (this.expectedClass != null && this.expectedClass.equals(value.getClass()) == false)
        throw new XmlUtilsException(sf("This SimpleObjectSerializer expects type '%s', but got '%s'.",
                this.expectedClass, value.getClass()));

      if (storeType)
        targetElement.setAttribute(DefaultXmlNames.CLASS_NAME, value.getClass().getName());

      if (validateFieldNames) {
        validateIncludedFieldNames();
        validateExludedFieldNames();
      }

      ISet<String> fieldNames = generateIncludedFields
              ? ObjectUtils.getFieldNames(value.getClass())
              : this.includedFieldNames;

      fieldNames.tryRemoveMany(excludedFieldNames);

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

  public SimpleObjectSerializer<T> withStoredType() {
    this.storeType = true;
    return this;
  }

  public SimpleObjectSerializer<T> withoutFieldNamesValidation() {
    this.validateFieldNames = false;
    return this;
  }

  private void validateExludedFieldNames() {
    validateFieldNames(this.excludedFieldNames, "There is an error in the exluded-field-names.");
  }

  private void validateFieldNames(ISet<String> fieldNames, String message) {
    ISet<Field> invalidFields = ObjectUtils.getFields(this.expectedClass)
            .where(q -> fieldNames.contains(q.getName()) == false).toSet();
    if (invalidFields.isEmpty() == false)
      throwInvalidFieldsException(message, invalidFields);
  }

  private void validateIncludedFieldNames() {
    validateFieldNames(this.includedFieldNames, "There is an error in the included-field-names.");
  }

  private void throwInvalidFieldsException(String message, ISet<Field> invalidFields) {
    throw new XmlUtilsException(message +
            "The following explicitly specified fields are missing" +
            sf("in the type %s: %s.", this.expectedClass.getName(), StringUtils.join(",", invalidFields.select(q -> q.getName()))));
  }
}
