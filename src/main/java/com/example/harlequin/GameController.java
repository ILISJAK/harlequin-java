package com.example.harlequin;

import com.almasb.fxgl.scene3d.Cone;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;

import java.util.*;

import javafx.scene.control.Label;
import javafx.util.Duration;

public class GameController {
    @FXML
    private Pane gamePane;
    @FXML
    private Rectangle healthBar, healthBarBackground;
    @FXML
    private Label playerLevelLabel, xpLabel, waveLabel, timerLabel, healthLabel;
    private Player player;
    private EnemyAIController enemyAIController;
    private GameStateManager gameStateManager;

    private boolean gamePaused = false;
    private List<Item> allUpgrades;
    private VBox upgradePane;
    private Rectangle background;

    private static final double SCREEN_WIDTH = Screen.getPrimary().getBounds().getWidth();
    private static final double SCREEN_HEIGHT = Screen.getPrimary().getBounds().getHeight();
    private static final double MARGIN = 60;

    private Map<Enemy, Circle> enemyMap = new HashMap<>();
    private final Set<KeyCode> activeKeys = new HashSet<>();


    private double screenWidth = Screen.getPrimary().getBounds().getWidth();
    private double screenHeight = Screen.getPrimary().getBounds().getHeight();

    private final AnimationTimer gameLoop = new AnimationTimer() {
        @Override
        public void handle(long now) {
            System.out.println("Game loop running...");

            double speed = player.getMovementSpeed();
            System.out.println("Player speed: " + speed);
            if (activeKeys.contains(KeyCode.W) && player.getSprite().getTranslateY() - speed - player.getSprite().getRadius() > 0) {
                player.getSprite().setTranslateY(player.getSprite().getTranslateY() - speed);
            }
            if (activeKeys.contains(KeyCode.S) && player.getSprite().getTranslateY() + speed + player.getSprite().getRadius() < screenHeight) {
                player.getSprite().setTranslateY(player.getSprite().getTranslateY() + speed);
            }
            if (activeKeys.contains(KeyCode.A) && player.getSprite().getTranslateX() - speed - player.getSprite().getRadius() > 0) {
                player.getSprite().setTranslateX(player.getSprite().getTranslateX() - speed);
            }
            if (activeKeys.contains(KeyCode.D) && player.getSprite().getTranslateX() + speed + player.getSprite().getRadius() < screenWidth) {
                player.getSprite().setTranslateX(player.getSprite().getTranslateX() + speed);
            }
            System.out.println("Number of enemies: " + enemyMap.size());
            enemyAIController.moveEnemies();
            player.attack();
            updatePlayerState();
        }
    };

    private void pauseGame() {
        gameLoop.stop();
        gamePaused = true;
        background = new Rectangle(SCREEN_WIDTH, SCREEN_HEIGHT, Color.BLACK);
        background.setOpacity(0.5);
        gamePane.getChildren().add(0, background);
    }
    private void resumeGame() {
        gameLoop.start();
        gamePaused = false;
        gamePane.getChildren().remove(background);
        gamePane.requestFocus();
    }

    private void displayUpgradeOptions() {
        List<Item> availableUpgrades = getAvailableUpgrades();

        // Create a new pane to hold the upgrade options
        upgradePane = new VBox(10);
        upgradePane.setAlignment(Pos.CENTER);
        upgradePane.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 20; -fx-border-color: black; -fx-border-width: 2;");

        // Create a clickable area for each available upgrade
        for (Item upgrade : availableUpgrades) {
            ImageView imageView = new ImageView(upgrade.getImage());
            imageView.setFitWidth(80);  // adjust the size as needed
            imageView.setFitHeight(80);  // adjust the size as needed

            Label label = new Label(upgrade.getName() + "\n" + upgrade.getDescription());
            label.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: black; -fx-border-width: 2; -fx-font-size: 20;");
            label.setAlignment(Pos.CENTER_LEFT);
            label.setPadding(new Insets(10));
            VBox.setMargin(label, new Insets(5));
            label.setMouseTransparent(true);

            HBox hbox = new HBox(10);  // 10 is the spacing between the children
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setPadding(new Insets(5));
            VBox.setMargin(hbox, new Insets(5));

            hbox.getChildren().addAll(imageView, label);

            hbox.setOnMouseEntered(event -> {
                label.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: darkgray; -fx-border-width: 2; -fx-font-size: 20;");
            });
            hbox.setOnMouseExited(event -> {
                label.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: black; -fx-border-width: 2; -fx-font-size: 20;");
            });

            hbox.setOnMouseClicked(event -> {
                // Apply the chosen upgrade
                upgrade.apply();
                // Resume the game
                resumeGame();
                // Close the upgrade pane
                gamePane.getChildren().remove(upgradePane);
                upgradePane.layoutXProperty().unbind();
                upgradePane.layoutYProperty().unbind();
            });

            upgradePane.getChildren().add(hbox);
        }

        // Add the upgrade pane to the game pane
        gamePane.getChildren().add(upgradePane);

        // Set the position of the upgrade pane
        Platform.runLater(() -> {
            upgradePane.setLayoutX(gamePane.getWidth() / 2 - upgradePane.getWidth() / 2);
            upgradePane.setLayoutY(gamePane.getHeight() / 2 - upgradePane.getHeight() / 2);
        });

        // Pause the game
        pauseGame();
    }


