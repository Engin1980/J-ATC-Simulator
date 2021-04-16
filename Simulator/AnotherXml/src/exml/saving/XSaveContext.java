package exml.saving;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Consumer2;
import eng.eSystem.functionalInterfaces.Selector;
import exml.base.XContext;

import java.util.Map;

public class XSaveContext extends XContext {
  private final IMap<Class<?>, Consumer2<?, XElement>> serializers = new EMap<>();
  private final IMap<Class<?>, Selector<?, String>> formatters = new EMap<>();
  /**
   * Used for saving instances' fields.
   */
  public final XSaveFieldContext fields = new XSaveFieldContext(this);
  /**
   * Used for saving instances.
   */
  public final XSaveObjectContext objects = new XSaveObjectContext(this);

  /**
   * Adds formatters for basic java types.
   */
  public void addDefaultFormatters() {
    this.setFormatter(short.class, q -> Short.toString(q));
    this.setFormatter(byte.class, q -> Byte.toString(q));
    this.setFormatter(int.class, q -> Integer.toString(q));
    this.setFormatter(long.class, q -> Long.toString(q));
    this.setFormatter(float.class, q -> Float.toString(q));
    this.setFormatter(double.class, q -> Double.toString(q));
    this.setFormatter(boolean.class, q -> Boolean.toString(q));
    this.setFormatter(char.class, q -> Character.toString(q));
    this.setFormatter(Short.class, q -> Short.toString(q));
    this.setFormatter(Byte.class, q -> Byte.toString(q));
    this.setFormatter(Integer.class, q -> Integer.toString(q));
    this.setFormatter(Long.class, q -> Long.toString(q));
    this.setFormatter(Float.class, q -> Float.toString(q));
    this.setFormatter(Double.class, q -> Double.toString(q));
    this.setFormatter(Boolean.class, q -> Boolean.toString(q));
    this.setFormatter(Character.class, q -> Character.toString(q));
    this.setFormatter(String.class, q -> q);
  }

  /**
   * Returns all formatters.
   * @return Returns all formatters.
   */
  public IReadOnlyMap<Class<?>, Selector<?, String>> getFormatters() {
    return formatters;
  }

  /**
   * Returns all serializers.
   * @return Returns all serializers.
   */
  public IReadOnlyMap<Class<?>, Consumer2<?, XElement>> getSerializers() {
    return serializers;
  }

  /**
   * Saves instance into element. Appends type information.
   * Method distinguishes among several types to select the appropriate function of {@link #objects} property.
   * @param obj Object to save
   * @param elm Target element. Expected to be empty, otherwise other data may be overwritten.
   */
  public void saveObject(Object obj, XElement elm) {
    if (obj instanceof Map)
      this.objects.saveEntries(((java.util.Map) obj).entrySet(), Object.class, Object.class, elm);
    else if (obj instanceof IMap)
      this.objects.saveEntries(((IMap) obj).getEntries(), Object.class, Object.class, elm);
    else if (obj instanceof IList)
      this.objects.saveItems((IList) obj, Object.class, elm);
    else if (obj instanceof java.util.List)
      this.objects.saveItems((java.util.List) obj, Object.class, elm);
    else if (obj instanceof ISet)
      this.objects.saveItems((ISet) obj, Object.class, elm);
    else if (obj instanceof java.util.Set)
      this.objects.saveItems((java.util.Set) obj, Object.class, elm);
    else
      this.objects.saveObject(obj, elm);

    if (obj != null)
      this.objects.saveObjectTypeToElement(elm, obj.getClass());
  }

  /** Crates a new element and saves an instance into it.
   * @param obj Instance to save.
   * @param elementName Name of the newly created element.
   * @return Newly created element.
   */
  public XElement saveObject(Object obj, String elementName) {
    XElement ret = new XElement(elementName);
    this.saveObject(obj, ret);
    return ret;
  }

  /**
   * @param type Type for which this formatter will be used.
   * @param formatter Formatter instance. Represents transformation from instance to string.
   * @param <T> Type for which this formatter will be used.
   */
  public <T> void setFormatter(Class<T> type, Selector<T, String> formatter) {
    this.formatters.set(type, formatter);
  }

  /**
   * @param type Type for which this serializer will be used.
   * @param serializer Serializer instance. Represents transformation from instance to {@link XElement}.
   * @param <T> Type for which this serializer will be used.
   */
  public <T> void setSerializer(Class<T> type, Consumer2<T, XElement> serializer) {
    this.serializers.set(type, serializer);
  }

  /**
   * Fluent design method to insert all default formatters.
   * @return Current instance of XSaveContext
   * @see #addDefaultFormatters()
   */
  public XSaveContext withDefaultFormatters() {
    this.addDefaultFormatters();
    return this;
  }

}
