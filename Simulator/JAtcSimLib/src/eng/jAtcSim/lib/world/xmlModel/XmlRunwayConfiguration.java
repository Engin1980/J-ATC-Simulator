package eng.jAtcSim.lib.world.xmlModel;

import eng.eSystem.collections.*;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.utilites.ArrayUtils;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.eSystem.xmlSerialization.annotations.XmlOptional;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.RunwayConfiguration;

import java.util.Objects;

public class XmlRunwayConfiguration {

  public static class XmlRunwayThresholdConfiguration {
    @XmlOptional
    public String categories = "ABCD";
    public String name;
    @XmlOptional
    public boolean primary = false;
    @XmlOptional
    public boolean showRoutes = true;
    @XmlOptional
    public boolean showApproach = true;
  }

  public int windFrom;
  public int windTo;
  public int windSpeedFrom;
  public int windSpeedTo;
  public IList<XmlRunwayThresholdConfiguration> arrivals;
  public IList<XmlRunwayThresholdConfiguration> departures;
}
