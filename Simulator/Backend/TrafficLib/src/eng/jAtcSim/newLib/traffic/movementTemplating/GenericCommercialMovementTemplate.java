package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;

import exml.annotations.XConstructor;

public class GenericCommercialMovementTemplate extends MovementTemplate {
  private String companyIcao;
  private Character category;


  @XConstructor
  private GenericCommercialMovementTemplate() {
  }

  public GenericCommercialMovementTemplate(eKind kind, ETimeStamp time, EntryExitInfo entryExitInfo) {
    this(null, null, kind, time, entryExitInfo);
  }

  public GenericCommercialMovementTemplate(String companyIcao, eKind kind, ETimeStamp time, EntryExitInfo entryExitInfo) {
    this(companyIcao, null, kind, time, entryExitInfo);
  }

  public GenericCommercialMovementTemplate(Character category, eKind kind, ETimeStamp time, EntryExitInfo entryExitInfo) {
    this(null, category, kind, time, entryExitInfo);
  }

  public GenericCommercialMovementTemplate(String companyIcao, Character category, eKind kind, ETimeStamp time, EntryExitInfo entryExitInfo) {
    super(kind, time, entryExitInfo);
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
