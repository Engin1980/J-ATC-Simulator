package eng.jAtcSim.lib.airplanes.pilots.navigators;

import eng.jAtcSim.lib.airplanes.Airplane;

public class HeadingNavigator implements INavigator {
  private final int heading;
  private boolean isApplied;

  public HeadingNavigator(int heading) {
    this.heading = heading;
    this.isApplied = false;
  }

  @Override
  public void navigate(Airplane.Airplane4Navigator plane) {
    if (!this.isApplied) {
      plane.setTargetHeading(heading);
      this.isApplied = true;
    }
  }
}
