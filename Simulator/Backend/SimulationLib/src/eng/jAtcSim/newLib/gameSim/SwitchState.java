package eng.jAtcSim.newLib.gameSim;

public enum SwitchState {
  none,
  asked,
  confirmed;

  public boolean isUnderSwitch() {
    return this == asked || this == confirmed;
  }
}
