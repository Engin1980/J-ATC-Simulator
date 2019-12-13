package eng.jAtcSim.newLib.airplanes.modules;

import com.sun.istack.internal.Nullable;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.AirplaneType;
import eng.jAtcSim.newLib.airplanes.behaviors.NewApproachBehavior;
import eng.jAtcSim.newLib.airplanes.interfaces.modules.ISha4Navigator;
import eng.jAtcSim.newLib.airplanes.interfaces.modules.IShaRO;
import eng.jAtcSim.newLib.airplanes.navigators.HeadingNavigator;
import eng.jAtcSim.newLib.airplanes.navigators.INavigator;
import eng.jAtcSim.newLib.airplanes.navigators.INavigator2Coordinate;
import eng.jAtcSim.newLib.global.Headings;
import eng.jAtcSim.newLib.global.HeadingsNew;
import eng.jAtcSim.newLib.global.Restriction;
import eng.jAtcSim.newLib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.newLib.world.approaches.Approach;

public class ShaModule implements IShaRO {

  private static class RestrictableItem {
    private int orderedValue;
    private Restriction restrictedValue;
    private int targetValue;

    RestrictableItem(int targetValue) {
      setTargetValue(targetValue);
    }

    public final void clearRestriction() {
      this.restrictedValue = null;
      this.refresh();
    }

    public int getTargetValue() {
      return this.targetValue;
    }

    public final void setRestriction(Restriction restriction) {
      this.restrictedValue = restriction;
      this.refresh();
    }

    public final void setTargetValue(int value) {
      this.orderedValue = value;
      this.refresh();
    }

    private void refresh() {
      if (restrictedValue == null)
        this.targetValue = this.orderedValue;
      else {
        switch (restrictedValue.direction) {
          case atLeast:
            this.targetValue = Math.max(this.orderedValue, this.restrictedValue.value);
            break;
          case atMost:
            this.targetValue = Math.min(this.orderedValue, this.restrictedValue.value);
            break;
          case exactly:
            this.targetValue = this.restrictedValue.value;
            break;
          default:
            throw new EEnumValueUnsupportedException(restrictedValue.direction);
        }
      }
    }
  }

  class Sha4Navigator implements ISha4Navigator {

    @Override
    public int getHeading() {
      return ShaModule.this.getHeading();
    }

    @Override
    public void setTargetHeading(int heading, boolean leftTurn) {
      ShaModule.this.finalHeading = heading;
      ShaModule.this.finalHeadingLeftTurn = leftTurn;
    }

    @Override
    public void setTargetHeading(int heading) {
      boolean leftTurn
          = HeadingsNew.getBetterDirectionToTurn(ShaModule.this.getHeading(), heading) == ChangeHeadingCommand.eDirection.left;
      this.setTargetHeading(heading, leftTurn);
    }
  }
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

  private final Airplane parent;
  private final Sha4Navigator sha4Navigator = new Sha4Navigator();
  //region Heading fields
  private int finalHeading;
  private HeadingInertialValue heading;
  private boolean finalHeadingLeftTurn;
  //endregion
  //region Altitude fields
  private InertialValue altitude;
  private RestrictableItem altitudeOrders;
  private double lastVerticalSpeed;
  //endregion
  //region Speed fields
  private InertialValue speed;
  private RestrictableItem speedOrders;
  //endregion
  private INavigator navigator;

  public ShaModule(Airplane parent) {
    assert parent != null;
    this.parent = parent;
  }

  public void clearTargetAltitudeRestriction() {
    this.altitudeOrders.clearRestriction();
  }

  public void clearTargetSpeedRestriction() {
    this.speedOrders.clearRestriction();
  }

