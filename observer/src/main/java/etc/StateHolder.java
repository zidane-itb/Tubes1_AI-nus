package etc;

import lombok.Getter;
import lombok.Setter;
import model.engine.GameObject;
import model.engine.GameState;

import java.util.Optional;

@Getter @Setter
public class StateHolder {

    private GameState gameState;
    private GameObject bot;


    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

}
