package eng.jAtcSim.startup.extenders;

import javax.swing.*;

public class SwingFactory {

  public static JScrollBar createHorizontalBar(int minimum, int maximum, int value){
    JScrollBar ret = new JScrollBar(JScrollBar.HORIZONTAL);
    ret.setMinimum(minimum);
    ret.setMaximum(maximum);
    ret.setValue(value);
    return ret;
  }

}
