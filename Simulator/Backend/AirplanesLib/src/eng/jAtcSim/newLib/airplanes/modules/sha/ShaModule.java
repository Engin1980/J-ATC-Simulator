package eng.jAtcSim.newLib.airplanes.modules.sha;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Headings;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.HeadingNavigator;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.Navigator;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.NavigatorResult;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.ToCoordinateNavigator;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import exml.XContext;
import exml.annotations.XConstructor;

public class ShaModule extends eng.jAtcSim.newLib.airplanes.modules.Module {

  private final static double GROUND_SPEED_CHANGE_MULTIPLIER = 1.5; //1.5; //3.0;

  private static double getHeadingChangeDenominator(AirplaneType planeType) {
    double ret;
    switch (planeType.category) {
      case 'A':
      case 'B':
        ret = 4;
        break;
      case 'C':
        ret = 5;
        break;
      case 'D':
      case 'E':
        ret = 7;
        break;
      default:
        throw new UnsupportedOperationException("Heading-change-denominator for category " + planeType.category
                + " cannot be determined.");
    }
    return ret;
  }

  private static ValueRequest getRequest(double current, double target, double maxIncreaseStep, double maxDecreaseStep) {
    // if on ground, nothing required
    final double RUN_OUT_COEFF = 0.2;
    final double RUN_OUT_DISTANCE = 3;

    double delta = target - current;
    if (delta == 0) {
      return new ValueRequest();
      // no change required
    }

    double absDelta = delta;
    double availableStep;
    if (delta > 0) {
      // needs to accelerate
      availableStep = maxIncreaseStep;
    } else {
      availableStep = maxDecreaseStep;
      absDelta = -delta;
    }

    ValueRequest ret = new ValueRequest();
    double deltaPress = absDelta / availableStep;
    if (deltaPress > RUN_OUT_DISTANCE) {
      ret.value = availableStep;
      ret.energy = 1;
    } else if (deltaPress > 1) {
      ret.value = availableStep * RUN_OUT_COEFF;
      ret.energy = RUN_OUT_COEFF;
    } else {
      absDelta = Math.min(absDelta, availableStep * RUN_OUT_COEFF);
      ret.value = absDelta;
      ret.energy = absDelta / availableStep;
    }

    if (delta < 0)
      ret.multiply(-1);

    return ret;
  }

  private final InertialValue altitude;
  private final HeadingInertialValue heading;
  private double lastVerticalSpeed = 0;
  private Navigator navigator;
  private final InertialValue speed;
  private final RestrictableItem targetAltitude;
  private int targetHeading;
  private LeftRight targetHeadingTurn;
  private final RestrictableItem targetSpeed;

  @XConstructor
  private ShaModule(XContext ctx,
                    InertialValue altitude, HeadingInertialValue heading,
                    InertialValue speed, RestrictableItem targetAltitude,
                    RestrictableItem targetSpeed) {
    super(ctx);
    this.altitude = altitude;
    this.heading = heading;
    this.speed = speed;
    this.targetAltitude = targetAltitude;
    this.targetSpeed = targetSpeed;
  }

  public ShaModule(Airplane plane, int heading, int altitude, int speed, AirplaneType planeType, int airportAltitude) {
    super(plane);
    this.targetAltitude = new RestrictableItem(altitude);
    this.targetHeading = heading;
    this.targetSpeed = new RestrictableItem(speed);

    this.navigator = new HeadingNavigator(heading);
    double headingChangeDenominator = getHeadingChangeDenominator(planeType);
    this.heading = new HeadingInertialValue(
            heading,
            planeType.headingChangeRate,
            planeType.headingChangeRate / headingChangeDenominator);

    this.altitude = new InertialValue(
            altitude,
            planeType.lowClimbRate / 7d / 60,
            planeType.highDescendRate / 7d / 60,
            (double) airportAltitude);

    this.speed = new InertialValue(
            speed,
            planeType.speedIncreaseRate / 4d,
            planeType.speedDecreaseRate / 6d,
            0d);
  }

