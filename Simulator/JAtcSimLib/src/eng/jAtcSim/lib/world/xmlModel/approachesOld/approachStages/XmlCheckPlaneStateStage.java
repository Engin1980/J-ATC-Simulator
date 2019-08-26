//package eng.jAtcSim.lib.world.xmlModel.approachesOld.approachStages;
//
//import eng.eSystem.xmlSerialization.annotations.XmlOptional;
//
//public class XmlCheckPlaneStateStage extends XmlStage {
//  public static XmlCheckPlaneStateStage createAltitude(int minAltitude, int maxAltitude) {
//    XmlCheckPlaneStateStage ret = new XmlCheckPlaneStateStage(minAltitude, maxAltitude, null, null, null, null);
//    return ret;
//  }
//
//  public static XmlCheckPlaneStateStage createHeading(int minHeading, int maxHeading) {
//    XmlCheckPlaneStateStage ret = new XmlCheckPlaneStateStage(null, null, minHeading, maxHeading, null, null);
//    return ret;
//  }
//
//  @XmlOptional
//  public Integer minAltitude;
//  @XmlOptional
//  public Integer maxAltitude;
//  @XmlOptional
//  public Integer minHeading;
//  @XmlOptional
//  public Integer maxHeading;
//  @XmlOptional
//  public Integer minSpeed;
//  @XmlOptional
//  public Integer maxSpeed;
//
//  public XmlCheckPlaneStateStage(Integer minAltitude, Integer maxAltitude, Integer minHeading, Integer maxHeading, Integer minSpeed, Integer maxSpeed) {
//    this.minAltitude = minAltitude;
//    this.maxAltitude = maxAltitude;
//    this.minHeading = minHeading;
//    this.maxHeading = maxHeading;
//    this.minSpeed = minSpeed;
//    this.maxSpeed = maxSpeed;
//  }
//
//  public XmlCheckPlaneStateStage() {
//  }
//}
