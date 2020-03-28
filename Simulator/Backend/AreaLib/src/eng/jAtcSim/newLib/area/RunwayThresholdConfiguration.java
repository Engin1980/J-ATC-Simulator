package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

import java.util.Objects;

public class RunwayThresholdConfiguration {

  static class XmlLoader {
    static RunwayThresholdConfiguration load(XElement source, Airport airport) {
      RunwayThresholdConfiguration ret = new RunwayThresholdConfiguration();
      read(source, ret, airport);
      return ret;
    }

    private static void read(XElement source, RunwayThresholdConfiguration rtc, Airport airport) {
      IReadOnlyList<ActiveRunway> activeRunways = airport.getRunways();
      XmlLoaderUtils.setContext(source);
      String name = XmlLoaderUtils.loadString("name");
      rtc.primary = XmlLoaderUtils.loadBoolean("primary", false);
      rtc. showRoutes = XmlLoaderUtils.loadBoolean("showRoutes", true);
      rtc. showApproach = XmlLoaderUtils.loadBoolean("showApproach", true);
      rtc.categories = XmlLoaderUtils.loadPlaneCategory("category", "ABCD");

      ActiveRunwayThreshold threshold = null;
      for (ActiveRunway activeRunway : activeRunways) {
        if (threshold != null) break;
        for (ActiveRunwayThreshold activeRunwayThreshold : activeRunway.getThresholds()) {
          if (activeRunwayThreshold.getName().equals(name)) {
            threshold = activeRunwayThreshold;
            break;
          }
        }
      }
      rtc.threshold = threshold;
    }
  }

  private  PlaneCategoryDefinitions categories;
  private  ActiveRunwayThreshold threshold;
  private  boolean primary;
  private  boolean showRoutes;
  private  boolean showApproach;

  private RunwayThresholdConfiguration() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RunwayThresholdConfiguration that = (RunwayThresholdConfiguration) o;
    return Objects.equals(categories, that.categories) &&
        Objects.equals(threshold, that.threshold);
  }

  public ActiveRunwayThreshold getThreshold() {
    return threshold;
  }

  @Override
  public int hashCode() {
    return Objects.hash(categories, threshold);
  }

  public boolean isForCategory(char category) {
    return this.categories.contains(category);
  }

  public PlaneCategoryDefinitions getCategories() {
    return categories;
  }

  public boolean isPrimary() {
    return primary;
  }

  public boolean isShowApproach() {
    return showApproach;
  }

  public boolean isShowRoutes() {
    return showRoutes;
  }
}