  public void clearTargetAltitudeRestriction() {
    this.targetAltitude.clearRestriction();
  }

  public void clearTargetSpeedRestriction() {
    this.targetSpeed.clearRestriction();
  }

  @Override
  public void elapseSecond() {
    // TODO here is && or || ???
    boolean isSpeedPreffered = rdr.getState().is(
            AirplaneState.takeOff, AirplaneState.takeOffRoll);


    if (this.getTargetAltitude() != this.getAltitude()
            || this.getTargetSpeed() != this.getSpeed()) {
      ValueRequest speedRequest = getSpeedRequest();
      ValueRequest altitudeRequest = getAltitudeRequest();

      double totalEnergy = Math.abs(speedRequest.energy + altitudeRequest.energy);
      if (totalEnergy > 1) {
        if (!isSpeedPreffered) {
          double energyMultiplier = 1 / totalEnergy;
          speedRequest.multiply(energyMultiplier);
          altitudeRequest.multiply(energyMultiplier);
        } else {
          // when speed is preferred
          double energyLeft = 1 - speedRequest.energy;
          altitudeRequest.multiply(energyLeft);
        }
      }

      adjustSpeed(speedRequest);
      adjustAltitude(altitudeRequest);

    } else if (this.lastVerticalSpeed != 0)
      this.lastVerticalSpeed = 0;

    NavigatorResult nr = this.navigator.navigate(rdr);
    if (nr != null) {
      this.targetHeading = nr.getHeading();
      this.targetHeadingTurn = nr.getTurn();
    }
    if (targetHeading != heading.getValue()) {
      adjustHeading();
    } else {
      this.heading.resetInertia();
    }
  }

  public int getAltitude() {
    return (int) altitude.value;
  }

  public int getGS() {
    return this.getTAS();
  }

  public int getHeading() {
    return (int) Math.round(heading.value);
  }

  public Navigator getNavigator() {
    return this.navigator;
  }

  public void setNavigator(Navigator navigator) {
    EAssert.Argument.isNotNull(navigator, "navigator");
    this.navigator = navigator;
  }

  public int getSpeed() {
    return (int) Math.round(speed.value);
  }

  public Restriction getSpeedRestriction() {
    return this.targetSpeed.getRestriction();
  }

  public void setSpeedRestriction(Restriction speedRestriction) {
    this.targetSpeed.setRestriction(speedRestriction);
  }

  public int getTAS() {
    double m = 1 + this.getAltitude() / 100000d;
    double ret = this.getSpeed() * m;
    return (int) Math.round(ret);
  }

  public int getTargetAltitude() {
    return targetAltitude.getTargetValue();
  }

  public void setTargetAltitude(int altitude) {
    this.targetAltitude.setTargetValue(altitude);
  }

  public int getTargetHeading() {
    return targetHeading;
  }

  public int getTargetSpeed() {
    return targetSpeed.getTargetValue();
  }

  public void setTargetSpeed(int speed) {
    this.targetSpeed.setTargetValue(speed);
  }

  public int getVerticalSpeed() {
    return (int) Math.round(this.lastVerticalSpeed);
  }

  public void resetHeading(double heading) {
    this.heading.reset(heading);
  }

  public void setAltitudeRestriction(Restriction altitudeRestriction) {
    this.targetAltitude.setRestriction(altitudeRestriction);
  }

  public Coordinate tryGetTargetCoordinate() {
    Coordinate ret = null;
    if (this.navigator instanceof ToCoordinateNavigator) {
      ToCoordinateNavigator toCoordinateNavigator = (ToCoordinateNavigator) this.navigator;
      ret = toCoordinateNavigator.getTargetCoordinate();
    }
    return ret;
  }

  private void adjustAltitude(ValueRequest altitudeRequest) {
    int airportAltitude = Context.getArea().getAirport().getAltitude();
    if (rdr.getState().is(AirplaneState.takeOffRoll, AirplaneState.landed, AirplaneState.holdingPoint)) {
      // not adjusting altitude at this states
      this.altitude.reset(airportAltitude);
    } else {
      this.altitude.add(altitudeRequest.value);
      this.lastVerticalSpeed = this.altitude.getInertia() * 60;
      if (this.altitude.getValue() < airportAltitude) {
        this.altitude.reset(airportAltitude);
      }
    }
  }

