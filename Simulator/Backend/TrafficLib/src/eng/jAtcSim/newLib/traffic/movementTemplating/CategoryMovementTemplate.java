package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;

public class CategoryMovementTemplate extends MovementTemplate {
  private char category;
  private boolean commercial;

  public CategoryMovementTemplate(char category, boolean isCommercial,
                                  eKind kind, ETimeStamp time, int delayInMinutes,
                                  EntryExitInfo entryExitInfo) {
    super(kind, time, delayInMinutes, entryExitInfo);
    EAssert.isTrue(category >= 'A' && category <= 'D');
    this.category = category;
    this.commercial = isCommercial;
  }

  public char getCategory() {
    return category;
  }

  public boolean isCommercial() {
    return commercial;
  }
}
