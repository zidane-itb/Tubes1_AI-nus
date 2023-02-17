package enums;

public enum PlayerActionEn {
  FORWARD(1), // Move
  STOP(2), // Move
  STARTAFTERBURNER(3), // Move
  STOPAFTERBURNER(4), // Move
  FIRETORPEDOES(5), // Shoot
  FIRESUPERNOVA(6), // Shoot
  DETONATESUPERNOVA(7), // Shoot
  FIRETELEPORT(8), // Teleport
  TELEPORT(9), // Teleport
  ACTIVATESHIELD(10); // Shield

  public final Integer value;

  private PlayerActionEn(Integer value) {
    this.value = value;
  }
}
