/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimxml.serialization;

import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.Optional;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.global.KeyItem;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Runway;
import jatcsimlib.world.RunwayThreshold;
import jatcsimxml.exceptions.XmlInvalidDataException;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Marek
 */
public class Reflecter {

  private static final boolean VERBOSE = false;

  static <T> void fillObject(Element el, T targetObject) {
    if (VERBOSE) {
      System.out.println("fillObject( <" + el.getNodeName() + "...>, " + targetObject.getClass().getSimpleName());
    }
    Class c = targetObject.getClass();
    Field[] fields = c.getDeclaredFields();

    for (Field f : fields) {
      if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
        continue; // statické přeskakujem
      }
      else if (f.getName().equals("parent"))
        continue; // parent neplníme, ty jsou reference na nadřazené objekty a plní se sami
      fillField(el, f, targetObject);
    }
  }

  static void fillList(Element el, List lst) {
    fillFieldList(el, lst, lst.getClass().getSimpleName());
  }

  private static <T> void fillField(Element el, Field f, T targetObject) {
    if (VERBOSE) {
      System.out.println("  fillField( <" + el.getNodeName() + "...>, " + targetObject.getClass().getSimpleName() + "." + f.getName());
    }

    Class c = f.getType();
    if (Mapping.isSimpleTypeOrEnum(c)) {
      // jednoduchý typ
      convertAndSetFieldValue(el, f, targetObject);
    } else if (List.class.isAssignableFrom(c)) {
      setFieldList(el, f, targetObject);
    } else {
      setFieldComplex(el, f, targetObject);
    }
  }

  private static <T> void convertAndSetFieldValue(Element el, Field f, T targetObject) {
    boolean required = f.getAnnotation(Optional.class) == null;
    String tmpS = extractSimpleValueFromElement(el, f.getName(), required);
    if (tmpS == null) {
      return;
    }
    Object tmpO = convertToType(tmpS, f.getType());
    setFieldValue(f, targetObject, tmpO);
  }

  private static String extractSimpleValueFromElement(Element el, String key, boolean isRequired) {
    String ret = null;
    if (el.hasAttribute(key)) {
      ret = el.getAttribute(key);
    } else {
      NodeList tmp = el.getElementsByTagName(key);
      if (tmp.getLength() > 0) {
        ret = tmp.item(0).getTextContent();
      }
    }

    if (ret == null && isRequired) {
      throw new ERuntimeException("Unable to find key \"" + key + "\" in element \"" + el.getNodeName() + "\"");
    }

    return ret;
  }

  private static <T> void setFieldValue(Field f, T targetObject, Object value) throws SecurityException, ERuntimeException {
    try {
      f.setAccessible(true);
      f.set(targetObject, value);
      f.setAccessible(false);
    } catch (IllegalArgumentException | IllegalAccessException ex) {
      throw new ERuntimeException(
          "Failed to set into " + f.getDeclaringClass().getName() + "." + f.getName() + " value " + value);
    }
  }

  private static Object convertToType(String value, Class<?> type) {
    Object ret;
    if (type.isEnum()) {
      //field.set(this, Enum.valueOf((Class<Enum>) field.getType(), value));
      ret = Enum.valueOf((Class<Enum>) type, value);
    } else {
      switch (type.getSimpleName()) {
        case "int":
        case "Integer":
          ret = Integer.parseInt(value);
          break;
        case "double":
        case "Double":
          ret = Double.parseDouble(value);
          break;
        case "boolean":
        case "Boolean":
          ret = Boolean.parseBoolean(value);
          break;
        case "String":
          ret = value;
          break;
        case "Coordinate":
          ret = Coordinate.parse(value);
          break;
        case "Color":
          ret = parseColor(value);
          break;
        default:
          throw new ERuntimeException("Type " + type.getName() + " is not supported.");
      }
    }

    return ret;
  }

  private static <T> void setFieldComplex(Element el, Field f, T ref) {
    Object newInstance = createInstance(f.getType());
    try {
      f.setAccessible(true);
      f.set(ref, newInstance);
      f.setAccessible(false);
    } catch (IllegalArgumentException | IllegalAccessException ex) {
      throw new ERuntimeException(
          "Failed to set value to field " + ref.getClass().getName() + "." + f.getName(), ex);
    }
    Element subEl;
    
    try {
      subEl = getElements(el, f.getName()).get(0);
    } catch (Exception e) {
      throw XmlInvalidDataException.createNoSuchElement(el, f.getName(), ref.getClass());
    }
    
    
    
    fillObject(subEl, newInstance);
  }

  private static Object createInstance(Class<?> type) {
    Object ret;

    // programuje se proti List, tak sem přijde požadavek na "List"
    // ale to je rozhraní, takže ho nahradím ArrayListem
    if (type.equals(List.class)) {
      type = ArrayList.class;
    }

    try {
      ret = type.newInstance();
    } catch (InstantiationException | IllegalAccessException ex) {
      throw new ERuntimeException("Failed to create new instance of " + type.getName(), ex);
    }
    return ret;
  }

  private static void setFieldList(Element el, Field f, Object targetObject) {
    List lst = (List) createInstance(f.getType());
    setFieldValue(f, targetObject, lst);

    // zanoření
    List<Element> tmp = getElements(el, f.getName());
    if (tmp.isEmpty()) {
      return;
    }
    el = tmp.get(0);

    String key = targetObject.getClass().getSimpleName() + "." + f.getName();
    fillFieldList(el, lst, key);
  }

  private static void fillFieldList(Element el, List lst, String classFieldKey) {
    List<Element> childs = getElements(el);
    for (Element e : childs) {
      Class itemType = getItemType(classFieldKey, e.getNodeName());
      Object inn = createInstance(itemType);
      lst.add(inn);

      fillObject(e, inn);
    }
  }

  private static List<Element> getElements(Element el) {
    return getElements(el, null);
  }

  private static List<Element> getElements(Element el, String subElementName) {
    List<Element> ret = new ArrayList();
    NodeList c = el.getChildNodes();
    for (int i = 0; i < c.getLength(); i++) {
      Node n = c.item(i);
      if (n.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      if (subElementName != null && n.getNodeName().equals(subElementName) == false) {
        continue;
      }
      Element eel = (Element) c.item(i);
      ret.add(eel);
    }
    return ret;
  }

  private static Class getItemType(String classFieldKey, String elementNameOrNull) {
    @SuppressWarnings("UnusedAssignment")
    Class ret = null;

    ret = Mapping.getMappedType(classFieldKey, elementNameOrNull);

    if (ret == null) {
      throw new ERuntimeException("No list-mapping found for " + classFieldKey + " and <" + elementNameOrNull + ">");
    }

    return ret;
  }

  private static Color parseColor(String value) {
    Color ret = null;
    String ps = "(..)(..)(..)";
    Pattern p = Pattern.compile(ps);

    Matcher m = p.matcher(value);
    if (m.find()) {
      String r = m.group(1);
      String g = m.group(2);
      String b = m.group(3);
      try {
        int ri = Integer.parseInt(r, 16);
        int gi = Integer.parseInt(g, 16);
        int bi = Integer.parseInt(b, 16);
        ret = new Color(ri, gi, bi);
      } finally {
      }
    }
    if (ret == null) {
      throw new ERuntimeException("Unable to parse \"" + value + "\" into color.");
    }

    return ret;
  }

}
