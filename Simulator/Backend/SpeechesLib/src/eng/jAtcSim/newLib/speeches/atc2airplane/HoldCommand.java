package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.eSystem.validation.EAssert;

public class HoldCommand extends ToNavaidCommand {

  public static HoldCommand createPublished(String navaidName) {
    EAssert.Argument.isNotNull(navaidName, "navaidName");

    HoldCommand ret = new HoldCommand(navaidName,true, 0, false);
    return ret;
  }

  public static HoldCommand createExplicit(String navaidName, int inboundRadial, boolean isLeftTurn) {
    EAssert.Argument.isNotNull(navaidName, "navaidName");

    HoldCommand ret = new HoldCommand(navaidName, false, inboundRadial, isLeftTurn);
    return ret;
  }

  private final boolean published;
  private final int inboundRadial;
  private final boolean leftTurn;

  private HoldCommand(String navaidName, boolean published, int inboundRadial, boolean leftTurn) {
    super(navaidName);
    this.published = published;
    this.inboundRadial = inboundRadial;
    this.leftTurn = leftTurn;
  }

  public int getInboundRadial() {
    return inboundRadial;
  }

  public boolean isLeftTurn() {
    EAssert.isFalse(this.published);
    return leftTurn;
  }

  public boolean isPublished() {
    return published;
  }

  public boolean isRightTurn() {
    EAssert.isFalse(this.published);
    return !isLeftTurn();
  }

  @Override
  public String toString() {
    if (isPublished()) {
      return "Hold over " + super.getNavaidName() + " as published {command}";
    } else {
      return "Hold over " + getNavaidName()
          + " inbound " + leftTurn
          + " turns " + (leftTurn ? "left" : "right")
          + " {command}";
    }
  }
}
