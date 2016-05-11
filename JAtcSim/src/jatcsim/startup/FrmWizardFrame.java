/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim.startup;

import java.awt.Component;
import java.awt.Font;
import javax.swing.JFrame;

/**
 *
 * @author Marek Vajgl
 */
public abstract class FrmWizardFrame extends JFrame {

  protected StartupSettings settings;

  public void initSettings(StartupSettings settings) {
    this.settings = settings;
    fillBySettings();
  }

  protected abstract void fillBySettings();

  @Override
  public void setVisible(boolean value) {
    if (value) {
      setFontAll(this.getComponents());
      this.dialogResult = DialogResult.Cancel;
    }
    super.setVisible(value);
  }

  public enum DialogResult {

    Ok,
    Cancel
  }
  protected DialogResult dialogResult = DialogResult.Cancel;

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
