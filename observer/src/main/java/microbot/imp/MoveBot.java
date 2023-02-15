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

import java.util.List;
import java.util.Comparator;
import java.util.Random;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import etc.StateHolder;
import etc.DebugUtil.TimedDebugLog;

// @RequiredArgsConstructor
@Getter @Setter
public class MoveBot  extends ActionCalculator implements ActionBot {


    TimedDebugLog playerDebug = new TimedDebugLog("MoveBot", 2);

    //TODO : chaseplayer, evadeboundary

    private final BotProcessor botProcessor;
    private final StateHolder stateHolder; // dont access this directly
    private final PlayerAction playerAction;

    List<MoveBotStrategy> moveBotStrategy;

    public MoveBot(BotProcessor botProcessor, StateHolder stateHolder, PlayerAction playerAction){
        this.botProcessor = botProcessor;
        this.stateHolder = stateHolder;
        this.playerAction = playerAction;

        this.moveBotStrategy = Stream.of(
            new FoodChase(),
            new EnemyEvade(),
            new EnemyChase(),
            new EvadeBoundary()
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

    class FoodChase extends MoveBotStrategy {
        private float thresholdRadius = 100f;

        private final int foodAmoundThreshold = 30;

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
                
                this.desireAmount = lerpInt((float)clampInt(insideThresholdFood.size(), 0, this.foodAmoundThreshold)/this.foodAmoundThreshold, 3, 4);   
            }

            
        }
    }

    class EnemyEvade extends MoveBotStrategy {
        private float thresholdRadius = 200f;

        private int safeSizeThreshold = 10;

        void execute(){        
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

                if(getDistanceBetween(stateHolder.getBot(), biggerPL.get(0)) > thresholdRadius){
                    this.desireAmount = 2;
                } else {
                    // this.desireAmount = lerpInt(1/ clampFloat((float)getDistanceBetween(stateHolder.getBot(), Position.getCentroid(biggerPL)), 1f, thresholdRadius), 70, 78);
                    this.desireAmount = lerpInt(1/(float)getDistanceBetween(stateHolder.getBot(), biggerPL.get(0)), 2, 4);
                }
            }
        }
    }

    class EnemyChase extends MoveBotStrategy {
        private float thresholdRadius = 150f;

        private int safeSizeThreshold = 20;

        void execute(){       
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
                    


                if(getDistanceBetween(stateHolder.getBot(), smallerPL.get(0)) > thresholdRadius){
                    this.desireAmount = 2; // let it
                } else {
                    this.desireAmount = lerpInt(1/(float)getDistanceBetween(stateHolder.getBot(), smallerPL.get(0)), 2, 4);

                    this.playerAction.heading = getHeadingBetween(stateHolder.getBot(), smallerPL.get(0));
                }
            }

        }
    }

    class EvadeBoundary extends MoveBotStrategy {
        void execute(){
            if(this.gameState.getWorld().getRadius() == null)
                return;

//
//  inner (-)  ||| (+) outer
//

            int distanceOutOfBound = (int)getDistanceBetween(stateHolder.getBot(), new Position()) - this.gameState.getWorld().getRadius();

            if(distanceOutOfBound <= 0){
                this.desireAmount = -1;
                return;
            }

            distanceOutOfBound = clampInt(distanceOutOfBound, 0, 100);

            this.playerAction.action = PlayerActionEn.FORWARD;
            this.playerAction.heading = getHeadingBetween(stateHolder.getBot(), this.gameState.getWorld().getCenterPoint());

            this.desireAmount = lerpInt(distanceOutOfBound/(100f), 2, 4);

            playerDebug.TriggerMessage("Keluar dari map, berusaha masuk. Distance from border : " + distanceOutOfBound);
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

        // MoveBotStrategy toExecute = moveBotStrategy
        //     .stream()
        //     .sorted(Comparator.comparing(movebot -> movebot.getDesire()))
        //     .collect(Collectors.toList()).get(moveBotStrategy.size()-1);

        playerDebug.TriggerMessage("size" + stateHolder.getBot().getSize());
        playerDebug.TriggerMessage("ini world >" + stateHolder.getGameState().getWorld().getRadius());

        // botProcessor.sendMessage(toExecute.getPlayerAction(), toExecute.getDesire());

        signalDone(botProcessor);
    }
}
