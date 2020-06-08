package eng.coordinatesViewer;

import eng.eSystem.events.EventAnonymousSimple;

import eng.eXmlSerialization.annotations.XmlIgnored;
import javafx.scene.image.Image;

public class Project {
  private static final double ZOOM_STEP = 0.9;
  @XmlIgnored
  private eng.eSystem.events.EventAnonymousSimple redrawRequiredEvent = new EventAnonymousSimple();
  private String fileName;
  private double ratio;
  @XmlIgnored
  private Image imgRes;
  private double baseWidth;
  private double baseHeight;
  private double latA;
  private double latB;
  private double lngA;
  private double lngB;
  private PointList borderPoints = new PointList();
  private PointList navaidPoints = new PointList();

  public PointList getBorderPoints() {
    return borderPoints;
  }

  public PointList getNavaidPoints() {
    return navaidPoints;
  }

  private Project(){}

  public EventAnonymousSimple getRedrawRequiredEvent() {
    return redrawRequiredEvent;
  }

  public static Project create(String fileName){
    Project ret = new Project();
    ret.fileName = fileName;
    Image tmp = new Image(fileName);

    if (tmp.getException() != null) {
      throw new RuntimeException("Failed to load image. ", tmp.getException());
    } else {
      ret.baseHeight = tmp.getHeight();
      ret.baseWidth = tmp.getWidth();
      ret.ratio = 1;
      ret.updateRatio();
      ret.redrawRequiredEvent.raise();
    }

    return ret;
  }

  public void zoomOut() {
    ratio *= ZOOM_STEP;
    updateRatio();
    redrawRequiredEvent.raise();
  }

  public double getZoomRatio(){
    return this.ratio;
  }

  public void zoomIn() {
    ratio /= ZOOM_STEP;
    updateRatio();
    redrawRequiredEvent.raise();
  }

  public void zoomReset() {
    this.ratio = 1;
    updateRatio();
    redrawRequiredEvent.raise();
  }

  public void reinit() {
    updateRatio();
  }

  private void updateRatio() {
    double w = baseWidth * ratio;
    double h = baseHeight * ratio;
    imgRes = new Image(fileName, w, h, true, false);
  }

  public Image getImage(){
    return imgRes;
  }

  public void alignGps(AlignPoint u, AlignPoint v){
    this.latA = (u.lat - v.lat) / (u.y - v.y);
    this.latB = u.lat - latA * u.y;

    this.lngA = (u.lng - v.lng) / (u.x - v.x);
    this.lngB = u.lng - lngA * u.x;
  }

  public Point convertPointToCoordinate(Point absolutePoint){
    Point ret = new Point( latA * absolutePoint.y + latB, lngA * absolutePoint.x + lngB);
    return ret;
  }

  public Point convertRelativeToAbsolutePoint(Point point){
    Point ret = new Point(point.x / ratio, point.y / ratio);
    return ret;
  }

  public Point convertAbsoluteToRelativepoint(Point point){
    Point ret = new Point(point.x * ratio, point.y * ratio);
    return ret;
  }

}
