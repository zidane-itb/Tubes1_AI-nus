package enums;

public enum ObjectTypeEn {
  PLAYER(1),
  FOOD(2),
  WORMHOLE(3),
  GAS_CLOUD(4),
  ASTEROID_FIELD(5);

  public final Integer value;

  ObjectTypeEn(Integer value) {
    this.value = value;
  }

  public static ObjectTypeEn valueOf(Integer value) {
    for (ObjectTypeEn objectType : ObjectTypeEn.values()) {
      if (objectType.value == value) return objectType;
    }

    throw new IllegalArgumentException("Value not found");
  }
}