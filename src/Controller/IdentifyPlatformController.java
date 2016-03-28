package Controller;

/**
 * Created by nafernando on 3/25/2016.
 */
class IdentifyPlatformController {




    public static String getPlatformPath() {
        String os = System.getProperty("os.name");

        if (os.contains("Windows")) {
            return ("C:\\vAssistant\\");
        } else if (os.contains("Mac")) {
            return ("//home//vAssistant//");

        } else if (os.contains("Linux")) {
            return ("//home//vAssistant//");
        }
        return null;

    }
}


/*
    public static String isWindows(){
        return ("C:\\vAssistant\\configuration\\");
    }
    public static String isMac(){
        return();
    }
    public static String islinux(){
        return();
    }
*/











