package eng.jAtcSim.settings;

import exml.IXPersistable;
import exml.annotations.XAttribute;

import java.nio.file.Path;

public class AppAutoSaveSettings implements IXPersistable {
  @XAttribute public int intervalInSeconds; //TODO let this are minutes in future
  @XAttribute public Path path;
}
