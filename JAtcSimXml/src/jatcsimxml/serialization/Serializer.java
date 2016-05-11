/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimxml.serialization;

import jatcsimlib.exceptions.ERuntimeException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Marek
 */
public class Serializer {

  public void fillObject (String xmlFileName, Object targetObject){
    
    Element el = loadXmlAndGetRootElement(xmlFileName);
    Reflecter.fillObject(el, targetObject);
  }

  private Element loadXmlAndGetRootElement(String xmlFileName) {
    Document doc = readXmlDocument(xmlFileName);
    Element el = doc.getDocumentElement();
    return el;
  }
  
  public void fillList (String xmlFileName, List targetObject){
    Element el = loadXmlAndGetRootElement(xmlFileName);
    Reflecter.fillList(el, targetObject);
  }
  
  private Document readXmlDocument(String fileName){
    Document doc = null;
    try {
      File fXmlFile = new File(fileName);
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      doc = dBuilder.parse(fXmlFile);
      
      //optional, but recommended
      //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
      doc.getDocumentElement().normalize();
    } catch (ParserConfigurationException | SAXException | IOException ex) {
      throw new ERuntimeException("Failed to load XML file " + fileName, ex);
    }
    
    return doc;
  }
}
