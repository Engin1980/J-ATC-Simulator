package eng.jAtcSim.radarBase;

import eng.eSystem.geo.Coordinate;

public class RadarViewPort {
  private Coordinate topLeft;
  private double widthInNm;

  public RadarViewPort(Coordinate topLeft, double widthInNm) {
    this.topLeft = topLeft;
    this.widthInNm = widthInNm;
  }

  private RadarViewPort() {
  }

  public Coordinate getTopLeft() {
    return topLeft;
  }

  public double getWidthInNm() {
    return widthInNm;
  }
}
