package eng.jAtcSim.lib.airplanes;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.lib.airplanes.pilots.navigators.HeadingNavigator;
import eng.jAtcSim.lib.airplanes.pilots.navigators.INavigator;
import eng.jAtcSim.lib.global.HeadingsNew;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;

public class SHA {

  private static class RestrictableItem {
    private int orderedValue;
    private Restriction restrictedValue;
    private int targetValue;

    public RestrictableItem(int targetValue) {
      setTargetValue(targetValue);
    }

    public final void setRestriction(Restriction restriction) {
      this.restrictedValue = restriction;
      this.refresh();
    }

    public final void clearRestriction() {
      this.restrictedValue = null;
      this.refresh();
    }

    public int getTargetValue() {
      return this.targetValue;
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

  //region Heading fields
  private int targetHeading;
  private HeadingInertialValue heading;
  private boolean targetHeadingLeftTurn;
  //endregion
  //region Altitude fields
  private InertialValue altitude;
  private RestrictableItem targetAltitude;
  private double lastVerticalSpeed;
  //endregion
  //region Speed fields
  private InertialValue speed;
  private RestrictableItem targetSpeed;
  //endregion
  private INavigator navigator;

  public SHA(int heading, int altitude, int speed, AirplaneType planeType, int airportAltitude) {
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
        (double) airportAltitude);

    this.speed = new InertialValue(
        speed,
        planeType.speedIncreaseRate / 4d,
        planeType.speedDecreaseRate / 6d,
        0d);

    this.navigator = new HeadingNavigator(heading);
  }

  //region Altitude-methods
  public void setTargetAltitudeRestriction(Restriction altitudeRestriction) {
    this.targetAltitude.setRestriction(altitudeRestriction);
  }

  public void clearTargetAltitudeRestriction() {
    this.targetAltitude.clearRestriction();
  }

  public int getTargetAltitude() {
    return targetAltitude.getTargetValue();
  }

  public void setTargetAltitude(int altitude) {
    this.targetAltitude.setTargetValue(altitude);
  }

  public double getAltitude() {
    return altitude.value;
  }

  //endregion
  //region Speed-methods

  public void setTargetSpeedRestriction(Restriction speedRestriction) {
    this.targetSpeed.setRestriction(speedRestriction);
  }

  public void clearTargetSpeedRestriction() {
    this.targetSpeed.clearRestriction();
  }

  public int getTargetSpeed() {
    return targetSpeed.getTargetValue();
  }


  public void setTargetSpeed(int speed) {
    this.targetSpeed.setTargetValue(speed);
  }

  public double getSpeed() {
    return speed.value;
  }

  //endregion
  //region Heading-methods

  public void setTargetHeading(int heading, boolean useLeftTurn) {
    this.targetHeading = heading;
    this.targetHeadingLeftTurn = useLeftTurn;
  }

  public int getTargetHeading() {
    return targetHeading;
  }


  public void setTargetHeading(int heading) {
    boolean useLeft
        = HeadingsNew.getBetterDirectionToTurn(this.heading.value, targetHeading) == ChangeHeadingCommand.eDirection.left;
    this.setTargetHeading(heading, useLeft);
  }

  public double getHeading() {
    return heading.value;
  }
  //endregion
}
