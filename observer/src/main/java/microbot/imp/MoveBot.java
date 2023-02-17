package microbot.imp;

import enums.ObjectTypeEn;
import enums.PlayerActionEn;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.AccessLevel;

import microbot.ActionBot;
import microbot.ActionCalculator;
import model.engine.GameObject;
import model.engine.GameState;
import model.engine.PlayerAction;
import model.engine.Position;
import processor.BotProcessor;

import java.util.List;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import etc.StateHolder;
import etc.DebugUtil.TimedDebugLog;
import etc.PlayerEffectHandler;

@RequiredArgsConstructor
@Getter @Setter
public class MoveBot  extends ActionCalculator implements ActionBot {

    long currentWaitTime = 0;
    long defaultWaitTime = 2 * 1_000_000_000;
    Duration deltaTime = Duration.ZERO;
    Instant lastCheckTime = Instant.now();

    private final BotProcessor botProcessor;
    private final StateHolder stateHolder;
    private final PlayerAction globalPlayerAction; // ngikutin constructor lombok

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
            return this.desireAmount;
        }

        abstract void execute();
    }

    class FoodChase extends MoveBotStrategy {
        private float thresholdRadius = 100f;

        private final int foodAmoundThreshold = 30;

        void execute(){

            this.playerAction.action = PlayerActionEn.FORWARD;
            this.playerAction.heading = new Random().nextInt(360);

            if (!this.gameState.getGameObjects().isEmpty()) {
                var foodList = this.gameState.getGameObjects()
                        .stream().filter(item -> item.getGameObjectType() == ObjectTypeEn.FOOD || item.getGameObjectType() == ObjectTypeEn.SUPER_FOOD)
                        .sorted(Comparator.comparing(item -> getDistanceBetween(stateHolder.getBot(), item)))
                        .collect(Collectors.toList());

                this.playerAction.heading = getHeadingBetween(stateHolder.getBot(), foodList.get(0));

                var insideThresholdFood = foodList
                        .stream().filter(food -> getDistanceBetween(stateHolder.getBot(), food) < thresholdRadius)
                        .collect(Collectors.toList());
                
                this.desireAmount = lerpInt((float)clampInt(insideThresholdFood.size(), 0, this.foodAmoundThreshold)/this.foodAmoundThreshold, 2, 5);

                if(currentWaitTime <= 0){
                    System.out.println((float)clampInt(insideThresholdFood.size(), 0, this.foodAmoundThreshold)/this.foodAmoundThreshold + " dari " + (float)clampInt(insideThresholdFood.size(), 0, this.foodAmoundThreshold) + " / " + this.foodAmoundThreshold);
                }
                
            }

            
        }
    }

    class EnemyEvade extends MoveBotStrategy {
        private float thresholdRadius = 200f;

        private int safeSizeThreshold = 10;

        void execute(){        
            // Position midPoint = new Position();

            if(!this.gameState.getPlayerGameObjects().isEmpty()){
                var playerList = this.gameState.getPlayerGameObjects()
                    .stream().filter(player -> player.getGameObjectType() == ObjectTypeEn.PLAYER && player.getId() != stateHolder.getBot().getId())
                    .sorted(Comparator.comparing(player -> getDistanceBetween(stateHolder.getBot(), player)))
                    .map(player -> player.getPosition())
                    .collect(Collectors.toList());
                
                Position midPoint = Position.getCentroid(playerList);

                this.playerAction.action = PlayerActionEn.FORWARD;
                this.playerAction.heading = rotateHeadingBy(getHeadingBetween(stateHolder.getBot(), midPoint), 180);

                var biggerPL = this.gameState.getPlayerGameObjects()
                    .stream().filter(player -> player.getGameObjectType() == ObjectTypeEn.PLAYER && player.getId() != stateHolder.getBot().getId() && player.getSize() - safeSizeThreshold >= stateHolder.getBot().getSize())
                    .sorted(Comparator.comparing(player -> getDistanceBetween(stateHolder.getBot(), player)))
                    .map(player -> player.getPosition())
                    .collect(Collectors.toList());
                
                if(biggerPL.isEmpty()){
                    this.desireAmount = -1;
                    return;
                }
                    


                
                // this.desireAmount = lerpInt(1/ clampFloat((float)getDistanceBetween(stateHolder.getBot(), Position.getCentroid(biggerPL)), 1f, thresholdRadius), 70, 78);
                this.desireAmount = lerpInt(1/(float)getDistanceBetween(stateHolder.getBot(), biggerPL.get(0)), 1, 4);
                
            }

        }
    }

    class EnemyChase extends MoveBotStrategy {
        private float thresholdRadius = 150f;

        private int safeSizeThreshold = 10;

        void execute(){        
            // Position midPoint = new Position();

            if(!this.gameState.getPlayerGameObjects().isEmpty()){
                var playerList = this.gameState.getPlayerGameObjects()
                    .stream().filter(player -> player.getGameObjectType() == ObjectTypeEn.PLAYER && player.getId() != stateHolder.getBot().getId())
                    .sorted(Comparator.comparing(player -> getDistanceBetween(stateHolder.getBot(), player)))
                    .map(player -> player.getPosition())
                    .collect(Collectors.toList());
                
                Position midPoint = Position.getCentroid(playerList);

                this.playerAction.action = PlayerActionEn.FORWARD;
                this.playerAction.heading = getHeadingBetween(stateHolder.getBot(), midPoint);

                var smallerPL = this.gameState.getPlayerGameObjects()
                    .stream().filter(player -> player.getGameObjectType() == ObjectTypeEn.PLAYER && player.getId() != stateHolder.getBot().getId() && player.getSize() + safeSizeThreshold < stateHolder.getBot().getSize())
                    .sorted(Comparator.comparing(player -> getDistanceBetween(stateHolder.getBot(), player)))
                    .map(player -> player.getPosition())
                    .collect(Collectors.toList());
                
                if(smallerPL.isEmpty()){
                    this.desireAmount = -1;
                    return;
                }
                    


                
                // this.desireAmount = lerpInt(1/ clampFloat((float)getDistanceBetween(stateHolder.getBot(), Position.getCentroid(biggerPL)), 1f, thresholdRadius), 70, 78);
                this.desireAmount = lerpInt(200/clampFloat((float)getDistanceBetween(stateHolder.getBot(), smallerPL.get(0)), 200, 600), 3, 4);

                this.playerAction.heading = getHeadingBetween(stateHolder.getBot(), smallerPL.get(0));
                
            }

        }
    }

    

    public void run() {
        if(stateHolder.getBot() == null){
            botProcessor.sendMessage(new PlayerAction(), -1);
            return;
        }

        if(stateHolder.getBot().getSize() < 5)
            System.out.println("harusnya mati!");

        FoodChase foodChase = new FoodChase();
        EnemyEvade enemyEvade = new EnemyEvade();
        EnemyChase enemyChase = new EnemyChase();
        
        // PlayerAction toExecute = foodChase.getDesire() > enemyEvade.getDesire() ? foodChase.getPlayerAction() : enemyEvade.getPlayerAction();

        // deltaTime = Duration.between(lastCheckTime, Instant.now());
        currentWaitTime -= Duration.between(lastCheckTime, Instant.now()).toNanos();
        lastCheckTime = Instant.now();

        List<MoveBotStrategy> toCalculate = Stream.of(foodChase, enemyEvade, enemyChase).collect(Collectors.toList());
        // if(foodChase.getDesire() > enemyEvade.getDesire()){
        //     if(currentWaitTime <= 0){
        //         System.out.println("foodchase -> " + foodChase.getDesire() + " > " + enemyEvade.getDesire());
        //         System.out.println("size" + stateHolder.getBot().getSize());
        //     }
        // } else {
        //     if(currentWaitTime <= 0){
        //         System.out.println("enemyeva -> " + enemyEvade.getDesire() + " > " + foodChase.getDesire());
        //         System.out.println("size" + stateHolder.getBot().getSize());
        //     }
        // }

        MoveBotStrategy toExecute = toCalculate
            .stream()
            .sorted(Comparator.comparing(movebot -> movebot.getDesire()))
            .collect(Collectors.toList()).get(toCalculate.size()-1);

        if(currentWaitTime <= 0){
            currentWaitTime = defaultWaitTime;
            System.out.println("size" + stateHolder.getBot().getSize());
        }
        // System.out.println(toExecute.heading + " " + toExecute.action);

        // if (enemyEvade.getDesire() > foodChase.getDesire()){
        //     System.out.println("evading to " + enemyEvade.getPlayerAction().heading);
        // } else {
        //     System.out.println("chasing food to " + foodChase.getPlayerAction().heading);
        // }
        // toExecute.describe();
        botProcessor.sendMessage(toExecute.getPlayerAction(), 100);
        

    }
}
