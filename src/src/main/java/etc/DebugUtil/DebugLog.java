package etc.DebugUtil;

import java.time.Instant;

import com.ctc.wstx.shaded.msv_core.driver.textui.Debug;

import java.time.Duration;

public abstract class DebugLog {
    

    protected String idString;
    // TODO : check for update method 



    // public DebugUtil(){
    //     this.idString = "default DebugUtil";
    //     // this.messageString = "default message";
    //     this.defaultWaitTime = secondsToNano(2);
    // }

    // public DebugUtil(String idString, int intervalInSeconds){
    //     this.idString = idString;
    //     // this.messageString = messageString;
    //     this.defaultWaitTime = secondsToNano(intervalInSeconds);
    // }

    // public void Update(){
    //     if(currentWaitTime <= 0)
    //         this.currentWaitTime = this.defaultWaitTime;
    // }

    abstract void Update();

    // public void TriggerMessage(String messageString){
    //     currentWaitTime -= Duration.between(lastCheckTime, Instant.now()).toNanos();
    //     lastCheckTime = Instant.now();
        
    //     if(currentWaitTime <= 0)
    //         System.out.println(this.idString + " : " + messageString);
    // }

    // abstract void TriggerMessage(String messageString);

    // public void TriggerMessage(Object messageString){
    //     currentWaitTime -= Duration.between(lastCheckTime, Instant.now()).toNanos();
    //     lastCheckTime = Instant.now();
        
    //     if(currentWaitTime <= 0)
    //         System.out.println(this.idString + " : " + messageString);
    // }

    abstract void TriggerMessage(Object messageString);
    
    
}