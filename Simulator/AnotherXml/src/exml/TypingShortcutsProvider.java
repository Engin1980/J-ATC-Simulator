package exml;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;

import java.util.Map;

public class TypingShortcutsProvider {
  public static void collapseTypes(XElement root) {
    IMap<String, String> typeEncodes = new EMap<>();

    collapseTypesByElement(root, typeEncodes);

    XElement types = new XElement(Constants.TYPES_ENCODING_ELEMENT);
    for (Map.Entry<String, String> typeName : typeEncodes) {
      XElement tmp = new XElement(typeName.getValue());
      tmp.setContent(typeName.getKey());
      types.addElement(tmp);
    }
    root.addElement(types);
  }

  public static void expandTypes(XElement root) {
    IMap<String, String> typeDecodes = new EMap<>();

    XElement types = root.getChild(Constants.TYPES_ENCODING_ELEMENT);
    for (XElement child : types.getChildren()) {
      typeDecodes.set(child.getName(), child.getContent());
    }

    expandTypesByElement(root, typeDecodes);
  }

  private static void expandTypesByElement(XElement elm, IMap<String, String> typeDecodes) {
    String t = elm.tryGetAttribute(Constants.TYPE_ATTRIBUTE).orElse(null);
    if (t != null) {
      String typeName = typeDecodes.get(t);
      elm.setAttribute(Constants.TYPE_ATTRIBUTE, typeName);
    }
    for (XElement child : elm.getChildren()) {
      expandTypesByElement(child, typeDecodes);
    }
  }

  private static void collapseTypesByElement(XElement elm, IMap<String, String> typeEncodes) {
    String t = elm.tryGetAttribute(Constants.TYPE_ATTRIBUTE).orElse(null);
    if (t != null) {
      if (typeEncodes.containsKey(t) == false)
        registerTypeShortName(t, typeEncodes);

      elm.setAttribute(Constants.TYPE_ATTRIBUTE, typeEncodes.get(t));
    }
    for (XElement child : elm.getChildren()) {
      collapseTypesByElement(child, typeEncodes);
    }
  }

  private static void registerTypeShortName(String typeName, IMap<String, String> typeEncodes) {
    EAssert.isFalse(typeEncodes.containsKey(typeName));
    String shortName;
    int lastDotIndex = typeName.lastIndexOf('.');
    if (lastDotIndex < 0)
      shortName = generateTypeShortName(typeName, typeEncodes);
    else
      shortName = generateTypeShortName(typeName.substring(lastDotIndex + 1), typeEncodes);
    typeEncodes.set(typeName, shortName);
  }

  private static String generateTypeShortName(String name, IMap<String, String> typeEncodes) {
    String ret = name;
    char ap = '0';
    while (typeEncodes.getValues().contains(ret)) {
      ret = name + ap;
      ap++;
    }
    return ret;
  }
}
