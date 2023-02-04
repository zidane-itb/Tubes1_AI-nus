package etc;

import com.sun.tools.javac.Main;
import enums.ObjectTypeEn;
import model.engine.GameObject;
import model.engine.GameState;
import model.engine.GameStateDto;
import model.engine.PlayerAction;
import model.engine.Position;
import processor.BotProcessor;
import microbot.ActionBot;
import microbot.imp.MoveBot;
import microbot.imp.ShootBot;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecHandler {

    private static ActionBot[] actionBots;
    private static ExecutorService executor;

    private static void whileVer() throws InterruptedException {
        while (true) {
            if (!isAllThreadsIdle())
                continue;

            runBots();
        }
    }

    public static boolean isAllThreadsIdle() {
        return ((ThreadPoolExecutor) executor).getActiveCount() == 0;
    }

    private static void runBots() {
        for (ActionBot actionBot : actionBots) {
            executor.execute(() -> {
                try {
                    actionBot.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private static void start() throws InterruptedException {
        Logger logger = LoggerFactory.getLogger(Main.class);
        BotProcessor botProcessor = new BotProcessor();
        String token = System.getenv("Token");
        token = (token != null) ? token : UUID.randomUUID().toString();

        String environmentIp = System.getenv("RUNNER_IPV4");

        String ip = (environmentIp != null && !environmentIp.isBlank()) ? environmentIp : "localhost";
        ip = ip.startsWith("http://") ? ip : "http://" + ip;

        String url = ip + ":" + "5000" + "/runnerhub";

        HubConnection hubConnection = HubConnectionBuilder.create(url)
                .build();

        StateHolder stateHolder = new StateHolder();
        actionBots = new ActionBot[]{new MoveBot(botProcessor, stateHolder), new ShootBot(botProcessor, stateHolder)};
        executor = Executors.newFixedThreadPool(actionBots.length);

        hubConnection.on("Disconnect", (id) -> {
            System.out.println("Disconnected:");

            hubConnection.stop();
        }, UUID.class);

        hubConnection.on("Registered", (id) -> {
            System.out.println("Registered with the runner " + id);

            Position position = new Position();
            GameObject bot = new GameObject(id, 10, 20, 0, position, ObjectTypeEn.PLAYER);
            stateHolder.setBot(bot);
        }, UUID.class);

        hubConnection.on("ReceiveGameState", (gameStateDto) -> {
            GameState gameState = new GameState();
            gameState.world = gameStateDto.getWorld();

            for (Map.Entry<String, List<Integer>> objectEntry : gameStateDto.getGameObjects().entrySet()) {
                gameState.getGameObjects().add(GameObject.FromStateList(UUID.fromString(objectEntry.getKey()), objectEntry.getValue()));
            }

            for (Map.Entry<String, List<Integer>> objectEntry : gameStateDto.getPlayerObjects().entrySet()) {
                gameState.getPlayerGameObjects().add(GameObject.FromStateList(UUID.fromString(objectEntry.getKey()), objectEntry.getValue()));
            }

            // move game state from bot processor
            stateHolder.setGameState(gameState);
        }, GameStateDto.class);

        hubConnection.start().blockingAwait();

        Thread.sleep(1000);
        System.out.println("Registering with the runner...");
        hubConnection.send("Register", token, "Coffee Bot");

        //This is a blocking call
        hubConnection.start().subscribe(() -> {
            while (hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
                if (!isAllThreadsIdle() || !botProcessor.isResultExist())
                    continue;

                GameObject bot = stateHolder.getBot();
                if (bot == null) {
                    continue;
                }

                PlayerAction playerAction = botProcessor.getPlayerAction();
                playerAction.setPlayerId(bot.getId());

                if (hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
                    hubConnection.send("SendPlayerAction", playerAction);
                }

                for (ActionBot actionBot: actionBots) {
                    executor.execute(() -> {
                        try {
                            actionBot.run();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

            }
        });

        hubConnection.stop();
    }

}
