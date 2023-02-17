package etc.DebugUtil;

import java.time.Instant;
import java.time.Duration;

public class TimedDebugLog extends DebugLog {
    final static int secInNano = 1_000_000_000;

    long currentWaitTime = 0;
    final long defaultWaitTime;

    Instant lastCheckTime = Instant.now();

    public TimedDebugLog(){
        this.idString = "DefaultTimedDebug";
        this.defaultWaitTime = secondsToNano(2);
    }

    public TimedDebugLog(String idString, int intervalInSeconds){
        this.idString = idString;
        this.defaultWaitTime = secondsToNano(intervalInSeconds);
    }

    public void Update(){
        if(currentWaitTime <= 0)
            this.currentWaitTime = this.defaultWaitTime;
    }

    public void TriggerMessage(Object messageString){
        currentWaitTime -= Duration.between(lastCheckTime, Instant.now()).toNanos();
        lastCheckTime = Instant.now();
        
        if(currentWaitTime <= 0)
            System.out.println(this.idString + " : " + messageString);
    }    

    private long secondsToNano(int seconds){
        return seconds * secInNano;
    }

    private int nanoToSeconds(long nano){
        return (int)(nano / secInNano);
    }
}
