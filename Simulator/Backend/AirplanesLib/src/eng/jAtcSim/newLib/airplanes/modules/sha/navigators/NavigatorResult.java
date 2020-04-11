package eng.jAtcSim.newLib.airplanes.modules.sha.navigators;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.shared.enums.LeftRight;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class NavigatorResult {

  private final int heading;
  private final LeftRight turn;

  public NavigatorResult(int heading, LeftRight turn) {
    this.heading = heading;
    this.turn = turn;
  }

  public int getHeading() {
    return heading;
  }

  public LeftRight getTurn() {
    return turn;
  }
}
