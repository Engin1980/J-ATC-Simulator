package eng.jAtcSim.lib.airplanes.behaviors;

import eng.eSystem.EStringBuilder;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.world.Navaid;

public class HoldBehavior extends DivertableBehavior {

  public enum eHoldPhase{
    directEntry,
    parallelEntry,
    parallelAgainst,
    parallelTurn,
    tearEntry,
    tearAgainst,
    firstTurn,
    outbound,
    secondTurn,
    inbound
  }

  private static final double NEAR_FIX_DISTANCE = 0.5;
  private static final int FL100 = 10000;
  //TODO Make all fields private
  public Boolean isBelowFL100 = null;
  public Navaid navaid;
  public int inboundRadial;
  public eHoldPhase phase;
  public ETime secondTurnTime;
  public boolean isLeftTurned;

  @XmlConstructor
  private HoldBehavior() {
  }

  @XmlConstructor
  public HoldBehavior(IAirplaneWriteSimple pilot, Navaid navaid, int inboundRadial, boolean isLeftTurned) {
    this.navaid = navaid;
    this.inboundRadial = inboundRadial;
    this.isLeftTurned = isLeftTurned;
    setHoldDataByEntry(pilot);
  }

  public int getAfterSecondTurnHeading() {
    int ret;
    if (isLeftTurned)
      ret = (int) Headings.to(inboundRadial + 30);
    else
      ret = (int) Headings.to(inboundRadial - 30);
    return ret;
  }

