import java.io.File;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainController {

    // Elementos de la interfaz
    @FXML private MediaView mediaView;
    @FXML private Slider progressSlider;
    @FXML private Slider volumeSlider;
    @FXML private TableView<MediaItem> libraryTable;
    @FXML private TableColumn<MediaItem, String> nameColumn;
    @FXML private TableColumn<MediaItem, String> formatColumn;
    @FXML private TableColumn<MediaItem, String> durationColumn;
    @FXML private VBox editorPanel;
    @FXML private CheckMenuItem toggleEditor;
    @FXML private Label fileTitle;
    @FXML private CheckMenuItem toggleLibrary;
    @FXML private VBox libraryPanel;
    
    private MediaPlayer mediaPlayer;
    private ObservableList<MediaItem> mediaLibrary = FXCollections.observableArrayList();
    
    // Estados del reproductor
    private boolean isPlaying = false;
    private double currentSpeed = 1.0;

    @FXML
    public void initialize() {
        configureMediaTable();
        setupMediaBindings();
        setupResizeListener(); // <- Agregar esta función
        libraryTable.setItems(mediaLibrary);
    }

private void setupResizeListener() {
    mediaView.sceneProperty().addListener((obs, oldScene, newScene) -> {
        if (newScene != null) {
            newScene.widthProperty().addListener((obsW, oldW, newW) -> adjustMediaViewSize());
            newScene.heightProperty().addListener((obsH, oldH, newH) -> adjustMediaViewSize());
        }
    });
}

private void adjustMediaViewSize() {
    if (mediaView.getScene() != null && mediaPlayer != null) {
        double sceneWidth = mediaView.getScene().getWidth();
        double sceneHeight = mediaView.getScene().getHeight() - 150; // Restamos margen superior e inferior

        double panelLeftWidth = editorPanel.isVisible() ? 150 : 0;
        double panelRightWidth = libraryPanel.isVisible() ? 250 : 0;
        double availableWidth = sceneWidth - (panelLeftWidth + panelRightWidth + 40); // Sumamos los márgenes

        double videoWidth = mediaPlayer.getMedia().getWidth();
        double videoHeight = mediaPlayer.getMedia().getHeight();

        if (videoWidth > 0 && videoHeight > 0) {
            double widthRatio = availableWidth / videoWidth;
            double heightRatio = sceneHeight / videoHeight;
            double scale = Math.min(widthRatio, heightRatio); // Mantener proporción

            mediaView.setFitWidth(videoWidth * scale);
            mediaView.setFitHeight(videoHeight * scale);
        } else {
            mediaView.setFitWidth(availableWidth);
            mediaView.setFitHeight(sceneHeight);
        }

        System.out.println("MediaView ajustado a: " + mediaView.getFitWidth() + "x" + mediaView.getFitHeight());
    }
}




    // Configuración inicial de la tabla de la biblioteca
    private void configureMediaTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        formatColumn.setCellValueFactory(new PropertyValueFactory<>("format"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        libraryTable.setItems(mediaLibrary);
    }
    
    // Vinculaciones de propiedades multimedia
    private void setupMediaBindings() {
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue());
            }
        });
        
        progressSlider.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
            }
        });
    }
    
    @FXML
