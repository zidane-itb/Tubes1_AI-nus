package etc;

public class PlayerEffectHandler {
    /*
    0 = No effect
    1 = Afterburner active
    2 = Asteriod Field
    4 = Gas cloud 
    */

    public static boolean isNoEffect(String hashCode){
        return hashCode.equals("0");
    }

    public static boolean isAfterburnerActive(String hashCode){
        return hashCode.equals("1") || hashCode.equals("3") || hashCode.equals("5")|| hashCode.equals("7");
    }

    public static boolean isAsteroidDebuffed(String hashCode){
        return hashCode.equals("2") || hashCode.equals("3") || hashCode.equals("6") || hashCode.equals("7");
    }

    public static boolean IsGasCloudDebuffed(String hashCode){
        return hashCode.equals("4") || hashCode.equals("5") || hashCode.equals("6") || hashCode.equals("7");
    }
}
