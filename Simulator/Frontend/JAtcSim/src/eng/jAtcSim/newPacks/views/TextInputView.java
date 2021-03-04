package eng.jAtcSim.newPacks.views;

import eng.jAtcSim.app.extenders.CommandInputTextFieldExtender;
import eng.jAtcSim.frmPacks.shared.SwingRadarPanel;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newPacks.IView;
import eng.jAtcSim.settings.AppSettings;

import javax.swing.*;

public class TextInputView implements IView {
  private CommandInputTextFieldExtender commandInputTextFieldExtender;
  public class CommandInputWrapper {
    public void addCommandTextToLine(char keyChar) {
      TextInputView.this.commandInputTextFieldExtender.appendText(Character.toString(keyChar), false);
    }

    public void addCommandTextToLine(String text) {
      TextInputView.this.commandInputTextFieldExtender.appendText(text, true);
    }

    public void eraseCommand() {
      TextInputView.this.commandInputTextFieldExtender.erase();
    }

    public void sendCommand() {
      TextInputView.this.commandInputTextFieldExtender.send();
    }

    public void setFocus() {
      TextInputView.this.commandInputTextFieldExtender.focus();
    }
  }

  @Override
  public void init(JPanel panel, ISimulation simulation, AppSettings settings) {

  }
}
