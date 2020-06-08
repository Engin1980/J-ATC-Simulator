package eng.jAtcSim.newLib.area.airplanes.interfaces.modules;

public interface ISha4Navigator {
  int getHeading();

  void setTargetHeading(int heading, boolean leftTurn);
  void setTargetHeading(int heading);
}
