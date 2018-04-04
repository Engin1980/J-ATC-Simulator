package eng.jAtcSim.lib.global;

public class DataFormat {
  public static class Altitude{

    /**
     * Converts altitude to "# ft" or "FL###" according to the altitude and transition level.
     * @param altitude
     * @param transitionLevel
     * @return
     */
    public static String toStandardAltitudeOrFL(double altitude, int transitionLevel){
      if (altitude < transitionLevel)
        return String.format("%.0f ft", altitude);
      else
        return String.format("FL%03d", toFL(altitude));
    }

    private static int toFL(double altitude){
      return (int) (altitude / 100);
    }
  }
}
