package model.engine;

import enums.PlayerActionEn;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter @Setter
public class PlayerAction {

  public UUID playerId;
  public PlayerActionEn action;
  public int heading;

  public void describe(){
    System.out.println("id : " + playerId + " action : " + action + " heading : " + heading);
  }

}
