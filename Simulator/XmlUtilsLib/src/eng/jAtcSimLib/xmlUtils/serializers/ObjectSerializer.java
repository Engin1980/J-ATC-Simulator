package eng.jAtcSimLib.xmlUtils.serializers;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.StringUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSimLib.xmlUtils.*;

import java.util.Map;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ObjectSerializer<T> implements Serializer<T> {

  public static <T> ObjectSerializer<T> createFor(Class<T> type) {
    ObjectSerializer<T> ret = new ObjectSerializer<>(type);
    return ret;
  }

  public static <T> ObjectSerializer<T> createEmpty() {
    ObjectSerializer<T> ret = new ObjectSerializer<>(null);
    return ret;
  }

  public static <T> ObjectSerializer<T> createDeepSerializer() {
    ObjectSerializer<T> ret = new ObjectSerializer<>(null);
    ret = ret
            .useDefaultSerializer((Serializer<Object>) ret)
            .withStoredType();
    return ret;
  }

  private final Class<? extends T> expectedClass;
  private boolean subClassAllowed = false;
  private boolean generateIncludedFields = true;
  private boolean storeType = false;
  private Serializer<Object> defaultSerializer = null;
  private final ISet<String> includedFieldNames = new ESet<>();
  private final ISet<String> excludedFieldNames = new ESet<>();
  private final IMap<Class<?>, Serializer<?>> customSerializers = new EMap<>();

  private ObjectSerializer(Class<? extends T> expectedClass) {
    this.expectedClass = expectedClass;
  }

//  public ObjectSerializer<T> applyRecursivelyOnObjectClass() {
//    this.useDefaultSerializer((Serializer<Object>) this);
//    return this;
//  }

  //TODEL
//  public ObjectSerializer<T> withArraySerializer(Serializer<Object> arraySerializer) {
//    this.arraySerializer = arraySerializer;
//    return this;
//  }

  public ObjectSerializer<T> useForSubclass() {
    EAssert.isTrue(expectedClass != null, "Cannot set 'useForSubclass()' when no expected class specified.");
    this.subClassAllowed = true;
    return this;
  }

  public ObjectSerializer<T> excludeFields(ICollection<String> excludedFieldNames) {
    excludedFieldNames.forEach(q -> this.includedFieldNames.tryRemove(q));
    return this;
  }

  public ObjectSerializer<T> includeFields(Class<?> type) {
    IReadOnlySet<String> fieldNames = ObjectUtils.getFields(type).select(q -> q.getName());
    this.includeFields(fieldNames);
    return this;
  }

  public ObjectSerializer<T> includeFields(ICollection<String> includedFieldNames) {
    this.generateIncludedFields = false;
    this.includedFieldNames.addMany(includedFieldNames);
    return this;
  }

  @Override
  public void invoke(XElement targetElement, Object value) {
    if (value == null) {
      XmlSaveUtils.saveNullIntoElementContent(targetElement);
    } else {
      if (this.expectedClass != null) {
        if (!subClassAllowed && this.expectedClass.equals(value.getClass()) == false)
          throw new XmlUtilsException(sf("This SimpleObjectSerializer expects type '%s', but got '%s'.",
                  this.expectedClass, value.getClass()));
        else if (subClassAllowed && this.expectedClass.isAssignableFrom(value.getClass()) == false)
          throw new XmlUtilsException(sf("This SimpleObjectSerializer expects type '%s' or subclass, but got '%s'.",
                  this.expectedClass, value.getClass()));

        validateIncludedFieldNames();
        validateExludedFieldNames();
      }
      if (storeType)
        targetElement.setAttribute(DefaultXmlNames.CLASS_NAME, value.getClass().getName());

      ISet<String> fieldNames = generateIncludedFields
              ? ObjectUtils.getFieldNames(value.getClass())
              : this.includedFieldNames;

      fieldNames.tryRemoveMany(excludedFieldNames);

      XmlSaveUtils.Field.storeFields(targetElement,
              value, fieldNames.toArray(String.class),
              customSerializers, defaultSerializer);
    }
  }

  public ObjectSerializer<T> useDefaultSerializer(Serializer<Object> defaultSerializer) {
    this.defaultSerializer = defaultSerializer;
    return this;
  }

  public <TType> ObjectSerializer<T> useFormatter(Class<TType> clazz, Formatter<TType> formatter) {
    this.customSerializers.set(clazz, formatter.toSerializer());
    return this;
  }

  public ObjectSerializer<T> useFormatters(IMap<Class<?>, Formatter<?>> formatters) {
    for (Map.Entry<Class<?>, Formatter<?>> entry : formatters) {
      this.customSerializers.set(entry.getKey(), entry.getValue().toSerializer());
    }
    return this;
  }

  public <TType> ObjectSerializer<T> useSerializer(Class<TType> clazz, Serializer<TType> serializer) {
    this.customSerializers.set(clazz, serializer);
    return this;
  }

  public ObjectSerializer<T> useSerializers(IMap<Class<?>, Serializer<?>> serializers) {
    this.customSerializers.setMany(serializers);
    return this;
  }

  public ObjectSerializer<T> withStoredType() {
    this.storeType = true;
    return this;
  }

  private void validateExludedFieldNames() {
    validateFieldNames(this.excludedFieldNames, "There is an error in the excluded-field-names.");
  }

  private void validateFieldNames(ISet<String> fieldNames, String message) {
    ISet<String> trueFieldNames = ObjectUtils.getFieldNames(this.expectedClass);
    ISet<String> invalidFieldNames = fieldNames.where(q -> trueFieldNames.contains(q) == false);
    if (invalidFieldNames.isEmpty() == false)
      throwInvalidFieldsException(message, invalidFieldNames);
  }

  private void validateIncludedFieldNames() {
    validateFieldNames(this.includedFieldNames, "There is an error in the included-field-names.");
  }

  private void throwInvalidFieldsException(String message, ISet<String> invalidFieldNames) {
    throw new XmlUtilsException(message +
            "The following explicitly specified fields are missing" +
            sf("in the type %s: %s.", this.expectedClass.getName(), StringUtils.join(",", invalidFieldNames)));
  }
}
