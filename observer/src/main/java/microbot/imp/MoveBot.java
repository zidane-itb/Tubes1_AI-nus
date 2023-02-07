package microbot.imp;

import enums.ObjectTypeEn;
import enums.PlayerActionEn;
import lombok.RequiredArgsConstructor;
import microbot.ActionBot;
import microbot.ActionCalculator;
import model.engine.GameState;
import model.engine.PlayerAction;
import model.engine.Position;
import processor.BotProcessor;

import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collectors;

import etc.StateHolder;

@RequiredArgsConstructor
public class MoveBot  extends ActionCalculator implements ActionBot {

    private final BotProcessor botProcessor;
    private final StateHolder stateHolder;

    public void run() {
        int min = 50;
        int max = 100;
        int random_int = (int)Math.floor(Math.random() * (max - min + 1) + min);

        PlayerAction playerAction = new PlayerAction();
        GameState gameState = stateHolder.getGameState();

        playerAction.action = PlayerActionEn.FORWARD;
        playerAction.heading = new Random().nextInt(360);

        if (!gameState.getGameObjects().isEmpty()) {
            var foodList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypeEn.FOOD)
                    .sorted(Comparator.comparing(item -> getDistanceBetween(stateHolder.getBot(), item)))
                    .collect(Collectors.toList());

            playerAction.heading = getHeadingBetween(stateHolder.getBot(), foodList.get(0));
        }

        botProcessor.sendMessage(playerAction, random_int);

        Position midPoint = new Position();

        if(!gameState.getPlayerGameObjects().isEmpty()){
            var playerList = gameState.getPlayerGameObjects()
                .stream().filter(player -> player.getGameObjectType() == ObjectTypeEn.PLAYER)
                // .sorted(Comparator.comparing(player -> getDistanceBetween(stateHolder.getBot(), player)))
                // .limit(max)
                .map(player -> player.getPosition())
                .collect(Collectors.toList());
            
            midPoint = Position.getCentroid(playerList);
        }

    }
}
