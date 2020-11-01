package eng.jAtcSimLib.xmlUtils.deserializers;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlySet;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Producer;
import eng.jAtcSimLib.xmlUtils.Deserializer;
import eng.jAtcSimLib.xmlUtils.ObjectUtils;
import eng.jAtcSimLib.xmlUtils.XmlLoadUtils;
import eng.jAtcSimLib.xmlUtils.XmlUtilsException;
import eng.jAtcSimLib.xmlUtils.serializers.DefaultXmlNames;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ObjectDeserializer implements Deserializer {

  private final IMap<Class<?>, Producer<?>> instanceProviders = new EMap<>();
  private final IMap<Class<?>, Deserializer> customDeserializers = new EMap<>();
  private Deserializer defaultDeserializer = null;

  @Override
  public Object deserialize(XElement element, Class<?> type) {
    Object ret;
    if (element.getContent().equals(DefaultXmlNames.NULL_CONTENT))
      ret = null;
    else {
      IReadOnlySet<String> fieldNames = ObjectUtils.getFieldNames(type);

      ret = XmlLoadUtils.Class.provideInstance(type);
      XmlLoadUtils.Field.restoreFields(element, ret, fieldNames, customDeserializers, defaultDeserializer, instanceProviders);
    }
    return ret;
  }
}
