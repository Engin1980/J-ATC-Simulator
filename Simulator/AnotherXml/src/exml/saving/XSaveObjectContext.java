package exml.saving;

import eng.eSystem.collections.ESet;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.ISet;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Consumer2;
import eng.eSystem.functionalInterfaces.Selector;
import eng.eSystem.utilites.ReflectionUtils;
import exml.Constants;
import exml.IXPersistable;
import exml.SharedUtils;

import java.util.Map;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class XSaveObjectContext {
  private final XSaveContext ctx;
  private final ISet<Object> processedObjects = new ESet<>();

  XSaveObjectContext(XSaveContext ctx) {
    this.ctx = ctx;
  }

  public <K, V> void saveEntries(Iterable<Map.Entry<K, V>> entries, Class<K> keyType, Class<V> valueType, XElement parentElement, String itemsElementName) {
    XElement elm = saveEntries(entries, keyType, valueType, itemsElementName);
    parentElement.addElement(elm);
  }

  public <K, V> void saveEntries(Iterable<Map.Entry<K, V>> entries, Class<K> keyType, Class<V> valueType, XElement elm) {
    for (Map.Entry<K, V> entry : entries) {
      K key = entry.getKey();
      XElement keyElement = new XElement(Constants.KEY_ELEMENT);
      saveObject(key, keyElement, keyType);

      V value = entry.getValue();
      XElement valueElement = new XElement(Constants.VALUE_ELEMENT);
      saveObject(value, valueElement, valueType);

      XElement entryElement = new XElement(Constants.ENTRY_ELEMENT);
      entryElement.addElement(keyElement);
      entryElement.addElement(valueElement);

      elm.addElement(entryElement);
    }
  }

  public <K, V> XElement saveEntries(Iterable<Map.Entry<K, V>> entries, Class<K> keyType, Class<V> valueType, String entriesElementName) {
    XElement elm = new XElement(entriesElementName);
    this.saveEntries(entries, keyType, valueType, elm);
    return elm;
  }

  public void saveItems(Iterable<?> items, Class<?> itemType, XElement parentElement, String itemsElementName) {
    XElement elm = saveItems(items, itemType, itemsElementName);
    parentElement.addElement(elm);
  }

  public void saveItems(Iterable<?> items, Class<?> itemType, XElement elm) {
    for (Object item : items) {
      XElement itemElement = new XElement(Constants.ITEM_ELEMENT);
      saveObject(item, itemElement, itemType);
      elm.addElement(itemElement);
    }
  }

  public XElement saveItems(Iterable<?> items, Class<?> itemType, String itemsElementName) {
    XElement elm = new XElement(itemsElementName);
    this.saveItems(items, itemType, elm);
    return elm;
  }


  public void saveObject(Object obj, XElement elm, Class<?> expectedObjectType){
    saveObject(obj, elm);
    addTypeAttributeIfRequired(elm, obj, expectedObjectType);
  }

  public void saveObject(Object obj, XElement elm) {
    if (isObjectToCheckCyclicSave(obj)) {
      if (processedObjects.contains(obj))
        throw new XSaveException(sf("Object '%s' (%s) is already being saved (cyclic dependency).", obj, obj.getClass()), ctx);
      else
        processedObjects.add(obj);
    }

    ctx.log.log("%s", obj);
    ctx.log.increaseIndent();

    if (obj == null) {
      elm.setContent(Constants.NULL);
    } else if (ctx.serializers.containsKey(obj.getClass())) {
      Consumer2<Object, XElement> serializer = (Consumer2<Object, XElement>) ctx.serializers.get(obj.getClass());
      serializer.invoke(obj, elm);
    } else if (ctx.formatters.containsKey(obj.getClass())) {
      Selector<Object, String> formatter = (Selector<Object, String>) ctx.formatters.get(obj.getClass());
      String s = formatter.invoke(obj);
      elm.setContent(s);
    } else if (obj.getClass().isEnum()) {
      elm.setContent(obj.toString());
    } else if (obj.getClass().isArray()) {
      IList<Object> lst = SharedUtils.convertArrayToList(obj);
      saveItems(lst, obj.getClass().getComponentType(), elm);
    } else if (obj instanceof IXPersistable) {
      IXPersistable persistable = (IXPersistable) obj;
      persistable.xSave(elm, this.ctx); // calls custom overload
      ctx.fields.saveRemainingFields(persistable, elm); // calls global save
    } else {
      throw new XSaveException(sf("Don't know how to save instance of '%s'.", obj.getClass()), ctx);
    }

    ctx.log.decreaseIndent();
    if (isObjectToCheckCyclicSave(obj))
      processedObjects.remove(obj);
  }

  private boolean isObjectToCheckCyclicSave(Object obj) {
    return obj != null
            && obj.getClass().isEnum() == false
            && (obj instanceof String) == false
            && ReflectionUtils.ClassUtils.isPrimitiveOrWrappedPrimitive(obj.getClass()) == false;
  }

  private void addTypeAttributeIfRequired(XElement elm, Object obj, Class<?> itemType) {
    if (obj != null && itemType != null && SaveUtils.isTypeSame(obj.getClass(), itemType) == false)
      elm.setAttribute(Constants.TYPE_ATTRIBUTE, obj.getClass().getName());
  }
}
