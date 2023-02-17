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

@RequiredArgsConstructor
public class TeleportBot extends ActionCalculator implements ActionBot {

    private final BotProcessor botProcessor;
    private final StateHolder stateHolder;
    private final PlayerAction playerAction;
    private boolean shot;
    private int prevTick;
    private double distance;

    @Override
    public void run() {
        GameState gameState = stateHolder.getGameState();
        GameObject bot = stateHolder.getBot();
        if (bot == null
                || gameState.getGameObjects() == null || gameState.getGameObjects().isEmpty()
                || bot.getSize() == 10) {
            botProcessor.sendMessage(playerAction, -1);
            return;
        }
        World world = gameState.getWorld();
        int cTick = world.getCurrentTick();
        if ((cTick-prevTick)*20>=world.getRadius()) {
            shot=false;
        }
        if (shot) {
            if ((cTick-prevTick)*25<distance) {
                botProcessor.sendMessage(playerAction, -1);
                return;
            }
            playerAction.setAction(PlayerActionEn.TELEPORT);
            botProcessor.sendMessage(playerAction, 5);
            return;
        }
        if (bot.getSize() > 40 && bot.getTeleportCount()>0) {
            shootTele(gameState.getPlayerGameObjects());
        }
    }

    private void shootTele(List<GameObject> playerObjects) {
        if (playerObjects != null && playerObjects.isEmpty())
            return;

        GameObject target = null, bot=stateHolder.getBot();
        // check for larger player around target
        for (GameObject player: playerObjects) {
            if (player.getId()==bot.getId())
                continue;
            if (player.getSize() > 1.2*bot.getSize()) {
                if (target != null
                        && isInRadius(target, player, 1.5*target.getSize()))
                    target = null;
                continue;
            }
            if ((target == null && player.getSize() < 0.7*bot.getSize())
                    || (target != null && target.getSize() < player.getSize()))
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
        playerAction.setAction(PlayerActionEn.FIRETELEPORT);
        playerAction.setHeading(getHeadingBetween(stateHolder.getBot(), target));
        distance = getDistanceBetween(bot, stateHolder.getPlayerMap().get(target.getId()));
        prevTick = stateHolder.getGameState().getWorld().getCurrentTick();

        botProcessor.sendMessage(playerAction, 4);
    }

    public void setShot(boolean shot){
        this.shot = shot;
    }

}
