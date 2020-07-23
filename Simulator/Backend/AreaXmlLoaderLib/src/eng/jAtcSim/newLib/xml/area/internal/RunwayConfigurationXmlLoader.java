package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.area.*;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;

public class RunwayConfigurationXmlLoader extends XmlLoader<RunwayConfiguration> {

  protected RunwayConfigurationXmlLoader(LoadingContext context) {
    super(context);
  }

  @Override
  public RunwayConfiguration load(XElement source) {
    log(2, "Xml-loading runway configuration");
    SmartXmlLoaderUtils.setContext(source);
    int windFrom = SmartXmlLoaderUtils.loadInteger("windFrom", 0);
    int windTo = SmartXmlLoaderUtils.loadInteger("windTo", 359);
    int windSpeedFrom = SmartXmlLoaderUtils.loadInteger("windSpeedFrom", 0);
    int windSpeedTo = SmartXmlLoaderUtils.loadInteger("windSpeedTo", 999);

    IList<RunwayThresholdConfiguration> departures = new EList<>();
    IList<RunwayThresholdConfiguration> arrivals = new EList<>();
    RunwayThresholdConfigurationXmlLoader rtcxl = new RunwayThresholdConfigurationXmlLoader(context);
    for (XElement child : source.getChildren()) {
      RunwayThresholdConfiguration rtc = rtcxl.load(child);
      switch (child.getName()) {
        case "departures":
          departures.add(rtc);
          break;
        case "arrivals":
          arrivals.add(rtc);
          break;
        default:
          throw new EEnumValueUnsupportedException(child.getName());
      }
    }

    RunwayConfiguration ret = new RunwayConfiguration(
        windFrom, windTo, windSpeedFrom, windSpeedTo, arrivals, departures);
    return ret;
  }
}
