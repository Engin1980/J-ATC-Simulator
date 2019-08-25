package eng.jAtcSim.lib.world.xml;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.exceptions.ToDoException;
import eng.jAtcSim.lib.global.Restriction;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public abstract class XmlLoader {

  private static XElement context;

  public static void setContext(XElement context) {
    XmlLoader.context = context;
  }

  public static String loadString(String key, boolean isMandatory) {
    assert context != null;
    String ret = loadString(context, key, isMandatory);
    return ret;
  }

  public static String loadString(XElement source, String key, boolean isMandatory) {
    String ret = source.getAttribute(key);
    if (ret == null) {
      XElement child = source.getChild(key);
      if (child == null)
        throw throwNotFound(source, key);
      else
        ret = child.getContent();
    }
    return ret;
  }

  public static Coordinate loadCoordinate(String key, boolean isMandatory) {
    assert context != null;
    Coordinate ret = loadCoordinate(context, key, isMandatory);
    return ret;
  }

  public static Coordinate loadCoordinate(XElement source, String key, boolean isMandatory) {
    String tmp = loadString(source, key, isMandatory);
    throw new ToDoException();
  }

  public static <T> T loadEnum(String key, Class<? extends T> type, boolean isMandatory) {
    assert context != null;
    T ret = loadEnum(context, key, type, isMandatory);
    return ret;
  }

  public static <T> T loadEnum(XElement source, String key, Class<? extends T> type, boolean isMandatory) {
    T ret;
    String tmp = loadString(source, key, isMandatory);
    if (tmp == null)
      ret = null;
    else
      try {
        ret = (T) Enum.valueOf((Class) type, tmp);
      } catch (Exception ex) {
        throw throwConvertFail(tmp, type, ex);
      }
    return ret;
  }

  public static Boolean loadBoolean(String key, boolean isMandatory) {
    assert context != null;
    Boolean ret = loadBoolean(context, key, isMandatory);
    return ret;
  }

  public static Boolean loadBoolean(XElement source, String key, boolean isMandatory) {
    Boolean ret;
    String tmp = loadString(source, key, isMandatory);
    if (tmp == null)
      ret = null;
    else
      switch (tmp.toUpperCase()) {
        case "0":
        case "FALSE":
          ret = false;
          break;
        case "1":
        case "TRUE":
          ret = true;
          break;
        default:
          throw throwConvertFail(tmp, Boolean.class);
      }
    return ret;
  }

  public static Integer loadInteger(String key, boolean isMandatory) {
    assert context != null;
    Integer ret = loadInteger(context, key, isMandatory);
    return ret;
  }

  public static Integer loadInteger(XElement source, String key, boolean isMandatory) {
    Integer ret;
    String tmp = loadString(source, key, isMandatory);
    if (tmp == null)
      ret = null;
    else
      try {
        ret = Integer.parseInt(tmp);
      } catch (Exception ex) {
        throw throwConvertFail(tmp, Integer.class, ex);
      }
    return ret;
  }

  public static Double loadDouble(String key, boolean isMandatory) {
    assert context != null;
    Double ret = loadDouble(context, key, isMandatory);
    return ret;
  }

  public static Double loadDouble(XElement source, String key, boolean isMandatory) {
    Double ret;
    String tmp = loadString(source, key, isMandatory);
    if (tmp == null)
      ret = null;
    else
      try {
        ret = Double.parseDouble(tmp);
      } catch (Exception ex) {
        throw throwConvertFail(tmp, Double.class, ex);
      }
    return ret;
  }

  public static RuntimeException throwNotFound(XElement element, String key) {
    return new EApplicationException(sf("Mandatory value not found for key '%s'", key));
  }

  public static RuntimeException throwConvertFail(Object value, Class targetType) {
    return throwConvertFail(value, targetType, null);
  }

  public static RuntimeException throwConvertFail(Object value, Class targetType, Exception innerException) {
    return new EApplicationException(sf(
        "The conversion of the value %s to type %s has failed.", value, targetType.getName()
    ));
  }

}
