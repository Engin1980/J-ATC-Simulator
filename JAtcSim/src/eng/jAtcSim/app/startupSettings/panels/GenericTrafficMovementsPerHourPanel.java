package eng.jAtcSim.app.startupSettings.panels;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.jAtcSim.Stylist;
import eng.jAtcSim.shared.LayoutManager;
import eng.jAtcSim.app.extenders.NumericUpDownExtender;
import eng.jAtcSim.app.extenders.SwingFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GenericTrafficMovementsPerHourPanel extends JPanel {

  private static final int HOURS = 24;
  private JLabel[] lbls = new JLabel[HOURS];
  private NumericUpDownExtender[] nums = new NumericUpDownExtender[HOURS];
  private boolean applied = false;

  public GenericTrafficMovementsPerHourPanel() {
    IList<JComponent> cmps = new EList<>();
    for (int i = 0; i < HOURS; i++) {
      lbls[i] = new JLabel(i + ":00-" + i + ":59");
      nums[i] = new NumericUpDownExtender(new JSpinner(), 0, 240, 30, 1);

    }

    for (int i = 0; i < HOURS / 2; i++) {
      cmps.add(lbls[i]);
      cmps.add(nums[i].getControl());
      cmps.add(lbls[i + 12]);
      cmps.add(nums[i + 12].getControl());
    }
    cmps.add((JComponent) null);
    cmps.add((JComponent) null);
    cmps.add(SwingFactory.createButton("Cancel", this::btnCancel_click));
    cmps.add(SwingFactory.createButton("Apply", this::btnApply_click));


    JPanel pnl = LayoutManager.createFormPanel(13, 4, cmps.toArray(JComponent.class));
    LayoutManager.fillBorderedPanel(this, 16, pnl);

    Stylist.apply(this, true);
  }

  public void setValues(int[] values) {
    assert values != null;
    assert values.length == 24;
    for (int i = 0; i < values.length; i++) {
      nums[i].setValue(values[i]);
    }
  }

  public int[] getValues(){
    int [] ret = null;
    if (this.applied){
      ret = new int[24];
      for (int i = 0; i < ret.length; i++) {
        ret[i] = nums[i].getValue();
      }
    }
    return ret;
  }

  public void setValues(int value) {
    for (NumericUpDownExtender num : nums) {
      num.setValue(value);
    }
  }

  private void btnApply_click(ActionEvent actionEvent) {
    this.applied = true;
    this.getRootPane().getParent().setVisible(false);
  }

  private void btnCancel_click(ActionEvent actionEvent) {
    this.applied = false;
    this.getRootPane().getParent().setVisible(false);
  }
}
