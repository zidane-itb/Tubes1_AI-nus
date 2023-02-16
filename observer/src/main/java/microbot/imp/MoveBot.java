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
import java.util.Comparator;
import java.util.Random;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import etc.StateHolder;
import etc.DebugUtil.TimedDebugLog;
import etc.PlayerEffectHandler;

// @RequiredArgsConstructor
@Getter @Setter
public class MoveBot  extends ActionCalculator implements ActionBot {


    TimedDebugLog playerDebug = new TimedDebugLog("MoveBot", 2);

    //TODO : chaseplayer, evadeboundary, avoid dust, avoid torepeda, avoid teleporter

    private final BotProcessor botProcessor;
    private final StateHolder stateHolder; // dont access this directly
    private final PlayerAction playerAction;

    List<MoveBotStrategy> moveBotStrategy;

    public MoveBot(BotProcessor botProcessor, StateHolder stateHolder, PlayerAction playerAction){
        this.botProcessor = botProcessor;
        this.stateHolder = stateHolder;
        this.playerAction = playerAction;

        this.moveBotStrategy = Stream.of(
            new DefaultMove(),
            new FoodChase()
            // new EnemyEvade(),
            // new EnemyChase(),
            // new EvadeBoundary()
        )
        .collect(Collectors.toList());
    }

    abstract class MoveBotStrategy{
        final int randomOffset = 3;

        protected int desireAmount;
        protected final int defaultDesireAmount = 1;

        @Getter(AccessLevel.PUBLIC)
        protected PlayerAction playerAction  = new PlayerAction();
        protected GameState gameState = stateHolder.getGameState();
        // protected GameState gameState = new GameState();



        public MoveBotStrategy(){
            this.desireAmount = this.defaultDesireAmount;

            this.playerAction.action = PlayerActionEn.FORWARD;
        }

        public int getDesire(){
            return this.desireAmount;
        }

        public void Update(){
            
            this.gameState = stateHolder.getGameState();
            
            // getgamestate here works

            this.execute();
        }

        abstract void execute();
    }

    class DefaultMove extends MoveBotStrategy {
        void execute(){
            this.desireAmount = 2;
            this.playerAction.heading = getHeadingBetween(stateHolder.getBot(), this.gameState.getWorld().getCenterPoint());
        }
    }

    class FoodChase extends MoveBotStrategy {
        private float thresholdRadius = 100f;

        private final float safeGreedOffset = 0f;

        private final int foodSearchRadius = 150;

        private final static int targetTick = 3;

        void execute(){
            this.playerAction.action = PlayerActionEn.FORWARD;
            // this.playerAction.heading = new Random().nextInt(360);

            if (!this.gameState.getGameObjects().isEmpty()) {
                int speed = stateHolder.getBot().getSpeed();

                var foodList = this.gameState.getGameObjects()
                        .stream()
                        .filter(item -> (getDistanceBetween(stateHolder.getBot(), item) < targetTick * speed * 2 
                        && (item.getGameObjectType() == ObjectTypeEn.FOOD || item.getGameObjectType() == ObjectTypeEn.SUPER_FOOD)))
                        .sorted(Comparator.comparing(item -> getDistanceBetween(stateHolder.getBot(), item)))
                        .collect(Collectors.toList());

                Double maxGreedValue = 0d, tempGreedValue = 0d;
                int nearbyCount = 0;

                // if(foodList.size() < 8){
                //     // System.out.println(foodList.size());
                //     this.desireAmount = 3;
                //     this.playerAction.heading = getHeadingBetween(stateHolder.getBot(), this.gameState.getWorld().getCenterPoint());
                //     return;
                // }

                for(GameObject food : foodList){
                    if(getDistanceBetween(stateHolder.getBot(), food) <= targetTick * speed){
                        for(GameObject nearbyFood : foodList){
                            if(getDistanceBetween(food, nearbyFood) <= foodSearchRadius){
                                if(food.getGameObjectType() == ObjectTypeEn.FOOD)
                                    nearbyCount += 1;
                                else // SUPER_FOOD weights thrice as much
                                    nearbyCount += 3;

                                // nearbyCount++;
                            }
                        }
                    }
                    
                    tempGreedValue = nearbyCount / Math.sqrt(getDistanceBetween(stateHolder.getBot(), food));
                    
                    if(tempGreedValue > maxGreedValue + safeGreedOffset){
                        maxGreedValue = tempGreedValue;
                        this.playerAction.heading = getHeadingBetween(stateHolder.getBot(), food);
                        // System.out.println(maxGreedValue + "-" + tempGreedValue + "-" + this.playerAction.heading);
                        
                    }

                    nearbyCount = 0;
                    maxGreedValue = 0d;
                }

                // this.playerAction.heading = getHeadingBetween(stateHolder.getBot(), foodList.get(0))

                int largestNo = 1, count = 1;
                for(GameObject bot : this.gameState.getPlayerGameObjects()){
                    if(bot.getId() == stateHolder.getBot().getId())
                        continue;

                    if (bot.getSize() > stateHolder.getBot().getSize())
                        largestNo += 1;
                    
                    count += 1;
                }

                // this.desireAmount = lerpInt((float)largestNo/count, 3, 4);
                this.desireAmount = slerpInt((float)largestNo/count, 1, 3);
            }
        }
    }

