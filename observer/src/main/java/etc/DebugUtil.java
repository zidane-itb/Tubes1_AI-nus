package etc;

import java.time.Instant;

import com.ctc.wstx.shaded.msv_core.driver.textui.Debug;

import java.time.Duration;

public class DebugUtil {
    private final static int secInNano = 1_000_000_000;

    private final String idString;

    long currentWaitTime = 0;
    private final long defaultWaitTime;
    Instant lastCheckTime = Instant.now();

    public DebugUtil(){
        this.idString = "default DebugUtil";
        // this.messageString = "default message";
        this.defaultWaitTime = secondsToNano(2);
    }

    public DebugUtil(String idString, int intervalInSeconds){
        this.idString = idString;
        // this.messageString = messageString;
        this.defaultWaitTime = secondsToNano(intervalInSeconds);
    }

    public void Update(){
        if(currentWaitTime <= 0)
            this.currentWaitTime = this.defaultWaitTime;
    }

    public void TriggerMessage(String messageString){
        currentWaitTime -= Duration.between(lastCheckTime, Instant.now()).toNanos();
        lastCheckTime = Instant.now();
        
        if(currentWaitTime <= 0)
            System.out.println(this.idString + " : " + messageString);
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
