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
import model.engine.World;
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
        GameObject bot = stateHolder.getBot();
        if (bot == null) {
            botProcessor.sendMessage(playerAction, -1);
            return;
        }

        if (snFired && gameState.getGameObjects() != null && !gameState.getGameObjects().isEmpty()) {
            GameObject sn = null;
            for (GameObject object: gameState.getGameObjects()) {
                if (object.getGameObjectType() == ObjectTypeEn.SUPERNOVA_BOMB) {
                    sn = object;
                    break;
                }
            }
            if (sn == null)
                snFired = false;
            GameObject target = stateHolder.getPlayerMap().get(largestPlayerId);
            World world = gameState.getWorld();
            if (sn != null && !isInRadius(bot, sn, 1.5*bot.getSize()) &&
                    (isInRadius(sn, target, target.getSize()) ||
                            !isInRadius(target, world.getCenterPoint(), 0.9 * world.radius))) {
                playerAction.setAction(PlayerActionEn.DETONATESUPERNOVA);
                sendMessage(playerAction, 5);
                snFired = false;
            }
        }
        if (bot.isSnAvailable()) {
            supernova(gameState.getPlayerGameObjects());
        }
        if (stateHolder.getBot().getSize() < 25) {
            signalDone(botProcessor);
            return;
        }
        torpedoes(gameState.getPlayerGameObjects(), bot);

        signalDone(botProcessor);
    }

    private void supernova(List<GameObject> playerObjects) {
        if (playerObjects != null && playerObjects.isEmpty())
            return;

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
        playerAction.setAction(PlayerActionEn.FIRESUPERNOVA);
        playerAction.setHeading(getHeadingBetween(stateHolder.getBot(), largestPlayer));
        snFired = true;

        sendMessage(playerAction, 3);
    }

    private void torpedoes(List<GameObject> playerObjects, GameObject bot) {
        if (playerObjects != null && playerObjects.isEmpty())
            return;

        GameObject closestPlayer = null;
        double closestDist = -1, temp;
        for (GameObject player: playerObjects) {
            temp = getDistanceBetween(bot, player);
            if (closestPlayer == null || temp < closestDist) {
                if (player.getId() == bot.getId()) {
                    continue;
                }
                closestPlayer = player; closestDist = temp;
            }
        }
        if (closestPlayer == null || !(closestDist-closestPlayer.getSize() > 1.5*bot.getSize())) {
            return;
        }
        playerAction.setAction(PlayerActionEn.FIRETORPEDOES);
        playerAction.setHeading(getHeadingBetween(stateHolder.getBot(), closestPlayer));

        sendMessage(playerAction, 3);
    }


}