private void handleOpenFile() {
    try {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Archivos Multimedia", "*.mp4", "*.mkv", "*.avi", "*.mp3", "*.wav")
        );
        
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            loadMediaFile(selectedFile);
            addToLibrary(selectedFile);
        }
    } catch (Exception e) {
        showError("Error al abrir archivo", "No se pudo cargar el archivo multimedia", e.getMessage());
    }
}
    
    private void loadMediaFile(File file) {
        if (!file.exists()) {
            showError("Error de carga", "El archivo no existe", "Puede que haya sido eliminado o movido.");
            return;
        }
    
        try {
            stopMedia(); // Detener reproducción actual
    
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
    
            // 🔥 Establecer el título del video
            fileTitle.setText(file.getName());
    
            // 🔥 Configurar volumen al 50% por defecto
            mediaPlayer.setVolume(0.5);
            volumeSlider.setValue(0.5);
    
            mediaPlayer.setOnReady(() -> {
                progressSlider.setMax(media.getDuration().toSeconds());
    
                // 🔥 Ajustar tamaño del video cuando esté listo
                adjustMediaViewSize();
            });
    
            mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (!progressSlider.isValueChanging()) {
                    progressSlider.setValue(newValue.toSeconds());
                }
            });
    
            mediaPlayer.setOnError(() -> {
                showError("Error multimedia", "No se pudo reproducir el archivo", mediaPlayer.getError().getMessage());
            });
    
            mediaPlayer.play();
    
        } catch (Exception e) {
            showError("Error de carga", "Formato no soportado o error al abrir el archivo", e.getMessage());
        }
    }
      
    

    

    // Control de reproducción
    @FXML
    private void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            mediaPlayer.setRate(currentSpeed);
            isPlaying = true;
        }
    }
    
    @FXML
    private void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }
    
    @FXML
    private void stopMedia() {
    if (mediaPlayer != null) {
        mediaPlayer.stop();
        mediaPlayer.seek(Duration.ZERO); // Regresa el video al inicio
        isPlaying = false;
        progressSlider.setValue(0);
    }
}

    
    // Control de velocidad
    @FXML
    private void setSpeed05() { setPlaybackSpeed(0.5); }
    @FXML
    private void setSpeed075() { setPlaybackSpeed(0.75); }
    @FXML
    private void setSpeed10() { setPlaybackSpeed(1.0); }
    @FXML
    private void setSpeed15() { setPlaybackSpeed(1.5); }
    @FXML
    private void setSpeed20() { setPlaybackSpeed(2.0); }
    
    private void setPlaybackSpeed(double speed) {
        currentSpeed = speed;
        if (mediaPlayer != null) {
            mediaPlayer.setRate(speed);
        }
    }
    
    // Control de tamaño de video
    @FXML
    private void reduceSize() {
        mediaView.setScaleX(mediaView.getScaleX() * 0.9);
        mediaView.setScaleY(mediaView.getScaleY() * 0.9);
    }
    
    @FXML
    private void increaseSize() {
        mediaView.setScaleX(mediaView.getScaleX() * 1.1);
        mediaView.setScaleY(mediaView.getScaleY() * 1.1);
    }
    
    private void addToLibrary(File file) {
        if (!file.exists()) {
            showError("Archivo no encontrado", "El archivo no existe", "No se puede agregar a la biblioteca.");
            return;
        }
    
        // Verificar si la biblioteca ya tiene 17 videos
        if (mediaLibrary.size() >= 17) {
            showError("Límite alcanzado", "No puedes agregar más de 17 videos.", "Elimina alguno para agregar más.");
            return;
        }
    
        // Verificar si el archivo ya está en la biblioteca
        boolean isDuplicate = mediaLibrary.stream()
            .anyMatch(item -> item.getFilePath().equals(file.getAbsolutePath()));
    
        if (isDuplicate) {
            showError("Archivo duplicado", "Este archivo ya está en la biblioteca.", file.getName());
            return;
        }
    
        // Cargar metadatos del archivo
        Media media = new Media(file.toURI().toString());
        MediaPlayer tempPlayer = new MediaPlayer(media);
    
        tempPlayer.setOnReady(() -> {
            try {
                double totalSeconds = media.getDuration().toSeconds();
                String formattedDuration = formatDuration(totalSeconds); // Formatear la duración
    
                MediaItem newItem = new MediaItem(
                    file.getAbsolutePath(),
                    file.getName(),
                    getFileExtension(file),
                    formattedDuration
                );
    
                mediaLibrary.add(newItem);
                libraryTable.setItems(FXCollections.observableArrayList(mediaLibrary));
                libraryTable.refresh();
    
                System.out.println("Archivo agregado a la biblioteca: " + file.getName());
            } catch (Exception e) {
                showError("Error al cargar metadatos", "No se pudieron cargar los metadatos del archivo", e.getMessage());
            } finally {
                tempPlayer.dispose(); // Liberar recursos del MediaPlayer temporal
            }
        });
    
        tempPlayer.setOnError(() -> {
            showError("Error al cargar archivo", "No se pudo leer el archivo", tempPlayer.getError().getMessage());
            tempPlayer.dispose();
        });
    }
    
    private String formatDuration(double totalSeconds) {
        int minutes = (int) (totalSeconds / 60);
        int seconds = (int) (totalSeconds % 60);
        return String.format("%02d:%02d", minutes, seconds); // Formato MM:SS
    }
    
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        return lastIndexOf == -1 ? "" : name.substring(lastIndexOf + 1).toUpperCase();
    }
    
    @FXML
