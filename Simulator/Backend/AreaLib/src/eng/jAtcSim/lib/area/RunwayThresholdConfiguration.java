package eng.jAtcSim.lib.area;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.sharedLib.PlaneCategoryDefinitions;
import eng.jAtcSim.sharedLib.xml.XmlLoader;

import java.util.Objects;

public class RunwayThresholdConfiguration {
  public static RunwayThresholdConfiguration load(XElement source, Airport airport) {
    RunwayThresholdConfiguration ret = new RunwayThresholdConfiguration();
    ret.read(source, airport);
    return ret;
  }

  private void read(XElement source, Airport airport) {
    IReadOnlyList<ActiveRunway> activeRunways = airport.getRunways();
    XmlLoader.setContext(source);
    String name = XmlLoader.loadString("name");
    this.primary = XmlLoader.loadBoolean("primary", false);
    this. showRoutes = XmlLoader.loadBoolean("showRoutes", true);
    this. showApproach = XmlLoader.loadBoolean("showApproach", true);
    this.categories = XmlLoader.loadPlaneCategory("category", "ABCD");

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
    this.threshold = threshold;
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
