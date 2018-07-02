package eng.coordinatesViewer;

public class Point {
  public final double x;
  public final double y;

  private Point() {
    x = -1;
    y = -1;
  }

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public String toString() {
    return "Point{" +
        "x=" + x +
        ", y=" + y +
        '}';
  }
}
