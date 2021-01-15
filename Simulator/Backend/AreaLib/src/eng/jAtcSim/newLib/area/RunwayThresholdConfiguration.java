package eng.jAtcSim.newLib.area;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.IXPersistable;
import exml.XContext;
import exml.annotations.XConstructor;

import java.util.Objects;

public class RunwayThresholdConfiguration implements IXPersistable {

  private final PlaneCategoryDefinitions categories;
  private final boolean primary;
  private final boolean showApproach;
  private final boolean showRoutes;
  private final ActiveRunwayThreshold threshold;

  //TODO really this must be here?
  @XmlConstructor
  @XConstructor
  public RunwayThresholdConfiguration() {
    categories = null;
    primary = false;
    showApproach = false;
    showRoutes = false;
    threshold = null;
  }

  public RunwayThresholdConfiguration(PlaneCategoryDefinitions categories,
                                      ActiveRunwayThreshold threshold,
                                      boolean primary,
                                      boolean showRoutes,
                                      boolean showApproach) {
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

  public PlaneCategoryDefinitions getCategories() {
    return categories;
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
