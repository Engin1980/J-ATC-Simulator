package eng.jAtcSim.lib.global.newSources;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.traffic.DensityBasedTraffic;
import eng.jAtcSim.lib.traffic.FlightListTraffic;
import eng.jAtcSim.lib.traffic.GenericTraffic;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.xml.AltitudeValueParser;

import javax.swing.*;

public class XmlTrafficSource extends TrafficSource {

  @XmlIgnore
  private Traffic traffic;
  private String fileName;

  public XmlTrafficSource(String fileName) {
    this.fileName = fileName;
  }

  @Override
  protected Traffic _getContent() {
    return traffic;
  }

  @Override
  public void init() {
    XmlSettings sett = new XmlSettings();

    System.out.println("## traffic loading will be probably needed to be rewritten");

    // list mappings
//    sett.forType(Traffic.class)
//        .addXmlElement("genericTraffic", GenericTraffic.class, false, null)
//        .addXmlItemElement("densityTraffic", DensityBasedTraffic.class, false, null)
//        .addXmlItemElement("flightListTraffic", FlightListTraffic.class, false, null)
//        .addXmlItemIgnoredElement("meta");

    sett.forType(int.class)
        .setCustomParser(new AltitudeValueParser());
    sett.forType(Integer.class)
        .setCustomParser(new AltitudeValueParser());

    // own loading
    XmlSerializer ser = new XmlSerializer(sett);
    traffic = ser.deserialize(this.fileName, Traffic.class);

    super.setInitialized();
  }
}
