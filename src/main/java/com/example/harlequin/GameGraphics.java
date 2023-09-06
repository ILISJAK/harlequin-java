package com.example.harlequin;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

public class GameGraphics {
    private static final Logger logger = Logger.getLogger(GameGraphics.class.getName());

    private Pane gamePane;
    private ImageView playerImageView;
    private GameController gameController;
    private int playerCurrentFrame = 0;
    private int playerTotalFrames = 4;
    private int frameHeight = 64;
    private int playerFrameWidth = 64;

    private Map<Enemy, ImageView> enemyImageViewMap = new HashMap<>();
    private Map<Enemy, Integer> enemyCurrentFrameMap = new HashMap<>();

    private Image playerIdleAnimation;
    private Image playerMoveRightAnimation;
    private Image playerMoveLeftAnimation;
    private Image tilesetImage;
    private int tileWidth = 32;
    private int tileHeight = 32;

    public GameGraphics(double width, double height, GameController gameController) {
        this.gameController = gameController;
        this.gamePane = gameController.getGamePane();
        loadImages();
        drawBackgroundTiles();
    }

    public void update() {
        // Update player position and animation
        updatePlayerPosition(gameController.getPlayer().getSprite().getTranslateX(), gameController.getPlayer().getSprite().getTranslateY());
        updatePlayerAnimation(gameController.getPlayer().getMoving(), gameController.getPlayer().getFacingLeft());

        // Update enemy positions and animations
        for (Map.Entry<Enemy, Circle> entry : gameController.getEnemyAIController().getEnemies().entrySet()) {
            Enemy enemy = entry.getKey();
            ImageView enemyImageView = getEnemyImageView(enemy);
            if (enemyImageView != null) {
                enemyImageView.setX(entry.getValue().getTranslateX() - enemyImageView.getFitWidth() / 2);
                enemyImageView.setY(entry.getValue().getTranslateY() - enemyImageView.getFitHeight() / 2);
                updateEnemyAnimation(enemy, gameController.getPlayer().getSprite().getTranslateX(), gameController.getPlayer().getSprite().getTranslateY(), entry.getValue().getTranslateX(), entry.getValue().getTranslateY());
            }
        }
    }


