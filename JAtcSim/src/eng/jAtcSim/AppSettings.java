package eng.jAtcSim;


import java.nio.file.Path;
import java.nio.file.Paths;

public class AppSettings {

  public Path resourcesFolder;
  public Path soundFolder;
  public Path logFolder;
  public Path applicationFolder;

  public AppSettings(){
    this.applicationFolder = getApplicationFolder();
    this.resourcesFolder = Paths.get(this.applicationFolder.toString(), "_SettingFiles");
    this.soundFolder = Paths.get(this.applicationFolder.toString(), "_Sounds");
    this.logFolder = Paths.get(this.applicationFolder.toString(), "_Log");
  }

  public static Path getApplicationFolder(){
    String curDir = System.getProperty("user.dir") + "\\";
    Path ret = Paths.get(curDir);
    return ret;
  }
}