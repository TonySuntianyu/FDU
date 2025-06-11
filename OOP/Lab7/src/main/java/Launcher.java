public class Launcher {
    public static void main(String[] args) {
        // This launcher is used to avoid issues with JavaFX module system
        // when running from command line without proper module-path setup
        GameApplication.main(args);
    }
} 