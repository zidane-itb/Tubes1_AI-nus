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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import etc.StateHolder;

@RequiredArgsConstructor
@Getter @Setter
public class MoveBot  extends ActionCalculator implements ActionBot {

    private final BotProcessor botProcessor;
    private final StateHolder stateHolder;
    private final PlayerAction globalPlayerAction;

    abstract class MoveBotStrategy{
        protected int desireAmount;

        @Getter(AccessLevel.PUBLIC)
        protected PlayerAction playerAction  = new PlayerAction();
        protected GameState gameState = stateHolder.getGameState();

        public MoveBotStrategy(){
            this.desireAmount = -1;

            execute();
        }

        public int getDesire(){
            return this.desireAmount;
        }

        abstract void execute();
    }

    /* Meng-handle pergerakan mencari food, super food, dan supernova */
    class FoodChase extends MoveBotStrategy {
        private final int foodAmoundThreshold = 40;

        void execute(){

            this.playerAction.action = PlayerActionEn.FORWARD;
            this.playerAction.heading = new Random().nextInt(360);

            if (!this.gameState.getGameObjects().isEmpty()) {
                var foodList = this.gameState.getGameObjects()
                        .stream().filter(item -> item.getGameObjectType() == ObjectTypeEn.FOOD 
                        || item.getGameObjectType() == ObjectTypeEn.SUPER_FOOD
                        || item.getGameObjectType() == ObjectTypeEn.SUPERNOVA_PICKUP)
                        .collect(Collectors.toList());

                int valueNearby = 0;
                double tempGreedyVal, maxGreedyVal = 0;

                for(GameObject item : foodList){
                    for(GameObject nearbyItem : this.gameState.getGameObjects()){
                        if(getDistanceBetween(nearbyItem, item) > 200)
                            continue;

                        if(nearbyItem.getGameObjectType() == ObjectTypeEn.FOOD)
                            valueNearby += 1;
                        if(nearbyItem.getGameObjectType() == ObjectTypeEn.SUPER_FOOD)
                            valueNearby += 3;
                        if(nearbyItem.getGameObjectType() == ObjectTypeEn.SUPERNOVA_PICKUP)
                            valueNearby += 9;
                    }

                    tempGreedyVal = valueNearby / getDistanceBetween(stateHolder.getBot(), item);

                    if(maxGreedyVal < tempGreedyVal){
                        maxGreedyVal = tempGreedyVal;
                        this.playerAction.heading = getHeadingBetween(stateHolder.getBot(), item);
                        this.desireAmount = lerpInt(easeInOut((float)clampInt(valueNearby, 0, this.foodAmoundThreshold)/this.foodAmoundThreshold), 2, 4);
                    }

                    valueNearby = 0;
                    tempGreedyVal = 0;
                }   
            }            
        }
    }

    /* Meng-handle pergerakan menghindari musuh yang lebih besar, gas_cloud, asteroid_field, dan wormhole */
    /* wormhole dihindari karena output yang sangat random dan berbahaya bagi pemain */
    class EnemyEvade extends MoveBotStrategy {
        private int safeSizeThreshold = 30;

        void execute(){        
            // Position midPoint = new Position();

            if(!this.gameState.getPlayerGameObjects().isEmpty()){
                var playerList = this.gameState.getPlayerGameObjects()
                    .stream().filter(item -> (item.getGameObjectType() == ObjectTypeEn.PLAYER 
                    && item.getId() != stateHolder.getBot().getId() 
                    && item.getSize() - safeSizeThreshold > stateHolder.getBot().getSize()
                    && getDistanceBetween(stateHolder.getBot(), item) - item.getSize() < 500)
                    || 
                    (item.getGameObjectType() == ObjectTypeEn.GAS_CLOUD
                    && getDistanceBetween(stateHolder.getBot(), item) - item.getSize() < 200)
                    ||
                    (item.getGameObjectType() == ObjectTypeEn.ASTEROID_FIELD
                    && getDistanceBetween(stateHolder.getBot(), item) - item.getSize() < 100)
                    ||
                    (item.getGameObjectType() == ObjectTypeEn.WORMHOLE
                    && getDistanceBetween(stateHolder.getBot(), item) - item.getSize() - stateHolder.getBot().getSize() < 10))
                    .collect(Collectors.toList());
                
                if(playerList.isEmpty()){
                    this.desireAmount = -1;
                    return;
                }

                Position midPoint = Position.getCentroid(playerList);

                this.playerAction.action = PlayerActionEn.FORWARD;
                this.playerAction.heading = rotateHeadingBy(getHeadingBetween(stateHolder.getBot(), midPoint), 180);

                this.desireAmount = lerpInt(easeInOut(1/getDistanceBetween(stateHolder.getBot(), playerList.get(0))), 1, 4);
                
            }

        }
    }

