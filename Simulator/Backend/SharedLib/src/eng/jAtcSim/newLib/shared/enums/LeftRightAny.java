package eng.jAtcSim.newLib.shared.enums;

import eng.eSystem.validation.EAssert;

public enum LeftRightAny {
  left,
  right,
  any;

  public LeftRight toLeftRight() {
    EAssert.Argument.isTrue(this != LeftRightAny.any, "Unable to convert 'any' to LeftRight.");

    if (this == LeftRightAny.left)
      return LeftRight.left;
    else
      return LeftRight.right;
  }
}
