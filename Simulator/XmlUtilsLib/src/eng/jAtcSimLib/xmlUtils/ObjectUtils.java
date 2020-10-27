package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.EAssert;

import java.lang.reflect.Field;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ObjectUtils {
  public static Object getFieldValue(Object object, String fieldName) {
    EAssert.Argument.isNotNull(object, "object");
    EAssert.Argument.isNonemptyString(fieldName, "fieldName");

    Class<?> cls = object.getClass();
    Field f;
    Object v;
    try {
      f = getField(cls, fieldName);
      f.setAccessible(true);
      v = f.get(object);
    } catch (NoSuchFieldException | IllegalAccessException ex) {
      throw new EApplicationException(sf("Failed to read field value of object - %s.%s", object.getClass(), fieldName));
    }
    return v;
  }

  private static Field getField(Class<?> type, String name) throws NoSuchFieldException {
    Field f;
    try {
      f = type.getDeclaredField(name);
    } catch (NoSuchFieldException e) {
      if (type.equals(Object.class))
        throw e;
      else {
        type = type.getSuperclass();
        f = getField(type, name);
      }
    }
    return f;
  }
}
