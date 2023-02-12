package etc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import model.engine.GameObject;
import model.engine.GameState;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter @Setter
public class StateHolder {

    @Setter(AccessLevel.NONE)
    private GameState gameState;
    private GameObject bot;
    private Map<UUID, GameObject> playerMap;

    public StateHolder() {
        gameState = new GameState();
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects()
                .stream().filter(gameObject -> gameObject.getId().equals(bot.getId())).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

}
