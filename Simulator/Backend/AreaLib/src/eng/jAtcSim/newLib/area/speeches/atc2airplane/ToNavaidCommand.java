package eng.jAtcSim.newLib.area.speeches.atc2airplane;


import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.speeches.IAtcCommand;

public abstract class ToNavaidCommand implements IAtcCommand {
  protected final Navaid navaid;

  protected ToNavaidCommand(Navaid navaid) {
    if (navaid == null) {
      throw new IllegalArgumentException("Argument \"navaid\" cannot be null.");
    }
    
    this.navaid = navaid;
  }

  public Navaid getNavaid() {
    return navaid;
  }
  
}
