package enums;

public enum PlayerActionEn {
  FORWARD(1),
  STOP(2),
  STARTAFTERBURNER(3),
  STOPAFTERBURNER(4),
  FIRETORPEDOES(5),
  FIRESUPERNOVA(6),
  DETONATESUPERNOVA(7),
  FIRETELEPORT(8),
  TELEPORT(9),
  ACTIVATESHIELD(10);

  public final Integer value;

  private PlayerActionEn(Integer value) {
    this.value = value;
  }
}
