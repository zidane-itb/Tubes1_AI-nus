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
import model.engine.World;
import processor.BotProcessor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class TeleportBot extends ActionCalculator implements ActionBot {

    private final BotProcessor botProcessor;
    private final StateHolder stateHolder;
    private final PlayerAction playerAction;

    private int prevTick;
    private double distance;
    private UUID targetId;
    private UUID prevTargetId;
    private Position oTargetPosition;

    @Override
    public void run() {
        GameState gameState = stateHolder.getGameState();
        GameObject bot = stateHolder.getBot();
        if (bot == null
                || gameState.getGameObjects() == null || gameState.getGameObjects().isEmpty()
                || bot.getSize() == 10 || bot.getTeleportCount() <= 0) {
            botProcessor.sendMessage(playerAction, -1);
            return;
        }
        World world = gameState.getWorld();
        if (stateHolder.isTeleShot()&&world.getCurrentTick()!=prevTick) {
            double distanceT = (world.getCurrentTick()-prevTick)*25;
            if (distanceT<distance) {
                botProcessor.sendMessage(playerAction, -1);
                return;
            }
            tele();
            return;
        }
        if (bot.getSize() > 100 && bot.getTeleportCount()>0&&!stateHolder.isTeleShot()) {
            shootTele(gameState.getPlayerGameObjects());
        }
    }

    private void tele() {
        playerAction.setAction(PlayerActionEn.TELEPORT);
        Map<UUID, GameObject> playerMap = stateHolder.getPlayerMap();
        GameObject target = playerMap.get(targetId), bot = stateHolder.getBot();
        if (target==null||target.getSize()>bot.getSize()
                || !isInRadius(target, oTargetPosition, bot.getSize()) ) {
            botProcessor.sendMessage(playerAction, -1);
            stateHolder.setTeleShot(false);
            return;
        }
        botProcessor.sendMessage(playerAction, 5);
    }

    private void shootTele(List<GameObject> playerObjects) {
        if (playerObjects == null || playerObjects.isEmpty() || stateHolder.getGameState().getWorld().getCurrentTick()==prevTick) {
            botProcessor.sendMessage(playerAction, -1);
            return;
        }

        GameObject target = null, bot=stateHolder.getBot();
        // set target and check for larger player around target
        for (GameObject player: playerObjects) {
            if (player == null
                    || player.getId()==bot.getId() || player.getId()==prevTargetId
                    || getDistanceBetween(bot, player) < 2*bot.getSize())
                continue;
            if (player.getSize() > 0.8*bot.getSize()) {
                if (target != null && player.getSize() > 1.2*bot.getSize()
                        && isInRadius(target, player, 1.5*player.getSize()))
                    target = null;
                continue;
            }
            if (target == null
                    || target.getSize() < player.getSize())
                target = player;
        }
        if (target == null) {
            botProcessor.sendMessage(playerAction, -1);
            return;
        }
        // check in gas cloud
        for (GameObject gameObject: stateHolder.getGameState().getGameObjects()) {
            if (gameObject.getGameObjectType() != ObjectTypeEn.GAS_CLOUD)
                continue;

            if (isInRadius(target, gameObject, gameObject.getSize())) {
                botProcessor.sendMessage(playerAction, -1);
                return;
            }
        }
        prevTargetId = targetId;
        targetId = target.getId();
        playerAction.setAction(PlayerActionEn.FIRETELEPORT);
        playerAction.setHeading(getHeadingBetween(stateHolder.getBot(), target));
        distance = getDistanceBetween(bot, target);
        prevTick = stateHolder.getGameState().getWorld().getCurrentTick();
        oTargetPosition = target.getPosition();

        botProcessor.sendMessage(playerAction, 4);
    }

}
