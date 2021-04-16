package eng.jAtcSim.newPacks;

public interface IViewWithCustomData extends IView {
  Object getCustomDataToSave();

  void setCustomDataOnLoad(Object data);
}
