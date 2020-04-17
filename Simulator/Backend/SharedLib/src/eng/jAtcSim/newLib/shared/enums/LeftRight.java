package eng.jAtcSim.newLib.shared.enums;

import eng.eSystem.validation.EAssert;

public enum LeftRight {
  left,
  right;

  public LeftRight getOpposite() {
    if (this == left)
      return right;
    else
      return left;
  }

  public LeftRightAny toLeftRightAny() {
    if (this == LeftRight.left)
      return LeftRightAny.left;
    else
      return LeftRightAny.right;
  }
}
