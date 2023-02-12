package microbot.imp;

import enums.ObjectTypeEn;
import enums.PlayerActionEn;
import etc.StateHolder;
import lombok.RequiredArgsConstructor;
import microbot.ActionBot;
import microbot.ActionCalculator;
import model.engine.GameState;
import model.engine.PlayerAction;
import processor.BotProcessor;

import java.util.Comparator;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class ShootBot extends ActionCalculator implements ActionBot {

    private final BotProcessor botProcessor;
    private final StateHolder stateHolder;
    private final PlayerAction playerAction;

    public void run() {
        if (stateHolder.getBot() != null && stateHolder.getBot().getSize() < 15) {
            botProcessor.sendMessage(playerAction, 0);
            return;
        }

        GameState gameState = stateHolder.getGameState();
        playerAction.setAction(PlayerActionEn.FIRETORPEDOES);

        if (gameState.getPlayerGameObjects() != null && !gameState.getPlayerGameObjects().isEmpty()) {
            var list = gameState.getPlayerGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypeEn.PLAYER)
                    .sorted(Comparator.comparing(item -> getDistanceBetween(stateHolder.getBot(), item)))
                    .collect(Collectors.toList());
            System.out.println(list.get(0).getId());

            playerAction.setHeading(getHeadingBetween(stateHolder.getBot(), list.get(0)));
        }

        botProcessor.sendMessage(playerAction, 999);
      //  System.out.println("shoot bot is executed.");
    }
}
