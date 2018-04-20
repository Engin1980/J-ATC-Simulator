package eng.jAtcSim.lib.global.xmlSources;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.utilites.StringUtils;
import eng.eSystem.xmlSerialization.XmlIgnore;

import java.io.IOException;
import java.nio.file.Paths;

public abstract class XmlSource<T> {
  private String xmlFileName;
  private int xmlFileHash;
  @XmlIgnore
  private boolean initialized;
  @XmlIgnore
  private T content;

  public XmlSource(String xmlFileName) {
    this.xmlFileName = xmlFileName;
  }

  public void load(){
    if (StringUtils.isNullOrEmpty(xmlFileName)){
      this.content = null;
      this.xmlFileHash = 0;
    } else {
      this.content = _load();
      this.xmlFileHash = generateFileHash();
    }
  }

 private int generateFileHash(){
    int sum = 0;
    byte [] data;

   try {
     data = java.nio.file.Files.readAllBytes(Paths.get(xmlFileName));
   } catch (IOException e) {
     throw new ERuntimeException("Failed to read file " + xmlFileName + " to generate hash.", e);
   }

   for (byte datum : data) {
     sum += datum;
   }
   return sum;
 }

 protected void setInitialized(){
    this.initialized = true;
 }

  protected abstract T _load();

  public String getXmlFileName() {
    return xmlFileName;
  }

  public int getXmlFileHash() {
    return xmlFileHash;
  }

  public T getContent() {
    if (!this.initialized)
      throw new EApplicationException("Cannot access content of XmlSource before it is initialized.");
    return content;
  }
}
