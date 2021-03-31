package exml.saving;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Consumer2;
import eng.eSystem.functionalInterfaces.Selector;
import exml.Log;
import exml.Values;

import java.util.Map;

public class XSaveContext {
  protected final Log log = new Log();
  protected final IMap<Class<?>, Consumer2<?, XElement>> serializers = new EMap<>();
  protected final IMap<Class<?>, Selector<?, String>> formatters = new EMap<>();
  public final Values values = new Values();
  public final XSaveFieldContext fields = new XSaveFieldContext(this);
  public final XSaveObjectContext objects = new XSaveObjectContext(this);
  protected XElement currentElement;
  protected Object currentObject;

  public void saveFieldItems(Object obj, String fieldName, Class<?> expectedItemType, XElement elm) {
    this.fields.saveFieldItems(obj, fieldName, expectedItemType, elm);
  }

  public XElement saveObject(Object obj, String elementName) {
    XElement ret = new XElement(elementName);
    saveObject(obj, ret);
    return ret;
  }

  public void saveObject(Object obj, XElement elm) {
    this.objects.saveObject(obj, elm);
  }

  public <T> void setFormatter(Class<T> type, Selector<T, String> formatter) {
    this.formatters.set(type, formatter);
  }

  public <T> void setSerializer(Class<T> type, Consumer2<T, XElement> serializer) {
    this.serializers.set(type, serializer);
  }

  public void addDefaultFormatters(){
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

  public XSaveContext withDefaultFormatters(){
    this.addDefaultFormatters();
    return this;
  }

}
