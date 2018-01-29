/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim.startup;

import java.awt.*;
import javax.swing.*;

/**
 *
 * @author Marek Vajgl
 */
public abstract class FrmWizardFrame extends JFrame {

  public JPanel wrapWithContinueButton(JPanel pnl, JButton btnContinue) {

    Dimension d = new Dimension(200, btnContinue.getHeight());
    btnContinue.setMinimumSize(d);
    btnContinue.setMaximumSize(d);

    JPanel ret = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.center, 0,
        pnl, LayoutManager.createBorderedPanel(10, btnContinue));
    return ret;
  }

  public enum DialogResult {

    Ok,
    Cancel
  }

  protected NewStartupSettings settings;
  private DialogResult dialogResult = DialogResult.Cancel;

  protected final Dimension BUTTON_DIMENSION = new Dimension(150, 1);
  protected final Dimension FILE_FIELD_DIMENSION = new Dimension(500, 1);
  protected final Dimension LARGE_FRAME_FIELD_DIMENSION = new Dimension(900, 1);
  protected final int distance = 10;

  public void initSettings(NewStartupSettings settings) {
    this.settings = settings;
    fillBySettings();
  }

  protected abstract void fillBySettings();

  protected abstract boolean isValidated();
  
  @Override
  public void setVisible(boolean value) {
    if (value) {
      setFontAll(this.getComponents());
      this.dialogResult = DialogResult.Cancel;
      this.setLocationRelativeTo(null);  // *** this will center your app ***
    }
    super.setVisible(value);
  }

  public DialogResult getDialogResult() {
    return dialogResult;
  }

  private void setDialogResult(DialogResult dialogResult) {
    this.dialogResult = dialogResult;
  }

  public void showDialog() {
    this.setVisible(true);

    while (this.isVisible()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException ex) {
      }
    }
  }
  
  protected void closeDialogIfValid(){
    if (isValidated()){
      this.dialogResult = DialogResult.Ok;
      this.setVisible(false);
    }
      
  }

  private static final Font f = new Font("Verdana", 0, 12);

  protected void setFontAll(Component[] components) {
    for (Component c : components) {
      c.setFont(f);
      if (c instanceof java.awt.Container) {
        setFontAll(((java.awt.Container) c).getComponents());
      }
    }
  }

}
