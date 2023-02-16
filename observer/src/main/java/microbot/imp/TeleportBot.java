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
public class TeleportBot extends ActionCalculator implements ActionBot {

    private final BotProcessor botProcessor;
    private final StateHolder stateHolder;
    private final PlayerAction playerAction;

    private boolean shot;
    private UUID targetId;
    private List<GameObject> largerPlayers;

    @Override
    public void run() {
        GameState gameState = stateHolder.getGameState();
        GameObject bot = stateHolder.getBot();
        if (bot == null
                || gameState.getGameObjects() == null || gameState.getGameObjects().isEmpty()
                || bot.getTeleportCount() == 0 || bot.getSize() == 10) {
            botProcessor.sendMessage(playerAction, -1);
            return;
        }

        if (!shot) {
            shootTele(gameState.getPlayerGameObjects());
            return;
        }
        for (GameObject object: gameState.getGameObjects()) {
            if (object.getGameObjectType() != ObjectTypeEn.TELEPORTER)
                continue;

            GameObject targetPlayer = stateHolder.getPlayerMap().get(targetId);
            if (isInRadius(object, targetPlayer, 1.1* targetPlayer.getSize())) {
                shot = false;
                playerAction.setAction(PlayerActionEn.TELEPORT);
                botProcessor.sendMessage(playerAction, 5);
                System.out.println("teleport");
                return;
            }
        }
    }

    private void shootTele(List<GameObject> playerObjects) {
        if (playerObjects != null && playerObjects.isEmpty())
            return;
        System.out.println("calculating teleport");

        // move array cleaning to garbage collector
        GameObject target = null, bot=stateHolder.getBot();
        for (GameObject player: playerObjects) {
            if (player.getId()==bot.getId())
                continue;
            if (player.getSize() > bot.getSize()) {
                continue;
            }
            if (target == null || target.getSize() < player.getSize()) {
                target = player;
            }
        }
        if (target == null) {
            System.out.println("ketendang 0");
            return;
        }
        // check for larger player around target
        // check in gas cloud
        for (GameObject gameObject: stateHolder.getGameState().getGameObjects()) {
            if (gameObject.getGameObjectType() != ObjectTypeEn.GAS_CLOUD)
                continue;

            if (isInRadius(target, gameObject, gameObject.getSize())) {
                botProcessor.sendMessage(playerAction, -1);
                System.out.println("ketendang 2");
                return;
            }
        }
        targetId = target.getId();
        playerAction.setAction(PlayerActionEn.FIRETELEPORT);
        playerAction.setHeading(getHeadingBetween(stateHolder.getBot(), target));
        shot = true;
        System.out.println("shoot teleport");

        botProcessor.sendMessage(playerAction, 4);
    }
}
