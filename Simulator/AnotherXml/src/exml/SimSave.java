package exml;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.ESet;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.ISet;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Consumer2;
import eng.eSystem.functionalInterfaces.Selector;
import eng.eSystem.utilites.ReflectionUtils;
import eng.eSystem.validation.EAssert;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class SimSave {

  private static final String NULL = "--null--";
  public final XContext ctx;
  public final IMap<Class<?>, Consumer2<?, XElement>> serializers = new EMap<>();
  public final ISet<Object> processedObjects = new ESet<>();

  private int indent = 0;

  SimSave(XContext ctx) {
    this.ctx = ctx;
  }

  private final IMap<Object, ISet<String>> usedFields = new EMap<>();

  public void saveField(Object obj, String fieldName, XElement elm) {
    log("." + fieldName);
    indent++;
    FieldUtils.saveField(obj, fieldName, elm, this.ctx);
    usedFields.getOrSet(obj, () -> new ESet<>()).add(fieldName);
    indent--;
  }

  public void saveFieldItems(Object obj, String itemsFieldName, Class<?> itemType, XElement elm) {
    FieldUtils.saveFieldItems(obj, itemsFieldName, itemType, elm, ctx);
    usedFields.getOrSet(obj, () -> new ESet<>()).add(itemsFieldName);
  }

  public void saveItems(Iterable<?> items, Class<?> itemType, XElement parentElement, String itemsElementName) {
    XElement elm = saveItems(items, itemType, itemsElementName);
    parentElement.addElement(elm);
  }

  public void saveItems(Iterable<?> items, Class<?> itemType, XElement elm) {
    for (Object item : items) {
      XElement itemElement = ctx.saver.saveObject(item, "item");
      if (itemType != null && TypeUtils.isTypeSame(item.getClass(), itemType) == false)
        itemElement.setAttribute("__type", item.getClass().getName());
      elm.addElement(itemElement);
    }
  }

  public XElement saveItems(Iterable<?> items, Class<?> itemType, String itemsElementName) {
    XElement elm = new XElement(itemsElementName);
    this.saveItems(items, itemType, elm);
    return elm;
  }

  public void saveValue(Object obj, String elementName, XElement elm) {
    XElement valEl = saveValue(obj, elementName);
    elm.addElement(valEl);
  }

  public XElement saveValue(Object obj, String elementName) {
    XElement ret = new XElement(elementName);
    this.saveObject(obj, ret);
    return ret;
  }

  public <T> void setSerializer(Class<T> cls, Consumer2<T, XElement> serializer) {
    this.serializers.set(cls, serializer);
  }

  public <T> void setFormatter(Class<T> cls, Selector<T, String> formatter) {
    this.serializers.set(cls, (T o, XElement e) -> e.setContent(formatter.invoke(o)));
  }

  public void saveRemainingFields(ISimPersistable obj, XElement elm) {
    EAssert.Argument.isNotNull(obj, "obj");
    EAssert.Argument.isNotNull(elm, "elm");

    //TODO replace next try-get second parameter with lambda
    ISet<String> remainingFields = FieldUtils.getRemainingFields(obj.getClass(), usedFields.tryGet(obj, new ESet<>()));

    for (String remainingField : remainingFields) {
      FieldUtils.saveField(obj, remainingField, elm, ctx);
    }

    usedFields.getOrSet(obj, new ESet<>()).addMany(remainingFields);
  }

  public void ignoreFields(ISimPersistable obj, String... fieldNames) {
    this.usedFields.getOrSet(obj, new ESet<>()).addMany(fieldNames);
  }

  public XElement saveObject(Object obj, String elementName) {
    XElement ret = new XElement(elementName);
    saveObject(obj, ret);
    return ret;
  }

  public void saveObject(Object obj, XElement elm) {
    if (obj != null
            && obj.getClass().isEnum() == false
            && (obj instanceof String) == false
            && ReflectionUtils.ClassUtils.isPrimitiveOrWrappedPrimitive(obj.getClass()) == false) {
      if (isAlreadyProcessedObject(obj))
        throw new SimPersistenceExeption(sf("Object '%s' (%s) is already being saved (cyclic dependency).", obj, obj.getClass()));
      else
        processedObjects.add(obj);
    }

    log("%s", obj);
    log("  --> %s", elm.toXPath());
    indent++;
    if (obj == null) {
      elm.setContent(NULL);
    } else if (serializers.containsKey(obj.getClass())) {
      Consumer2<Object, XElement> serializer = (Consumer2<Object, XElement>) serializers.get(obj.getClass());
      serializer.invoke(obj, elm);
    } else if (obj.getClass().isEnum()) {
      elm.setContent(obj.toString());
    } else if (obj instanceof ISimPersistable) {
      ISimPersistable persistable = (ISimPersistable) obj;
      persistable.save(elm, this.ctx);
    } else {
      throw new SimPersistenceExeption(sf("Don't know how to save instance of '%s'.", obj.getClass()));
    }
    indent--;
  }

  private boolean isAlreadyProcessedObject(Object obj) {
    for (Object processedObject : this.processedObjects) {
      if (processedObject == obj)
        return true;
    }
    return false;
  }

  private void log(String s, Object... params) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < indent; i++) {
      sb.append(" ");
    }

    sb.append(String.format(s, params));

    System.out.println(sb.toString());
  }
}
