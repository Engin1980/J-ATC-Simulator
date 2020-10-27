package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Consumer2;

/**
 * Represents serializer used to stored data into element
 * @param <T> Type which this serializer is serializing
 */
public interface Serializer<T> extends Consumer2<XElement, T> {

  /**
   * Serializes value into the element
   * @param targetElement target element
   * @param value value to be stored
   */
  @Override
  void invoke(XElement targetElement, T value);
}
