package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;

import java.util.Objects;

public class RunwayThresholdConfiguration {

  static class XmlReader{
    static RunwayThresholdConfiguration load(XElement source, Airport airport) {
      RunwayThresholdConfiguration ret = new RunwayThresholdConfiguration();
      read(source, ret, airport);
      return ret;
    }

    private static void read(XElement source, RunwayThresholdConfiguration rtc, Airport airport) {
      IReadOnlyList<ActiveRunway> activeRunways = airport.getRunways();
      XmlLoader.setContext(source);
      String name = XmlLoader.loadString("name");
      rtc.primary = XmlLoader.loadBoolean("primary", false);
      rtc. showRoutes = XmlLoader.loadBoolean("showRoutes", true);
      rtc. showApproach = XmlLoader.loadBoolean("showApproach", true);
      rtc.categories = XmlLoader.loadPlaneCategory("category", "ABCD");

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
