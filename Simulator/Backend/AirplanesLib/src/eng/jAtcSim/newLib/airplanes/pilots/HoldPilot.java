package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.mood.Mood;
import eng.jAtcSim.newLib.shared.GAcc;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public class HoldPilot extends Pilot {

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
  //TODO Make all fields private
  public Navaid navaid;
  public int inboundRadial;
  public eHoldPhase phase;
  public EDayTimeStamp secondTurnTime;
  public boolean isLeftTurned;

  public HoldPilot(IPilotPlane pilot, Navaid navaid, int inboundRadial, boolean isLeftTurned) {
    super(pilot);
    this.navaid = navaid;
    this.inboundRadial = inboundRadial;
    this.isLeftTurned = isLeftTurned;
    setHoldDataByEntry();
  }

  private int getAfterSecondTurnHeading() {
    int ret;
    if (isLeftTurned)
      ret = (int) Headings.to(inboundRadial + 30);
    else
      ret = (int) Headings.to(inboundRadial - 30);
    return ret;
  }

  @Override
  public void elapseSecond() {
    if (plane.getState() != Airplane.State.holding)
      super.throwIllegalStateException();

    EDayTimeRun now = GAcc.getNow();

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
          if (plane.isArrival())
            plane.addExperience(Mood.ArrivalExperience.holdCycleFinished);
          else
            plane.addExperience(Mood.DepartureExperience.holdCycleFinished);
        } else {
          double newHeading = Coordinates.getHeadingToRadial(
              plane.getCoordinate(), this.navaid.getCoordinate(), this.inboundRadial,
              Coordinates.eHeadingToRadialBehavior.standard);
          plane.setTargetHeading(newHeading);

        }
        break;
      case firstTurn:
        if (plane.getTargetHeading() == plane.getHeading()) {
          this.secondTurnTime = now.toStamp().addSeconds(60);
          this.phase = eHoldPhase.outbound;
        }
        break;
      case outbound:
        if (now.isAfter(this.secondTurnTime)) {
          plane.setTargetHeading(this.getAfterSecondTurnHeading(), this.isLeftTurned);
          this.phase = eHoldPhase.secondTurn;
        }
        break;
      case secondTurn:
        if (plane.getTargetHeading() == plane.getHeading()) {
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
          this.secondTurnTime = now.toStamp().addSeconds(120);

          this.phase = eHoldPhase.tearAgainst;
        } else {
          double newHeading = Coordinates.getBearing(plane.getCoordinate(), this.navaid.getCoordinate());
          plane.setTargetHeading(newHeading);
        }
        break;

      case tearAgainst:
        if (now.isAfter(this.secondTurnTime)) {
          this.secondTurnTime = null;
          plane.setTargetHeading(this.inboundRadial, this.isLeftTurned);
          this.phase = eHoldPhase.secondTurn;
        }
        break;

      case parallelEntry:
        if (Coordinates.getDistanceInNM(plane.getCoordinate(), this.navaid.getCoordinate()) < NEAR_FIX_DISTANCE) {
          plane.setTargetHeading(this.getOutboundHeading(), !this.isLeftTurned);
          this.secondTurnTime = now.toStamp().addSeconds(60);
          this.phase = eHoldPhase.parallelAgainst;
        } else {
          int newHeading = (int) Coordinates.getBearing(plane.getCoordinate(), this.navaid.getCoordinate());
          plane.setTargetHeading(newHeading);
        }
        break;

      case parallelAgainst:
        if (now.isAfter(this.secondTurnTime)) {
          double newHeading = (this.isLeftTurned)
              ? Headings.add(this.getOutboundHeading(), -210)
              : Headings.add(this.getOutboundHeading(), +210);
          plane.setTargetHeading(newHeading, !this.isLeftTurned);
          this.phase = eHoldPhase.parallelTurn;
        }
        break;

      case parallelTurn:
        if (plane.getHeading() == plane.getTargetHeading()) {
          this.phase = eHoldPhase.directEntry;
        }
        break;

      default:
        throw new EEnumValueUnsupportedException(this.phase);
    }

    throw new ToDoException("Use this or not?");
//    if (isBelowFL100 == null) {
//      isBelowFL100 = plane.getAltitude() <= FL100;
//    } else if (isBelowFL100 && plane.getAltitude() > FL100) {
//      plane.adjustTargetSpeed();
//      isBelowFL100 = plane.getAltitude() <= FL100;
//    } else if (!isBelowFL100 && plane.getAltitude() <= FL100) {
//      plane.adjustTargetSpeed();
//      isBelowFL100 = plane.getAltitude() <= FL100;
//    }
  }

  private double getOutboundHeading() {
    return Headings.add(inboundRadial, 180);
  }

  private void setHoldDataByEntry() {
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


  @Override
  public boolean isDivertable() {
    return true;
  }
}