    class EnemyEvade extends MoveBotStrategy {
        private int minThresholdRadius = 50, maxThresholdRadius = 200;

        private int safeSizeThreshold = 10;

        void execute(){        
            if(!this.gameState.getPlayerGameObjects().isEmpty()){
                var biggerPL = this.gameState.getPlayerGameObjects()
                    .stream().filter(player -> player.getId() != stateHolder.getBot().getId() // not player
                        && player.getSize() - safeSizeThreshold >= stateHolder.getBot().getSize()  // bigger
                        && getDistanceBetween(stateHolder.getBot(), player) <= maxThresholdRadius) // in search radius
                    .sorted(Comparator.comparing(player -> getDistanceBetween(stateHolder.getBot(), player)))
                    .map(player -> player.getPosition())
                    .collect(Collectors.toList());
                
                if(biggerPL.isEmpty()){
                    this.desireAmount = -1;

                    return;
                }
                
                this.playerAction.heading = rotateHeadingBy(getHeadingBetween(stateHolder.getBot(), Position.getCentroid(biggerPL)), 180);
                // this.desireAmount = lerpInt(minThresholdRadius / clampInt((int)getDistanceBetween(stateHolder.getBot(), biggerPL.get(0)), minThresholdRadius, maxThresholdRadius), 2, 5);
                this.desireAmount = slerpInt(minThresholdRadius / clampInt((int)getDistanceBetween(stateHolder.getBot(), biggerPL.get(0)), minThresholdRadius, maxThresholdRadius), 2, 5);
                playerDebug.TriggerMessage("evading -> " + this.desireAmount + ", evading " + biggerPL.size() + " enemies");
            }
        }
    }

    class EnemyChase extends MoveBotStrategy {
        private float thresholdRadius = 150f;

        private int safeSizeThreshold = 10;

        private int minThresholdRadius = 200, maxThresholdRadius = 500;

