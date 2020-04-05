package eng.jAtcSim.newLib.airplanes.modules.sha;

import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.LocalInstanceProvider;
import eng.jAtcSim.newLib.shared.Restriction;

public class ShaModule {

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

  private int targetHeading;
  private HeadingInertialValue heading;
  private InertialValue altitude;
  private RestrictableItem targetAltitude;
  private double lastVerticalSpeed = 0;
  private InertialValue speed;
  private RestrictableItem targetSpeed;

  public void clearTargetAltitudeRestriction() {
    this.targetAltitude.clearRestriction();
  }

  public void clearTargetSpeedRestriction() {
    this.targetSpeed.clearRestriction();
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

  public int getSpeed() {
    return (int) Math.round(speed.value);
  }

  public Restriction getSpeedRestriction() {
    return this.targetSpeed.getRestriction();
  }

  public int getTAS() {
    double m = 1 + this.getAltitude() / 100000d;
    double ret = this.getSpeed() * m;
    return (int) Math.round(ret);
  }

  public int getTargetAltitude() {
    return targetAltitude.getTargetValue();
  }

  public int getTargetHeading() {
    return targetHeading;
  }

  public int getTargetSpeed() {
    return targetSpeed.getTargetValue();
  }

  public int getVerticalSpeed() {
    return (int) Math.round(this.lastVerticalSpeed);
  }

  public ShaModule(int heading, int altitude, int speed, AirplaneType planeType) {
    this.targetAltitude = new RestrictableItem(altitude);
    this.targetHeading = heading;
    this.targetSpeed = new RestrictableItem(speed);

    double headingChangeDenominator = getHeadingChangeDenominator(planeType);
    this.heading = new HeadingInertialValue(
        heading,
        planeType.headingChangeRate,
        planeType.headingChangeRate / headingChangeDenominator);

    this.altitude = new InertialValue(
        altitude,
        planeType.lowClimbRate / 7d / 60,
        planeType.highDescendRate / 7d / 60,
        (double) LocalInstanceProvider.getAirport().getAltitude());

    this.speed = new InertialValue(
        speed,
        planeType.speedIncreaseRate / 4d,
        planeType.speedDecreaseRate / 6d,
        0d);
  }

  public void setAltitudeRestriction(Restriction altitudeRestriction) {
    this.targetAltitude.setRestriction(altitudeRestriction);
  }

  public void setSpeedRestriction(Restriction speedRestriction) {
    this.targetSpeed.setRestriction(speedRestriction);
  }

  public void setTargetAltitude(int altitude) {
    this.targetAltitude.setTargetValue(altitude);
  }

  public void setTargetSpeed(int speed) {
    this.targetSpeed.setTargetValue(speed);
  }
}



