package eng.jAtcSimLib.xmlUtils.serializers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.ArrayUtils;
import eng.eSystem.utilites.StringUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSimLib.xmlUtils.ObjectUtils;
import eng.jAtcSimLib.xmlUtils.Serializer;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;
import eng.jAtcSimLib.xmlUtils.XmlUtilsException;

import java.lang.reflect.Field;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class SimpleObjectSerializer implements Serializer<Object> {

  private final Class<?> expectedClass;
  private final String[] includedFieldNames;

  private SimpleObjectSerializer(Class<?> expectedClass, String[] includedFieldNames) {
    this.expectedClass = expectedClass;
    this.includedFieldNames = includedFieldNames;
  }

  public static SimpleObjectSerializer createFor(Class<?> type) {
    IReadOnlyList<Field> fields = ObjectUtils.getFields(type);
    String[] tmp = fields.select(q -> q.getName()).toArray(String.class);
    return new SimpleObjectSerializer(type, tmp);
  }

  public static SimpleObjectSerializer createForTypeWithFields(Class<?> type, String... includedFieldNames) {
    IList<Field> allFields = new EList<>(ObjectUtils.getFields(type));
    IList<String> missingFields = new EList<>(includedFieldNames)
            .where(q -> allFields.isNone(p -> p.getName().equals(q)));

    if (missingFields != null)
      throw new XmlUtilsException("Unable to create SimpleObjectSerializer. " +
              "The following explicitly specified fields are missing" +
              sf("in the type %s: %s.", type.getName(), StringUtils.join(",", missingFields)));

    return new SimpleObjectSerializer(type, includedFieldNames);
  }

  public static SimpleObjectSerializer createForTypeExceptFields(Class<?> type, String... excludedFieldNames) {
    IList<Field> allFields = new EList<>(ObjectUtils.getFields(type));
    IList<String> missingFields = new EList<>(excludedFieldNames)
            .where(q -> allFields.isNone(p -> p.getName().equals(q)));

    if (missingFields != null)
      throw new XmlUtilsException("Unable to create SimpleObjectSerializer. " +
              "The following explicitly specified fields are missing" +
              sf("in the type %s: %s.", type.getName(), StringUtils.join(",", missingFields)));

    IList<Field> includedFields = allFields.where(q -> ArrayUtils.contains(excludedFieldNames, q) == false);
    String[] includedFieldNames = includedFields.select(q -> q.getName()).toArray(String.class);

    return new SimpleObjectSerializer(type, includedFieldNames);
  }

  @Override
  public void invoke(XElement targetElement, Object value) {
    if (value == null)
      XmlSaveUtils.saveNullIntoElement(targetElement);
    else {
      EAssert.isTrue(
              this.expectedClass.isAssignableFrom(value.getClass()),
              sf("This SimpleObjectSerializer expectes type '%s', but got '%s'.",
                      this.expectedClass, value.getClass()));
      XmlSaveUtils.Field.storeFields(targetElement, value, includedFieldNames);
    }
  }
}
