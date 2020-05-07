package eng.jAtcSim.abstractRadar.global.events;

public class KeyEventArg {
  private int keyCode;

  public KeyEventArg(int keyCode) {
    this.keyCode = keyCode;
  }

  public int getKeyCode() {
    return keyCode;
  }
}
