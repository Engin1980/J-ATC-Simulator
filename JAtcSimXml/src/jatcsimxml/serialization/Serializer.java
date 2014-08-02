/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimxml.serialization;

import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.world.Area;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
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

  public Area loadArea (String xmlFileName){
    Area ret;
    
    Document doc = readXmlDocument(xmlFileName);
    
    ret = new Area();
    fillObject(xmlFileName, ret);
    
    return ret;
  }
  
  public void fillObject (String xmlFileName, Object targetObject){
    
    Document doc = readXmlDocument(xmlFileName);
    
    Element el = doc.getDocumentElement();
    Reflecter.fillObject(el, targetObject);
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