  //region Elapse-Second
  public void elapseSecond() {
    // TODO here is && or || ???
    boolean isSpeedPreffered = parent.getState().is(Airplane.State.takeOffGoAround, Airplane.State.takeOffRoll);


    if (parent.getSha().getTargetAltitude() != parent.getSha().getAltitude()
        || parent.getSha().getTargetSpeed() != parent.getSha().getSpeed()) {
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

    navigator.navigate(this.sha4Navigator, parent.getCoordinate());
    if (finalHeading != heading.getValue()) {
      adjustHeading();
    } else {
      this.heading.resetInertia();
    }
  }

  public int getAltitude() {
    return (int) altitude.value;
  }

  @Override
  public int getGS() {
    return this.getTAS();
  }

  public int getHeading() {
    return (int) Math.round(heading.value);
  }

  public int getSpeed() {
    return (int) Math.round(speed.value);
  }

  public Restriction getSpeedRestriction() {
    return this.speedOrders.restrictedValue;
  }

  //endregion
  //region Speed-methods

  @Override
  public int getTAS() {
    double m = 1 + this.getAltitude() / 100000d;
    double ret = this.getSpeed() * m;
    return (int) Math.round(ret);
  }

  public int getTargetAltitude() {
    return altitudeOrders.getTargetValue();
  }

  public int getTargetHeading() {
    return finalHeading;
  }

  public int getTargetSpeed() {
    return speedOrders.getTargetValue();
  }

  public int getVerticalSpeed() {
    return (int) Math.round(this.lastVerticalSpeed);
  }

  //endregion

  //region Heading-methods

  public void init(int heading, int altitude, int speed, AirplaneType planeType, int airportAltitude) {
    this.altitudeOrders = new RestrictableItem(altitude);
    this.finalHeading = heading;
    this.speedOrders = new RestrictableItem(speed);

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

    this.navigator = new HeadingNavigator(heading);
  }

  //region Altitude-methods
  public void setAltitudeRestriction(Restriction altitudeRestriction) {
    this.altitudeOrders.setRestriction(altitudeRestriction);
  }

  //endregion

  public void setNavigator(INavigator navigator) {
    assert navigator != null;
    this.navigator = navigator;
  }

  //endregion

  public void setSpeedRestriction(Restriction speedRestriction) {
    this.speedOrders.setRestriction(speedRestriction);
  }

  public void setTargetAltitude(int altitude) {
    this.altitudeOrders.setTargetValue(altitude);
  }

  public void setTargetSpeed(int speed) {
    this.speedOrders.setTargetValue(speed);
  }

  public Coordinate tryGetTargetCoordinate() {
    if (navigator instanceof INavigator2Coordinate) {
      INavigator2Coordinate nc = (INavigator2Coordinate) navigator;
      return nc.getTargetCoordinate();
    } else
      return null;
  }

  private void adjustAltitude(ValueRequest altitudeRequest) {
    if (parent.getState().is(Airplane.State.takeOffRoll, Airplane.State.landed, Airplane.State.holdingPoint)) {
      // not adjusting altitude at this states
      this.altitude.reset(Acc.airport().getAltitude());
    } else {
      this.altitude.add(altitudeRequest.value);
      this.lastVerticalSpeed = this.altitude.getInertia() * 60;
      if (this.altitude.getValue() < Acc.airport().getAltitude()) {
        this.altitude.reset(Acc.airport().getAltitude());
      }
    }
  }

  private double adjustDescentRateByApproachStateIfRequired(double descentRateForAltitude) {
    double ret;
    if (parent.getState().is(Airplane.State.approachDescend, Airplane.State.longFinal, Airplane.State.shortFinal)) {
      double restrictedDescentRate;
      switch (parent.getState()) {
        case approachDescend:
          restrictedDescentRate = 2000;
          break;
        case longFinal:
          NewApproachBehavior nab = this.parent.getBehaviorModule().getAs(NewApproachBehavior.class);
          restrictedDescentRate = nab.getApproachInfo().getType() == Approach.ApproachType.visual ?
              2000 : 1300;
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
    double diff = Headings.getDifference(heading.getValue(), finalHeading, true);

    boolean isLeft = finalHeadingLeftTurn;
    if (diff < 5)
      isLeft = HeadingsNew.getBetterDirectionToTurn(heading.getValue(), finalHeading) == ChangeHeadingCommand.eDirection.left;

    if (isLeft)
      this.heading.add(-diff);
    else
      this.heading.add(diff);
  }

  private void adjustSpeed(ValueRequest speedRequest) {
    this.speed.add(speedRequest.value);
  }

  private ValueRequest getAltitudeRequest() {
    ValueRequest ret;
    // if on ground, nothing required
    if (parent.getState().isOnGround() && altitude.getValue() == Acc.airport().getAltitude()) {
      ret = new ValueRequest();
      ret.energy = 0;
      ret.value = 0;
    } else {
      double climbRateForAltitude = parent.getType().getClimbRateForAltitude(this.altitude.getValue());
      double descentRateForAltitude = parent.getType().getDescendRateForAltitude(this.altitude.getValue());
      descentRateForAltitude = adjustDescentRateByApproachStateIfRequired(descentRateForAltitude);
      ret = getRequest(
          this.altitude.getValue(),
          this.altitudeOrders.targetValue,
          climbRateForAltitude,
          descentRateForAltitude);
    }

    return ret;
  }

  private ValueRequest getSpeedRequest() {
    ValueRequest ret;
    double delta = speedOrders.targetValue - speed.getValue();
    if (delta == 0) {
      // no change required
      ret = new ValueRequest();
      ret.energy = 0;
      ret.value = 0;
    } else {
      double incStep = parent.getType().speedIncreaseRate;
      double decStep = parent.getType().speedDecreaseRate;
      if (parent.getState().isOnGround()) {
        incStep *= GROUND_SPEED_CHANGE_MULTIPLIER;
        decStep *= GROUND_SPEED_CHANGE_MULTIPLIER;
      }
      ret = getRequest(
          this.speed.getValue(),
          this.speedOrders.targetValue,
          incStep, decStep);
    }

    return ret;
  }
  //endregion
}

class InertialValue {
  private final double maxPositiveInertiaChange;
  private final double maxNegativeInertiaChange;
  protected double value;
  private double inertia;
  private Double minimum;

  @XmlConstructor
  private InertialValue() {
    maxPositiveInertiaChange = Double.MIN_VALUE;
    maxNegativeInertiaChange = Double.MIN_VALUE;
  }

  public InertialValue(double value,
                       double maxPositiveInertiaChange, double maxNegativeInertiaChange,
                       @Nullable Double minimum) {
    this.value = value;
    this.inertia = 0;
    this.minimum = minimum;
    this.maxPositiveInertiaChange = maxPositiveInertiaChange;
    this.maxNegativeInertiaChange = maxNegativeInertiaChange;
  }

  public void add(double val) {
    double adjustedValue;
    if (val > inertia)
      adjustedValue = Math.min(val, inertia + maxPositiveInertiaChange);
    else
      adjustedValue = Math.max(val, inertia - maxNegativeInertiaChange);

    this.inertia = adjustedValue;
    this.value += this.inertia;

    if ((this.minimum != null) && (this.value < this.minimum)) {
      this.value = this.minimum;
      this.inertia = 0;
    }
  }

  public double getInertia() {
    return inertia;
  }

  public double getMaxNegativeInertiaChange() {
    return maxNegativeInertiaChange;
  }

  public double getMaxPositiveInertiaChange() {
    return maxPositiveInertiaChange;
  }

  public double getValue() {
    return value;
  }

  public void reset(double value) {
    this.value = value;
    this.inertia = 0;
  }

  public void set(double value) {
    double diff = value - this.value;
    this.add(diff);
  }
}

class HeadingInertialValue {
  private final double maxInertia;
  private final double maxInertiaChange;
  protected double value;
  private IList<Double> thresholds = new EList<>();
  private int inertiaStep = 0;

  HeadingInertialValue(double value,
                       double maxInertia, double maxInertiaChange) {
    this.value = value;
    this.maxInertia = maxInertia;
    this.maxInertiaChange = maxInertiaChange;
    buildHashMap();
  }

  @XmlConstructor
  private HeadingInertialValue() {
    maxInertia = Double.MIN_VALUE;
    maxInertiaChange = Double.MIN_VALUE;
  }

  public void add(double val) {
    if (Math.abs(val) < maxInertiaChange) {
      this.value += val;
      this.inertiaStep = 0;
    } else {
      int stepBlock = getFromHashMap(val);
      if (stepBlock < inertiaStep)
        inertiaStep--;
      else if (stepBlock > inertiaStep)
        inertiaStep++;

      double step = inertiaStep * maxInertiaChange;
      step = Math.min(step, this.maxInertia);
      if (val > 0)
        step = Math.min(step, val);
      else
        step = Math.max(step, val);

      this.value += step;
    }

    this.value = Headings.to(this.value);
  }

  public double getInertia() {
    return inertiaStep * maxInertiaChange;
  }

  public double getMaxInertia() {
    return maxInertia;
  }

  public double getValue() {
    return value;
  }

  public void reset(double value) {
    this.value = value;
    this.inertiaStep = 0;
  }

  private void buildHashMap() {
    IList<Double> tmp = new EList<>();
    int index = 1;
    int cumIndex = 1;
    double maxThr = maxInertia / maxInertiaChange + 1;
    double thr = 0;
    while (thr <= maxThr) {
      thr = cumIndex * this.maxInertiaChange;
      tmp.add(thr);
      index++;
      cumIndex += index;
    }

    tmp.removeAt(0);

    this.thresholds = tmp;
  }

  private int getFromHashMap(double val) {
    boolean isNeg = false;
    if (val < 0) {
      isNeg = true;
      val = -val;
    }
    int ret = 0;
    while (ret < this.thresholds.size()) {
      if (val >= this.thresholds.get(ret))
        ret++;
      else
        break;
    }
    ret = ret + 1;
    if (isNeg)
      ret = -ret;
    return ret;
  }

  void resetInertia() {
    if (this.inertiaStep != 0)
      this.inertiaStep = 0;
  }
}

class ValueRequest {
  public double value;
  public double energy;

  public void multiply(double multiplier) {
    this.value *= multiplier;
    this.energy *= multiplier;
  }

  @Override
  public String toString() {
    return "ValueRequest{" +
        "value=" + value +
        ", energy=" + energy +
        '}';
  }
}
