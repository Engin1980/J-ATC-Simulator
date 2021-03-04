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
  protected final XSaveFieldContext fields = new XSaveFieldContext(this);
  protected final XSaveObjectContext objects = new XSaveObjectContext(this);
  protected XElement currentElement;
  protected Object currentObject;

  public <T extends Iterable<Map.Entry<K, V>>, K, V> void saveEntries(T lst, Class<K> expectedKeyType, Class<V> expectedValueType, XElement elm) {
    this.objects.saveEntries(lst, expectedKeyType, expectedValueType, elm);
  }

  public void saveFieldItems(Object obj, String fieldName, Class<?> expectedItemType, XElement elm) {
    this.fields.saveFieldItems(obj, fieldName, expectedItemType, elm);
  }

  public <T extends Iterable<?>> void saveItems(T items, Class<?> expectedItemType, XElement elm) {
    this.objects.saveItems(items, expectedItemType, elm);
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

}
