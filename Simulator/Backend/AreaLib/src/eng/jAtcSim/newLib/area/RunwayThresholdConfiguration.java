package eng.jAtcSim.newLib.area;

import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;

import java.util.Objects;

public class RunwayThresholdConfiguration {

  private final PlaneCategoryDefinitions categories;
  private final ActiveRunwayThreshold threshold;
  private final boolean primary;
  private final boolean showRoutes;
  private final boolean showApproach;

  public RunwayThresholdConfiguration(PlaneCategoryDefinitions categories, ActiveRunwayThreshold threshold, boolean primary, boolean showRoutes, boolean showApproach) {
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
