package eng.jAtcSim.abstractRadar;

import eng.eSystem.geo.Coordinate;

public class RadarViewPort {
  private Coordinate topLeft;
  private double widthInNm;

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
