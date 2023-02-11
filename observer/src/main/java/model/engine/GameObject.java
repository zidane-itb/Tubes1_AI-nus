package model.engine;

import enums.ObjectTypeEn;

import java.util.*;

import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;


@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class GameObject {
  public UUID id;
  public Integer size;
  public Integer speed;
  public Integer currentHeading;
  public Position position;
  public ObjectTypeEn gameObjectType;

  /* Player-exclusive attributes */
  public int effectHashCode;
  public int torpedoSalvoCount;
  public int supernovaAvailable;
  public int teleporterCount;
  public int shieldCount;

  public GameObject(UUID id, Integer size, Integer speed, Integer currentHeading, Position position, ObjectTypeEn gameObjectType) {
    this.id = id;
    this.size = size;
    this.speed = speed;
    this.currentHeading = currentHeading;
    this.position = position;
    this.gameObjectType = gameObjectType;

    this.effectHashCode = 0;
    this.torpedoSalvoCount = 0;
    this.supernovaAvailable = 0;
    this.teleporterCount = 0;
    this.shieldCount = 0;
  }

  public GameObject(UUID id, Integer size, Integer speed, Integer currentHeading, Position position, ObjectTypeEn gameObjectType, Integer effectHashCode, Integer torpedoSalvoCount, Integer supernovaAvailable, int teleporterCount, int shieldCount){
    this.id = id;
    this.size = size;
    this.speed = speed;
    this.currentHeading = currentHeading;
    this.position = position;
    this.gameObjectType = gameObjectType;

    this.effectHashCode = effectHashCode;
    this.torpedoSalvoCount = torpedoSalvoCount;
    this.supernovaAvailable = supernovaAvailable;
    this.teleporterCount = teleporterCount;
    this.shieldCount = shieldCount;
  }

  // public UUID getId() {
  //   return id;
  // }

  // public void setId(UUID id) {
  //   this.id = id;
  // }

  // public int getSize() {
  //   return size;
  // }

  // public void setSize(int size) {
  //   this.size = size;
  // }

  // public int getSpeed() {
  //   return speed;
  // }

  // public void setSpeed(int speed) {
  //   this.speed = speed;
  // }

  // public Position getPosition() {
  //   return position;
  // }

  // public void setPosition(Position position) {
  //   this.position = position;
  // }

  // public ObjectTypeEn getGameObjectType() {
  //   return gameObjectType;
  // }

  // public void setGameObjectType(ObjectTypeEn gameObjectType) {
  //   this.gameObjectType = gameObjectType;
  // }

  // public static GameObject FromStateList(UUID id, List<Integer> stateList)
  // {
  //   Position position = new Position(stateList.get(4), stateList.get(5));
  //   return new GameObject(id, stateList.get(0), stateList.get(1), stateList.get(2), position, ObjectTypeEn.valueOf(stateList.get(3)));
  // }

  public static GameObject FromStateList(UUID id, List<Integer> stateList)
  {
    Position position = new Position(stateList.get(4), stateList.get(5));

    if(stateList.size() == 11)
      return new GameObject(id, stateList.get(0), stateList.get(1), stateList.get(2), position, ObjectTypeEn.valueOf(stateList.get(3)), stateList.get(6), stateList.get(7), stateList.get(8), stateList.get(9), stateList.get(10));
    else
      return new GameObject(id, stateList.get(0), stateList.get(1), stateList.get(2), position, ObjectTypeEn.valueOf(stateList.get(3)));
  }
}
