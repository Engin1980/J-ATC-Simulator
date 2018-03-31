package eng.coordinatesViewer;

public class AlignPoint extends Point {
  public final double lat;
  public final double lng;

  public AlignPoint(double x, double y, double lat, double lng) {
    super(x,y);
    this.lat = lat;
    this.lng = lng;
  }
}
