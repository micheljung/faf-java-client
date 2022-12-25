package com.faforever.client.game;

import com.faforever.client.fa.FaStrings;
import com.faforever.client.fx.Controller;
import com.faforever.client.fx.JavaFxUtil;
import com.faforever.client.fx.contextmenu.ContextMenuBuilder;
import com.faforever.client.fx.contextmenu.CopyLabelMenuItem;
import com.faforever.client.i18n.I18n;
import com.faforever.client.map.MapService;
import com.faforever.client.map.MapService.PreviewSize;
import com.faforever.client.map.MapSize;
import com.faforever.client.mod.ModManagerController;
import com.faforever.client.mod.ModService;
import com.faforever.client.notification.NotificationService;
import com.faforever.client.preferences.PreferencesService;
import com.faforever.client.theme.UiService;
import com.faforever.client.user.UserService;
import com.faforever.client.util.PopupUtil;
import com.google.common.base.Strings;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

import static javafx.scene.layout.BackgroundPosition.CENTER;
import static javafx.scene.layout.BackgroundRepeat.NO_REPEAT;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractCreateGameController implements Controller<GridPane> {

  public static final PseudoClass PSEUDO_CLASS_INVALID = PseudoClass.getPseudoClass("invalid");
  private static final int MAX_RATING_LENGTH = 4;

  protected final ApplicationContext applicationContext;
  protected final UiService uiService;
  protected final MapService mapService;
  protected final ModService modService;
  protected final GameService gameService;
  protected final NotificationService notificationService;
  protected final ContextMenuBuilder contextMenuBuilder;
  protected final PreferencesService preferencesService;
  protected final UserService userService;
  protected final I18n i18n;
  private final ObservableList<String> warningList = FXCollections.observableArrayList();
  public GridPane root;
  public VBox gameDataVBox;
  public TextField titleTextField;
  public TextField passwordTextField;
  public CheckBox onlyForFriendsCheckBox;
  public TextField minRankingTextField;
  public TextField maxRankingTextField;
  public CheckBox enforceRankingCheckBox;
  public ModManagerController modManagerController;
  public VBox rankingContainer;
  public StackPane mapListContainer;
  public StackPane featuredModListContainer;
  public Pane mapPreviewPane;
  public Label mapNameLabel;
  public Label mapSizeLabel;
  public Label mapPlayersLabel;
  public Label versionLabel;
  public Label mapDescriptionLabel;
  public CheckBox offlineModeCheckbox;
  public VBox warningsContainer;
  public Button createGameButton;
  private Runnable onCloseControllerRequest;

  @Override
  public void initialize() {
    setUpViews();

    mapListContainer.getChildren().setAll(getMapListContainer());
    featuredModListContainer.getChildren().setAll(getFeaturedModListContainer());

    setUpListeners();
  }

  protected void setUpViews() {
    contextMenuBuilder.addCopyLabelContextMenu(mapDescriptionLabel);
    modManagerController.setCloseable(false);
    JavaFxUtil.bindManagedToVisible(rankingContainer, mapSizeLabel, mapPlayersLabel, versionLabel);
    JavaFxUtil.bind(mapSizeLabel.visibleProperty(), mapSizeLabel.textProperty().isNotEmpty());
    JavaFxUtil.bind(mapPlayersLabel.visibleProperty(), mapPlayersLabel.textProperty().isNotEmpty());
    JavaFxUtil.bind(versionLabel.visibleProperty(), versionLabel.textProperty().isNotEmpty());
    JavaFxUtil.bind(mapPreviewPane.prefHeightProperty(), mapPreviewPane.widthProperty());
    JavaFxUtil.bind(gameDataVBox.disableProperty(), offlineModeCheckbox.selectedProperty());
    JavaFxUtil.bind(warningsContainer.visibleProperty(), offlineModeCheckbox.selectedProperty().not());
    JavaFxUtil.makeNumericTextField(minRankingTextField, MAX_RATING_LENGTH, true);
    JavaFxUtil.makeNumericTextField(maxRankingTextField, MAX_RATING_LENGTH, true);
  }

  private void setUpListeners() {
    JavaFxUtil.addListener(offlineModeCheckbox.selectedProperty(), observable -> updateWarnings());
    JavaFxUtil.addAndTriggerListener(warningList, (InvalidationListener) observable -> updateWarnings());
    JavaFxUtil.addAndTriggerListener(userService.connectionStateProperty(), observable -> {
      switch (userService.getConnectionState()) {
        case DISCONNECTED -> {
          setWarning("game.create.disconnected", true);
          setWarning("game.create.connecting", false);
        }
        case CONNECTING -> {
          setWarning("game.create.disconnected", false);
          setWarning("game.create.connecting", true);
        }
        case CONNECTED -> {
          setWarning("game.create.disconnected", false);
          setWarning("game.create.connecting", false);
        }
      }
    });
    JavaFxUtil.addAndTriggerListener(titleTextField.textProperty(), (observable, oldValue, newValue) -> {
      boolean isBlank = StringUtils.isBlank(newValue);
      boolean isAscii = StandardCharsets.US_ASCII.newEncoder().canEncode(Strings.nullToEmpty(newValue));
      titleTextField.pseudoClassStateChanged(PSEUDO_CLASS_INVALID, isBlank || !isAscii);
      setWarning("game.create.titleMissing", isBlank);
      setWarning("game.create.titleNotAscii", !isAscii);
    });
    JavaFxUtil.addAndTriggerListener(passwordTextField.textProperty(), (observable, oldValue, newValue) ->
        setWarning("game.create.passwordNotAscii", !Strings.nullToEmpty(newValue)
            .isEmpty() && !StandardCharsets.US_ASCII.newEncoder().canEncode(newValue)));
  }

  private void updateWarnings() {
    JavaFxUtil.runLater(() -> {
      if (offlineModeCheckbox.isSelected()) {
        createGameButton.setDisable(false);
        warningsContainer.getChildren().clear();
      } else {
        boolean hasWarning = !warningList.isEmpty();
        createGameButton.setDisable(hasWarning);
        if (hasWarning) {
          warningsContainer.getChildren().setAll(warningList.stream().map(this::createWarning).collect(Collectors.toList()));
        } else {
          warningsContainer.getChildren().clear();
        }
      }
    });
  }

  private Label createWarning(String messageKey) {
    Region warningIcon = new Region();
    warningIcon.getStyleClass().addAll("icon", "warn-icon");
    return new Label(i18n.get(messageKey), warningIcon);
  }

  protected void setWarning(String messageKey, boolean warning) {
    if (warning) {
      if (!warningList.contains(messageKey)) {
        warningList.add(messageKey);
      }
    } else {
      warningList.remove(messageKey);
    }
  }

  public abstract Node getMapListContainer();

  public abstract Node getFeaturedModListContainer();

  public void onCreateGameButtonClicked() {
    if (true) {
      notificationService.addPersistentErrorNotification(new RuntimeException(), "coop.unknownMission");
    }
    closeController();
    if (offlineModeCheckbox.isSelected()) {
      startOfflineGame();
    } else {
      startGame();
    }
  }

  protected abstract void startGame();

  protected abstract void startOfflineGame();

  private void closeController() {
    if (onCloseControllerRequest != null) {
      onCloseControllerRequest.run();
    }
  }

  public void setOnCloseControllerRequest(Runnable onCloseControllerRequest) {
    this.onCloseControllerRequest = onCloseControllerRequest;
  }

  protected void setMapDetail(String mapFolderName, MapSize mapSize, String displayName, String description, Integer maxPlayers, String version) {
    JavaFxUtil.runLater(() -> {
      setMapPreview(mapFolderName);
      mapSizeLabel.setText(mapSize != null ? i18n.get("mapPreview.size", mapSize.getWidthInKm(), mapSize.getHeightInKm()) : null);
      mapNameLabel.setText(displayName);
      mapPlayersLabel.setText(maxPlayers != null ? i18n.number(maxPlayers) : null);
      mapDescriptionLabel.setText(Optional.ofNullable(description)
          .map(Strings::emptyToNull)
          .map(FaStrings::removeLocalizationTag)
          .orElseGet(() -> i18n.get("map.noDescriptionAvailable")));
      versionLabel.setText(version != null ? i18n.get("versionFormat", version) : null);
    });
  }

  private void setMapPreview(String mapFolderName) {
    Image mapPreviewImage = mapService.loadPreview(mapFolderName, PreviewSize.LARGE);
    JavaFxUtil.addAndTriggerListener(mapPreviewImage.progressProperty(), (observable, oldPValue, newValue) -> {
      if (newValue.doubleValue() == 1.0d) {
        JavaFxUtil.runLater(() -> {
          Image map = mapPreviewImage.isError() ? uiService.getThemeImage(UiService.NO_IMAGE_AVAILABLE) : mapPreviewImage;
          mapPreviewPane.setUserData(map);
          mapPreviewPane.setBackground(new Background(new BackgroundImage(map, NO_REPEAT, NO_REPEAT, CENTER,
              new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false))));
        });
      }
    });
  }

  protected void clearMapDetail() {
    JavaFxUtil.runLater(() -> {
      mapPreviewPane.setBackground(null);
      mapSizeLabel.setText(null);
      mapNameLabel.setText(null);
      mapPlayersLabel.setText(null);
      mapDescriptionLabel.setText(null);
      versionLabel.setText(null);
    });
  }

  @Nullable
  protected Integer getMinRating() {
    return getRating(minRankingTextField);
  }

  @Nullable
  protected Integer getMaxRating() {
    return getRating(maxRankingTextField);
  }

  @Nullable
  private Integer getRating(TextField textField) {
    String rating = textField.getText();
    return rating.isEmpty() ? null : Integer.parseInt(rating);
  }

  public void onMapPreviewImageClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getButton() == MouseButton.PRIMARY) {
      Optional.ofNullable(((Image) mapPreviewPane.getUserData())).ifPresent(PopupUtil::showImagePopup);
    }
  }

  public void onMapNameLabelContextMenuRequest(ContextMenuEvent contextMenuEvent) {
    // No other way to make same design without using StackPane class therefore we use this dirty hack
    if (mapNameLabel.getBoundsInParent().contains(contextMenuEvent.getX(), contextMenuEvent.getY())) {
      contextMenuBuilder.newBuilder()
          .addItem(CopyLabelMenuItem.class, mapNameLabel)
          .build()
          .show(mapNameLabel, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
    }
  }

  @Override
  public GridPane getRoot() {
    return root;
  }
}
