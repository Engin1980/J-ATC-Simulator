package eng.jAtcSim.newLib.area.airplanes.sha;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.shared.Restriction;

class RestrictableItem {
  private int orderedValue;
  private Restriction restrictedValue;
  private int targetValue;

  RestrictableItem(int targetValue) {
    setTargetValue(targetValue);
  }

  public final void clearRestriction() {
    this.restrictedValue = null;
    this.refresh();
  }

  public int getTargetValue() {
    return this.targetValue;
  }

  public final void setRestriction(Restriction restriction) {
    this.restrictedValue = restriction;
    this.refresh();
  }

  public final void setTargetValue(int value) {
    this.orderedValue = value;
    this.refresh();
  }

  private void refresh() {
    if (restrictedValue == null)
      this.targetValue = this.orderedValue;
    else {
      switch (restrictedValue.direction) {
        case atLeast:
          this.targetValue = Math.max(this.orderedValue, this.restrictedValue.value);
          break;
        case atMost:
          this.targetValue = Math.min(this.orderedValue, this.restrictedValue.value);
          break;
        case exactly:
          this.targetValue = this.restrictedValue.value;
          break;
        default:
          throw new EEnumValueUnsupportedException(restrictedValue.direction);
      }
    }
  }
}
