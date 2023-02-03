package processor;

import enums.Action;

import java.util.ArrayList;
import java.util.List;

public class MainProcessor {

    private final List<Action> actions;
    private int highestPrio;
    private int msgCount;


    public MainProcessor() {
        this.actions = new ArrayList<>();
        this.highestPrio = 0;
        msgCount = 0;
    }

    public void execute() {
        if (actions.isEmpty()) return;

        System.out.println(actions.get(0));

        actions.clear();
        highestPrio = 0;
    }

    public synchronized void sendMessage(Action action, int priority) {
        this.msgCount += 1;

        if (priority > highestPrio) {
            actions.clear();
            actions.add(action);
            highestPrio = priority;
        } else if (priority == highestPrio) {
            actions.add(action);
        }

        if (msgCount == 2) {
            execute();
            System.out.println(msgCount);
            msgCount = 0;
        }
    }


}
