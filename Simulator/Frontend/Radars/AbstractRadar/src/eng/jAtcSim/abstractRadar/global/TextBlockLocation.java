package eng.jAtcSim.abstractRadar.global;

public enum TextBlockLocation {
  topLeft,
  topRight,
  bottomLeft,
  bottomRight;

  public boolean isBottom(){
    return this == bottomLeft || this == bottomRight;
  }

  public boolean isRight(){
    return this == bottomRight || this == topRight;
  }
}
