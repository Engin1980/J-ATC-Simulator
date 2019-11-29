//package eng.jAtcSim.lib.eng.jAtcSim.lib.world.xmlModel.approachesOld.approachStages;
//
//import eng.eSystem.geo.Coordinate;
//import eng.eSystem.xmlSerialization.annotations.XmlOptional;
//
//public class XmlCheckPlaneLocationStage extends XmlStage {
//  @XmlOptional
//  public String fix;
//  @XmlOptional
//  public Coordinate coordinate;
//  public int minHeading;
//  public int maxHeading;
//  public double minDistance;
//  public int maxDistance;
//
//  public XmlCheckPlaneLocationStage() {
//  }
//
//  public XmlCheckPlaneLocationStage(String fix, int minHeading, int maxHeading, double minDistance, int maxDistance) {
//    this.fix = fix;
//    this.coordinate = null;
//    this.minHeading = minHeading;
//    this.maxHeading = maxHeading;
//    this.minDistance = minDistance;
//    this.maxDistance = maxDistance;
//  }
//
//  public XmlCheckPlaneLocationStage(Coordinate coordinate, int minHeading, int maxHeading, double minDistance, int maxDistance) {
//    this.fix = null;
//    this.coordinate = coordinate;
//    this.minHeading = minHeading;
//    this.maxHeading = maxHeading;
//    this.minDistance = minDistance;
//    this.maxDistance = maxDistance;
//  }
//}