  private double adjustDescentRateByApproachStateIfRequired(double descentRateForAltitude) {
    double ret;
    if (rdr.getState().is(AirplaneState.approachDescend, AirplaneState.longFinal, AirplaneState.shortFinal)) {
      double restrictedDescentRate;
      switch (rdr.getState()) {
        case approachDescend:
          restrictedDescentRate = 2000;
          break;
        case longFinal:
          // maxNegativeVerticalSpeedMustBeSet
//          ApproachPilot ap = plane.getPilot
//          eng.jAtcSim.newLib.area.airplanes.behaviors.NewApproachBehavior nab = this.parent.getBehaviorModule().getAs(eng.jAtcSim.newLib.area.airplanes.behaviors.NewApproachBehavior.class);
//          restrictedDescentRate = nab.getApproachInfo().getType() == Approach.ApproachType.visual ?
//              2000 : 1300;
//          break;
          restrictedDescentRate = 1700;
          break;
        case shortFinal:
          restrictedDescentRate = 1300;
          break;
        default:
          throw new UnsupportedOperationException("This situation is not supported.");
      }
      restrictedDescentRate /= 60d;
      ret = Math.min(descentRateForAltitude, restrictedDescentRate);
    } else
      ret = descentRateForAltitude;
    return ret;
  }

  private void adjustHeading() {
    double diff = Headings.getDifference(heading.getValue(), targetHeading, true);

    LeftRight turn = targetHeadingTurn;
    if (diff < 5)
      turn = Navigator.getBetterDirectionToTurn(heading.getValue(), targetHeading);

    switch (turn) {
      case left:
        this.heading.add(-diff);
        break;
      case right:
        this.heading.add(diff);
        break;
      default:
        throw new EEnumValueUnsupportedException(turn);
    }
  }

  private void adjustSpeed(ValueRequest speedRequest) {
    this.speed.add(speedRequest.value);
  }

  private ValueRequest getAltitudeRequest() {
    ValueRequest ret;
    // if on ground, nothing required
    if (rdr.getState().isOnGround()) { // && altitude.getValue() == Acc.airport().getAltitude()) {
      ret = new ValueRequest();
      ret.energy = 0;
      ret.value = 0;
    } else {
      double climbRateForAltitude = rdr.getType().getClimbRateForAltitude(this.altitude.getValue());
      double descentRateForAltitude = rdr.getType().getDescendRateForAltitude(this.altitude.getValue());
      descentRateForAltitude = adjustDescentRateByApproachStateIfRequired(descentRateForAltitude);
      ret = getRequest(
              this.altitude.getValue(),
              this.targetAltitude.getTargetValue(),
              climbRateForAltitude,
              descentRateForAltitude);
    }

    return ret;
  }

  private ValueRequest getSpeedRequest() {
    ValueRequest ret;
    double delta = targetSpeed.getTargetValue() - speed.getValue();
    if (delta == 0) {
      // no change required
      ret = new ValueRequest();
      ret.energy = 0;
      ret.value = 0;
    } else {
      double incStep = rdr.getType().speedIncreaseRate;
      double decStep = rdr.getType().speedDecreaseRate;
      if (rdr.getState().isOnGround()) {
        incStep *= GROUND_SPEED_CHANGE_MULTIPLIER;
        decStep *= GROUND_SPEED_CHANGE_MULTIPLIER;
      }
      ret = getRequest(
              this.speed.getValue(),
              this.targetSpeed.getTargetValue(),
              incStep, decStep);
    }

    return ret;
  }

  @Override
  public void save(XElement elm, XContext ctx) {
    super.save(elm, ctx);
    ctx.saver.saveRemainingFields(this, elm);
  }

  @Override
  public void load(XElement elm, XContext ctx) {
    super.load(elm, ctx);
  }
}



