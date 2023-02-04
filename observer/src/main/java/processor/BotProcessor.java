package processor;

import enums.PlayerActionEn;
import lombok.Getter;
import lombok.Setter;
import model.engine.PlayerAction;

import java.util.*;

@Getter @Setter
public class BotProcessor {

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
        highestPrio = 0;

        if (playerActions.isEmpty())
            return;

        if (playerActions.size() == 1) {
            this.playerAction = playerActions.get(0);
            resultExist = true;
            playerActions.clear();
            return;
        }

        playerAction.action = PlayerActionEn.FORWARD;
        playerAction.heading = new Random().nextInt(360);

//        if (!gameState.getGameObjects().isEmpty()) {
//            var foodList = gameState.getGameObjects()
//                    .stream().filter(item -> item.getGameObjectType() == ObjectTypeEn.FOOD)
//                    .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item)))
//                    .collect(Collectors.toList());
//
//            playerAction.heading = getHeadingBetween(foodList.get(0));
//        }

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

        if (msgCount == 2) {
            computeAction();
            msgCount = 0;
        }
    }




}
