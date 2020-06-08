package eng.jAtcSim.newLib.area.global;

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

  public static class Heading{
    public static String to(int heading){
      return String.format("%03d", heading);
    }
  }

  public static class Distance{
    public static String to(double distance){
      return String.format("%.2f nm", distance);
    }
  }

  public static class Speed{
    public static String to(int speed){
      return String.format("%d kt", speed);
    }
  }

  public static class Frequency{
    public static String to(double frequency){
      return String.format("%5.2f", frequency);
    }
  }
}
