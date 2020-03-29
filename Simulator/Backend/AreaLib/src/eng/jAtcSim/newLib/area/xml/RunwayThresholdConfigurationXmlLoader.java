package eng.jAtcSim.newLib.area.xml;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.ActiveRunway;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.RunwayThresholdConfiguration;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class RunwayThresholdConfigurationXmlLoader implements IXmlLoader<RunwayThresholdConfiguration> {
  private final IReadOnlyList<ActiveRunway> activeRunways;

  public RunwayThresholdConfigurationXmlLoader(IReadOnlyList<ActiveRunway> activeRunways) {
    EAssert.Argument.isNotNull(activeRunways, "Parameter 'activeRunways' cannot be null.");
    this.activeRunways = activeRunways;
  }

  @Override
  public RunwayThresholdConfiguration load(XElement source) {
    XmlLoaderUtils.setContext(source);
    String name = XmlLoaderUtils.loadString("name");
    boolean primary = XmlLoaderUtils.loadBoolean("primary", false);
    boolean showRoutes = XmlLoaderUtils.loadBoolean("showRoutes", true);
    boolean showApproach = XmlLoaderUtils.loadBoolean("showApproach", true);
    PlaneCategoryDefinitions categories = XmlLoaderUtils.loadPlaneCategory("category", "ABCD");

    ActiveRunwayThreshold threshold = null;
    for (ActiveRunway activeRunway : this.activeRunways) {
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
