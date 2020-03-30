package eng.jAtcSim.newLib.area.xml;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.area.*;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class RunwayConfigurationXmlLoader extends XmlLoaderWithNavaids<RunwayConfiguration> {
  private final IReadOnlyList<ActiveRunway> activeRunways;

  public RunwayConfigurationXmlLoader(NavaidList navaids, IReadOnlyList<ActiveRunway> activeRunways) {
    super(navaids);
    this.activeRunways = activeRunways;
  }

  @Override
  public RunwayConfiguration load(XElement source) {
    XmlLoaderUtils.setContext(source);
    int windFrom = XmlLoaderUtils.loadInteger("windFrom", 0);
    int windTo = XmlLoaderUtils.loadInteger("windTo", 359);
    int windSpeedFrom = XmlLoaderUtils.loadInteger("windSpeedFrom", 0);
    int windSpeedTo = XmlLoaderUtils.loadInteger("windSpeedTo", 999);

    IList<RunwayThresholdConfiguration> departures = new EList<>();
    IList<RunwayThresholdConfiguration> arrivals = new EList<>();
    RunwayThresholdConfigurationXmlLoader rtcxl = new RunwayThresholdConfigurationXmlLoader(activeRunways);
    for (XElement child : source.getChildren()) {
      RunwayThresholdConfiguration rtc = rtcxl.load(child);
      switch (child.getName()) {
        case "departure":
          departures.add(rtc);
          break;
        case "arrivals":
          arrivals.add(rtc);
          break;
        default:
          throw new EEnumValueUnsupportedException(child.getName());
      }
    }

    checkAllCategoriesAreApplied(departures, arrivals);

    RunwayConfiguration ret = new RunwayConfiguration(
        windFrom, windTo, windSpeedFrom, windSpeedTo, arrivals, departures);
    return ret;
  }

  private static void checkAllCategoriesAreApplied(
      IList<RunwayThresholdConfiguration> departures,
      IList<RunwayThresholdConfiguration> arrivals) {
    //TODO this must be called after binding
    IList<ActiveRunway> rwys = new EDistinctList<>(EDistinctList.Behavior.skip);
    rwys.add(arrivals.select(q -> q.getThreshold().getParent()).distinct());
    rwys.add(departures.select(q -> q.getThreshold().getParent()).distinct());

    // check if all categories are applied
    for (char i = 'A'; i <= 'D'; i++) {
      char c = i;
      if (!arrivals.isAny(q -> q.isForCategory(c)))
        throw new EApplicationException("Unable to find arrival threshold for category " + c);
      if (!departures.isAny(q -> q.isForCategory(c)))
        throw new EApplicationException("Unable to find departure threshold for category " + c);
    }
  }
}