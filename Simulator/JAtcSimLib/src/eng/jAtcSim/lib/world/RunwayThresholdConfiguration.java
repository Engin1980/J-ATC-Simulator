package eng.jAtcSim.lib.world;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.world.xml.XmlLoader;

import java.util.Objects;

public class RunwayThresholdConfiguration {
  public static RunwayThresholdConfiguration load(XElement source, IReadOnlyList<ActiveRunway> activeRunways) {
    XmlLoader.setContext(source);
    String name = XmlLoader.loadString("name");
    boolean isPrimary = XmlLoader.loadBoolean("primary", false);
    boolean showRoutes = XmlLoader.loadBoolean("showRoutes", true);
    boolean showApproach = XmlLoader.loadBoolean("showApproach", true);
    String categoryS = XmlLoader.loadString("category", "ABCD");
    PlaneCategoryDefinitions category = new PlaneCategoryDefinitions(categoryS);

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

    RunwayThresholdConfiguration ret = new RunwayThresholdConfiguration(
        threshold, category, isPrimary, showRoutes, showApproach);

    return ret;
  }

  private final PlaneCategoryDefinitions categories;
  private final ActiveRunwayThreshold threshold;
  private final boolean primary;
  private final boolean showRoutes;
  private final boolean showApproach;

  public RunwayThresholdConfiguration(ActiveRunwayThreshold threshold, PlaneCategoryDefinitions categories,
                                      boolean primary, boolean showRoutes, boolean showApproach) {
    this.categories = categories;
    this.threshold = threshold;
    this.primary = primary;
    this.showRoutes = showRoutes;
    this.showApproach = showApproach;
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
