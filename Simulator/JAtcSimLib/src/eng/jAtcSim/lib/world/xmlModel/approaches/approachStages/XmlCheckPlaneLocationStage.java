package eng.jAtcSim.lib.world.xmlModel.approaches.approachStages;

public class XmlCheckPlaneLocationStage extends XmlStage {
  public String fix;
  public int minHeading;
  public int maxHeading;
  public double minDistance;
  public int maxDistance;

  public XmlCheckPlaneLocationStage() {
  }

  public XmlCheckPlaneLocationStage(String fix, int minHeading, int maxHeading, double minDistance, int maxDistance) {
    this.fix = fix;
    this.minHeading = minHeading;
    this.maxHeading = maxHeading;
    this.minDistance = minDistance;
    this.maxDistance = maxDistance;
  }
}
