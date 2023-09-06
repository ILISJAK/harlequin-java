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
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Screen;

import java.io.InputStream;
import java.util.*;

import javafx.scene.control.Label;
import javafx.util.Duration;

public class GameController {
    @FXML
    private Pane gamePane;
    @FXML
    private Rectangle healthBar, healthBarBackground, xpBar, xpBarBackground;
    @FXML
    private Label playerLevelLabel, xpLabel, waveLabel, timerLabel, healthLabel;

    private Player player;
    private EnemyAIController enemyAIController;
    private GameStateManager gameStateManager;
    private GameGraphics gameGraphics;

    private boolean gamePaused = false;
    private List<Item> allUpgrades;
    private VBox upgradePane;
    private Rectangle background;

    Font customFont;

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
            gameGraphics.update();
        }
    };

    private void pauseGame() {
        gameLoop.stop();
        gameStateManager.stopEnemySpawnTimeline();
        gamePaused = true;
        background = new Rectangle(SCREEN_WIDTH, SCREEN_HEIGHT, Color.BLACK);
        background.setOpacity(0.5);
        gamePane.getChildren().add(0, background);
    }
    private void resumeGame() {
        gameLoop.start();
        gameStateManager.resumeEnemySpawnTimeline();
        gamePaused = false;
        gamePane.getChildren().remove(background);
        gamePane.requestFocus();
    }

    private void displayUpgradeOptions() {
        List<Item> availableUpgrades = getAvailableUpgrades();

        upgradePane = new VBox(10);
        upgradePane.setAlignment(Pos.CENTER);
        upgradePane.setStyle("-fx-background-color: #B0C4DE; -fx-padding: 20; -fx-border-color: black; -fx-border-width: 2;");

        Label header = new Label("Level up!");
        header.setStyle("-fx-font-family: 'Pixeled'; -fx-font-size: 24;");
        upgradePane.getChildren().add(header);

        for (Item upgrade : availableUpgrades) {
            ImageView imageView = new ImageView(upgrade.getImage());
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);

            Label label = new Label(upgrade.getName() + "\n" + upgrade.getDescription());
            label.setStyle("-fx-font-family: 'Pixeled'; -fx-font-size: 20; -fx-padding: 10;");
            label.setAlignment(Pos.CENTER_LEFT);
            label.setPrefWidth(800);
            label.setMouseTransparent(true);

            HBox hbox = new HBox(10);
            hbox.setPrefWidth(upgradePane.getPrefWidth());
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setPadding(new Insets(5));
            VBox.setMargin(hbox, new Insets(5));
            hbox.getChildren().addAll(imageView, label);
            hbox.setStyle("-fx-background-color: #DCCBC7;");

            hbox.setOnMouseEntered(event -> {
                hbox.setStyle("-fx-background-color: #D3D3D3; -fx-border-color: darkgray; -fx-border-width: 2;");
                imageView.setEffect(new Glow(0.4));
            });
            hbox.setOnMouseExited(event -> {
                hbox.setStyle("-fx-background-color: #DCCBC7; -fx-border-color: black; -fx-border-width: 2;");
                imageView.setEffect(null);
            });

            hbox.setOnMouseClicked(event -> {
                upgrade.apply();
                resumeGame();
                gamePane.getChildren().remove(upgradePane);
                upgradePane.translateXProperty().unbind();
                upgradePane.translateYProperty().unbind();
            });

            upgradePane.getChildren().add(hbox);
        }

        Label footer = new Label("Increase your luck for a chance to get 4 upgrades.");
        footer.setStyle("-fx-font-family: 'Pixeled'; -fx-font-size: 24;");
        upgradePane.getChildren().add(footer);

        gamePane.getChildren().add(upgradePane);

        upgradePane.translateXProperty().bind(gamePane.widthProperty().subtract(upgradePane.widthProperty()).divide(2));
        upgradePane.translateYProperty().bind(gamePane.heightProperty().subtract(upgradePane.heightProperty()).divide(2));

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
        damageLabel.setFont(customFont);
        damageLabel.setStyle("-fx-text-fill: red;");
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
            gameOverLabel.setFont(customFont);
            gameOverLabel.setStyle("-fx-text-fill: red;");
            gameOverLabel.setAlignment(Pos.CENTER);
            gamePane.getChildren().add(gameOverLabel);

            gameOverLabel.translateXProperty().bind(gamePane.widthProperty().subtract(gameOverLabel.widthProperty()).divide(2));
            gameOverLabel.translateYProperty().bind(gamePane.heightProperty().subtract(gameOverLabel.heightProperty()).divide(2));

        }
    }

    public void updatePlayerState(){
        updateXpLabel();
        updatePlayerLevelLabel();
        updateXpBar();
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
    private void updateXpBar() {
        double percentageXp = (double) player.getExperience() / player.getXpNeeded();
        xpBar.setWidth(xpBarBackground.getWidth() * percentageXp);
    }

    private void updatePlayerLevelLabel() {
        playerLevelLabel.setText("Player Level: " + player.getLevel());
    }
    public void initialize() {
        try {
            if (gamePane == null) {
                System.err.println("gamePane is null.");
                return;
            }
            gamePane.setPrefSize(screenWidth, screenHeight);

            customFont = Font.loadFont(getClass().getResource("/com/example/harlequin/font/Pixeled.ttf").toExternalForm(), 24);

            if (customFont == null) {
                System.err.println("Custom font is null!");
            } else {
                playerLevelLabel.setFont(customFont);
                xpLabel.setFont(customFont);
                waveLabel.setFont(customFont);
                timerLabel.setFont(customFont);
            }

            // Adjust the layout of the UI elements
            xpBarBackground.setLayoutX(10);  // 10 pixels from the left
            xpBarBackground.setLayoutY(10);  // 10 pixels from the top
            xpBar.setLayoutX(xpBarBackground.getLayoutX());
            xpBar.setLayoutY(xpBarBackground.getLayoutY());
            healthBarBackground.setLayoutX(screenWidth / 2 - 100);  // Center the health bar
            healthBarBackground.setLayoutY(screenHeight - 20);  // 20 pixels from the bottom
            healthBar.setLayoutX(healthBarBackground.getLayoutX());
            healthBar.setLayoutY(healthBarBackground.getLayoutY());
            playerLevelLabel.setLayoutX(screenWidth - 420);
            playerLevelLabel.setLayoutY(20);
            xpLabel.setLayoutX(screenWidth - 420);
            xpLabel.setLayoutY(50);
            waveLabel.setLayoutX(screenWidth - 420);
            waveLabel.setLayoutY(80);
            timerLabel.setLayoutX(screenWidth - 420);
            timerLabel.setLayoutY(110);

            try {
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

            player = new Player("Hero", 1, 100, 5, 15, 2, 35, 0.1, 2);
            player.getSprite().setOpacity(0.1);
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
            EnemyDatabase enemyDatabase = EnemyDatabase.getInstance();
            enemyDatabase.addEnemyType("Tormented", new Enemy("Tormented", 20, 30, 22, 1, 1.5, 50, 0.1, 1.2, new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0018_Lucifer_Skeleton_Grunt_Pixel_Art/Left/Png/SkeletonWithSwordLefttRun.png").toExternalForm()), new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0018_Lucifer_Skeleton_Grunt_Pixel_Art/Right/Png/SkeletonWithSwordRightRun.png").toExternalForm())));
            enemyDatabase.addEnemyType("Acolyte", new Enemy("Acolyte", 20, 20, 25, 0, 1.65, 50, 0.1, 1.2, new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0013_Lucifer_Cultist_Pixel_Art/Left/Png/CultistLeftWalk.png").toExternalForm()), new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0013_Lucifer_Cultist_Pixel_Art/Right/Png/CultistRightWalk.png").toExternalForm())));
            enemyDatabase.addEnemyType("Scaled Behemoth", new Enemy("Scaled Behemoth", 20, 300, 23, 1, 1.4, 70, 0.1, 1.2, new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0016_Lucifer_Goblin_Beast_Pixel_Art/Left/Png/GoblinBeastLeftWalk.png").toExternalForm()), new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0016_Lucifer_Goblin_Beast_Pixel_Art/Right/Png/GoblinBeastRightWalk.png").toExternalForm())));
            enemyDatabase.addEnemyType("Mauler", new Enemy("Mauler", 20, 15, 28, 1, 1.85, 50, 0.1, 1.2, new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0015_Lucifer_Goblin_Berserker_Pixel_Art/Left/Png/GoblinLeftRun.png").toExternalForm()), new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0015_Lucifer_Goblin_Berserker_Pixel_Art/Right/Png/GoblinRightRun.png").toExternalForm())));
        //  enemyDatabase.addEnemyType("Chiroptera", new Enemy("Chiroptera", 20, 230, 27, 1, 1.65, 70, 0.1, 1.2, new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0017_Lucifer_Goblin_Rider_Pixel_Art/Left/Png/GoblinRiderLeftMove.png").toExternalForm()), new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0017_Lucifer_Goblin_Rider_Pixel_Art/Right/Png/GoblinRiderRightMove.png").toExternalForm()), 4, 320, 80));
            enemyDatabase.addEnemyType("Soulless", new Enemy("Soulless", 20, 35, 20, 1, 1.5, 50, 0.1, 1.2, new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0018_Lucifer_Skeleton_Grunt_Pixel_Art/Left/Png/SkeletonWithSwordLefttRun.png").toExternalForm()), new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0018_Lucifer_Skeleton_Grunt_Pixel_Art/Right/Png/SkeletonWithSwordRightRun.png").toExternalForm())));
        //  enemyDatabase.addEnemyType("Marrowsworn", new Enemy("Marrowsworn", 20, 260, 25, 1, 1.5, 70, 0.1, 1.2, new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0020_Lucifer_Skeleton_Ancient_Pixel_Art/Left/Png/AncientSkeletonLeftWalk.png").toExternalForm()), new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0020_Lucifer_Skeleton_Ancient_Pixel_Art/Right/Png/AncientSkeletonRightWalk.png").toExternalForm()), 8, 768, 96));
            enemyDatabase.addEnemyType("Squire", new Enemy("Squire", 20, 30, 19, 2, 1.3, 50, 0.1, 1.2, new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0009_Lucifer_Warrior_Pixel_Art/Left/Png/WarriorLeftWalk.png").toExternalForm()), new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0009_Lucifer_Warrior_Pixel_Art/Right/Png/WarriorRightWalk.png").toExternalForm())));
            enemyDatabase.addEnemyType("Fanatic", new Enemy("Fanatic", 20, 25, 23, 1, 1.65, 50, 0.1, 1.2, new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0010_Lucifer_Necromancer_Pixel_Art/Left/Png/NecromancerLeftRun.png").toExternalForm()), new Image(getClass().getResource("/com/example/harlequin/img/Foozle_2DC0010_Lucifer_Necromancer_Pixel_Art/Right/Png/NecromancerRightRun.png").toExternalForm())));

            Weapon coneWeapon = new Weapon("ConeTest", "Conetest", 10, new ConeAttackStrategy((Pane) player.getSprite().getParent(), player, enemyAIController), () -> player.setLevel(player.getLevel()), 6, new Image(getClass().getResourceAsStream("/com/example/harlequin/img/dead-eye.png")));
            Weapon projectileWeapon = new Weapon("ProjectileTest", "ProjectileTest", 10, new ProjectileAttackStrategy((Pane) player.getSprite().getParent(), player, enemyAIController), () -> player.setLevel(player.getLevel()), 6, new Image(getClass().getResourceAsStream("/com/example/harlequin/img/dead-eye.png")));
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

            initGameGraphics();
            gameLoop.start();
            // Initialize wave-related labels and start the first wave
            updateWaveLabel();
            gameStateManager.startWave();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initGameGraphics(){
        gameGraphics = new GameGraphics(screenWidth, screenHeight, this);
    }

    public void handleKeyPressed(KeyEvent event) {
        activeKeys.add(event.getCode());

        // Update player animation based on movement direction
        boolean moving = true;
        boolean facingLeft = false;

        if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.S || event.getCode() == KeyCode.A || event.getCode() == KeyCode.D) {
            if (activeKeys.contains(KeyCode.A)) {
                facingLeft = true;
            }
        } else {
            moving = false;
        }

        gameGraphics.updatePlayerAnimation(moving, facingLeft);
    }
    public void handleKeyReleased(KeyEvent event) {
        activeKeys.remove(event.getCode());

        // Update player animation based on movement direction
        boolean moving = !activeKeys.isEmpty();
        boolean facingLeft = false;

        if (moving && activeKeys.contains(KeyCode.A)) {
            facingLeft = true;
        }

        gameGraphics.updatePlayerAnimation(moving, facingLeft);
    }

    public EnemyAIController getEnemyAIController() {
        return enemyAIController;
    }

    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }

    public GameGraphics getGameGraphics() {
        return gameGraphics;
    }

    public Pane getGamePane() {
        return gamePane;
    }

    public Player getPlayer() {
        return player;
    }

    public static double getScreenHeight() {
        return SCREEN_HEIGHT;
    }

    public static double getScreenWidth() {
        return SCREEN_WIDTH;
    }
}
