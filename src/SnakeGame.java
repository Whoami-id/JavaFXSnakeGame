
import java.net.URL;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SnakeGame extends Application {

    // Variablen
    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT;
    }

    // Fenster
    private static Stage window;

    public static final int BLOCK_SIZE = 20;
    public static final int GAME_WIDTH = 30 * BLOCK_SIZE;
    public static final int GAME_HEIGHT = 20 * BLOCK_SIZE;

    private static double speed = 0.2;
    private static boolean isEndless = false;

    private Direction direction = Direction.RIGHT;
    private boolean moved = false;
    private boolean running = false;

    private final Timeline timeline = new Timeline();

    private ObservableList<Node> snake;

    private MediaPlayer mediaPlayer;
    private final Slider volumeSlider = new Slider();
    private final Label volumeLabel = new Label("1.0");

    private int score = 0;
    private final Label scoreLabel = new Label("Score: " + score);
    private final Label infoLabel = new Label("Drücke ESC für Exit und SPACE für Pause!");

    // --------------------------------
    // -------- Gameszene
    // --------------------------------
    private Pane createGameContent() {
        final Pane root = new Pane();
        root.setPrefSize(GAME_WIDTH, GAME_HEIGHT);

        root.setStyle(""
                // + "-fx-background-image: url(images/background.png);"
                + "-fx-background-size: 20 20;" + "-fx-background-repeat: repeat;" + "-fx-border-color: black;"
                + "-fx-border-style: solid;" + "-fx-border-width: 2;");

        // Schlange
        final Group snakeBody = new Group();
        snake = snakeBody.getChildren();

        // Essen
        final Rectangle food = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
        final Image foodImage = new Image("images/food.png");
        final ImagePattern imagePattern = new ImagePattern(foodImage);
        food.setFill(imagePattern);

        createRandomFood(food);

        // Animation
        final KeyFrame keyFrame = new KeyFrame(Duration.seconds(speed), event -> {
            if (!running) {
                return;
            }

            final boolean toRemove = snake.size() > 1;

            Node tail; // Kopf bzw. Ende der Schlange
            if (toRemove) {
                tail = snake.remove(snake.size() - 1);
            } else {
                tail = snake.get(0);
            }

            final double tailX = tail.getTranslateX();
            final double tailY = tail.getTranslateY();

            switch (direction) {
                case UP:
                    tail.setTranslateX(snake.get(0).getTranslateX());
                    tail.setTranslateY(snake.get(0).getTranslateY() - BLOCK_SIZE);
                    break;
                case DOWN:
                    tail.setTranslateX(snake.get(0).getTranslateX());
                    tail.setTranslateY(snake.get(0).getTranslateY() + BLOCK_SIZE);
                    break;
                case LEFT:
                    tail.setTranslateX(snake.get(0).getTranslateX() - BLOCK_SIZE);
                    tail.setTranslateY(snake.get(0).getTranslateY());
                    break;
                case RIGHT:
                    tail.setTranslateX(snake.get(0).getTranslateX() + BLOCK_SIZE);
                    tail.setTranslateY(snake.get(0).getTranslateY());
                default:
                    break;
            }

            moved = true;

            if (toRemove) {
                snake.add(0, tail);
            }

            // Kollision
            for (final Node rect : snake) {
                if (rect != tail && tail.getTranslateX() == rect.getTranslateX()
                        && tail.getTranslateY() == rect.getTranslateY()) {
                    score = 0;
                    scoreLabel.setText("Score: " + score);
                    restartGame();
                    break;
                }
            }

            // Wand oder nicht?
            if (isEndless) {
                gameIsEndless(tail, root);
            } else {
                gameIsNotEndless(tail, food);
            }

            // Food einsammeln
            if (tail.getTranslateX() == food.getTranslateX() && tail.getTranslateY() == food.getTranslateY()) {
                createRandomFood(food);
                score += 20;
                scoreLabel.setText("Score: " + score);

                final Rectangle rectangle = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
                rectangle.setTranslateX(tailX);
                rectangle.setTranslateY(tailY);
                snake.add(rectangle);
            }
        });

        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE);

        // ScoreLabel
        scoreLabel.setFont(Font.font("Arial", 30));
        scoreLabel.setTranslateX(GAME_WIDTH / 2);

        // Infolabel
        infoLabel.setFont(Font.font("Arial", FontPosture.ITALIC, 10));

        root.getChildren().addAll(food, snakeBody, scoreLabel, infoLabel);

        return root;
    }

    // **** Random food spawn
    private void createRandomFood(final Node food) {
        food.setTranslateX((int) (Math.random() * (GAME_WIDTH - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
        food.setTranslateY((int) (Math.random() * (GAME_HEIGHT - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
    }

    // **** Isendless
    private void gameIsEndless(final Node tail, final Parent root) {
        root.setStyle(""
                // + "-fx-background-image: url(images/background.png);"
                + "-fx-background-size: 20 20;" + "-fx-background-repeat: repeat;");
        if (tail.getTranslateX() < 0) {
            tail.setTranslateX(GAME_WIDTH - BLOCK_SIZE);
        }

        if (tail.getTranslateX() >= GAME_WIDTH) {
            tail.setTranslateX(0);
        }

        if (tail.getTranslateY() < 0) {
            tail.setTranslateY(GAME_HEIGHT - BLOCK_SIZE);
        }

        if (tail.getTranslateY() >= GAME_HEIGHT) {
            tail.setTranslateY(0);
        }
    }

    // **** IsNotEndless
    private void gameIsNotEndless(final Node tail, final Node food) {
        if (tail.getTranslateX() < 0 || tail.getTranslateX() >= GAME_WIDTH || tail.getTranslateY() < 0
                || tail.getTranslateY() >= GAME_HEIGHT) {
            score = 0;
            scoreLabel.setText("Score: " + score);
            restartGame();
            createRandomFood(food);
        }
    }

    // **** Startgame
    private void startGame() {
        final Rectangle head = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
        snake.add(head);

        timeline.play();
        running = true;
    }

    // **** Restartgame
    private void restartGame() {
        stopGame();
        startGame();
    }

    // **** Stopgame
    private void stopGame() {
        running = false;
        timeline.stop();
        snake.clear();
    }

    // --------------------------------
    // -------- Startszene
    // --------------------------------
    private BorderPane createStartScreen() {
        final BorderPane root = new BorderPane();

        // Start
        final Label startLabel = new Label();
        final Image image = new Image(getClass().getResourceAsStream("images/snake.png"));
        final ImageView imageView = new ImageView(image);
        startLabel.setGraphic(imageView);

        final Button startButton = new Button("Start");
        startButton.setOnAction(event -> {
            // System.out.println("Spielstart");
            final Scene scene = new Scene(createGameContent());
            keypressed(scene);

            window.setScene(scene);
            window.setResizable(false);
            window.setTitle("Snake Game");
            window.show();

            startGame();
        });

        final VBox vBox = new VBox(30);
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(startLabel, startButton);
        root.setTop(vBox);

        // Exit
        final Button exitButton = new Button("Exit");
        BorderPane.setAlignment(exitButton, Pos.CENTER);
        BorderPane.setMargin(exitButton, new Insets(20));
        root.setBottom(exitButton);

        exitButton.setOnAction(event -> Platform.exit());

        // Einstellungen
        final Button gameSpeed = new Button("Speed");
        final Button endlessOrNot = new Button("Rand x");
        final Label speedLabel = new Label("Leicht");

        gameSpeed.setOnAction(event -> {
            if (speed == 0.2) {
                SnakeGame.speed = 0.15;
                speedLabel.setText("Mitel");
            } else if (speed == 0.15) {
                SnakeGame.speed = 0.09;
                speedLabel.setText("Schwer");
            } else if (speed == 0.09) {
                SnakeGame.speed = 0.2;
                speedLabel.setText("Leicht");
            }
        });

        endlessOrNot.setOnAction(event -> {
            if (isEndless) {
                endlessOrNot.setText("Rand x");
                isEndless = false;
            } else {
                endlessOrNot.setText("Rand x");
                isEndless = true;
            }

        });

        final HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(gameSpeed, speedLabel, endlessOrNot);
        root.setCenter(hBox);

        // Musik
        final Button muteButton = new Button("",
                new ImageView(new Image(getClass().getResourceAsStream("images/mute.png"))));
        final Button unmuteButton = new Button("",
                new ImageView(new Image(getClass().getResourceAsStream("images/unmute.png"))));

        muteButton.setOnAction(event -> mediaPlayer.pause());

        unmuteButton.setOnAction(event -> mediaPlayer.play());

        final HBox hBox2 = new HBox(5);
        hBox2.getChildren().addAll(volumeSlider, volumeLabel);

        final VBox vBox2 = new VBox(5);
        vBox2.setAlignment(Pos.CENTER_RIGHT);
        vBox2.getChildren().addAll(unmuteButton, muteButton, new Separator(), hBox2);

        root.setRight(vBox2);
        BorderPane.setMargin(vBox2, new Insets(20));

        return root;
    }

    // ***** Musik
    private void playMusic(final String title) {
        final String musicFile = title;
        final URL fileUrl = getClass().getResource(musicFile);

        final Media media = new Media(fileUrl.toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
    }

    // *** Tastatur Interaktion
    private void keypressed(final Scene scene) {
        scene.setOnKeyPressed(event -> {
            // System.out.println("Nutzer hat die Taste... " + event.getCode());
            if (!moved) {
                return;
            }

            switch (event.getCode()) {
                case W:
                case UP:
                    if (direction != Direction.DOWN) {
                        direction = Direction.UP;
                        break;
                    }
                case S:
                case DOWN:
                    if (direction != Direction.UP) {
                        direction = Direction.DOWN;
                        break;
                    }
                case A:
                case LEFT:
                    if (direction != Direction.RIGHT) {
                        direction = Direction.LEFT;
                        break;
                    }
                case D:
                case RIGHT:
                    if (direction != Direction.LEFT) {
                        direction = Direction.RIGHT;
                        break;
                    }
                case SPACE:
                    timeline.pause();
                    scene.setOnKeyPressed(event1 -> {
                        if (event1.getCode() == KeyCode.SPACE) {
                            timeline.playFromStart();
                            keypressed(scene);
                        } else if (event1.getCode() == KeyCode.ESCAPE) {
                            Platform.exit();
                        }

                    });
                    break;
                case ESCAPE:
                    Platform.exit();
                    break;
                case ENTER:
                    break;
                default:
                    break;
            }
            moved = false;
        });
    }

    // 1.
    @Override
    public void init() throws Exception {
        final String musicFile = "music/snakeMusic.mp3";
        playMusic(musicFile);

        volumeSlider.setValue(mediaPlayer.getVolume() * 100);
        volumeSlider.setPrefWidth(80);
        volumeSlider.setShowTickLabels(true);

        volumeSlider.valueProperty().addListener((InvalidationListener) observable -> {
            // System.out.println("Test Slider");
            mediaPlayer.setVolume(volumeSlider.getValue() / 100);
            volumeLabel.setText(String.format("%.2f", volumeSlider.getValue() / 100));
        });
    }

    // 2.
    @Override
    public void start(final Stage primaryStage) throws Exception {
        final Parent root = createStartScreen();

        primaryStage.setResizable(false);
        primaryStage.setTitle("Snake");
        window = primaryStage;

        window.setScene(new Scene(root, GAME_WIDTH, GAME_HEIGHT));
        primaryStage.show();
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
