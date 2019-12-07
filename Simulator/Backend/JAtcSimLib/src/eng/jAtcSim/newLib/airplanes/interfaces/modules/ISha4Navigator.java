package eng.jAtcSim.newLib.airplanes.interfaces.modules;

import eng.eSystem.collections.*;

public interface ISha4Navigator {
  int getHeading();

  void setTargetHeading(int heading, boolean leftTurn);
  void setTargetHeading(int heading);
}
