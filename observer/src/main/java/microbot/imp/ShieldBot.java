package microbot.imp;

import enums.ObjectTypeEn;
import enums.PlayerActionEn;
import etc.StateHolder;
import lombok.RequiredArgsConstructor;
import microbot.ActionBot;
import microbot.ActionCalculator;
import model.engine.GameObject;
import model.engine.GameState;
import model.engine.PlayerAction;
import model.engine.Position;
import processor.BotProcessor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ShieldBot extends ActionCalculator implements ActionBot {
    private final BotProcessor botProcessor;
    private final StateHolder stateHolder;

    private boolean calculateTorpedoHit (List<GameObject> torpedoList, int botSize) {
        boolean hit = false;
        int hitCenterHeading;
        int hitCenterDistance;
        int distanceToCenter;
        int headingDiff;
        GameObject bot = stateHolder.getBot();
        GameObject currentTorpedo;

        int i = 0;
        while (i < torpedoList.size() && !hit) {
            currentTorpedo = torpedoList.get(i);
            hitCenterHeading = getHeadingBetween(currentTorpedo, bot);
            hitCenterDistance = (int) getDistanceBetween(currentTorpedo, bot);
            headingDiff = Math.abs(currentTorpedo.getCurrentHeading() - hitCenterHeading);
            if (headingDiff < 90) {
                distanceToCenter = (int) Math.tan(headingDiff) * hitCenterDistance;
                hit = (distanceToCenter < botSize);
            }
            i++;
        }
        return hit;
    }

    private boolean hasShield (int shieldCount) {
        return shieldCount > 0;
    }

    private boolean canUseTorpedo (int size, int shieldCount) {
        return size > 30 && hasShield(shieldCount);
    }

    public void run () {
        int priorityNum = 0;
        PlayerAction playerAction = new PlayerAction();
        GameState gameState = stateHolder.getGameState();
        playerAction.action = PlayerActionEn.ACTIVATESHIELD;
        int botSize = stateHolder.getBot().getSize();
        int shieldCount = stateHolder.getBot().getShieldCount();

        List<GameObject> torpedoList = gameState.getGameObjects().stream().filter(item -> item.getGameObjectType() == ObjectTypeEn.TORPEDO_SALVO).collect(Collectors.toList());
        if (!torpedoList.isEmpty()) {
            if (canUseTorpedo(botSize, shieldCount) && calculateTorpedoHit(torpedoList, botSize)) {
                botProcessor.sendMessage(playerAction, priorityNum);
            }
        }
    }
}