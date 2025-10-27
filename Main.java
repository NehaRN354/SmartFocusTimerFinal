import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

public class Main extends Application {

    private int focusMinutes = 25;
    private int secondsLeft;
    private Timeline timeline;

    private Label timerLabel;
    private Button startBtn, pauseBtn, resetBtn;

    private ListView<CheckBox> taskListView;
    private Canvas clockCanvas;
    private double canvasSize = 150;

    private MediaPlayer mediaPlayer;
    private ComboBox<String> musicSelector;
    private Button playMusicBtn, stopMusicBtn;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Smart Focus Timer");

        // --- Focus Time Input ---
        TextField focusTimeField = new TextField("25"); // default 25 minutes
        focusTimeField.setPrefWidth(60);

        Button setFocusBtn = new Button("Set Focus Time");
        setFocusBtn.setOnAction(e -> {
            try {
                int value = Integer.parseInt(focusTimeField.getText());
                if (value <= 0) throw new NumberFormatException();
                focusMinutes = value;
                resetTimer();
            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter a valid positive number!");
                alert.show();
            }
        });

        HBox selectorBox = new HBox(10, new Label("Focus time (min):"), focusTimeField, setFocusBtn);
        selectorBox.setPadding(new Insets(10));
        selectorBox.setStyle("-fx-alignment: center;");

        // --- Timer Label ---
        timerLabel = new Label(formatTime(focusMinutes * 60));
        timerLabel.setStyle("-fx-font-size: 36px; -fx-text-fill: #ffffff;");

        // --- Clock Canvas ---
        clockCanvas = new Canvas(canvasSize, canvasSize);
        drawClock(0);

        VBox timerBox = new VBox(clockCanvas, timerLabel);
        timerBox.setPadding(new Insets(20));
        timerBox.setStyle("-fx-alignment: center; -fx-spacing: 10;");

        // --- Buttons ---
        startBtn = new Button("Start");
        pauseBtn = new Button("Pause");
        resetBtn = new Button("Reset");
        HBox controlsBox = new HBox(10, startBtn, pauseBtn, resetBtn);
        controlsBox.setPadding(new Insets(10));
        controlsBox.setStyle("-fx-alignment: center;");

        // --- Task List ---
        taskListView = new ListView<>();
        taskListView.setStyle("-fx-control-inner-background: #2b2b2b; -fx-text-fill: #ffffff;");

        Button addTaskBtn = new Button("Add Task");
        Button editTaskBtn = new Button("Edit Task");
        Button deleteTaskBtn = new Button("Delete Task");
        HBox taskButtons = new HBox(10, addTaskBtn, editTaskBtn, deleteTaskBtn);
        taskButtons.setPadding(new Insets(5));
        taskButtons.setStyle("-fx-alignment: center;");

        VBox tasksBox = new VBox(new Label("Tasks"), taskListView, taskButtons);
        tasksBox.setPadding(new Insets(10));
        tasksBox.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: #ffffff;");
        tasksBox.setSpacing(5);

        // --- Music Selection (only lofi tracks) ---
        musicSelector = new ComboBox<>();
        musicSelector.getItems().addAll("lofi1.mp3", "lofi2.mp3", "lofi3.mp3");
        musicSelector.setValue("lofi1.mp3");

        playMusicBtn = new Button("Play Music");
        stopMusicBtn = new Button("Stop Music");
        HBox musicBox = new HBox(10, new Label("Music:"), musicSelector, playMusicBtn, stopMusicBtn);
        musicBox.setPadding(new Insets(10));
        musicBox.setStyle("-fx-alignment: center;");

        // --- Main Layout ---
        BorderPane root = new BorderPane();
        root.setTop(new VBox(selectorBox, timerBox, musicBox));
        root.setCenter(tasksBox);
        root.setBottom(controlsBox);
        root.setStyle("-fx-background-color: #1e1e1e;");

        Scene scene = new Scene(root, 450, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // --- Timer Timeline ---
        secondsLeft = focusMinutes * 60;
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        timeline.setCycleCount(Timeline.INDEFINITE);

        // --- Button Actions ---
        startBtn.setOnAction(e -> {
            playSound("session_start.wav");
            timeline.play();
        });
        pauseBtn.setOnAction(e -> {
            playSound("session_start.wav");
            timeline.pause();
        });
        resetBtn.setOnAction(e -> {
            playSound("session_start.wav");
            resetTimer();
        });

        addTaskBtn.setOnAction(e -> addTask());
        editTaskBtn.setOnAction(e -> editTask());
        deleteTaskBtn.setOnAction(e -> deleteTask());

        playMusicBtn.setOnAction(e -> playMusic(musicSelector.getValue()));
        stopMusicBtn.setOnAction(e -> stopMusic());
    }

    private void tick() {
        if (secondsLeft > 0) {
            secondsLeft--;
            timerLabel.setText(formatTime(secondsLeft));
            drawClock(1 - ((double) secondsLeft / (focusMinutes * 60)));
        } else {
            timeline.pause();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Focus session over!");
            alert.show();
        }
    }

    private void resetTimer() {
        timeline.stop();
        secondsLeft = focusMinutes * 60;
        timerLabel.setText(formatTime(secondsLeft));
        drawClock(0);
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void drawClock(double progress) {
        GraphicsContext gc = clockCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasSize, canvasSize);

        gc.setStroke(Color.GRAY);
        gc.setLineWidth(8);
        gc.strokeOval(5,5,canvasSize-10, canvasSize-10);

        gc.setStroke(Color.LIMEGREEN);
        gc.setLineWidth(8);
        gc.strokeArc(5,5,canvasSize-10,canvasSize-10,90,-360*progress,ArcType.OPEN);
    }

    // --- Task Methods ---
    private void addTask() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Add a new task");
        dialog.setTitle("Add Task");
        dialog.setContentText("Task:");
        dialog.showAndWait().ifPresent(name -> {
            CheckBox cb = new CheckBox(name);
            cb.setStyle("-fx-text-fill: #ffffff;");
            taskListView.getItems().add(cb);
        });
    }

    private void editTask() {
        CheckBox selected = taskListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            TextInputDialog dialog = new TextInputDialog(selected.getText());
            dialog.setHeaderText("Edit task");
            dialog.showAndWait().ifPresent(newName -> selected.setText(newName));
        }
    }

    private void deleteTask() {
        int selectedIndex = taskListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            taskListView.getItems().remove(selectedIndex);
        }
    }

    // --- Sound Methods ---
    private void playSound(String filename) {
        try {
            Media sound = new Media(new File("src/resources/sounds/" + filename).toURI().toString());
            MediaPlayer mp = new MediaPlayer(sound);
            mp.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Music Methods ---
    private void playMusic(String filename) {
        stopMusic();
        try {
            Media media = new Media(new File("src/resources/sounds/" + filename).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
