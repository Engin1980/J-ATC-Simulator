package exml;

import exml.annotations.XIgnored;
import exml.annotations.XOptional;

import java.lang.reflect.Field;

public enum FieldObligation {
  mandatory,
  optional,
  ignored;

  public static FieldObligation getFieldObligation(Field field) {
    if (field.getDeclaredAnnotation(XIgnored.class) != null)
      return ignored;
    else if (field.getDeclaredAnnotation(XOptional.class) != null)
      return optional;
    else
      return mandatory;
  }
}
