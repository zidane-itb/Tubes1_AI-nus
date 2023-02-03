import bot.ActionBot;
import bot.imp.MoveBot;
import bot.imp.ShootBot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class Main {
    private static ExecutorService executor = Executors.newFixedThreadPool( 2 );
    private static ActionBot[] actionBots;
    static {
        actionBots = new ActionBot[]{new MoveBot(), new ShootBot()};
    }
}
