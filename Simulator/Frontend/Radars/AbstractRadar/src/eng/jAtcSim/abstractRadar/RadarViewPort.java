package eng.jAtcSim.abstractRadar;

import eng.eSystem.geo.Coordinate;
import exml.IXPersistable;
import exml.annotations.XConstructor;

public class RadarViewPort implements IXPersistable {
  private final Coordinate topLeft;
  private final double widthInNm;

  @XConstructor
  public RadarViewPort(Coordinate topLeft, double widthInNm) {
    this.topLeft = topLeft;
    this.widthInNm = widthInNm;
  }

  public Coordinate getTopLeft() {
    return topLeft;
  }

  public double getWidthInNm() {
    return widthInNm;
  }
}
