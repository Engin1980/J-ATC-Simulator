package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.accessors.IPlaneInterface;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.HeadingNavigator;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.mood.Mood;
import eng.jAtcSim.newLib.shared.GAcc;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.jAtcSim.newLib.shared.enums.LeftRightAny;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public class HoldPilot extends Pilot {

  public enum eHoldPhase {
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
  public LeftRight turn;

  public HoldPilot(IPlaneInterface pilot, Navaid navaid, int inboundRadial, LeftRight turn) {
    super(pilot);
    this.navaid = navaid;
    this.inboundRadial = inboundRadial;
    this.turn = turn;
    setHoldDataByEntry();
  }

  @Override
  public void elapseSecondInternal() {
    if (plane.getState() != Airplane.State.holding)
      super.throwIllegalStateException();

    EDayTimeRun now = GAcc.getNow();

    switch (this.phase) {
      case directEntry:
        if (Coordinates.getDistanceInNM(plane.getCoordinate(), this.navaid.getCoordinate()) < NEAR_FIX_DISTANCE) {
          setTargetHeading(this.getOutboundHeading(), turn.toLeftRightAny());
          this.phase = eHoldPhase.firstTurn;
        } else {
          int newHeading = (int) Coordinates.getBearing(plane.getCoordinate(), this.navaid.getCoordinate());
          setTargetHeading(newHeading, LeftRightAny.any);
        }
        break;
      case inbound:
        if (Coordinates.getDistanceInNM(plane.getCoordinate(), this.navaid.getCoordinate()) < NEAR_FIX_DISTANCE) {
          setTargetHeading(this.getOutboundHeading(), turn.toLeftRightAny());
          this.phase = eHoldPhase.firstTurn;
          if (plane.isArrival())
            plane.addExperience(Mood.ArrivalExperience.holdCycleFinished);
          else
            plane.addExperience(Mood.DepartureExperience.holdCycleFinished);
        } else {
          double newHeading = Coordinates.getHeadingToRadial(
              plane.getCoordinate(), this.navaid.getCoordinate(), this.inboundRadial,
              Coordinates.eHeadingToRadialBehavior.standard);
          setTargetHeading(newHeading, LeftRightAny.any);

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
          setTargetHeading(this.getAfterSecondTurnHeading(), turn.toLeftRightAny());
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
          newHeading = this.turn == LeftRight.left
              ? Headings.add(this.inboundRadial, -150)
              : Headings.add(this.inboundRadial, 150);
          setTargetHeading(newHeading, LeftRightAny.any);
          this.secondTurnTime = now.toStamp().addSeconds(120);

          this.phase = eHoldPhase.tearAgainst;
        } else {
          double newHeading = Coordinates.getBearing(plane.getCoordinate(), this.navaid.getCoordinate());
          setTargetHeading(newHeading, LeftRightAny.any);
        }
        break;

      case tearAgainst:
        if (now.isAfter(this.secondTurnTime)) {
          this.secondTurnTime = null;
          setTargetHeading(this.inboundRadial, turn.toLeftRightAny());
          this.phase = eHoldPhase.secondTurn;
        }
        break;

      case parallelEntry:
        if (Coordinates.getDistanceInNM(plane.getCoordinate(), this.navaid.getCoordinate()) < NEAR_FIX_DISTANCE) {
          setTargetHeading(this.getOutboundHeading(), turn.getOpposite().toLeftRightAny());
          this.secondTurnTime = now.toStamp().addSeconds(60);
          this.phase = eHoldPhase.parallelAgainst;
        } else {
          int newHeading = (int) Coordinates.getBearing(plane.getCoordinate(), this.navaid.getCoordinate());
          setTargetHeading(newHeading, LeftRightAny.any);
        }
        break;

      case parallelAgainst:
        if (now.isAfter(this.secondTurnTime)) {
          double newHeading = (turn == LeftRight.left)
              ? Headings.add(this.getOutboundHeading(), -210)
              : Headings.add(this.getOutboundHeading(), +210);
          setTargetHeading(newHeading, turn.getOpposite().toLeftRightAny());
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

  @Override
  public boolean isDivertable() {
    return true;
  }

  private int getAfterSecondTurnHeading() {
    int ret;
    if (turn == LeftRight.left)
      ret = (int) Headings.to(inboundRadial + 30);
    else
      ret = (int) Headings.to(inboundRadial - 30);
    return ret;
  }

  @Override
  protected Airplane.State[] getInitialStates() {
    return new Airplane.State[]{
        Airplane.State.arrivingHigh,
        Airplane.State.arrivingLow,
        Airplane.State.holding,
        Airplane.State.departingLow,
        Airplane.State.departingHigh
    };
  }

  private double getOutboundHeading() {
    return Headings.add(inboundRadial, 180);
  }

  @Override
  protected Airplane.State[] getValidStates() {
    return new Airplane.State[]{Airplane.State.holding};
  }

  private void setHoldDataByEntry() {
    double y = Coordinates.getBearing(plane.getCoordinate(), this.navaid.getCoordinate());
    y = Headings.add(y, 180);

    int h = this.inboundRadial;
    double a;
    double b;
    if (this.turn == LeftRight.left) {
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

  private void setTargetHeading(double heading, LeftRightAny turn) {
    plane.setTargetHeading(new HeadingNavigator(heading, turn));
  }
}
