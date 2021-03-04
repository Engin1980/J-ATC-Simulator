package exml;

import java.lang.reflect.Field;

public enum FieldSource {
  attribute,
  element;

  public static FieldSource getFieldSource(Field field) {
    if (field.getDeclaredAnnotation(exml.annotations.XAttribute.class) != null)
      return FieldSource.attribute;
    else
      return FieldSource.element;
  }
}