private void handleLibrarySelection() {
    MediaItem selected = libraryTable.getSelectionModel().getSelectedItem();
    if (selected != null) {
        File file = new File(selected.getFilePath());
        if (file.exists()) {
            loadMediaFile(file); // Cargar y reproducir archivo seleccionado
        } else {
            showError("Archivo no encontrado", "El archivo ya no existe en el sistema", "Puede que haya sido movido o eliminado.");
        }
    }
}

    
    // Control de interfaz
        @FXML
    private void toggleEditor() {
        boolean isVisible = editorPanel.isVisible();
        editorPanel.setVisible(!isVisible);
        adjustMediaViewSize(); // 🔥 Recalcular tamaño del video
    }

    @FXML
    private void toggleLibrary() {
        boolean isVisible = libraryPanel.isVisible();
        libraryPanel.setVisible(!isVisible);
        adjustMediaViewSize(); // 🔥 Recalcular tamaño del video
    }

    
    @FXML
    private void toggleFullscreen() {
    if (mediaView.getScene() != null) {
        Stage stage = (Stage) mediaView.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
        }
    }

    
    @FXML
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText("Reproductor Multimedia");
        alert.setContentText("Versión 1.0\nDesarrollado por Marcos Vidal González");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }
   
    @FXML
private void handleManageLibrary() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Archivos Multimedia", "*.mp4", "*.mkv", "*.avi", "*.mp3", "*.wav")
    );
    fileChooser.setTitle("Seleccionar archivos para la biblioteca");
    List<File> selectedFiles = fileChooser.showOpenMultipleDialog(new Stage());

    if (selectedFiles != null) {
        for (File file : selectedFiles) {
            if (mediaLibrary.size() < 17) {
                addToLibrary(file);
            } else {
                showError("Límite alcanzado", "No puedes agregar más de 17 videos.", "Elimina alguno para agregar más.");
                break; // Detener el proceso si ya alcanzamos el límite
            }
        }
        }
    }




@FXML
private void seekVideo() {
    if (mediaPlayer != null) {
        mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
    }
}

@FXML
private void adjustVolume() {
    if (mediaPlayer != null) {
        mediaPlayer.setVolume(volumeSlider.getValue());
        System.out.println("Volumen ajustado a: " + mediaPlayer.getVolume());
    }
}

@FXML
private void next() {
    int index = libraryTable.getSelectionModel().getSelectedIndex();
    if (index < mediaLibrary.size() - 1) {
        libraryTable.getSelectionModel().select(index + 1);
        handleLibrarySelection(); // Cargar el archivo siguiente
    } else {
        System.out.println("No hay más archivos en la lista.");
    }
}

@FXML
private void stop() {
    if (mediaPlayer != null) {
        mediaPlayer.stop();
        isPlaying = false;
        progressSlider.setValue(0);
    }
}

@FXML
private void previous() {
    int index = libraryTable.getSelectionModel().getSelectedIndex();
    if (index > 0) {
        libraryTable.getSelectionModel().select(index - 1);
        handleLibrarySelection(); // Cargar el archivo seleccionado
    }
}


    @FXML
    private void handleExit() {
    if (mediaPlayer != null) {
        mediaPlayer.stop();
        mediaPlayer.dispose();
        }
    System.exit(0);
    }

    
    // Manejo de errores
    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Clase para representar elementos multimedia
    public static class MediaItem {
        private final String path;
        private final String name;
        private final String format;
        private final String duration;
        
        public MediaItem(String path, String name, String format, String duration) {
            this.path = path;
            this.name = name;
            this.format = format;
            this.duration = duration;
        }
        
        // Getters requeridos para TableView
        public String getPath() { return path; }
        public String getFilePath() { return path; }
        public String getName() { return name; }
        public String getFormat() { return format; }
        public String getDuration() { return duration; }
    }
}