package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.enums.LeftRight;

import exml.annotations.XConstructor;

public class HoldCommand extends ToNavaidCommand {

  public static HoldCommand createPublished(String navaidName) {
    EAssert.Argument.isNotNull(navaidName, "navaidName");

    HoldCommand ret = new HoldCommand(navaidName,true, 0, LeftRight.left);
    return ret;
  }

  public static HoldCommand createExplicit(String navaidName, int inboundRadial, LeftRight turn) {
    EAssert.Argument.isNotNull(navaidName, "navaidName");
    HoldCommand ret = new HoldCommand(navaidName, false, inboundRadial, turn);
    return ret;
  }

  private final boolean published;
  private final int inboundRadial;
  private final LeftRight turn;

  @XConstructor

  private HoldCommand() {
    super("?");
    this.published = false;
    this.inboundRadial = 0;
    this.turn = LeftRight.left;
  }

  private HoldCommand(String navaidName, boolean published, int inboundRadial, LeftRight turn) {
    super(navaidName);
    this.published = published;
    this.inboundRadial = inboundRadial;
    this.turn = turn;
  }

  public int getInboundRadial() {
    return inboundRadial;
  }

  public LeftRight getTurn() {
    EAssert.isFalse(this.published);
    return turn;
  }

  public boolean isPublished() {
    return published;
  }

  @Override
  public String toString() {
    if (isPublished()) {
      return "Hold over " + super.getNavaidName() + " as published {command}";
    } else {
      return "Hold over " + getNavaidName()
          + " inbound " + inboundRadial
          + " turns " + turn.toString()
          + " {command}";
    }
  }
}
