package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.HeadingNavigator;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.mood.Mood;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.jAtcSim.newLib.shared.enums.LeftRightAny;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import exml.XContext;
import exml.annotations.XConstructor;

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

  @XConstructor
  private HoldPilot(XContext ctx) {
    super(ctx.loader.parents.get(Airplane.class));
  }

  public HoldPilot(Airplane plane, Navaid navaid, int inboundRadial, LeftRight turn) {
    super(plane);
    this.navaid = navaid;
    this.inboundRadial = inboundRadial;
    this.turn = turn;
    setHoldDataByEntry();
  }

  @Override
  public void elapseSecondInternal() {
    if (rdr.getState() != AirplaneState.holding)
      super.throwIllegalStateException();

    EDayTimeRun now = Context.getShared().getNow();

    switch (this.phase) {
      case directEntry:
        if (Coordinates.getDistanceInNM(rdr.getCoordinate(), this.navaid.getCoordinate()) < NEAR_FIX_DISTANCE) {
          setTargetHeading(this.getOutboundHeading(), turn.toLeftRightAny());
          this.phase = eHoldPhase.firstTurn;
        } else {
          int newHeading = (int) Coordinates.getBearing(rdr.getCoordinate(), this.navaid.getCoordinate());
          setTargetHeading(newHeading, LeftRightAny.any);
        }
        break;
      case inbound:
        if (Coordinates.getDistanceInNM(rdr.getCoordinate(), this.navaid.getCoordinate()) < NEAR_FIX_DISTANCE) {
          setTargetHeading(this.getOutboundHeading(), turn.toLeftRightAny());
          this.phase = eHoldPhase.firstTurn;
          if (rdr.isArrival())
            wrt.addExperience(Mood.ArrivalExperience.holdCycleFinished);
          else
            wrt.addExperience(Mood.DepartureExperience.holdCycleFinished);
        } else {
          //FIXME
          double newHeading = Coordinates.getHeadingToRadial(
                  rdr.getCoordinate(), this.navaid.getCoordinate(), this.inboundRadial,
                  Coordinates.eHeadingToRadialBehavior.standard);
          setTargetHeading(newHeading, LeftRightAny.any);

        }
        break;
      case firstTurn:
        if (rdr.getSha().getTargetHeading() == rdr.getSha().getHeading()) {
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
        if (rdr.getSha().getTargetHeading() == rdr.getSha().getHeading()) {
          this.phase = eHoldPhase.inbound;
        }
        break;

      case tearEntry:
        if (Coordinates.getDistanceInNM(rdr.getCoordinate(), this.navaid.getCoordinate()) < NEAR_FIX_DISTANCE) {

          double newHeading;
          newHeading = this.turn == LeftRight.left
                  ? Headings.add(this.inboundRadial, -150)
                  : Headings.add(this.inboundRadial, 150);
          setTargetHeading(newHeading, LeftRightAny.any);
          this.secondTurnTime = now.toStamp().addSeconds(120);

          this.phase = eHoldPhase.tearAgainst;
        } else {
          double newHeading = Coordinates.getBearing(rdr.getCoordinate(), this.navaid.getCoordinate());
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
        if (Coordinates.getDistanceInNM(rdr.getCoordinate(), this.navaid.getCoordinate()) < NEAR_FIX_DISTANCE) {
          setTargetHeading(this.getOutboundHeading(), turn.getOpposite().toLeftRightAny());
          this.secondTurnTime = now.toStamp().addSeconds(60);
          this.phase = eHoldPhase.parallelAgainst;
        } else {
          int newHeading = (int) Coordinates.getBearing(rdr.getCoordinate(), this.navaid.getCoordinate());
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
        if (rdr.getSha().getHeading() == rdr.getSha().getTargetHeading()) {
          this.phase = eHoldPhase.directEntry;
        }
        break;

      default:
        throw new EEnumValueUnsupportedException(this.phase);
    }

    //TODEL
//    throw new ToDoException("Use this or not?");
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

  @Override
  public void load(XElement elm, XContext ctx) {
    super.load(elm, ctx);
  }

  @Override
  public void save(XElement elm, XContext ctx) {
    super.save(elm, ctx);
    ctx.saver.saveRemainingFields(this, elm);
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
  protected AirplaneState[] getInitialStates() {
    return new AirplaneState[]{
            AirplaneState.arrivingHigh,
            AirplaneState.arrivingLow,
            AirplaneState.holding,
            AirplaneState.departingLow,
            AirplaneState.departingHigh
    };
  }

  private double getOutboundHeading() {
    return Headings.add(inboundRadial, 180);
  }

  @Override
  protected AirplaneState[] getValidStates() {
    return new AirplaneState[]{AirplaneState.holding};
  }

  private void setHoldDataByEntry() {
    double y = Coordinates.getBearing(rdr.getCoordinate(), this.navaid.getCoordinate());
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
    wrt.setTargetHeading(new HeadingNavigator(heading, turn));
  }
}
