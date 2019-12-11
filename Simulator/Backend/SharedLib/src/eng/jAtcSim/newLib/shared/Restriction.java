package eng.jAtcSim.newLib.shared;

public class Restriction {
  
  public enum eDirection{
    atMost,
    atLeast,
    exactly
  }
  
  public final eDirection direction;
  public final int value;

  public Restriction(eDirection direction, int value) {
    this.direction = direction;
    this.value = value;
  }

  @Override
  public String toString() {
    return direction + " " + value;
  }
  
}
