package exml.saving;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Consumer2;
import eng.eSystem.functionalInterfaces.Selector;
import eng.eSystem.utilites.ReflectionUtils;
import eng.eSystem.validation.EAssert;
import exml.Constants;
import exml.IXPersistable;
import exml.internal.SharedUtils;
import exml.saving.internal.RedundancyChecker;

import java.util.Map;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class XSaveObjectContext {
  private final XSaveContext ctx;
  private final RedundancyChecker redundancyChecker;

  XSaveObjectContext(XSaveContext ctx) {
    this.ctx = ctx;
    this.redundancyChecker = new RedundancyChecker(ctx);
  }

  public <K, V> void saveEntries(Iterable<Map.Entry<K, V>> entries, Class<K> keyType, Class<V> valueType, XElement parentElement, String itemsElementName) {
    XElement elm = saveEntries(entries, keyType, valueType, itemsElementName);
    parentElement.addElement(elm);
  }

  public <K, V> void saveEntries(Iterable<Map.Entry<K, V>> entries, Class<K> keyType, Class<V> valueType, XElement elm) {
    doBeforeObjectSave(entries);

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

    doAfterObjectSave(entries);
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
    doBeforeObjectSave(items);

    for (Object item : items) {
      XElement itemElement = new XElement(Constants.ITEM_ELEMENT);
      saveObject(item, itemElement, itemType);
      elm.addElement(itemElement);
    }

    doAfterObjectSave(items);
  }

  public XElement saveItems(Iterable<?> items, Class<?> itemType, String itemsElementName) {
    XElement elm = new XElement(itemsElementName);
    this.saveItems(items, itemType, elm);
    return elm;
  }

  public void saveObject(Object obj, XElement elm, Class<?> expectedObjectType) {
    saveObject(obj, elm);
    addTypeAttributeIfRequired(elm, obj, expectedObjectType);
  }

  public void saveObject(Object obj, XElement elm) {
    doBeforeObjectSave(obj);

    if (obj == null) {
      elm.setContent(Constants.NULL);
    } else if (ctx.getSerializers().containsKey(obj.getClass())) {
      Consumer2<Object, XElement> serializer = (Consumer2<Object, XElement>) ctx.getSerializers().get(obj.getClass());
      serializer.invoke(obj, elm);
    } else if (ctx.getFormatters().containsKey(obj.getClass())) {
      Selector<Object, String> formatter = (Selector<Object, String>) ctx.getFormatters().get(obj.getClass());
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
      throw new XSaveException(sf("Don't know how to save an instance of '%s'.", obj.getClass()), ctx);
    }

    doAfterObjectSave(obj);
  }

  public void saveObjectTypeToElement(XElement elm, Class<?> type) {
    elm.setAttribute(Constants.TYPE_ATTRIBUTE, type.getName());
  }

  private void doBeforeObjectSave(Object obj) {
    redundancyChecker.add(obj);

    ctx.getLog().logObject(obj);
    ctx.getLog().increaseIndent();
    ctx.getCurrentObject().push(obj);
  }

  private void doAfterObjectSave(Object obj) {
    EAssert.isTrue(ctx.getCurrentObject().pop() == obj, "Current object stack does not contain the correct object on the top!");
    ctx.getLog().decreaseIndent();
    redundancyChecker.remove(obj);
  }

  private void addTypeAttributeIfRequired(XElement elm, Object obj, Class<?> itemType) {
    if (obj != null && itemType != null && isTypeSame(obj.getClass(), itemType) == false)
      saveObjectTypeToElement(elm, obj.getClass());
  }

  private boolean isTypeSame(Class<?> a, Class<?> b) {
    if (a.isPrimitive())
      if (b.isPrimitive())
        return a.equals(b);
      else
        return ReflectionUtils.ClassUtils.tryWrapPrimitive(a).equals(b);
    else if (b.isPrimitive())
      return a.equals(ReflectionUtils.ClassUtils.tryWrapPrimitive(b));
    else
      return a.equals(b);
  }
}
