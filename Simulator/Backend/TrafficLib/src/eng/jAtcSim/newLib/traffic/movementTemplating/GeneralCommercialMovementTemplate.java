package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;

public class GeneralCommercialMovementTemplate extends MovementTemplate {
  private String companyIcao;
  private Character category;

  public GeneralCommercialMovementTemplate(eKind kind, ETimeStamp time, int delayInMinutes, EntryExitInfo entryExitInfo) {
    this(null, null, kind, time, delayInMinutes, entryExitInfo);
  }

  public GeneralCommercialMovementTemplate(String companyIcao, eKind kind, ETimeStamp time, int delayInMinutes, EntryExitInfo entryExitInfo) {
    this(companyIcao, null, kind, time, delayInMinutes, entryExitInfo);
  }

  public GeneralCommercialMovementTemplate(Character category, eKind kind, ETimeStamp time, int delayInMinutes, EntryExitInfo entryExitInfo) {
    this(null, category, kind, time, delayInMinutes, entryExitInfo);
  }

  public GeneralCommercialMovementTemplate(String companyIcao, Character category, eKind kind, ETimeStamp time, int delayInMinutes, EntryExitInfo entryExitInfo) {
    super(kind, time, delayInMinutes, entryExitInfo);
    EAssert.isTrue(companyIcao == null || companyIcao.length() > 0);
    EAssert.isTrue(category == null || PlaneCategoryDefinitions.getAll().contains(category));
    this.companyIcao = companyIcao;
    this.category = category;
  }

  public Character getCategory() {
    return category;
  }

  public String getCompanyIcao() {
    return companyIcao;
  }
}
