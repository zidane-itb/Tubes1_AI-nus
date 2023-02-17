package processor;

import enums.PlayerActionEn;
import lombok.Getter;
import lombok.Setter;
import model.engine.PlayerAction;

import java.util.*;

@Getter @Setter
public class BotProcessor {

    private static HashMap<Integer, Map<PlayerActionEn, Integer>> prioMap = new HashMap<>();

    private PlayerAction playerAction;
    private boolean resultExist;
    private final List<PlayerAction> playerActions;
    private int highestPrio;
    private int msgCount;

    public BotProcessor() {
        this.playerAction = new PlayerAction();
        this.playerActions = new ArrayList<>();
        this.resultExist = false;
    }

    public PlayerAction getPlayerAction() {
        // when this function executes, we're going to say that the computed player action expired
        resultExist = false;
        return playerAction;
    }

    public void computeAction() {
        if (playerActions.isEmpty()||highestPrio==-1)
            return;
        highestPrio = 0;
        if (playerActions.size() == 1) {
            this.playerAction = playerActions.get(0);
            resultExist = true;
            playerActions.clear();
            return;
        }
        Map<PlayerActionEn, Integer> prMap = prioMap.get(highestPrio);
        // just in case
        if (prMap==null) {
            this.playerAction=playerActions.get(0);
            resultExist = true;
            return;
        }
        int pr = 0;
        PlayerAction finalAction = null;
        for (PlayerAction action: playerActions) {
            if (prMap.getOrDefault(action.getAction(), 1)>pr) {
                finalAction = action;
            }
        }
        this.playerAction=finalAction;
        resultExist = true;
        playerActions.clear();
    }

    public synchronized void sendMessage(PlayerAction playerAction, int priority) {
        this.msgCount += 1;
        if (priority > highestPrio) {
            playerActions.clear();
            playerActions.add(playerAction);
            highestPrio = priority;
        } else if (priority == highestPrio) {
            playerActions.add(playerAction);
        }
        if (msgCount == 4) {
            computeAction();
            msgCount = 0;
        }
    }

    static {
        Map<PlayerActionEn, Integer> threeMap = new HashMap<>();
        threeMap.put(PlayerActionEn.FORWARD, 1);
        threeMap.put(PlayerActionEn.FIRETORPEDOES, 2);
        threeMap.put(PlayerActionEn.ACTIVATESHIELD, 3);
        Map<PlayerActionEn, Integer> fourMap = new HashMap<>();
        fourMap.put(PlayerActionEn.FIRESUPERNOVA, 1);
        fourMap.put(PlayerActionEn.FIRETELEPORT, 2);
        fourMap.put(PlayerActionEn.FORWARD, 3);
        Map<PlayerActionEn, Integer> fiveMap = new HashMap<>();
        fiveMap.put(PlayerActionEn.FORWARD, 1);
        fiveMap.put(PlayerActionEn.TELEPORT, 2);
        fiveMap.put(PlayerActionEn.DETONATESUPERNOVA, 3);

        prioMap.put(3, threeMap);
        prioMap.put(4, fourMap);
        prioMap.put(5, fiveMap);
    }

}