    private void loadImages() {
        try {
            logger.info("Loading images");

            // Load player idle animation (crow_idle.png)
            playerIdleAnimation = new Image(getClass().getResource("/com/example/harlequin/img/CrowAnimations/crow_idle.png").toExternalForm());

            // Load player move right animation (crow_walk.png)
            playerMoveRightAnimation = new Image(getClass().getResource("/com/example/harlequin/img/CrowAnimations/crow_walk.png").toExternalForm());

            // Load player move left animation (crow_walk_left.png)
            playerMoveLeftAnimation = new Image(getClass().getResource("/com/example/harlequin/img/CrowAnimations/crow_walk_left.png").toExternalForm());

            playerImageView = new ImageView(playerMoveRightAnimation);
            playerImageView.setViewport(new Rectangle2D(0, 0, playerFrameWidth, frameHeight));
            playerImageView.setFitWidth(playerFrameWidth * 3);
            playerImageView.setFitHeight(frameHeight * 3);

            gamePane.getChildren().add(playerImageView);

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.2), event -> updatePlayerFrame()));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();

        } catch (Exception e) {
            logger.severe("Exception while loading images: " + e);
        }
    }

    private void updatePlayerFrame() {
        int frameX = playerCurrentFrame * playerFrameWidth;

        playerImageView.setViewport(new Rectangle2D(frameX, 0, playerFrameWidth, frameHeight));

        playerCurrentFrame = (playerCurrentFrame + 1) % playerTotalFrames;
    }

    public void updatePlayerAnimation(boolean moving, boolean facingLeft) {
        gameController.getPlayer().setMoving(moving);
        gameController.getPlayer().setFacingLeft(facingLeft);

        if (!gameController.getPlayer().getMoving()) {
            playerImageView.setImage(playerIdleAnimation);
        } else {
            if (gameController.getPlayer().getFacingLeft()) {
                playerImageView.setImage(playerMoveLeftAnimation);
            } else {
                playerImageView.setImage(playerMoveRightAnimation);
            }
        }

        // Reset the current frame
        playerCurrentFrame = 0;
    }

    private void updatePlayerPosition(double x, double y) {
        playerImageView.setX(x - playerImageView.getFitWidth() / 2);
        playerImageView.setY(y - playerImageView.getFitHeight() / 2);
    }

    public ImageView createEnemyImageView(Enemy enemy) {
        int enemyFrameWidth = enemy.getFrameWidth() / enemy.getFrameCount();
        int enemyFrameHeight = enemy.getFrameHeight();  // get frame height from enemy

        ImageView enemyImageView = new ImageView(enemy.getEnemyMoveRightAnimation());
        enemyImageView.setFitWidth(enemyFrameWidth * 3);
        enemyImageView.setFitHeight(enemyFrameHeight * 3);  // use actual frame height

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.2), event -> updateEnemyFrame(enemy)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        enemyImageViewMap.put(enemy, enemyImageView);
        enemyCurrentFrameMap.put(enemy, 0);
        System.out.println("Created ImageView for enemy: " + enemy);

        return enemyImageView;
    }

    public ImageView getEnemyImageView(Enemy enemy) {
        ImageView enemyImageView = enemyImageViewMap.get(enemy);
        if (enemyImageView == null) {
            System.out.println("No ImageView found for enemy: " + enemy);
        }
        return enemyImageView;
    }

    private void updateEnemyFrame(Enemy enemy) {
        int enemyFrameWidth = enemy.getFrameWidth() / enemy.getFrameCount();
        int enemyFrameHeight = enemy.getFrameHeight();  // get frame height from enemy
        int enemyCurrentFrame = enemyCurrentFrameMap.get(enemy);
        int frameX = enemyCurrentFrame * enemyFrameWidth;

        ImageView enemyImageView = enemyImageViewMap.get(enemy);
        enemyImageView.setViewport(new Rectangle2D(frameX, 0, enemyFrameWidth, enemyFrameHeight));  // use actual frame height

        enemyCurrentFrame = (enemyCurrentFrame + 1) % enemy.getFrameCount();
        enemyCurrentFrameMap.put(enemy, enemyCurrentFrame);
    }


    public void updateEnemyAnimation(Enemy enemy, double playerX, double playerY, double enemyX, double enemyY) {
        ImageView enemyImageView = enemyImageViewMap.get(enemy);

        if (enemyImageView != null) {
            if (playerX > enemyX) {
                // If the enemy's current animation is not the move right animation
                if (!enemyImageView.getImage().equals(enemy.getEnemyMoveRightAnimation())) {
                    enemyImageView.setImage(enemy.getEnemyMoveRightAnimation());
                    enemyCurrentFrameMap.put(enemy, 0); // Reset the animation frame
                }
            } else {
                // If the enemy's current animation is not the move left animation
                if (!enemyImageView.getImage().equals(enemy.getEnemyMoveLeftAnimation())) {
                    enemyImageView.setImage(enemy.getEnemyMoveLeftAnimation());
                    enemyCurrentFrameMap.put(enemy, 0); // Reset the animation frame
                }
            }
        }
    }


    private void drawBackgroundTiles() {
        tilesetImage = new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DT0003_Lucifer_Dungeon_Tileset_Pixel_Art/Png/DungeonTileset.png").toExternalForm());
        int numCols = (int)tilesetImage.getWidth() / tileWidth;
        int numRows = (int)tilesetImage.getHeight() / tileHeight;
        Random random = new Random();

        for (int i = 0; i < GameController.getScreenHeight(); i += tileHeight) {
            for (int j = 0; j < GameController.getScreenWidth(); j += tileWidth) {
                int tileNum = 105 + random.nextInt(3);
                int row = tileNum / numCols;
                int col = tileNum % numCols;
                Rectangle2D viewport = new Rectangle2D(col * tileWidth, row * tileHeight, tileWidth, tileHeight);
                ImageView tileImageView = new ImageView(tilesetImage);
                tileImageView.setViewport(viewport);
                tileImageView.setX(j);
                tileImageView.setY(i);
                gamePane.getChildren().add(0, tileImageView);
            }
        }
    }
}
