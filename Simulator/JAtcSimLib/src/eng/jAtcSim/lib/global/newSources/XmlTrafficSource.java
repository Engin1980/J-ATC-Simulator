package eng.jAtcSim.lib.global.newSources;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.exceptions.EXmlException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;
import eng.eSystem.xmlSerialization.annotations.XmlElement;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.coordinates.CoordinateValueParser;
import eng.jAtcSim.lib.traffic.DensityBasedTraffic;
import eng.jAtcSim.lib.traffic.FlightListTraffic;
import eng.jAtcSim.lib.traffic.GenericTraffic;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.xml.AltitudeValueParser;

import javax.swing.*;

public class XmlTrafficSource extends TrafficSource {

  static class TrafficDefinition{
    @XmlElement(elementName = "genericTraffic", type= GenericTraffic.class)
    @XmlElement(elementName = "densityTraffic", type=DensityBasedTraffic.class)
    @XmlElement(elementName = "flightListTraffic", type=FlightListTraffic.class)
    public Traffic traffic;
  }

  @XmlIgnore
  private Traffic traffic;
  private String fileName;

  public XmlTrafficSource(String fileName)
  {
    this.fileName = fileName;
  }

  @Override
  protected Traffic _getContent() {
    return traffic;
  }

  @Override
  public void init() {
    TrafficDefinition def;

//    XmlSettings sett = new XmlSettings();
//    sett.forType(int.class)
//        .setCustomParser(new AltitudeValueParser());
//    sett.forType(Integer.class)
//        .setCustomParser(new AltitudeValueParser());
//    sett.forType(Coordinate.class)
//        .setCustomParser(new CoordinateValueParser());
//
//    XmlSerializer ser = new XmlSerializer(sett);
    try {
//      def = ser.deserialize(this.fileName, TrafficDefinition.class);
//      if (def.traffic instanceof FlightListTraffic){
//        ((FlightListTraffic)def.traffic).bind();
//      }
//      this.traffic = def.traffic;
      XDocument doc = XDocument.load(this.fileName);
      XElement source = doc.getRoot().getChildren().getFirst(q->!q.getName().equals("meta"));

      switch (source.getName()){
        case "genericTraffic":
          this.traffic = GenericTraffic.load(source);
          break;
        case "densityTraffic":
          this.traffic = DensityBasedTraffic.load(source);
          break;
        case "flightListTraffic":
          this.traffic = FlightListTraffic.load(source);
          break;
        default:
          throw new EEnumValueUnsupportedException(source.getName());
      }
    } catch (Exception e) {
      throw new EApplicationException("Unable to load traffic from file '" + this.fileName + "'.", e);
    }

    super.setInitialized();
  }
}
