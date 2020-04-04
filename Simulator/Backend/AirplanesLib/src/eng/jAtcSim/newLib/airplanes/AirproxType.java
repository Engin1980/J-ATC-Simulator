package eng.jAtcSim.newLib.airplanes;

public enum AirproxType {
  /** No air proximity */
  none,
  /** Air proximity of APP and non-APP airplane */
  partial,
  /** Air proximity warning */
  warning,
  /** Air proximity between APP and APP airplane */
  full;

  public static AirproxType combine(AirproxType a, AirproxType b){
    AirproxType ret;
    if (a == full || b == full)
      ret = full;
    else if (a == partial || b == partial)
      ret = partial;
    else if (a == warning || b == warning)
      ret = warning;
    else
      ret = none;
    return ret;
  }
}
