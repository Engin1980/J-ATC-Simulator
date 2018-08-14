package eng.jAtcSim.radarBase;

public class RadarDisplaySettings {

  private boolean tmaBorderVisible = true;
  private boolean countryBorderVisible = true;
  private boolean mrvaBorderVisible = true;
  private boolean mrvaBorderAltitudeVisible = true;
  private boolean restrictedBorderVisible = true;
  private boolean restrictedBorderAltitudeVisible = true;
  private boolean ctrBorderVisible = true;
  private boolean vorVisible = true;
  private boolean ndbVisible = true;
  private boolean airportVisible = true;
  private boolean sidVisible = true;
  private boolean starVisible = true;
  private boolean fixVisible = true;
  private boolean fixRouteVisible = true;
  private boolean fixMinorVisible = false;
  private boolean ringsVisible = true;
  private boolean planeHistoryVisible = true;
  private boolean planeHeadingLineVisible = true;
  private int minAltitude = 0;
  private int maxAltitude = 99_000;

  public boolean isPlaneHistoryVisible() {
    return planeHistoryVisible;
  }

  public boolean isPlaneHeadingLineVisible() {
    return planeHeadingLineVisible;
  }

  public void setPlaneHeadingLineVisible(boolean planeHeadingLineVisible) {
    this.planeHeadingLineVisible = planeHeadingLineVisible;
  }

  public boolean isMrvaBorderVisible() {
    return mrvaBorderVisible;
  }

  public void setMrvaBorderVisible(boolean mrvaBorderVisible) {
    this.mrvaBorderVisible = mrvaBorderVisible;
  }

  public boolean isMrvaBorderAltitudeVisible() {
    return mrvaBorderAltitudeVisible;
  }

  public void setMrvaBorderAltitudeVisible(boolean mrvaBorderAltitudeVisible) {
    this.mrvaBorderAltitudeVisible = mrvaBorderAltitudeVisible;
  }

  public boolean isFixVisible() {
    return fixVisible;
  }

  public void setFixVisible(boolean fixVisible) {
    this.fixVisible = fixVisible;
  }

  public boolean isFixRouteVisible() {
    return fixRouteVisible;
  }

  public void setFixRouteVisible(boolean fixRouteVisible) {
    this.fixRouteVisible = fixRouteVisible;
  }

  public boolean isFixMinorVisible() {
    return fixMinorVisible;
  }

  public void setFixMinorVisible(boolean fixMinorVisible) {
    this.fixMinorVisible = fixMinorVisible;
  }

  public boolean isSidVisible() {
    return sidVisible;
  }

  public void setSidVisible(boolean sidVisible) {
    this.sidVisible = sidVisible;
  }

  public boolean isStarVisible() {
    return starVisible;
  }

  public void setStarVisible(boolean starVisible) {
    this.starVisible = starVisible;
  }

  public boolean isCountryBorderVisible() {
    return countryBorderVisible;
  }

  public void setCountryBorderVisible(boolean countryBorderVisible) {
    this.countryBorderVisible = countryBorderVisible;
  }

  public boolean isCtrBorderVisible() {
    return ctrBorderVisible;
  }

  public void setCtrBorderVisible(boolean ctrBorderVisible) {
    this.ctrBorderVisible = ctrBorderVisible;
  }

  public boolean isTmaBorderVisible() {
    return tmaBorderVisible;
  }

  public void setTmaBorderVisible(boolean tmaBorderVisible) {
    this.tmaBorderVisible = tmaBorderVisible;
  }

  public boolean isVorVisible() {
    return vorVisible;
  }

  public void setVorVisible(boolean vorVisible) {
    this.vorVisible = vorVisible;
  }

  public boolean isNdbVisible() {
    return ndbVisible;
  }

  public void setNdbVisible(boolean ndbVisible) {
    this.ndbVisible = ndbVisible;
  }

  public boolean isAirportVisible() {
    return airportVisible;
  }

  public void setAirportVisible(boolean airportVisible) {
    this.airportVisible = airportVisible;
  }

  public boolean isRingsVisible() {
    return ringsVisible;
  }

  public void setRingsVisible(boolean ringsVisible) {
    this.ringsVisible = ringsVisible;
  }

  public void setPlaneHistoryVisible(boolean planeHistoryVisible) {
    this.planeHistoryVisible = planeHistoryVisible;
  }

  public boolean getPlaneHistoryVisible() {
    return planeHistoryVisible;
  }

  public void setMinAltitude(int minAltitude) {
    this.minAltitude = minAltitude;
  }

  public int getMinAltitude() {
    return minAltitude;
  }

  public void setMaxAltitude(int maxAltitude) {
    this.maxAltitude = maxAltitude;
  }

  public int getMaxAltitude() {
    return maxAltitude;
  }

  public boolean isRestrictedBorderVisible() {
    return this.restrictedBorderVisible;
  }

  public void setRestrictedBorderVisible(boolean restrictedBorderVisible) {
    this.restrictedBorderVisible = restrictedBorderVisible;
  }

  public boolean isRestrictedBorderAltitudeVisible() {
    return restrictedBorderAltitudeVisible;
  }

  public void setRestrictedBorderAltitudeVisible(boolean restrictedBorderAltitudeVisible) {
    this.restrictedBorderAltitudeVisible = restrictedBorderAltitudeVisible;
  }
}
