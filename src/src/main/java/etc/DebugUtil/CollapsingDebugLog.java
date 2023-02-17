package etc.DebugUtil;

public class CollapsingDebugLog extends DebugLog {
    int collapseStack = 0;

    String lastString = "";

    public CollapsingDebugLog(){
        this.idString = "DefaultCollapsingDebugLog";
    }

    public CollapsingDebugLog(String idString){
        this.idString = idString;
    }

    public void Update(){

    }

    public void TriggerMessage(Object messageString){
        collapseStack += 1;

        if(!lastString.toString().equals(messageString.toString())){
            if(collapseStack > 1)
                System.out.println("^^ " + this.idString + " debug printed " + (collapseStack + 1) + " times");

            collapseStack = 0;

            System.out.println(this.idString + " : " + messageString);
            lastString = messageString.toString();
        } else {
            if(collapseStack  >= 20){
                System.out.println(this.idString + " : " + messageString);
                System.out.println("^^ " + this.idString + " debug printed " + (collapseStack + 1) + " times");
                collapseStack = 0;
            }
        }
    }
}
