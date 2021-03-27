package exml.saving;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.ISet;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Selector;
import eng.eSystem.validation.EAssert;
import exml.*;

import java.lang.reflect.Field;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class XSaveFieldContext {
  private static class ProcessFieldInfo {
    public static ProcessFieldInfo createSkipped() {
      ProcessFieldInfo ret = new ProcessFieldInfo();
      ret.processField = false;
      ret.field = null;
      ret.value = null;
      return ret;
    }

    public static ProcessFieldInfo createUsed(Field field, Object value) {
      ProcessFieldInfo ret = new ProcessFieldInfo();
      ret.processField = true;
      ret.field = field;
      ret.value = value;
      return ret;
    }

    public boolean processField;
    public Field field;
    public Object value;

    private ProcessFieldInfo() {
    }
  }

  private final XSaveContext ctx;
  private final UsedFieldEvidence usedFieldEvidence = new UsedFieldEvidence();

  XSaveFieldContext(XSaveContext ctx) {
    this.ctx = ctx;
  }

  public void ignoreFields(Object obj, String... fieldNames) {
    this.usedFieldEvidence.add(obj, fieldNames);
  }

  public void saveField(Object obj, String fieldName, XElement elm) {
    Field field = SharedUtils.getField(obj.getClass(), fieldName);

    ctx.log.log("." + fieldName);
    ctx.log.increaseIndent();

    FieldSource source = FieldSource.getFieldSource(field);
    if (source == FieldSource.attribute)
      saveFieldToAttribute(obj, fieldName, elm);
    else
      saveFieldToElement(obj, fieldName, elm);

    ctx.log.decreaseIndent();
  }

  @Deprecated
  public void saveFieldEntries(Object obj, String entriesFieldName, Class<?> keyType, Class<?> valueType, XElement elm) {
    throw new UnsupportedOperationException("I think this is not used.");
//    FieldUtils.saveFieldEntries(obj, entriesFieldName, keyType, valueType, elm, ctx);
//    usedFields.getOrSet(obj, () -> new ESet<>()).add(entriesFieldName);
  }

  public void saveFieldItems(Object obj, String itemsFieldName, Class<?> itemType, XElement elm) {
    ProcessFieldInfo pfi = prepareFieldToProcess(obj, itemsFieldName);
    if (pfi.processField == false)
      return;

    XElement fieldElement = new XElement(pfi.field.getName());
    EAssert.Argument.isTrue(pfi.value instanceof Iterable,
            sf("Value of '%s.%s' must be Iterable, found '%s'.", obj.getClass(), itemsFieldName, pfi.value));
    Iterable<?> items = (Iterable<?>) pfi.value;
    ctx.objects.saveItems(items, itemType, fieldElement);
    elm.addElement(fieldElement);
  }

  public void saveFieldToAttribute(Object obj, String fieldName, XElement elm) {
    ProcessFieldInfo pfi = prepareFieldToProcess(obj, fieldName);
    if (pfi.processField == false) return;

    if (pfi.value == null) {
      elm.setAttribute(pfi.field.getName(), Constants.NULL);
    } else if (ctx.formatters.containsKey(obj.getClass())) {
      Selector<Object, String> formatter = (Selector<Object, String>) ctx.formatters.get(obj.getClass());
      String s = formatter.invoke(pfi.value);
      elm.setAttribute(pfi.field.getName(), s);
    } else if (pfi.value.getClass().isEnum()) {
      String s = pfi.value.toString();
      elm.setAttribute(pfi.field.getName(), s);
    } else if (pfi.value.getClass().isArray()) {
      IList<Object> lst = SharedUtils.convertArrayToList(pfi.value);
      ctx.objects.saveItems(lst, pfi.value.getClass().getComponentType(), elm);
    } else if (pfi.value instanceof IXPersistable) {
      IXPersistable persistable = (IXPersistable) pfi.value;
      persistable.save(elm, this.ctx); // calls custom overload
      ctx.fields.saveRemainingFields(persistable, elm); // calls global save
    } else {
      throw new XSaveException(sf("Don't know how to save instance of '%s'.", pfi.value.getClass()), ctx);
    }
  }

  public void saveFieldToElement(Object obj, String fieldName, XElement elm) {
    ProcessFieldInfo pfi = prepareFieldToProcess(obj, fieldName);
    if (pfi.processField == false)
      return;

    XElement fieldElement = new XElement(pfi.field.getName());
    ctx.objects.saveObject(pfi.value, fieldElement, pfi.field.getType());
    elm.addElement(fieldElement);
  }

  public void saveRemainingFields(IXPersistable obj, XElement elm) {
    EAssert.Argument.isNotNull(obj, "obj");
    EAssert.Argument.isNotNull(elm, "elm");

    //TODO replace next try-get second parameter with lambda
    ISet<String> remainingFields = usedFieldEvidence.getRemainingFields(obj);

    for (String remainingField : remainingFields) {
      this.saveField(obj, remainingField, elm);
    }

    usedFieldEvidence.add(obj, remainingFields);
  }

  private ProcessFieldInfo prepareFieldToProcess(Object obj, String fieldName) {
    ProcessFieldInfo ret;
    usedFieldEvidence.add(obj, fieldName);
    Field field = SharedUtils.getField(obj.getClass(), fieldName);
    FieldObligation obligation = FieldObligation.getFieldObligation(field);
    if (obligation == FieldObligation.ignored)
      ret = ProcessFieldInfo.createSkipped();
    else {
      Object fieldValue = getFieldValue(obj, field);
      if (obligation == FieldObligation.optional && fieldValue == null)
        ret = ProcessFieldInfo.createSkipped();
      else
        ret = ProcessFieldInfo.createUsed(field, fieldValue);
    }
    return ret;
  }

  private Object getFieldValue(Object obj, Field field) {
    Object ret;

    try {
      field.setAccessible(true);
      ret = field.get(obj);
      field.setAccessible(false);
    } catch (IllegalAccessException e) {
      throw new XSaveException(sf("Failed to load field '%s' value from '%s'.", field.getName(), obj.getClass()), e, ctx);
    }
    return ret;
  }
}
