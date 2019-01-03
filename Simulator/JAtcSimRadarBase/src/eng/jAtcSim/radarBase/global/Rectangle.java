package eng.jAtcSim.radarBase.global;

public class Rectangle {
  public final Point a;
  public final Point d;

  public Rectangle(Point topLeft, Point bottomRight) {
    this.a = topLeft;
    this.d = bottomRight;
  }

  public boolean isInside(Point p){
    boolean ret =
        a.x < p.x && d.x > p.x && a.y < p.y && d.y > p.y;
    return ret;
  }

  public boolean hasUnion(Rectangle p) {
    Rectangle o = this;
    boolean ret;
    // left, above, right, below
    ret = o.a.x > p.d.x || o.a.y > p.d.y || o.d.x < p.a.x || o.d.y < p.a.y;
    ret = !ret;
    return ret;
  }

  @Override
  public String toString() {
    return "Rectangle{" +
        "a=" + a +
        ", d=" + d +
        '}';
  }
}
