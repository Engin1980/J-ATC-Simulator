package eng.jAtcSim.lib.world.xmlModel;

import eng.eSystem.collections.IList;
import eng.eSystem.xmlSerialization.annotations.XmlItemElement;

public class XmlArea {
  @XmlItemElement(elementName = "airport", type = XmlAirport.class)
  public IList<XmlAirport> airports; // = new EList<>();
  @XmlItemElement(elementName = "border", type = XmlNavaid.class)
  public IList<XmlNavaid> navaids; // = new EList<>();
  @XmlItemElement(elementName = "border", type = XmlBorder.class)
  public IList<XmlBorder> borders; // = new EList();

  public String icao;
}
