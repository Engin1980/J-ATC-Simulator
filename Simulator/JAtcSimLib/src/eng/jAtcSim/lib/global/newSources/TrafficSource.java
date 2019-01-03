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
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.lib.world.xml.AltitudeValueParser;

public abstract class TrafficSource extends Source<Traffic> {

  public abstract void init();

}