    public void updateWaveLabel() {
        waveLabel.setText("Wave: " + gameStateManager.getCurrentWave());
    }

    public void updateTimerLabel() {
        timerLabel.setText("Time Remaining: " + gameStateManager.getTimeRemainingInWave() + "s");
    }
    private void updateHealthBar() {
        double percentageHealth = (double) player.getHealth() / player.getMaxHealth();
        healthBar.setWidth(200 * percentageHealth);  // Assuming the max width of the health bar is 200
        if (percentageHealth < 0.3) {
            healthBar.setFill(Color.RED); // Health bar turns red when health is below 30%
        } else if (percentageHealth < 0.6) {
            healthBar.setFill(Color.ORANGE); // Health bar turns orange when health is below 60%
        } else {
            healthBar.setFill(Color.GREEN);
        }

        Label healthLabel = (Label) healthBar.getUserData();
        healthLabel.setText(player.getHealth() + "/" + player.getMaxHealth());
    }

    private void displayDamage(double damage) {
        Label damageLabel = new Label("-" + String.valueOf(damage));
        damageLabel.setStyle("-fx-text-fill: red; -fx-font-size: 24;");
        damageLabel.setTranslateX(player.getSprite().getTranslateX());
        damageLabel.setTranslateY(player.getSprite().getTranslateY() - 20); // Slightly above the player

        // Add to scene
        ((Pane) player.getSprite().getParent()).getChildren().add(damageLabel);

        // Animate the damage label
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1500), damageLabel);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setOnFinished(event -> {
            // Remove label from scene
            ((Pane) player.getSprite().getParent()).getChildren().remove(damageLabel);
        });
        fadeTransition.play();
    }

    private void checkGameOver() {
        if (player.getHealth() <= 0) {
            gameLoop.stop();
            gameStateManager.stopEnemySpawnTimeline();

            Label gameOverLabel = new Label("GAME OVER");
            gameOverLabel.setStyle("-fx-text-fill: red; -fx-font-size: 48;");
            gameOverLabel.setAlignment(Pos.CENTER);
            gameOverLabel.layoutXProperty().bind(gamePane.widthProperty().subtract(gameOverLabel.widthProperty()).divide(2));
            gameOverLabel.layoutYProperty().bind(gamePane.heightProperty().subtract(gameOverLabel.heightProperty()).divide(2));

            ((Pane) player.getSprite().getParent()).getChildren().add(gameOverLabel);
            gamePane.requestLayout();

            gameOverLabel.layoutXProperty().unbind();
            gameOverLabel.layoutYProperty().unbind();
        }
    }
    public void updatePlayerState(){
        updateXpLabel();
        updatePlayerLevelLabel();
        if (player.hasLeveledUp()) {
            displayUpgradeOptions();
            player.setHasLeveledUp(false);
        }
    }


    private List<Item> getAvailableUpgrades() {
        int numberOfUpgradesToOffer = 3;
        List<Item> availableUpgrades = new ArrayList<>(numberOfUpgradesToOffer);
        List<Item> allUpgradesCopy = new ArrayList<>(allUpgrades);  // create a copy of the list to avoid modifying the original list
        for (int i = 0; i < numberOfUpgradesToOffer; i++) {
            Item randomUpgrade = allUpgradesCopy.get(new Random().nextInt(allUpgradesCopy.size()));
            availableUpgrades.add(randomUpgrade);
            allUpgradesCopy.remove(randomUpgrade);  // remove the selected upgrade so it won't be selected again
        }
        return availableUpgrades;
    }

    private void updateXpLabel(){
        xpLabel.setText("XP: " + player.getExperience() + "/" + player.getXpNeeded());
    }
    private void updatePlayerLevelLabel() {
        playerLevelLabel.setText("Player Level: " + player.getLevel());
    }
    public void initialize() {
        gamePane.setPrefSize(screenWidth, screenHeight);

        // Adjust the layout of the UI elements
        healthBarBackground.setLayoutX(screenWidth / 2 - 100);  // Center the health bar
        healthBarBackground.setLayoutY(screenHeight - 20);  // 20 pixels from the bottom
        healthBar.setLayoutX(healthBarBackground.getLayoutX());
        healthBar.setLayoutY(healthBarBackground.getLayoutY());
        playerLevelLabel.setLayoutX(screenWidth - 220);
        playerLevelLabel.setLayoutY(20);
        xpLabel.setLayoutX(screenWidth - 220);
        xpLabel.setLayoutY(50);
        waveLabel.setLayoutX(screenWidth - 220);
        waveLabel.setLayoutY(80);
        timerLabel.setLayoutX(screenWidth - 220);
        timerLabel.setLayoutY(110);

        try{
            allUpgrades = Arrays.asList(
                    new Item("Joker", "Increases critical strike chance by 10%", () -> player.setCritChance(player.getCritChance() + 0.1), 5, new Image(getClass().getResourceAsStream("/com/example/harlequin/img/card-joker.png"))),
                    new Item("Cloak", "Movement speed increased by 5%", () -> player.setMovementSpeed(player.getMovementSpeed() * 1.05), 5, new Image(getClass().getResourceAsStream("/com/example/harlequin/img/wing-cloak.png"))),
                    new Item("Holy Grail", "Strength increased by 10%", () -> player.setAttackPower(player.getAttackPower() * 1.10), 5, new Image(getClass().getResourceAsStream("/com/example/harlequin/img/holy-grail.png"))),
                    new Item("Mask", "Increase defense by 5%", () -> player.setDefense(player.getDefense() * 1.05), 5, new Image(getClass().getResourceAsStream("/com/example/harlequin/img/duality-mask.png"))),
                    new Item("Lobster", "Set critical damage multiplier to 2.2", () -> player.setCritMultiplier(player.getCritMultiplier() + 0.2), 5, new Image(getClass().getResourceAsStream("/com/example/harlequin/img/dead-eye.png")))
            );
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }

        player = new Player("Hero", 1, 100, 5, 15, 2, 50, 0.1, 2);
        ((Pane) healthBar.getParent()).getChildren().add(player.getSprite());
        player.getSprite().setTranslateX(screenWidth / 2);
        player.getSprite().setTranslateY(screenHeight / 2);

        Label healthLabel = new Label();
        healthLabel.setLayoutX(healthBar.getLayoutX() + 20); // adjust as needed
        healthLabel.setLayoutY(healthBar.getLayoutY() - 20); // adjust as needed
        healthLabel.setStyle("-fx-font-size: 18;");
        ((Pane) healthBar.getParent()).getChildren().add(healthLabel);

        healthBar.setUserData(healthLabel);

        updatePlayerState();

        gameStateManager = new GameStateManager(this);
        enemyAIController = new EnemyAIController(player, enemyMap, (Pane) player.getSprite().getParent(), gameStateManager.getCurrentWave(), screenWidth, screenHeight, this);

        Weapon coneWeapon = new Weapon("ConeTest", "Conetest", 10, new ConeAttackStrategy((Pane) player.getSprite().getParent(), player, enemyAIController), () -> player.setLevel(player.getLevel()), 6, new Image(getClass().getResourceAsStream("/com/example/harlequin/img/dead-eye.png")));
        Weapon projectileWeapon = new Weapon("ProjectileTest", "ProjectileTest", 10, new ProjectileAttackStrategy((Pane) player.getSprite().getParent(), player, enemyAIController), ()-> player.setLevel(player.getLevel()), 6, new Image(getClass().getResourceAsStream("/com/example/harlequin/img/dead-eye.png")));
        projectileWeapon.setCOOLDOWN_DURATION(50);
        player.setWeapons(Arrays.asList(coneWeapon, projectileWeapon));

        System.out.println("After Initialization: ");
        System.out.println("Player object: " + player);
        System.out.println("EnemyAIController object: " + enemyAIController);

        // Add the handler to the gamePane
        if (gamePane != null) {
            gamePane.addEventHandler(EnemyCollisionEvent.ENEMY_COLLIDED, collisionEvent -> {
                System.out.println("Damage from event: " + collisionEvent.getDamageDealt());
                System.out.println("Player object: " + player);
                System.out.println("EnemyAIController object: " + enemyAIController);
                if (player != null) {
                    System.out.println("Player health before taking damage: " + player.getHealth());
                    player.takeDamage(collisionEvent.getDamageDealt());
                    System.out.println("Player health after taking damage: " + player.getHealth());
                    updateHealthBar();
                    displayDamage(collisionEvent.getDamageDealt());
                }
                if (enemyAIController != null) {
                    enemyAIController.startAttackCooldown();
                }
                checkGameOver();
            });
        } else {
            System.out.println("gamePane is null.");
        }

        gameLoop.start();
        // Initialize wave-related labels and start the first wave
        updateWaveLabel();
        gameStateManager.startWave();
    }

    public void handleKeyPressed(KeyEvent event) {
        activeKeys.add(event.getCode());
    }
    public void handleKeyReleased(KeyEvent event) {
        activeKeys.remove(event.getCode());
    }

    public EnemyAIController getEnemyAIController() {
        return enemyAIController;
    }

    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }

    public Pane getGamePane() {
        return gamePane;
    }
}

