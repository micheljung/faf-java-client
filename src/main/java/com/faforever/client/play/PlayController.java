package com.faforever.client.play;

import com.faforever.client.coop.CoopController;
import com.faforever.client.fx.AbstractViewController;
import com.faforever.client.fx.JavaFxUtil;
import com.faforever.client.game.CustomGamesController;
import com.faforever.client.main.event.NavigateEvent;
import com.faforever.client.main.event.NavigationItem;
import com.faforever.client.main.event.Open1v1Event;
import com.faforever.client.main.event.OpenCoopEvent;
import com.faforever.client.main.event.OpenCustomGamesEvent;
import com.faforever.client.rankedmatch.Ladder1v1Controller;
import com.faforever.client.theme.UiService;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlayController extends AbstractViewController<Node> {
  public Node playRoot;
  private final UiService uiService;
  private final EventBus eventBus;
  public CustomGamesController customGamesController;
  public Ladder1v1Controller ladderController;
  public CoopController coopController;
  public ToggleButton customGamesButton;
  public ToggleButton ladderButton;
  public ToggleButton coopButton;
  public ToggleGroup playNavigation;
  public AnchorPane contentPane;
  private boolean isHandlingEvent;


  public PlayController(UiService uiService, EventBus eventBus) {
    this.uiService = uiService;
    this.eventBus = eventBus;
  }

  @Override
  public void initialize() {
    //eventBus.post(new OpenCustomGamesEvent());
    customGamesButton.setUserData(NavigationItem.CUSTOM_GAMES);
    ladderButton.setUserData(NavigationItem.RANKED_1V1);
    coopButton.setUserData(NavigationItem.COOP);
    eventBus.register(this);
    customGamesButton.setSelected(true);
    /*playRootTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (isHandlingEvent) {
        return;
      }

      if (newValue == customGamesTab) {
        eventBus.post(new OpenCustomGamesEvent());
      } else if (newValue == ladderTab) {
        eventBus.post(new Open1v1Event());
      } else if (newValue == coopTab) {
        eventBus.post(new OpenCoopEvent());
      }
    });*/
  }

  @Override
  protected void onDisplay(NavigateEvent navigateEvent) {



      if (Objects.equals(navigateEvent.getClass(), NavigateEvent.class)
          || navigateEvent instanceof OpenCustomGamesEvent) {

        customGamesController.display(navigateEvent);
      }
      if (navigateEvent instanceof Open1v1Event) {

      }
      if (navigateEvent instanceof OpenCoopEvent) {
        coopController = uiService.loadFxml("theme/play/coop/coop.fxml");
        coopController.display(navigateEvent);
      }



    Node node = ladderController.getRoot();
    ObservableList<Node> children = contentPane.getChildren();

    if (!children.contains(node)) {
      children.add(node);
      JavaFxUtil.setAnchors(node, 0d);
    }
    ladderButton.setSelected(true);

    //ladderController = uiService.loadFxml("theme/play/ranked_1v1.fxml");
    ladderController.display(navigateEvent);

  }

  @Override
  protected void onHide() {
/*    customGamesController.hide();
    ladderController.hide();
    coopController.hide();*/
  }

  @Override
  public Node getRoot() {
    return playRoot;
  }

  public void on1v1NavigateButtonClicked(ActionEvent actionEvent) {
    eventBus.post(new Open1v1Event());
  }

  public void onCustomNavigateButtonClicked(ActionEvent actionEvent) {
    eventBus.post(new OpenCustomGamesEvent());
  }

  public void onCoopNavigateButtonClicked(ActionEvent event) {
    Node node = ladderController.getRoot();
    ObservableList<Node> children = contentPane.getChildren();

    if (!children.contains(node)) {
      children.add(node);
      JavaFxUtil.setAnchors(node, 0d);
    }
    ladderButton.setSelected(true);

    //ladderController = uiService.loadFxml("theme/play/ranked_1v1.fxml");
    ladderController.display(new NavigateEvent((NavigationItem) ((Node) event.getSource()).getUserData()));
  }

  @Subscribe
  public void onOpen1v1Event(Open1v1Event open1v1Event) {


  }

}