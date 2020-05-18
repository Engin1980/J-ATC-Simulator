package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.ActiveRunway;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.RunwayThresholdConfiguration;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;

public class RunwayThresholdConfigurationXmlLoader extends XmlLoader {

  public RunwayThresholdConfigurationXmlLoader(Context context) {
    super(context);
  }

  public RunwayThresholdConfiguration load(XElement source) {
    SmartXmlLoaderUtils.setContext(source);
    String name = SmartXmlLoaderUtils.loadString("name");
    boolean primary = SmartXmlLoaderUtils.loadBoolean("primary", false);
    boolean showRoutes = SmartXmlLoaderUtils.loadBoolean("showRoutes", true);
    boolean showApproach = SmartXmlLoaderUtils.loadBoolean("showApproach", true);
    PlaneCategoryDefinitions categories = SmartXmlLoaderUtils.loadPlaneCategory("category", "ABCD");

    ActiveRunwayThreshold threshold = null;
    for (ActiveRunway activeRunway : context.airport.activeRunways) {
      if (threshold != null) break;
      for (ActiveRunwayThreshold activeRunwayThreshold : activeRunway.getThresholds()) {
        if (activeRunwayThreshold.getName().equals(name)) {
          threshold = activeRunwayThreshold;
          break;
        }
      }
    }

    RunwayThresholdConfiguration ret = new RunwayThresholdConfiguration(
        categories, threshold, primary, showRoutes, showApproach
    );
    return ret;
  }
}
