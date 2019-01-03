package eng.jAtcSim.lib.stats.write.specific;

public class PlanesStats {
  private PlanesSubStats planesInSim = new PlanesSubStats();
  private PlanesSubStats planesUnderApp = new PlanesSubStats();
  private PlanesSubStats finishedPlanes = new PlanesSubStats();
  private PlanesSubStats delay = new PlanesSubStats();

  public PlanesSubStats getPlanesInSim() {
    return planesInSim;
  }

  public PlanesSubStats getPlanesUnderApp() {
    return planesUnderApp;
  }

  public PlanesSubStats getFinishedPlanes() {
    return finishedPlanes;
  }

  public PlanesSubStats getDelay() {
    return delay;
  }
}