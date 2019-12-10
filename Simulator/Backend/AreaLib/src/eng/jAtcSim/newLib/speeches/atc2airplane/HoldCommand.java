package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.PublishedHold;
import eng.jAtcSim.sharedLib.exceptions.ApplicationException;
import eng.jAtcSim.sharedLib.xml.XmlLoader;

public class HoldCommand extends ToNavaidCommand {

  public static HoldCommand create(PublishedHold publishedHold) {
    if (publishedHold == null) {
      throw new IllegalArgumentException("Value of {publishedHold} cannot not be null.");
    }

    HoldCommand ret = new HoldCommand(publishedHold.getNavaid(), false, publishedHold.getInboundRadial(), publishedHold.isLeftTurn());
    return ret;
  }

  public static HoldCommand create(Navaid navaid, int inboundRadial, boolean isLeftTurn) {
    if (navaid == null) {
      throw new IllegalArgumentException("Value of {navaid} cannot not be null.");
    }

    HoldCommand ret = new HoldCommand(navaid, false, inboundRadial, isLeftTurn);
    return ret;
  }

  public static HoldCommand load(XElement element, Airport parent) {
    assert element.getName().equals("hold");

    HoldCommand ret;

    XmlLoader.setContext(element);
    String fix = XmlLoader.loadString("fix");
    Integer inboundRadial = XmlLoader.loadInteger("inboundRadial", null);
    String turns = XmlLoader.loadStringRestricted("turns", new String[]{"left", "right"}, null);

    Navaid navaid = parent.getParent().getNavaids().get(fix);

    if (inboundRadial == null && turns == null) {
      PublishedHold publishedHold = parent.getHolds().getFirst(q -> q.getNavaid().equals(navaid));
      ret = HoldCommand.create(publishedHold);
    } else if (inboundRadial == null || turns == null) {
      throw new ApplicationException("For hold command, both or none of 'inboundRadial' and 'turns' must be set.");
    } else {
      ret = HoldCommand.create(navaid, inboundRadial, turns.equals("left"));
    }
    return ret;
  }

  private final boolean published;
  private final int inboundHeading;
  private final boolean leftTurn;

  private HoldCommand(Navaid navaid, boolean published, int inboundHeading, boolean leftTurn) {
    super(navaid);
    this.published = published;
    this.inboundHeading = inboundHeading;
    this.leftTurn = leftTurn;
  }

  public int getInboundHeading() {
    return inboundHeading;
  }

  public boolean isLeftTurn() {
    return leftTurn;
  }

  public boolean isPublished() {
    return published;
  }

  public boolean isRightTurn() {
    return !isLeftTurn();
  }

  @Override
  public String toString() {
    if (isPublished()) {
      return "Hold over " + super.getNavaid().getName() + " as published {command}";
    } else {
      return "Hold over " + getNavaid().getName()
          + " inbound " + leftTurn
          + " turns " + (leftTurn ? "left" : "right")
          + " {command}";
    }
  }
}
