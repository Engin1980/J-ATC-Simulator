package eng.jAtcSim.lib.world.approaches;

public class GnssApproach extends Approach {

  private int daA;
  private int daB;
  private int daC;
  private int daD;

  @Override
  protected void _bind() {
  }

  public int getDA(char category){
    switch (category){
      case 'A':
        return daA;
      case 'B':
        return daB;
      case 'C':
        return daC;
      case 'D':
        return daD;
        default:
          throw new UnsupportedOperationException();
    }
  }
}
