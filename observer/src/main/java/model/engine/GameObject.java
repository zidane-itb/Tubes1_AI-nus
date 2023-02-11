package model.engine;

import enums.ObjectTypeEn;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@AllArgsConstructor
@Getter @Setter
public class GameObject {

  private UUID id;
  private Integer size;
  private Integer speed;
  private Integer currentHeading;
  private Position position;
  private ObjectTypeEn gameObjectType;
  private String effectHashCode;
  private int torpedoCount;
  private int teleportCount;
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private int snAvailable;
  private int shieldCount;

  public boolean isSnAvailable() {
    return snAvailable == 1;
  }

  public static GameObject FromStateList(UUID id, List<Integer> stateList) {
    String hashCode = stateList.size() >= 7 ? String.valueOf(stateList.get(6)) : "0";
    int tCount = stateList.size() >= 8 ? stateList.get(7) : 0,
            snAvailable = stateList.size() >= 9 ? stateList.get(8) : 0,
            teCount = stateList.size() >= 10 ? stateList.get(9) : 0,
            sCount = stateList.size() >= 11 ? stateList.get(10) : 0;

    Position position = new Position(stateList.get(4), stateList.get(5));
    return new GameObject(id, stateList.get(0), stateList.get(1), stateList.get(2), position,
            ObjectTypeEn.valueOf(stateList.get(3)), hashCode, tCount, snAvailable, teCount, sCount);
  }
}
