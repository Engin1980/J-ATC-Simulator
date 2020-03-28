package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.*;

class LineUtils{
  /* Author is Sun/Oracle. Taken from import java.awt.geom.Line2D. */
  public static boolean linesIntersect(double x1, double y1,
                                       double x2, double y2,
                                       double x3, double y3,
                                       double x4, double y4)
  {
    return ((relativeCCW(x1, y1, x2, y2, x3, y3) *
        relativeCCW(x1, y1, x2, y2, x4, y4) <= 0)
        && (relativeCCW(x3, y3, x4, y4, x1, y1) *
        relativeCCW(x3, y3, x4, y4, x2, y2) <= 0));
  }

  private static int relativeCCW(double x1, double y1,
                                 double x2, double y2,
                                 double px, double py)
  {
    x2 -= x1;
    y2 -= y1;
    px -= x1;
    py -= y1;
    double ccw = px * y2 - py * x2;
    if (ccw == 0.0) {
      ccw = px * x2 + py * y2;
      if (ccw > 0.0) {
        px -= x2;
        py -= y2;
        ccw = px * x2 + py * y2;
        if (ccw < 0.0) {
          ccw = 0.0;
        }
      }
    }
    return (ccw < 0.0) ? -1 : ((ccw > 0.0) ? 1 : 0);
  }
}