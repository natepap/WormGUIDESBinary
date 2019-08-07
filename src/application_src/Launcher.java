package application_src;


public class Launcher {

    public static void main(String[] args){
        MainApp.main(new String[] {"--module-path=C:\\Users\\njpap\\java tools\\javafx-sdk-12.0.1\\lib",
                "--add-modules=javafx.fxml,javafx.controls,javafx.base,javafx.graphics,javafx.media,javafx.swing,javafx.web",
                "--add-exports javafx.graphics/com.sun.javafx.utils=ALL-UNNAMED"});
    }
}
