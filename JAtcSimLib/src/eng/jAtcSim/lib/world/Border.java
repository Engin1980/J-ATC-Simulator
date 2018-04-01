/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.world;

import eng.eSystem.collections.IList;
import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.coordinates.Coordinate;

/**
 * @author Marek
 */
public class Border {
  public enum eType {
    country,
    tma,
    ctr,
    restricted,
    mrva,
    other
  }

  private String name;
  private eType type;
  private IList<BorderPoint> points;
  private boolean enclosed;
  @XmlOptional
  private int minAltitude = 0;
  @XmlOptional
  private int maxAltitude = 99000;
  @XmlOptional
  private Coordinate labelCoordinate;

  public String getName() {
    return name;
  }

  public eType getType() {
    return type;
  }

  public IList<BorderPoint> getPoints() {
    return points;
  }

  public boolean isEnclosed() {
    return enclosed;
  }

  public int getMinAltitude() {
    return minAltitude;
  }

  public int getMaxAltitude() {
    return maxAltitude;
  }

  public Coordinate getLabelCoordinate() {
    if (labelCoordinate == null) {
      generateLabelCoordinate();
    }
    return labelCoordinate;
  }

  private void generateLabelCoordinate() {
    IList<BorderExactPoint> tmp = points.where(q -> q instanceof BorderExactPoint).select(q -> (BorderExactPoint) q);
    double latMin = tmp.min(q -> q.getCoordinate().getLatitude().get());
    double latMax = tmp.max(q -> q.getCoordinate().getLatitude().get());
    double lngMin = tmp.min(q -> q.getCoordinate().getLongitude().get());
    double lngMax = tmp.max(q -> q.getCoordinate().getLongitude().get());

    double lat = (latMax + latMin) / 2;
    double lng = (lngMax + lngMin) / 2;

    this.labelCoordinate = new Coordinate(lat, lng);
  }
}
