package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.Tuple;
import eng.eSystem.functionalInterfaces.Selector;
import eng.eSystem.swing.DialogResult;
import eng.jAtcSim.newLib.gameSim.ISimulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class AdjustSelectionPanelWrapper<T> {

  public interface ActionSelectionPanelWraperListener<T> {
    Tuple<Iterable<T>, Selector<T, String>> doInit(ISimulation simulation);

    Iterable<T> doRequest();

    void doResponse(Iterable<T> data);
  }

  private final JFrame frm;
  private final ActionSelectionPanelWraperListener<T> lis;
  private final AdjustSelectionPanel<T> pnl;

  public AdjustSelectionPanelWrapper(ActionSelectionPanelWraperListener<T> listener, ISimulation sim, JButton... buttons) {
    this.lis = listener;

    Tuple<Iterable<T>, Selector<T, String>> tmp = lis.doInit(sim);
    this.pnl = new AdjustSelectionPanel<>(tmp.getB());
    this.pnl.setItems(tmp.getA());

    this.frm = new JFrame();
    frm.getContentPane().add(pnl);
    frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frm.addWindowListener(new WindowAdapter() {
      @Override
      public void windowActivated(WindowEvent e) {
        pnl.resetDialogResult();
      }

      @Override
      public void windowDeactivated(WindowEvent e) {
        if (pnl.getDialogResult() == DialogResult.ok) {
          lis.doResponse(pnl.getCheckedItems());
        }
      }
    });
    frm.addWindowFocusListener(new WindowAdapter() {
      @Override
      public void windowLostFocus(WindowEvent e) {
        frm.setVisible(false);
      }
    });
    frm.setUndecorated(true);
    frm.pack();


    for (JButton button : buttons) {
      button.addMouseListener(new MouseAdapter() {
        boolean pressed;

        @Override
        public void mouseEntered(MouseEvent e) {
          pressed = true;
        }

        @Override
        public void mouseExited(MouseEvent e) {
          pressed = false;
        }

        @Override
        public void mousePressed(MouseEvent e) {
          button.getModel().setArmed(true);
          button.getModel().setPressed(true);
          pressed = true;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
          //if(isRightButtonPressed) {underlyingButton.getModel().setPressed(true));
          button.getModel().setArmed(false);
          button.getModel().setPressed(false);

          if (pressed) {
            if (SwingUtilities.isRightMouseButton(e)) {
              pnl.setCheckedItems(lis.doRequest());
              Point p = MouseInfo.getPointerInfo().getLocation();
              frm.setLocation(p);
              frm.setVisible(true);
            }
          }
          pressed = false;

        }
      });
    }
  }
}
