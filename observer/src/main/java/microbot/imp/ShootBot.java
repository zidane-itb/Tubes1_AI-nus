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
import processor.BotProcessor;

import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
public class ShootBot extends ActionCalculator implements ActionBot {

    private final BotProcessor botProcessor;
    private final StateHolder stateHolder;
    private final PlayerAction playerAction;

    private boolean snFired;
    private UUID largestPlayerId;

    public void run() {
        GameState gameState = stateHolder.getGameState();

        if (snFired && gameState.getGameObjects() != null && !gameState.getGameObjects().isEmpty()) {
            GameObject sn = null;
            for (GameObject object: gameState.getGameObjects()) {
                if (object.getGameObjectType() == ObjectTypeEn.SUPERNOVA_BOMB) {
                    sn = object;
                    break;
                }
            }
            //|| gameState.getWorld().
            if (sn != null && (isInRadius(sn, stateHolder.getPlayerMap().get(largestPlayerId), 20)
                    )) {
                sendMessage(playerAction, 999);
                snFired = false;
            }
        }

        if (stateHolder.getBot().isSnAvailable()) {
            supernova(gameState.getPlayerGameObjects());
        }
        if (stateHolder.getBot() != null && stateHolder.getBot().getSize() < 15) {
            signalDone(botProcessor);
            return;
        }
        torpedoes(gameState.getPlayerGameObjects());

        signalDone(botProcessor);
    }

    private void supernova(List<GameObject> playerObjects) {
        playerAction.setAction(PlayerActionEn.FIRESUPERNOVA);
        if (playerObjects != null && !playerObjects.isEmpty()) {
            GameObject largestPlayer = null;
            double size = -1, cSize;
            for (GameObject player: playerObjects) {
                cSize = player.getSize();
                if (largestPlayer == null || cSize > size) {
                    if (player.getId() == stateHolder.getBot().getId()) {
                        continue;
                    }
                    largestPlayer = player; size = cSize;
                }
            }
            if (largestPlayer == null) {
                return;
            }
            largestPlayerId = largestPlayer.getId();
            playerAction.setHeading(getHeadingBetween(stateHolder.getBot(), largestPlayer));
        }
        snFired = true;
        sendMessage(playerAction, 999);
    }

    private void torpedoes(List<GameObject> playerObjects) {
        playerAction.setAction(PlayerActionEn.FIRETORPEDOES);
        if (playerObjects != null && !playerObjects.isEmpty()) {
            GameObject closestPlayer = null;
            double dist = -1, cDist;
            for (GameObject player: playerObjects) {
                cDist = getDistanceBetween(stateHolder.getBot(), player);
                if (closestPlayer == null || cDist < dist) {
                    if (player.getId() == stateHolder.getBot().getId()) {
                        continue;
                    }
                    closestPlayer = player; dist = cDist;
                }
            }
            if (closestPlayer == null) {
                return;
            }
            playerAction.setHeading(getHeadingBetween(stateHolder.getBot(), closestPlayer));
        }
        sendMessage(playerAction, 999);
    }

}
