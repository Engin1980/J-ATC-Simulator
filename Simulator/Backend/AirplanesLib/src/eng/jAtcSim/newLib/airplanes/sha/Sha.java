package eng.jAtcSim.newLib.airplanes.sha;

import com.sun.istack.internal.Nullable;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.sha.drivers.HeadingDriver;
import eng.jAtcSim.newLib.airplanes.sha.drivers.IShaDriver;
import eng.jAtcSim.newLib.shared.Restriction;

public class Sha {
  public static Sha create(int heading, int altitude, int speed, AirplaneType planeType, int airportAltitude) {
    Sha ret = new Sha();
    ret.altitude.altitudeOrders = new RestrictableItem(altitude);
    ret.heading.finalHeading = heading;
    ret.speed.speedOrders = new RestrictableItem(speed);

    double headingChangeDenominator = getHeadingChangeDenominator(planeType);
    ret.heading.heading = new HeadingInertialValue(
        heading,
        planeType.headingChangeRate,
        planeType.headingChangeRate / headingChangeDenominator);

    ret.altitude.altitude = new InertialValue(
        altitude,
        planeType.lowClimbRate / 7d / 60,
        planeType.highDescendRate / 7d / 60,
        (double) airportAltitude);

    ret.speed.speed = new InertialValue(
        speed,
        planeType.speedIncreaseRate / 4d,
        planeType.speedDecreaseRate / 6d,
        0d);

    ret.driver = new HeadingDriver(heading);

    return ret;
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
  private final ShaSpeed speed = new ShaSpeed();
  private final ShaAltitude altitude = new ShaAltitude();
  private final ShaHeading heading = new ShaHeading();
  private IShaDriver driver = null;

  private Sha() {
  }
}






