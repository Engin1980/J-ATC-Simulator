package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;

public class GeneralCommercialMovementTemplate extends MovementTemplate {
  private String companyIcao;
  private Character category;

  public GeneralCommercialMovementTemplate(eKind kind, ETimeStamp time, EntryExitInfo entryExitInfo, int delay) {
    this(null, null, kind, time, entryExitInfo, delay);
  }

  public GeneralCommercialMovementTemplate(String companyIcao, eKind kind, ETimeStamp time, EntryExitInfo entryExitInfo, int delay) {
    this(companyIcao, null, kind, time, entryExitInfo, delay);
  }

  public GeneralCommercialMovementTemplate(Character category, eKind kind, ETimeStamp time, EntryExitInfo entryExitInfo, int delay) {
    this(null, category, kind, time, entryExitInfo, delay);
  }

  public GeneralCommercialMovementTemplate(String companyIcao, Character category, eKind kind, ETimeStamp time, EntryExitInfo entryExitInfo, int delay) {
    super(kind, time, entryExitInfo, delay);
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
