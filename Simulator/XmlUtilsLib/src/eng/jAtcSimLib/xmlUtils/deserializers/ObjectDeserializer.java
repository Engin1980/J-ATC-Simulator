package eng.jAtcSimLib.xmlUtils.deserializers;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Producer;
import eng.jAtcSimLib.xmlUtils.*;
import eng.jAtcSimLib.xmlUtils.serializers.DefaultXmlNames;

import java.util.Map;

import static eng.eSystem.utilites.FunctionShortcuts.coalesce;
import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ObjectDeserializer implements Deserializer {

  public static ObjectDeserializer createEmpty() {
    return new ObjectDeserializer(null);
  }

  public static ObjectDeserializer createFor(Class<?> type) {
    return new ObjectDeserializer(type);
  }

  public static ObjectDeserializer createDeepDeserializer() {
    ObjectDeserializer ret = ObjectDeserializer.createEmpty();
    ret.useDefaultDeserializer(ret);
    return ret;
  }

  public ObjectDeserializer excludeFields(String ... fieldNames) {
    this.excludedFields.addMany(fieldNames);
    return this;
  }

  public ObjectDeserializer useDefaultDeserializer(Deserializer deserializer) {
    this.defaultDeserializer = deserializer;
    return this;
  }

  public <T> ObjectDeserializer useInstanceProvider(Class<T> type, Producer<T> instanceProvider) {
    this.instanceProviders.set(type, instanceProvider);
    return this;
  }

  public ObjectDeserializer useInstanceProviders(IMap<Class<?>, Producer<?>> instanceProviders) {
    for (Map.Entry<Class<?>, Producer<?>> entry : instanceProviders) {
      this.instanceProviders.set(entry.getKey(), entry.getValue());
    }
    return this;
  }

  public <T> ObjectDeserializer useDeserializer(Class<T> type, Deserializer deserializer) {
    this.customDeserializers.set(type, deserializer);
    return this;
  }

  public <T> ObjectDeserializer useParser(Class<T> type, Parser parser) {
    this.customDeserializers.set(type, parser.toDeserializer());
    return this;
  }

  public ObjectDeserializer useDeserializers(IMap<Class<?>, Deserializer> deserializers) {
    for (Map.Entry<Class<?>, Deserializer> entry : deserializers) {
      this.customDeserializers.set(entry.getKey(), entry.getValue());
    }
    return this;
  }

  public ObjectDeserializer useFormatters(IMap<Class<?>, Parser> parsers) {
    for (Map.Entry<Class<?>, Parser> entry : parsers) {
      this.customDeserializers.set(entry.getKey(), entry.getValue().toDeserializer());
    }
    return this;
  }

  private final IMap<Class<?>, Producer<?>> instanceProviders = new EMap<>();
  private final IMap<Class<?>, Deserializer> customDeserializers = new EMap<>();
  private Deserializer defaultDeserializer = null;
  private final ISet<String> excludedFields = new ESet<>();
  private final Class<?> type;

  private ObjectDeserializer(Class<?> type) {
    this.type = type;
  }

  @Override
  public Object deserialize(XElement element) {
    Object ret;
    if (element.getContent().equals(DefaultXmlNames.NULL_CONTENT))
      ret = null;
    else {
      Class<?> expectedType = getExpectedType(element);

      if (expectedType == null)
        throw new XmlUtilsException(sf("Object-Deserializer unable to deserialize from xml-element '%s'. Neither default type attribute nor xml-attribute for type set.", element.getName()));

      IReadOnlySet<String> fieldNames = ObjectUtils.getFieldNames(expectedType);

      ret = XmlLoadUtils.Class.provideInstance(type);
      XmlLoadUtils.Field.restoreFields(element, ret, fieldNames, customDeserializers, defaultDeserializer, instanceProviders);
    }
    return ret;
  }

  private Class<?> getExpectedType(XElement element) {
    Class<?> ret = XmlLoadUtils.Class.loadType(element, this.type);
    return ret;
  }
}
