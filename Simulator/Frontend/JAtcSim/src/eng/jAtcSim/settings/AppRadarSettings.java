package eng.jAtcSim.settings;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.abstractRadar.settings.RadarDisplaySettings;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;

import java.nio.file.Path;

public class AppRadarSettings {
  public static class DisplaySettings {
    public static DisplaySettings load(XElement elm) {
      DisplaySettings ret = new DisplaySettings();

      ret.tma = SmartXmlLoaderUtils.loadBoolean(elm, "tma");
      ret.country = SmartXmlLoaderUtils.loadBoolean(elm, "country");
      ret.mrva = SmartXmlLoaderUtils.loadBoolean(elm, "mrva");
      ret.mrvaLabel = SmartXmlLoaderUtils.loadBoolean(elm, "mrvaLabel");
      ret.ctr = SmartXmlLoaderUtils.loadBoolean(elm, "ctr");
      ret.vor = SmartXmlLoaderUtils.loadBoolean(elm, "vor");
      ret.ndb = SmartXmlLoaderUtils.loadBoolean(elm, "ndb");
      ret.sid = SmartXmlLoaderUtils.loadBoolean(elm, "sid");
      ret.star = SmartXmlLoaderUtils.loadBoolean(elm, "star");
      ret.fix = SmartXmlLoaderUtils.loadBoolean(elm, "fix");
      ret.routeFix = SmartXmlLoaderUtils.loadBoolean(elm, "routeFix");
      ret.minorFix = SmartXmlLoaderUtils.loadBoolean(elm, "minorFix");
      ret.rings = SmartXmlLoaderUtils.loadBoolean(elm, "rings");
      ret.history = SmartXmlLoaderUtils.loadBoolean(elm, "history");
      ret.minAltitude = SmartXmlLoaderUtils.loadInteger(elm, "minAltitude");
      ret.maxAltitude = SmartXmlLoaderUtils.loadInteger(elm, "maxAltitude");
      return ret;
    }
    public boolean airport = true;
    public boolean country = true;
    public boolean ctr = true;
    public boolean fix = true;
    public boolean history = true;
    public int maxAltitude = 99000;
    public int minAltitude = 0;
    public boolean minorFix = true;
    public boolean mrva = true;
    public boolean mrvaLabel = true;
    public boolean ndb = true;
    public boolean rings = true;
    public boolean routeFix = true;
    public boolean sid = true;
    public boolean star = true;
    public boolean tma = true;
    public boolean vor = true;

    public RadarDisplaySettings toRadarDisplaySettings() {
      RadarDisplaySettings ret = new RadarDisplaySettings();
      ret.setAirportVisible(this.airport);
      ret.setCountryBorderVisible(this.country);
      ret.setCtrBorderVisible(this.ctr);
      ret.setFixMinorVisible(this.minorFix);
      ret.setFixRouteVisible(this.routeFix);
      ret.setFixVisible(this.fix);
      ret.setMrvaBorderAltitudeVisible(this.mrvaLabel);
      ret.setMrvaBorderVisible(this.mrva);
      ret.setNdbVisible(this.ndb);
      ret.setRingsVisible(this.rings);
      ret.setSidVisible(this.sid);
      ret.setStarVisible(this.star);
      ret.setTmaBorderVisible(this.tma);
      ret.setVorVisible(this.vor);
      ret.setPlaneHistoryVisible(this.history);
      ret.setMinAltitude(this.minAltitude);
      ret.setMaxAltitude(this.maxAltitude);
      return ret;
    }
  }
  public DisplaySettings displaySettings;
  public int displayTextDelay;
  public Path styleSettingsFile;
}
