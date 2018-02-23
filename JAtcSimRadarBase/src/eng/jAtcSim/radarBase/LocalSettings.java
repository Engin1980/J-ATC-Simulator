package eng.jAtcSim.radarBase;

public class LocalSettings {

  private boolean tmaBorderVisible = true;
  private boolean countryBorderVisible = true;
  private boolean ctrBorderVisible = true;
  private boolean vorVisible = true;
  private boolean ndbVisible = true;
  private boolean airportVisible = true;
  private boolean sidVisible = true;
  private boolean starVisible = true;

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
}
