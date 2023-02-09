package microbot.imp;

import enums.ObjectTypeEn;
import enums.PlayerActionEn;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.AccessLevel;

import microbot.ActionBot;
import microbot.ActionCalculator;
import model.engine.GameState;
import model.engine.PlayerAction;
import model.engine.Position;
import processor.BotProcessor;

import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import etc.StateHolder;

@RequiredArgsConstructor
@Getter @Setter
public class MoveBot  extends ActionCalculator implements ActionBot {

    private final BotProcessor botProcessor;
    private final StateHolder stateHolder;

    abstract class MoveBotStrategy{
        final int randomOffset = 3;

        protected int desireAmount;

        @Getter(AccessLevel.PUBLIC)
        protected PlayerAction playerAction  = new PlayerAction();
        protected GameState gameState = stateHolder.getGameState();



        public MoveBotStrategy(){
            this.desireAmount = 40;

            execute();
        }

        public MoveBotStrategy(int defaultDesire){
            this.desireAmount = defaultDesire;

            execute();
        }

        public int getDesire(){
            return clampInt((int)Math.floor(Math.random() * (2 * randomOffset) - randomOffset  + desireAmount), 0, 100);
        }

        abstract void execute();
    }

    class FoodChase extends MoveBotStrategy {
        private float thresholdRadius = 40f;

        private int foodAmoundThreshold = 30;

        void execute(){

            this.playerAction.action = PlayerActionEn.FORWARD;
            this.playerAction.heading = new Random().nextInt(360);

            if (!this.gameState.getGameObjects().isEmpty()) {
                var foodList = this.gameState.getGameObjects()
                        .stream().filter(item -> item.getGameObjectType() == ObjectTypeEn.FOOD)
                        .sorted(Comparator.comparing(item -> getDistanceBetween(stateHolder.getBot(), item)))
                        .collect(Collectors.toList());

                this.playerAction.heading = getHeadingBetween(stateHolder.getBot(), foodList.get(0));

                var insideThresholdFood = foodList
                        .stream().filter(food -> getDistanceBetween(stateHolder.getBot(), food) < thresholdRadius)
                        .collect(Collectors.toList());
                
                this.desireAmount = lerpInt((float)clampInt(insideThresholdFood.size(), 0, foodAmoundThreshold)/foodAmoundThreshold, 80, 99);
            }

            
        }
    }

    class EnemyEvade extends MoveBotStrategy {
        private float thresholdRadius = 200f;

        void execute(){        
            // Position midPoint = new Position();

            if(!this.gameState.getPlayerGameObjects().isEmpty()){
                var playerList = this.gameState.getPlayerGameObjects()
                    .stream().filter(player -> player.getGameObjectType() == ObjectTypeEn.PLAYER)
                    .filter(player -> player.getId() != stateHolder.getBot().getId()) // not this bot
                    // .sorted(Comparator.comparing(player -> getDistanceBetween(stateHolder.getBot(), player)))
                    // .limit(max)
                    .map(player -> player.getPosition())
                    .collect(Collectors.toList());
                
                Position midPoint = Position.getCentroid(playerList);

                this.playerAction.action = PlayerActionEn.FORWARD;
                this.playerAction.heading = rotateHeadingBy(getHeadingBetween(stateHolder.getBot(), midPoint), 180);

                this.desireAmount = lerpInt(1/ clampFloat((float)getDistanceBetween(stateHolder.getBot(), midPoint), 1f, thresholdRadius), 70, 75);
            }

        }
    }

    

    public void run() {
        // int min = 50;
        // int max = 100;
        // int random_int = (int)Math.floor(Math.random() * (max - min + 1) + min);

        // PlayerAction playerAction = new PlayerAction();
        // GameState gameState = stateHolder.getGameState();

        // playerAction.action = PlayerActionEn.FORWARD;
        // playerAction.heading = new Random().nextInt(360);

        // if (!gameState.getGameObjects().isEmpty()) {
        //     var foodList = gameState.getGameObjects()
        //             .stream().filter(item -> item.getGameObjectType() == ObjectTypeEn.FOOD)
        //             .sorted(Comparator.comparing(item -> getDistanceBetween(stateHolder.getBot(), item)))
        //             .collect(Collectors.toList());

        //     playerAction.heading = getHeadingBetween(stateHolder.getBot(), foodList.get(0));
        // }

        // botProcessor.sendMessage(playerAction, random_int);

        // Position midPoint = new Position();

        // if(!gameState.getPlayerGameObjects().isEmpty()){
        //     var playerList = gameState.getPlayerGameObjects()
        //         .stream().filter(player -> player.getGameObjectType() == ObjectTypeEn.PLAYER)
        //         // .sorted(Comparator.comparing(player -> getDistanceBetween(stateHolder.getBot(), player)))
        //         // .limit(max)
        //         .map(player -> player.getPosition())
        //         .collect(Collectors.toList());
            
        //     midPoint = Position.getCentroid(playerList);
        // }

        FoodChase foodChase = new FoodChase();
        EnemyEvade enemyEvade = new EnemyEvade();
        
        PlayerAction toExecute = foodChase.getDesire() > enemyEvade.getDesire() ? foodChase.getPlayerAction() : enemyEvade.getPlayerAction();

        if(foodChase.getDesire() > enemyEvade.getDesire()){
            System.out.println("foodchase -> " + foodChase.getDesire() );
        } else {
            System.out.println("enemyeva -> " + enemyEvade.getDesire());
        }
        // System.out.println(toExecute.heading + " " + toExecute.action);

        // if (enemyEvade.getDesire() > foodChase.getDesire()){
        //     System.out.println("evading to " + enemyEvade.getPlayerAction().heading);
        // } else {
        //     System.out.println("chasing food to " + foodChase.getPlayerAction().heading);
        // }


        // toExecute.describe();
        botProcessor.sendMessage(toExecute, enemyEvade.getDesire());
        

    }
}