    /* Meng-handle pergerakan menghindari torpedo salvo dan teleporter musuh */
    class TorpedoEvade extends MoveBotStrategy{
        void execute(){
            if(!this.gameState.getPlayerGameObjects().isEmpty()){
                var torpedoList = this.gameState.getPlayerGameObjects()
                    .stream().filter(item -> (item.getGameObjectType() == ObjectTypeEn.TORPEDO_SALVO
                    && getDistanceBetween(stateHolder.getBot(), item) - item.getSize() < 400)
                    ||
                    (item.getGameObjectType() == ObjectTypeEn.TELEPORTER
                    && getDistanceBetween(stateHolder.getBot(), item) - 2 * stateHolder.getBot().getSize() < 200))
                    .sorted(Comparator.comparing(item -> getDistanceBetween(stateHolder.getBot(), item)))
                    .collect(Collectors.toList());
                
                if(torpedoList.isEmpty()){
                    this.desireAmount = -1;
                    return;
                }   
    
                this.playerAction.action = PlayerActionEn.FORWARD;
                this.playerAction.heading = torpedoList.get(0).getGameObjectType() == ObjectTypeEn.TORPEDO_SALVO ?
                                                                    (
                                                                        getHeadingDifference(stateHolder.getBot().getCurrentHeading(), rotateHeadingBy(torpedoList.get(0).getCurrentHeading(), 90)) < 180 
                                                                        ? 
                                                                        rotateHeadingBy(torpedoList.get(0).getCurrentHeading(), 90) 
                                                                        : 
                                                                        rotateHeadingBy(torpedoList.get(0).getCurrentHeading(), -90)
                                                                    )                   
                                                                        :
                                                                        // TELEPORTER
                                                                    (
                                                                        rotateHeadingBy(getHeadingBetween(stateHolder.getBot(), torpedoList.get(0)), 180)
                                                                    );
    
                this.desireAmount = lerpInt(easeInOut(100/clampDouble(getDistanceBetween(stateHolder.getBot(), torpedoList.get(0)), 100, 400)), 2, 4);
                
            }
        }
    }

    /* Meng-handle pergerakan mengejar musuh yang lebih kecil */
    class EnemyChase extends MoveBotStrategy {
        private int safeSizeThreshold = 20;
        private int safeMinimumSizeThreshold = 35;

        void execute(){        
            if(!this.gameState.getPlayerGameObjects().isEmpty()){

                
                var smallerPL = this.gameState.getPlayerGameObjects()
                    .stream().filter(player -> player.getGameObjectType() == ObjectTypeEn.PLAYER && player.getId() != stateHolder.getBot().getId() && player.getSize() + safeSizeThreshold < stateHolder.getBot().getSize())
                    .sorted(Comparator.comparing(player -> getDistanceBetween(stateHolder.getBot(), player)))
                    .map(player -> player.getPosition())
                    .collect(Collectors.toList());
                
                if(smallerPL.isEmpty() || stateHolder.getBot().getSize() <= safeMinimumSizeThreshold){
                    this.desireAmount = -1;
                    return;
                }
                
                this.playerAction.action = PlayerActionEn.FORWARD;
                this.playerAction.heading = getHeadingBetween(stateHolder.getBot(), smallerPL.get(0));
                this.desireAmount = lerpInt(easeOut(200/clampDouble(getDistanceBetween(stateHolder.getBot(), smallerPL.get(0)), 200, 600)), 2, 4);
            }

        }
    }

    
    /* runner code */
    public void run() {
        if(stateHolder.getBot() == null){
            botProcessor.sendMessage(new PlayerAction(), -1);
            return;
        }

        List<MoveBotStrategy> toCalculate = Stream.of(new FoodChase(), 
                                                    new EnemyEvade(), 
                                                    new EnemyChase(), 
                                                    new TorpedoEvade())
                                            .collect(Collectors.toList());
        

        MoveBotStrategy toExecute = toCalculate
            .stream()
            .sorted(Comparator.comparing(movebot -> movebot.getDesire()))
            .collect(Collectors.toList()).get(toCalculate.size()-1);
        
        botProcessor.sendMessage(toExecute.getPlayerAction(), toExecute.getDesire());
    }
}