  @Override
  public void fly(IAirplaneWriteSimple plane) {
    if (plane.getState() != Airplane.State.holding)
      super.throwIllegalStateException(plane);

    switch (this.phase) {
      case directEntry:
        if (Coordinates.getDistanceInNM(plane.getCoordinate(), this.navaid.getCoordinate()) < NEAR_FIX_DISTANCE) {
          plane.setTargetHeading(this.getOutboundHeading(), this.isLeftTurned);
          this.phase = eHoldPhase.firstTurn;
        } else {
          int newHeading = (int) Coordinates.getBearing(plane.getCoordinate(), this.navaid.getCoordinate());
          plane.setTargetHeading(newHeading);
        }
        break;
      case inbound:
        if (Coordinates.getDistanceInNM(plane.getCoordinate(), this.navaid.getCoordinate()) < NEAR_FIX_DISTANCE) {
          plane.setTargetHeading(this.getOutboundHeading(), this.isLeftTurned);
          this.phase = eHoldPhase.firstTurn;
          if (plane.getFlightModule().isArrival())
            plane.getAdvanced().addExperience(Mood.ArrivalExperience.holdCycleFinished);
          else
            plane.getAdvanced().addExperience(Mood.DepartureExperience.holdCycleFinished);
        } else {
          double newHeading = Coordinates.getHeadingToRadial(
              plane.getCoordinate(), this.navaid.getCoordinate(), this.inboundRadial,
              Coordinates.eHeadingToRadialBehavior.standard);
          plane.setTargetHeading(newHeading);

        }
        break;
      case firstTurn:
        if (plane.getSha().getTargetHeading() == plane.getSha().getHeading()) {
          this.secondTurnTime = Acc.now().addSeconds(60);
          this.phase = eHoldPhase.outbound;
        }
        break;
      case outbound:
        if (Acc.now().isAfter(this.secondTurnTime)) {
          plane.setTargetHeading(this.getAfterSecondTurnHeading(), this.isLeftTurned);
          this.phase = eHoldPhase.secondTurn;
        }
        break;
      case secondTurn:
        if (plane.getSha().getTargetHeading() == plane.getSha().getHeading()) {
          this.phase = eHoldPhase.inbound;
        }
        break;

      case tearEntry:
        if (Coordinates.getDistanceInNM(plane.getCoordinate(), this.navaid.getCoordinate()) < NEAR_FIX_DISTANCE) {

          double newHeading;
          newHeading = this.isLeftTurned
              ? Headings.add(this.inboundRadial, -150)
              : Headings.add(this.inboundRadial, 150);
          plane.setTargetHeading(newHeading);
          this.secondTurnTime = Acc.now().addSeconds(120);

          this.phase = eHoldPhase.tearAgainst;
        } else {
          double newHeading = Coordinates.getBearing(plane.getCoordinate(), this.navaid.getCoordinate());
          plane.setTargetHeading(newHeading);
        }
        break;

      case tearAgainst:
        if (Acc.now().isAfter(this.secondTurnTime)) {
          this.secondTurnTime = null;
          plane.setTargetHeading(this.inboundRadial, this.isLeftTurned);
          this.phase = eHoldPhase.secondTurn;
        }
        break;

      case parallelEntry:
        if (Coordinates.getDistanceInNM(plane.getCoordinate(), this.navaid.getCoordinate()) < NEAR_FIX_DISTANCE) {
          plane.setTargetHeading(this.getOutboundHeading(), !this.isLeftTurned);
          this.secondTurnTime = Acc.now().addSeconds(60);
          this.phase = eHoldPhase.parallelAgainst;
        } else {
          int newHeading = (int) Coordinates.getBearing(plane.getCoordinate(), this.navaid.getCoordinate());
          plane.setTargetHeading(newHeading);
        }
        break;

      case parallelAgainst:
        if (Acc.now().isAfter(this.secondTurnTime)) {
          double newHeading = (this.isLeftTurned)
              ? Headings.add(this.getOutboundHeading(), -210)
              : Headings.add(this.getOutboundHeading(), +210);
          plane.setTargetHeading(newHeading, !this.isLeftTurned);
          this.phase = eHoldPhase.parallelTurn;
        }
        break;

      case parallelTurn:
        if (plane.getSha().getHeading() == plane.getSha().getTargetHeading()) {
          this.phase = eHoldPhase.directEntry;
        }
        break;

      default:
        throw new EEnumValueUnsupportedException(this.phase);
    }

    if (isBelowFL100 == null) {
      isBelowFL100 = plane.getSha().getAltitude() <= FL100;
    } else if (isBelowFL100 && plane.getSha().getAltitude() > FL100) {
      plane.adjustTargetSpeed();
      isBelowFL100 = plane.getSha().getAltitude() <= FL100;
    } else if (!isBelowFL100 && plane.getSha().getAltitude() <= FL100) {
      plane.adjustTargetSpeed();
      isBelowFL100 = plane.getSha().getAltitude() <= FL100;
    }
  }

  @Override
  public String toLogString() {

    EStringBuilder sb = new EStringBuilder();

    sb.appendFormat("HLD %s incrs: %03d/%s in: %s",
        this.navaid.getName(),
        this.inboundRadial,
        this.isLeftTurned ? "L" : "R",
        this.phase.toString());

    return sb.toString();
  }

  private double getOutboundHeading() {
    return Headings.add(inboundRadial, 180);
  }

  private void setHoldDataByEntry(IAirplaneWriteSimple plane) {
    double y = Coordinates.getBearing(plane.getCoordinate(), this.navaid.getCoordinate());
    y = Headings.add(y, 180);

    int h = this.inboundRadial;
    double a;
    double b;
    if (this.isLeftTurned) {
      a = Headings.add(h, -110);
      b = Headings.add(h, 75);
      if (Headings.isBetween(a, y, h))
        this.phase = eHoldPhase.parallelEntry;
      else if (Headings.isBetween(h, y, b))
        this.phase = eHoldPhase.tearEntry;
      else
        this.phase = eHoldPhase.directEntry;
    } else {
      a = Headings.add(h, -75);
      b = Headings.add(h, 110);
      if (Headings.isBetween(a, y, h))
        this.phase = eHoldPhase.tearEntry;
      else if (Headings.isBetween(h, y, b))
        this.phase = eHoldPhase.parallelEntry;
      else
        this.phase = eHoldPhase.directEntry;
    }
  }
}
