package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;

public class GeneralCommercialMovementTemplate extends MovementTemplate {
  private String companyIcao;
  private Character category;

  public GeneralCommercialMovementTemplate(String companyIcao, Character category,
                                           eKind kind, ETimeStamp time, int delayInMinutes,
                                           EntryExitInfo entryExitInfo) {
    super(kind, time, delayInMinutes, entryExitInfo);
    EAssert.isNonemptyString(companyIcao);
    EAssert.isTrue(category == null || PlaneCategoryDefinitions.getAll().contains(category));
    this.companyIcao = companyIcao;
    this.category = category;
  }

  public String getCompanyIcao() {
    return companyIcao;
  }

  public Character getCategory() {
    return category;
  }
}
