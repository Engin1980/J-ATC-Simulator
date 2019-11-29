/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.radarBase.global;

/**
 * @author Marek
 */
public class Point {
  public final int x;
  public final int y;

  public static Point sum(Point a, Point b) {
    Point ret = new Point(a.x + b.x, a.y + b.y);
    return ret;
  }

  public static double getDistance(Point a, Point b) {
    double ret;
    if (a.x == b.x && a.y == b.y)
      ret = 0;
    else
      ret = Math.sqrt(Math.pow(a.x - b.x,2) + Math.pow(a.y - b.y,2));
    return ret;
  }

  public static int getManhattanDistance(Point a, Point b) {
    int ret = Math.max(Math.abs(a.x-b.x), Math.abs(a.y-b.y));
    return ret;
  }

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Point clone(){
    return new Point(this.x, this.y);
  }

  @Override
  public String toString() {
    return "{" + x + ", " + y + '}';
  }
}
