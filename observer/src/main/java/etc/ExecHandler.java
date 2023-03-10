package etc;

import com.sun.tools.javac.Main;
import enums.ObjectTypeEn;
import enums.PlayerActionEn;
import microbot.imp.TeleportBot;
import model.engine.GameObject;
import model.engine.GameState;
import model.engine.GameStateDto;
import model.engine.PlayerAction;
import model.engine.Position;
import processor.BotProcessor;
import microbot.ActionBot;
import microbot.imp.MoveBot;
import microbot.imp.ShootBot;
import microbot.imp.ShieldBot;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecHandler {

    private static ActionBot[] actionBots;
    private static ExecutorService executor;

    public static boolean isAllThreadsIdle() {
        return ((ThreadPoolExecutor) executor).getActiveCount() == 0;
    }

    private static void warmupJvm() {
            System.out.println("warming up the jvm");
            int[] x = {0};
            while (x[0] < 10000*actionBots.length) {
                for (ActionBot actionBot: actionBots) {
                        executor.execute(() -> {
                            x[0] += 1;
                            try {
                                actionBot.run();
                            } catch (NullPointerException e) {}
                        });
                }
            }
            System.out.println("jvm warmed up. run count: " + x[0]);

    }

    public static void start() throws InterruptedException {
        Logger logger = LoggerFactory.getLogger(Main.class);
        BotProcessor botProcessor = new BotProcessor();
        StateHolder stateHolder = new StateHolder();
        PlayerAction playerAction = new PlayerAction();
        actionBots = new ActionBot[]{
            new MoveBot(botProcessor, stateHolder, new PlayerAction()),
            new ShootBot(botProcessor, stateHolder, new PlayerAction()),
            new TeleportBot(botProcessor, stateHolder, new PlayerAction()),
            new ShieldBot(botProcessor, stateHolder, new PlayerAction())
        };
        executor = Executors.newFixedThreadPool(actionBots.length);
        warmupJvm();

        String token = System.getenv("Token");
        token = (token != null) ? token : UUID.randomUUID().toString();

        String environmentIp = System.getenv("RUNNER_IPV4");

        String ip = (environmentIp != null && !environmentIp.isBlank()) ? environmentIp : "localhost";
        ip = ip.startsWith("http://") ? ip : "http://" + ip;

        String url = ip + ":" + "5000" + "/runnerhub";

        HubConnection hubConnection = HubConnectionBuilder.create(url)
                .build();

        hubConnection.on("Disconnect", (id) -> {
            System.out.println("Disconnected:");

            hubConnection.stop();
        }, UUID.class);

        hubConnection.on("Registered", (id) -> {
            System.out.println("Registered with the runner " + id);

            Position position = new Position();
            List<Integer> stateList = Stream.of(10, 20, 0, ObjectTypeEn.toValue(ObjectTypeEn.PLAYER), position.x, position.y)
                                        .collect(Collectors.toList());
            GameObject bot = GameObject.FromStateList((id), stateList);
            // GameObject bot = new GameObject(id, 10, 20, 0, position, ObjectTypeEn.PLAYER);
            stateHolder.setBot(bot);
        }, UUID.class);

        hubConnection.on("ReceiveGameState", (gameStateDto) -> {
            GameState gameState = new GameState();
            gameState.world = gameStateDto.getWorld();
            // we instantiate a new hash map instead of clearing it to move the object clearing ...
            // ... to the garbage collection thread so this function won't wait for us to clear ...
            // ... the existing map first
            Map<UUID, GameObject> playerMap = new HashMap<>();
            for (Map.Entry<String, List<Integer>> objectEntry : gameStateDto.getGameObjects().entrySet()) {
                gameState.getGameObjects().add(GameObject.FromStateList(UUID.fromString(objectEntry.getKey()), objectEntry.getValue()));
            }
            for (Map.Entry<String, List<Integer>> objectEntry : gameStateDto.getPlayerObjects().entrySet()) {
                UUID id = UUID.fromString(objectEntry.getKey());
                GameObject player = GameObject.FromStateList(id, objectEntry.getValue());
                gameState.getPlayerGameObjects().add(player);
                playerMap.put(id, player);
            }
            stateHolder.setGameState(gameState);
            stateHolder.setPlayerMap(playerMap);
        }, GameStateDto.class);

        hubConnection.on("ReceivePlayerConsumed", () -> {
            System.out.println("mati");
        });

        hubConnection.on("ReceiveGameComplete", (finalState) -> {
            System.out.println("selesai");
            System.out.println(finalState);
        }, String.class);

        hubConnection.start().blockingAwait();

        Thread.sleep(1000);
        System.out.println("Registering with the runner...");
        hubConnection.send("Register", token, "AI-nus");
        stateHolder.setTeleShot(false);

        //This is a blocking call
        hubConnection.start().subscribe(() -> {
            while (hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
                Thread.sleep(20);
                if (isAllThreadsIdle()&&!botProcessor.isResultExist()) {
                    for (ActionBot actionBot: actionBots) {
                        executor.execute(actionBot::run);
                    }
                    continue;
                }
                if (!botProcessor.isResultExist())
                    continue;
                GameObject bot = stateHolder.getBot();
                if (bot == null) {
                    continue;
                }
                PlayerAction temp = botProcessor.getPlayerAction();
                playerAction.setAction(temp.getAction());
                playerAction.setHeading(temp.getHeading());
                playerAction.setPlayerId(bot.getId());
                if (hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
                    hubConnection.send("SendPlayerAction", playerAction);
                    if (playerAction.getAction() ==PlayerActionEn.FIRETELEPORT) {
                        stateHolder.setTeleShot(true);
                    }
                    if (playerAction.getAction()==PlayerActionEn.TELEPORT) {
                        stateHolder.setTeleShot(false);
                    }
                }

            }
        });

        hubConnection.stop();
    }

}