        void execute(){       
            if(!this.gameState.getPlayerGameObjects().isEmpty()){
                this.playerAction.action = PlayerActionEn.FORWARD;
                this.playerAction.heading = getHeadingBetween(stateHolder.getBot(), new Position());

                var smallerPL = this.gameState.getPlayerGameObjects()
                    .stream().filter(player -> player.getId() != stateHolder.getBot().getId() 
                        && player.getSize() + safeSizeThreshold < stateHolder.getBot().getSize())
                        // && getDistanceBetween(stateHolder.getBot(), player) < maxThresholdRadius)
                    .sorted(Comparator.comparing(player -> getDistanceBetween(stateHolder.getBot(), player)))
                    .map(player -> player.getPosition())
                    .collect(Collectors.toList());
                
                if(smallerPL.isEmpty()){
                    this.desireAmount = -1;
                    return;
                }
                    

                this.desireAmount =  slerpInt(minThresholdRadius / clampInt((int)getDistanceBetween(stateHolder.getBot(), smallerPL.get(0)), minThresholdRadius, maxThresholdRadius), 2, 4);

                this.playerAction.heading = getHeadingBetween(stateHolder.getBot(), smallerPL.get(0));
                
                playerDebug.TriggerMessage("chasing -> " + this.desireAmount + ", chasing " + smallerPL.size() + " enemies");

                int largestNo = 1, count = 1;
                for(GameObject bot : this.gameState.getPlayerGameObjects()){
                    if(bot.getId() == stateHolder.getBot().getId())
                        continue;

                    if (bot.getSize() > stateHolder.getBot().getSize())
                        largestNo += 1;
                    
                    count += 1;
                }

                if(largestNo <= 2 && !PlayerEffectHandler.isAfterburnerActive(stateHolder.getBot().getEffectHashCode())){
                    this.playerAction.action = PlayerActionEn.STARTAFTERBURNER;
                    this.desireAmount = 4;

                    System.out.println("activating afterburener!");
                }

                if((largestNo > 2 || stateHolder.getBot().getSize() < 60) && PlayerEffectHandler.isAfterburnerActive(stateHolder.getBot().getEffectHashCode())){
                    this.playerAction.action = PlayerActionEn.STOPAFTERBURNER;
                    this.desireAmount = 5;

                    System.out.println("stopping afterburener!");
                }
            }

        }
    }

    class EvadeBoundary extends MoveBotStrategy {
        int safeDistanceOffset = 20;

        void execute(){
            if(this.gameState.getWorld().getRadius() == null)
                return;

//
//  inner (-)  ||| (+) outer
//

            int distanceOutOfBound = (int)getDistanceBetween(stateHolder.getBot(), new Position()) - this.gameState.getWorld().getRadius();

            if(distanceOutOfBound <= -safeDistanceOffset){ // - means just before (inside bound)
                this.desireAmount = -1;
                return;
            }

            distanceOutOfBound = clampInt(distanceOutOfBound, -safeDistanceOffset, 100);

            this.playerAction.action = PlayerActionEn.FORWARD;
            this.playerAction.heading = getHeadingBetween(stateHolder.getBot(), this.gameState.getWorld().getCenterPoint());

            this.desireAmount = slerpInt(distanceOutOfBound/(100f), 2, 4);

            playerDebug.TriggerMessage("Keluar dari map, berusaha masuk. Distance from border : " + distanceOutOfBound);
        }
    }

    class EvadeGasCloud extends MoveBotStrategy {
        
        void execute(){
            if(this.gameState.getGameObjects() == null)
                return;

            var gasClouds = this.gameState.getGameObjects()
                .stream()
                .filter(cloud -> cloud.getGameObjectType() == ObjectTypeEn.GAS_CLOUD)
                .sorted(Comparator.comparing(cloud -> getDistanceBetween(stateHolder.getBot(), cloud)))
                .collect(Collectors.toList());
            

            boolean inCloud = false;

            this.desireAmount = -1;

            for(GameObject cloud : gasClouds){
                if(!inCloud && getDistanceBetween(stateHolder.getBot(), cloud) <= cloud.getSize()){
                    inCloud = true;

                    this.playerAction.heading = rotateHeadingBy(getHeadingBetween(stateHolder.getBot(), cloud), -90 * (int)Math.signum((stateHolder.getBot().getCurrentHeading() - getHeadingBetween(stateHolder.getBot(), cloud)) - 180));
                    this.desireAmount = 4;
                }
            }
        }
    }
    

    public void run() {
        playerDebug.Update();
        
        if(stateHolder.getBot() == null)
            return;

        moveBotStrategy.forEach((movebot) -> {
            movebot.Update();
            botProcessor.sendMessage(movebot.getPlayerAction(), movebot.getDesire());
            // System.out.println(movebot.getClass().toString() + " -> " + movebot.getDesire());
        });

        playerDebug.TriggerMessage("size" + stateHolder.getBot().getSize());
        playerDebug.TriggerMessage("ini world >" + stateHolder.getGameState().getWorld().getRadius());

        // botProcessor.sendMessage(toExecute.getPlayerAction(), toExecute.getDesire());

        signalDone(botProcessor);
    }
}
