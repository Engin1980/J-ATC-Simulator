package eng.jAtcSim.newLib.airplanes.modules.sha;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.IPlainObjectSimPersistable;

class RestrictableItem implements IPlainObjectSimPersistable {
  /**
   * Ordered value is what has been ordered without respect to the restriction.
   */
  private int orderedValue;
  /**
   * Restriction.
   */
  private Restriction restrictedValue;
  /**
   * Ordered value with applied restriction.
   */
  private int targetValue;

  @XmlConstructor
  RestrictableItem(int targetValue) {
    setTargetValue(targetValue);
  }

  final void clearRestriction() {
    this.restrictedValue = null;
    this.refresh();
  }

  Restriction getRestriction() {
    return restrictedValue;
  }

  int getTargetValue() {
    return this.targetValue;
  }

  private void refresh() {
    if (restrictedValue == null)
      this.targetValue = this.orderedValue;
    else {
      switch (restrictedValue.direction) {
        case above:
          this.targetValue = Math.max(this.orderedValue, this.restrictedValue.value);
          break;
        case below:
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

  final void setRestriction(Restriction restriction) {
    this.restrictedValue = restriction;
    this.refresh();
  }

  final void setTargetValue(int value) {
    this.orderedValue = value;
    this.refresh();
  }
}
