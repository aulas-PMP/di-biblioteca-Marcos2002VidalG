import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 1200, 800);
        
        primaryStage.setTitle("Reproductor Multimedia Avanzado");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setResizable(true); // Permitir cambiar el tamaño

        // Evento para alternar pantalla completa con F11
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case F11:
                    primaryStage.setFullScreen(!primaryStage.isFullScreen());
                    break;
                default:
                    break;
            }
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
