package com.faforever.client.game;


import com.faforever.client.fx.Controller;
import com.faforever.client.fx.JavaFxUtil;
import com.faforever.client.player.PlayerService;
import com.faforever.client.theme.UiService;
import com.google.common.base.Joiner;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
@RequiredArgsConstructor
public class GameTooltipController implements Controller<Node> {

  private final UiService uiService;
  private final PlayerService playerService;

  public TitledPane modsPane;
  public TilePane teamsPane;
  public Label modsLabel;
  public VBox gameTooltipRoot;
  private InvalidationListener teamInvalidationListener;
  private InvalidationListener simModsInvalidationListener;
  private int maxPrefColumns;
  private Game game;
  private boolean showMods;

  public void initialize() {
    JavaFxUtil.bindManagedToVisible(modsPane);
    modsPane.visibleProperty().bind(modsLabel.textProperty().isNotEmpty());
    maxPrefColumns = teamsPane.getPrefColumns();
    showMods = true;
  }

  public void setGame(Game game) {
    this.game = game;
  }

  public void displayGame() {
    resetListeners();
    if (game == null) {
      return;
    }
    WeakInvalidationListener weakTeamInvalidationListener = new WeakInvalidationListener(teamInvalidationListener);
    JavaFxUtil.addAndTriggerListener(game.teamsProperty(), weakTeamInvalidationListener);
    if (showMods) {
      WeakInvalidationListener weakModInvalidationListener = new WeakInvalidationListener(simModsInvalidationListener);
      JavaFxUtil.addAndTriggerListener(game.simModsProperty(), weakModInvalidationListener);
    } else {
      JavaFxUtil.runLater(() -> modsPane.setVisible(false));
    }
  }

  private void resetListeners() {
    teamInvalidationListener = change -> createTeams();
    simModsInvalidationListener = change -> createModsList(game.getSimMods());
  }

  private void createTeams() {
    if (game != null) {
      TeamCardController.createAndAdd(game, playerService, uiService, teamsPane);
      JavaFxUtil.runLater(() -> teamsPane.setPrefColumns(Math.min(game.getTeams().size(), maxPrefColumns)));
    }
  }

  private void createModsList(Map<? extends String, ? extends String> simMods) {
    String stringSimMods = Joiner.on(System.getProperty("line.separator")).join(simMods.values());
    JavaFxUtil.runLater(() -> modsLabel.setText(stringSimMods));
  }

  public void setShowMods(boolean showMods) {
    this.showMods = showMods;
  }

  public Node getRoot() {
    return gameTooltipRoot;
  }
}
