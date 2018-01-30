/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.speaking.fromAtc.commands;

import jatcsimlib.world.Navaid;
import jatcsimlib.world.PublishedHold;

/**
 *
 * @author Marek
 */
public class HoldCommand extends ToNavaidCommand {

  private final int inboundHeading;
  private final boolean leftTurn;
  private final PublishedHold publishedHold;

  public HoldCommand(PublishedHold publishedHold) {
    super(publishedHold.getNavaid());
    if (publishedHold == null) {
      throw new IllegalArgumentException("Argument \"publishedHold\" cannot be null.");
    }

    this.publishedHold = publishedHold;
    this.inboundHeading = Integer.MIN_VALUE;
    this.leftTurn = false;
  }

  public HoldCommand(Navaid navaid, int inboundHeading, boolean leftTurn) {
    super(navaid);
    
    this.inboundHeading = inboundHeading;
    this.leftTurn = leftTurn;
    this.publishedHold = null;
  }

  public Navaid getNavaid() {
    if (publishedHold != null) {
      return publishedHold.getNavaid();
    } else {
      return this.navaid;
    }
  }

  public int getInboundRadial() {
    if (publishedHold != null) {
      return publishedHold.getInboundRadial();
    } else {
      return inboundHeading;
    }
  }

  public boolean isLeftTurn() {
    if (publishedHold != null) {
      return publishedHold.isLeftTurn();
    } else {
      return leftTurn;
    }
  }

  public boolean isPublished() {
    return publishedHold != null;
  }

  @Override
  public String toString() {
    if (isPublished()) {
      return "Hold over " + getNavaid().getName() + " as published {command}";
    } else {
      return "Hold over " + getNavaid().getName()
          + " inbound " + leftTurn
          + " turns " + (leftTurn ? "left" : "right")
          + " {command}";
    }
  }

}
