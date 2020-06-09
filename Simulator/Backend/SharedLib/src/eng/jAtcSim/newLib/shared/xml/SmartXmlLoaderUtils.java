package eng.jAtcSim.newLib.shared.xml;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public abstract class SmartXmlLoaderUtils {

  private static XElement context;
  private static boolean printLogToConsole = false;

  public static boolean isPrintLogToConsole() {
    return printLogToConsole;
  }

  public static int loadAltitude(String key) {
    assert context != null;
    return loadAltitude(context, key);
  }

  public static Integer loadAltitude(String key, Integer defaultValue) {
    assert context != null;
    return loadAltitude(context, key, defaultValue);
  }

  public static int loadAltitude(XElement source, String key) {
    Integer ret = loadAltitude(source, key, null);
    if (ret == null) throw throwNotFound(source, key);
    return ret;
  }

  public static Integer loadAltitude(XElement source, String key, Integer defaultValue) {
    String tmp = loadString(source, key, null);
    String intS;
    Integer ret;
    if (tmp == null)
      ret = null;
    else {
      if (tmp.startsWith("FL")) {
        intS = tmp.substring(2);
        ret = 100;
      } else {
        intS = tmp;
        ret = 1;
      }
      try {
        ret *= Integer.parseInt(intS);
      } catch (Exception ex) {
        throw new EApplicationException(sf("Error in loading altitude. Failed to convert value '%s' to altitude.", tmp));
      }
    }
    return ret;
  }

  public static boolean loadBoolean(String key) {
    assert context != null;
    return loadBoolean(context, key);
  }

  public static Boolean loadBoolean(String key, Boolean defaultValue) {
    assert context != null;
    return loadBoolean(context, key, defaultValue);
  }

  public static Boolean loadBoolean(XElement source, String key, Boolean defaultValue) {
    Boolean ret;
    String tmp = loadString(source, key, null);
    if (tmp == null)
      ret = defaultValue;
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

  public static boolean loadBoolean(XElement source, String key) {
    Boolean ret = loadBoolean(source, key, null);
    if (ret == null) throw throwNotFound(source, key);
    return ret;
  }

  public static char loadChar(String key) {
    assert context != null;
    return loadChar(context, key);
  }

  public static char loadChar(XElement source, String key) {
    Character ret = loadChar(source, key, null);
    if (ret == null)
      throw throwNotFound(source, key);
    return ret;
  }

  public static Character loadChar(String key, Character defaultValue) {
    assert context != null;
    return loadChar(context, key, defaultValue);
  }

  public static Character loadChar(XElement source, String key, Character defaultValue) {
    String s = SmartXmlLoaderUtils.loadString(source, key, null);
    if (s == null || s.length() < 1)
      return defaultValue;
    else
      return s.charAt(0);
  }

  public static Coordinate loadCoordinate(String key) {
    assert context != null;
    Coordinate ret = loadCoordinate(context, key, null);
    return ret;
  }

  public static Coordinate loadCoordinate(String key, Coordinate defaultValue) {
    assert context != null;
    Coordinate ret = loadCoordinate(context, key, defaultValue);
    return ret;
  }

  public static Coordinate loadCoordinate(XElement source, String key) {
    String tmp = loadString(source, key, null);
    if (tmp == null)
      throw throwNotFound(source, key);
    throw new ToDoException();
  }

  public static Coordinate loadCoordinate(XElement source, String key, Coordinate defaultValue) {
    String tmp = loadString(source, key, null);
    if (tmp == null)
      return defaultValue;
    else
      throw new ToDoException();
  }

  public static Double loadDouble(String key, Double defaultValue) {
    assert context != null;
    return loadDouble(context, key, defaultValue);
  }

  public static double loadDouble(String key) {
    assert context != null;
    return loadDouble(context, key);
  }

  public static double loadDouble(XElement source, String key) {
    Double ret = loadDouble(source, key, null);
    if (ret == null)
      throw throwNotFound(source, key);
    return ret;
  }

  public static Double loadDouble(XElement source, String key, Double defaultValue) {
    Double ret;
    String tmp = loadString(source, key, null);
    if (tmp == null)
      ret = defaultValue;
    else
      try {
        ret = Double.parseDouble(tmp);
      } catch (Exception ex) {
        throw throwConvertFail(tmp, Double.class, ex);
      }
    return ret;
  }

  public static <T> T loadEnum(String key, Class<? extends T> type, T defaultValue) {
    assert context != null;
    T ret = loadEnum(context, key, type, defaultValue);
    return ret;
  }

  public static <T> T loadEnum(String key, Class<? extends T> type) {
    assert context != null;
    T ret = loadEnum(context, key, type);
    return ret;
  }

  public static <T> T loadEnum(XElement source, String key, Class<? extends T> type) {
    T ret = loadEnum(source, key, type, null);
    if (ret == null)
      throw throwNotFound(source, key);
    return ret;
  }

  public static <T> T loadEnum(XElement source, String key, Class<? extends T> type, T defaultValue) {
    T ret;
    String tmp = loadString(source, key, null);
    if (tmp == null)
      ret = defaultValue;
    else
      try {
        ret = (T) Enum.valueOf((Class) type, tmp);
      } catch (Exception ex) {
        throw throwConvertFail(tmp, type, ex);
      }
    return ret;
  }

  public static int loadInteger(String key) {
    assert context != null;
    return loadInteger(context, key);
  }

  public static Integer loadInteger(String key, Integer defaultValue) {
    assert context != null;
    return loadInteger(context, key, defaultValue);
  }

  public static int loadInteger(XElement source, String key) {
    Integer tmp = loadInteger(source, key, null);
    if (tmp == null)
      throw throwNotFound(source, key);
    return tmp;
  }

  public static Integer loadInteger(XElement source, String key, Integer defaultValue) {
    Integer ret;
    String tmp = loadString(source, key, null);
    if (tmp == null)
      ret = defaultValue;
    else
      try {
        ret = Integer.parseInt(tmp);
      } catch (Exception ex) {
        throw throwConvertFail(tmp, Integer.class, ex);
      }
    return ret;
  }

  // this is duplicite with IXmlLoader as IXmlLoader is functional interface
//  public static <T> void loadList(IReadOnlyList<XElement> elements, IList<T> list, Function<XElement, T> function) {
//    for (XElement element : elements) {
//      T item = function.apply(element);
//      list.add(item);
//    }
//  }

  public static <T> IList<T> loadList(IReadOnlyList<XElement> elements, IXmlLoader<T> xmlLoader) {
    IList<T> ret = new EList<>();
    for (XElement element : elements) {
      T item = xmlLoader.load(element);
      ret.add(item);
    }
    return ret;
  }

  public static <T> void loadList(IReadOnlyList<XElement> elements, IList<T> list, IXmlLoader<T> xmlLoader) {
    for (XElement element : elements) {
      T item = xmlLoader.load(element);
      list.add(item);
    }
  }

  public static PlaneCategoryDefinitions loadPlaneCategory(String key, String defaultValue) {
    assert context != null;
    return loadPlaneCategory(context, key, defaultValue);
  }

  public static PlaneCategoryDefinitions loadPlaneCategory(String key) {
    assert context != null;
    return loadPlaneCategory(context, key);
  }

  public static PlaneCategoryDefinitions loadPlaneCategory(XElement source, String key) {
    String tmp = loadString(source, key);
    PlaneCategoryDefinitions ret = new PlaneCategoryDefinitions(tmp);
    return ret;
  }

  public static PlaneCategoryDefinitions loadPlaneCategory(XElement source, String key, String defaultValue) {
    String tmp = loadString(source, key, null);
    PlaneCategoryDefinitions ret = (tmp == null) ?
        PlaneCategoryDefinitions.getAll() :
        new PlaneCategoryDefinitions(tmp);
    return ret;
  }

  public static String loadString(String key) {
    assert context != null;
    String ret = loadString(context, key);
    return ret;
  }

  public static String loadString(String key, String defaultValue) {
    assert context != null;
    String ret = loadString(context, key, defaultValue);
    return ret;
  }

  public static String loadString(XElement source, String key) {
    String ret = readValueFromXml(source, key);
    if (ret == null) throw throwNotFound(source, key);
    return ret;
  }

  public static String loadString(XElement source, String key, String defaultValue) {
    String ret = readValueFromXml(source, key);
    if (ret == null) ret = defaultValue;
    return ret;
  }

  public static String loadStringRestricted(String key, String[] possibleValues) {
    String ret = loadStringRestricted(SmartXmlLoaderUtils.context, key, possibleValues);
    return ret;
  }

  public static String loadStringRestricted(String key, String[] possibleValues, String defaultValue) {
    String ret = loadStringRestricted(SmartXmlLoaderUtils.context, key, possibleValues, defaultValue);
    return ret;
  }

  public static String loadStringRestricted(XElement source, String key, String[] possibleValues) {
    String ret = loadString(source, key);
    checkValueIsInPossibilities(ret, possibleValues);
    return ret;
  }

  public static String loadStringRestricted(XElement source, String key, String[] possibleValues, String defaultValue) {
    String ret = loadString(source, key, defaultValue);
    checkValueIsInPossibilities(ret, possibleValues);
    return ret;
  }

  public static void setContext(XElement context) {
    if (printLogToConsole)
      System.out.println("XmlLoader - context change " + context.toXmlPath(true));
    SmartXmlLoaderUtils.context = context;
  }

  public static void setPrintLogToConsole(boolean printLogToConsole) {
    SmartXmlLoaderUtils.printLogToConsole = printLogToConsole;
  }

  private static String readValueFromXml(XElement source, String key) {
    if (printLogToConsole)
      System.out.println("XmlLoader - loading value  " + context.toXmlPath(true) + " -- looking for " + key);
    String ret = source.getAttribute(key);
    if (ret == null) {
      XElement child = source.getChild(key);
      if (child == null)
        ret = null;
      else
        ret = child.getContent();
    }
    return ret;
  }

  private static void checkValueIsInPossibilities(String content, String[] possibleValues) {
    if (content != null && possibleValues != null) {
      IList<String> tmp = new EList<>(possibleValues);
      if (tmp.contains(content) == false)
        throw new EApplicationException("Error in loading - string does not match any of required values.");
    }
  }

  private static RuntimeException throwNotFound(XElement element, String key) {
    return new EApplicationException(sf("Mandatory value not found for key '%s'", key));
  }

  private static RuntimeException throwConvertFail(Object value, Class<?> targetType) {
    return throwConvertFail(value, targetType, null);
  }

  private static RuntimeException throwConvertFail(Object value, Class<?> targetType, Exception innerException) {
    return new EApplicationException(sf(
        "The conversion of the value %s to type %s has failed.", value, targetType.getName()
    ), innerException);
  }
}
