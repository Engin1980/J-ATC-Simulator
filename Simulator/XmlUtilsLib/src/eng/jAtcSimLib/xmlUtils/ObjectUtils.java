package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.utilites.ReflectionUtils;
import eng.eSystem.validation.EAssert;

import java.lang.reflect.Field;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ObjectUtils {
  public static IReadOnlyList<String> getFieldNamesExcept(Class<?> clazz, String ... ignoredFieldNames) {
    IList<String> tmp = new EList<>(ignoredFieldNames);
    return getFieldNames(clazz).minus(tmp);
  }

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

  public static IReadOnlyList<Field> getFields(Class<?> clazz) {
    return ReflectionUtils.ClassUtils.getFields(clazz);
  }

  public static IReadOnlyList<String> getFieldNames(Class<?> clazz) {
    return getFields(clazz).select(q->q.getName());
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
