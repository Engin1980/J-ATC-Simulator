package eng.jAtcSim.newLib.speeches.atc.atc2user;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;

public class RunwayInUseNotification implements IAtcSpeech {
  public static class RunwayThresholdInUseInfo {
    private final String thresholdName;
    private final PlaneCategoryDefinitions categories;

    public RunwayThresholdInUseInfo(String thresholdName, PlaneCategoryDefinitions categories) {
      EAssert.Argument.isNotNull(thresholdName, "thresholdName");
      EAssert.Argument.isNotNull(categories, "categories");
      this.thresholdName = thresholdName;
      this.categories = categories;
    }

    public String getThresholdName() {
      return thresholdName;
    }

    public PlaneCategoryDefinitions getCategories() {
      return categories;
    }
  }

  private final IReadOnlyList<RunwayThresholdInUseInfo> departuresThresholds;
  private final IReadOnlyList<RunwayThresholdInUseInfo> arrivalsThresholds;
  private final EDayTimeStamp expectedTime;

  public RunwayInUseNotification(
      IReadOnlyList<RunwayThresholdInUseInfo> departuresThresholds,
      IReadOnlyList<RunwayThresholdInUseInfo> arrivalsThresholds,
      EDayTimeStamp expectedTime) {
    EAssert.Argument.isNotNull(departuresThresholds, "departuresThresholds");
    EAssert.Argument.isNotNull(arrivalsThresholds, "arrivalsThresholds");
    this.departuresThresholds = departuresThresholds;
    this.arrivalsThresholds = arrivalsThresholds;
    this.expectedTime = expectedTime;
  }

  public IReadOnlyList<RunwayThresholdInUseInfo> getArrivalsThresholds() {
    return arrivalsThresholds;
  }

  public EDayTimeStamp getExpectedTime() {
    return expectedTime;
  }

  public IReadOnlyList<RunwayThresholdInUseInfo> getDeparturesThresholds() {
    return departuresThresholds;
  }
}
