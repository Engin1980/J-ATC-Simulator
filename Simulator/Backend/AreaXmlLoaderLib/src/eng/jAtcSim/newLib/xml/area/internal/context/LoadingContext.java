package eng.jAtcSim.newLib.xml.area.internal.context;

import eng.eSystem.collections.IList;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.area.*;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.xml.area.internal.XmlMappingDictinary;

public class LoadingContext {
  public static class AreaInfo{

    public NavaidList navaids;
    public IList<Border> borders;
    public String icao;
  }

  public static class AirportInfo{

    public String icao;
    public Navaid mainNavaid;
    public XmlMappingDictinary<DARoute> daMappings;
    public XmlMappingDictinary<IafRoute> iafMappings;
    public XmlMappingDictinary<GaRoute> gaMappings;
    public IList<ActiveRunway> activeRunways;
    public int altitude;
  }

  public static class ThresholdInfo{

    public Coordinate coordinate;
    public String name;
    public int course;

    public int getOppositeCourse(){
      return (int)Headings.getOpposite(course);
    }
  }

  public final AreaInfo area = new AreaInfo();
  public final AirportInfo airport = new AirportInfo();
  public final ThresholdInfo threshold = new ThresholdInfo();
}
