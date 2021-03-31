package exml.loading;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.ISet;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Selector;
import eng.eSystem.validation.EAssert;
import exml.*;

import java.lang.reflect.Field;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class XLoadFieldContext {

  private final XLoadContext ctx;
  private final UsedFieldEvidence usedFieldEvidence = new UsedFieldEvidence();

  XLoadFieldContext(XLoadContext ctx) {
    this.ctx = ctx;
  }

  public void ignoreFields(Object obj, String... fieldNames) {
    this.usedFieldEvidence.add(obj, fieldNames);
  }

  public void loadAllRemaningFields(Object obj, XElement elm) {
    EAssert.Argument.isNotNull(obj, "obj");
    EAssert.Argument.isNotNull(elm, "elm");

    ISet<String> remainingFields = this.usedFieldEvidence.getRemainingFields(obj);

    for (String remainingField : remainingFields) {
      this.ctx.fields.loadField(obj, remainingField, elm);
    }

    this.ctx.fields.ignoreFields(obj, remainingFields.toArray(String.class));
  }

  public void loadField(Object obj, String fieldName, XElement elm) {
    Field field = SharedUtils.getField(obj.getClass(), fieldName);

    ctx.log.log("." + fieldName);
    ctx.log.increaseIndent();
    FieldSource source = FieldSource.getFieldSource(field);
    if (source == FieldSource.attribute)
      loadFieldFromAttribute(obj, field, elm);
    else
      loadFieldFromElement(obj, field, elm);
    ctx.log.decreaseIndent();
  }

  public void loadFieldFromAttribute(Object obj, Field field, XElement elm) {
    usedFieldEvidence.add(obj, field.getName());
    FieldObligation obligation = FieldObligation.getFieldObligation(field);
    if (obligation == FieldObligation.ignored) return;
    String attributeValue = elm.tryGetAttribute(field.getName());
    if (attributeValue == null)
      if (obligation == FieldObligation.mandatory)
        throw new XLoadException(sf("Unable to find mandatory attribute for field '%s'.", field.getName()), ctx);
      else
        return;

    Object value;
    if (attributeValue.equals(Constants.NULL)) {
      value = null;
    } else if (ctx.parsers.containsKey(field.getType())) {
      Selector<String, ?> parser = ctx.parsers.get(field.getType());
      value = parser.invoke(attributeValue);
    } else if (field.getType().isEnum()) {
      value = LoadUtils.loadEnum(attributeValue, field.getType());
    } else if (field.getType().isArray()) {
      IList<Object> lst = ctx.objects.loadItems(elm, field.getType().getComponentType());
      value = SharedUtils.convertListToArray(lst, field.getType());
    } else if (IXPersistable.class.isAssignableFrom(field.getType())) {
      value = loadPersistable(elm, field.getType());
    } else {
      throw new XLoadException(sf("No deserializer/parser specified for type '%s' loaded from '%s'.", field.getType(), elm.toXPath()), ctx);
    }

    setFieldValue(obj, field, value);
  }

  public void loadFieldFromElement(Object obj, Field field, XElement elm) {
    usedFieldEvidence.add(obj, field.getName());
    FieldObligation obligation = FieldObligation.getFieldObligation(field);
    if (obligation == FieldObligation.ignored) return;
    XElement fieldElement = elm.tryGetChild(field.getName());

    if (fieldElement == null)
      if (obligation == FieldObligation.mandatory)
        throw new XLoadException(sf("Unable to find mandatory element for field '%s' in '%s'.", field.getName(), elm.toXPath()), ctx);
      else
        return; // ignored or optionals are skipped

    Object value = ctx.objects.loadObject(fieldElement, field.getType());
    setFieldValue(obj, field, value);
  }

  //TODO verify if this is called somewhere?
  public void loadFieldItems(Object obj, String itemsFieldName, Object itemsContainer, Class<?> itemType, XElement elm) {
    usedFieldEvidence.add(obj, itemsFieldName);

    Field field = SharedUtils.getField(obj.getClass(), itemsFieldName);
    XElement itemsElement = elm.getChild(itemsFieldName);

    ctx.objects.loadItems(itemsElement, itemsContainer, itemType);
    setFieldValue(obj, field, itemsContainer);
  }

  private <T> T loadPersistable(XElement elm, Class<T> type) {
    EAssert.Argument.isTrue(IXPersistable.class.isAssignableFrom(type));

    IXPersistable ret;
    ret = (IXPersistable) ConstructionUtils.provideInstance(type, ctx);
    ret.xLoad(elm, this.ctx);
    loadAllRemaningFields(ret, elm);
    ret.xPostLoad(ctx);
    return (T) ret;
  }

  private void setFieldValue(Object obj, Field field, Object val) {
    try {
      field.setAccessible(true);
      field.set(obj, val);
      field.setAccessible(false);
    } catch (IllegalAccessException e) {
      throw new XLoadException(sf("Failed to set value '%s' into '%s.%s'.", val, obj.getClass().getName(), field.getName()), e, ctx);
    }
  }
}
