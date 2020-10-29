package eng.jAtcSimLib.xmlUtils.serializers.recursiveObjectSerializer;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSimLib.xmlUtils.Formatter;
import eng.jAtcSimLib.xmlUtils.ObjectUtils;
import eng.jAtcSimLib.xmlUtils.Serializer;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;
import eng.jAtcSimLib.xmlUtils.formatters.CoordinateFormatter;
import eng.jAtcSimLib.xmlUtils.serializers.DefaultXmlNames;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

public class RecursiveObjectSerializer<T> implements Serializer<T> {

  private final IMap<Class<?>, Serializer<?>> customSerializers = new EMap<>();
  private final IMap<Class<?>, Formatter<?>> customFormatters = new EMap<>();

  @Override
  public void invoke(XElement targetElement, T value) {
    serializeValueOrNull(targetElement, value);
  }

  public <V> RecursiveObjectSerializer<T> with(Class<V> type, Formatter<V> formatter) {
    this.customFormatters.set(type, formatter);
    return this;
  }

  public <V> RecursiveObjectSerializer<T> with(Class<V> type, Serializer<V> serializer) {
    this.customSerializers.set(type, serializer);
    return this;
  }

  private void serializeValueOrNull(XElement target, Object value) {
    if (value == null)
      XmlSaveUtils.saveNullIntoElementContent(target);
    else
      serializeValue(target, value);
  }

  private void serializeValue(XElement target, Object value) {
    Class<?> type = value.getClass();
    target.setAttribute(DefaultXmlNames.CLASS_NAME, type.getName());

    if (value instanceof Integer)
      XmlSaveUtils.saveIntoElementContent(target, (Integer) value);
    else if (value instanceof Short)
      XmlSaveUtils.saveIntoElementContent(target, (Short) value);
    else if (value instanceof Byte)
      XmlSaveUtils.saveIntoElementContent(target, (Byte) value);
    else if (value instanceof Long)
      XmlSaveUtils.saveIntoElementContent(target, (Long) value);
    else if (value instanceof Float)
      XmlSaveUtils.saveIntoElementContent(target, (Float) value);
    else if (value instanceof Double)
      XmlSaveUtils.saveIntoElementContent(target, (Double) value);
    else if (value instanceof Boolean)
      XmlSaveUtils.saveIntoElementContent(target, (Boolean) value);
    else if (value instanceof Character)
      XmlSaveUtils.saveIntoElementContent(target, (Character) value);
    else if (value instanceof String)
      XmlSaveUtils.saveIntoElementContent(target, (String) value);
    else if (value.getClass().isEnum())
      XmlSaveUtils.saveIntoElementContent(target, value.toString());
    else if (value instanceof Iterable)
      serializeItemsValue(target, (Iterable) value);
    else if (value.getClass().isArray())
      serializeArrayValue(target, value);
    else {
      if (this.customSerializers.containsKey(value.getClass()))
        serializeValueUsingCustomSerializer(target, value, (Serializer<Object>) this.customSerializers.get(value.getClass()));
      else if (this.customFormatters.containsKey(value.getClass()))
        serializeValueUsingCustomFormatter(target, value, (Formatter<Object>) this.customFormatters.get(value.getClass()));
      else
        serializeObjectValue(target, value);
    }
  }

  private void serializeValueUsingCustomFormatter(XElement target, Object value, Formatter<Object> formatter) {
    XmlSaveUtils.saveIntoElementContent(target, value, formatter);
  }

  private void serializeValueUsingCustomSerializer(XElement target, Object value, Serializer<Object> serializer) {
    XmlSaveUtils.saveIntoElementContent(target, value, serializer);
  }

  private void serializeArrayValue(XElement target, Object arrayValue) {
    IList<Object> tmp = new EList<>();
    for (int i = 0; i < Array.getLength(arrayValue); i++) {
      Object item = Array.get(arrayValue, i);
      tmp.add(item);
    }
    serializeItemsValue(target, tmp);
  }

  private void serializeItemsValue(XElement target, Iterable<?> items) {
    int count = 0;

    for (Object item : items) {
      XElement itemElement = new XElement(DefaultXmlNames.DEFAULT_ITEM_ELEMENT_NAME);
      this.serializeValueOrNull(itemElement, item);
      target.addElement(itemElement);
      count++;
    }

    target.setAttribute(DefaultXmlNames.ITEMS_COUNT, Integer.toString(count));
  }

  private void serializeObjectValue(XElement target, Object value) {
    ISet<Field> fields = ObjectUtils.getFields(value.getClass());
    for (Field field : fields) {
      Object fieldValue = ObjectUtils.getFieldValue(value, field.getName());
      XElement fieldElement = new XElement(field.getName());
      this.serializeValueOrNull(fieldElement, fieldValue);
      target.addElement(fieldElement);
    }
  }
}
