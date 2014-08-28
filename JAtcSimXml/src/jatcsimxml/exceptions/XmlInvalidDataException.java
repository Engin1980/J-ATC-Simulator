/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimxml.exceptions;

import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.EStringBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author Marek
 */
public class XmlInvalidDataException extends ERuntimeException {

  public static XmlInvalidDataException createNoSuchElement(Element parentElement, String fieldName, Class<? extends Object> targetClass) {
    EStringBuilder sb = new EStringBuilder();

    sb.append("Failed to find subelement in tree. ");
    sb.appendFormat("Looking for \"%s\" in \"%s\" to be put in object \"%s\". [[Node info: %s]]",
      fieldName,
      getXPath(parentElement),
      targetClass.getSimpleName(),
      getAllAttributes(parentElement));

    return new XmlInvalidDataException(sb.toString());
  }

  private XmlInvalidDataException(String message) {
    super(message);
  }

  private static String getXPath(Element elm) {
    String ret = "";
    Node parentNode = elm.getParentNode();
    if (parentNode instanceof Element) {
      ret = getXPath((Element) parentNode);
    }

    ret = ret + "/" + elm.getTagName();

    return ret;
  }

  private static String getAllAttributes(Element elm){
    EStringBuilder sb = new EStringBuilder();
    
    NamedNodeMap nnm = elm.getAttributes();
    for (int i = 0; i < nnm.getLength(); i++) {
      Node n = nnm.item(i);
      sb.appendFormat("%s=%s;", n.getNodeName(), n.getNodeValue());
    }
    return sb.toString();
  }
}
