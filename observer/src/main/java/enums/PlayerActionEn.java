package enums;

public enum PlayerActionEn {
  FORWARD(1),
  STOP(2),
  START_AFTERBURNER(3),
  STOP_AFTERBURNER(4);

  public final Integer value;

  private PlayerActionEn(Integer value) {
    this.value = value;
  }
}
