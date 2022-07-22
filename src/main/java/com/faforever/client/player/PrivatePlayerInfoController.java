package com.faforever.client.player;

import com.faforever.client.achievements.AchievementService;
import com.faforever.client.chat.ChatChannelUser;
import com.faforever.client.domain.GameBean;
import com.faforever.client.domain.LeaderboardRatingBean;
import com.faforever.client.domain.PlayerBean;
import com.faforever.client.fx.Controller;
import com.faforever.client.fx.JavaFxUtil;
import com.faforever.client.game.GameDetailController;
import com.faforever.client.i18n.I18n;
import com.faforever.client.leaderboard.LeaderboardService;
import com.faforever.client.util.Assert;
import com.faforever.client.util.IdenticonUtil;
import com.faforever.client.util.RatingUtil;
import com.faforever.client.util.TimeService;
import com.faforever.commons.api.dto.AchievementState;
import com.faforever.commons.lobby.GameStatus;
import com.google.common.annotations.VisibleForTesting;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor
public class PrivatePlayerInfoController implements Controller<Node> {

  private final I18n i18n;
  private final AchievementService achievementService;
  private final LeaderboardService leaderboardService;
  private final TimeService timeService;

  public ImageView userImageView;
  public Label usernameLabel;
  public ImageView countryImageView;
  public Label countryLabel;
  public Label ratingsLabels;
  public Label ratingsValues;
  public Label gamesPlayedLabel;
  public GameDetailController gameDetailController;
  public Pane gameDetailWrapper;
  public Label unlockedAchievementsLabel;
  public Node privateUserInfoRoot;
  public Label gamesPlayedLabelLabel;
  public Label unlockedAchievementsLabelLabel;
  public Label playtimeLabel;
  public Label playtimeValueLabel;
  public Separator separator;

  private ChatChannelUser chatUser;
  private InvalidationListener gameInvalidationListener;
  private InvalidationListener gameStatusInvalidationListener;
  private InvalidationListener chatUserPropertiesInvalidationListener;
  private InvalidationListener ratingInvalidationListener;
  private Timeline playTimeTimeline;

  @Override
  public Node getRoot() {
    return privateUserInfoRoot;
  }

  public void initialize() {
    JavaFxUtil.bindManagedToVisible(gameDetailWrapper, countryLabel, gamesPlayedLabel, unlockedAchievementsLabel,
        ratingsLabels, ratingsValues, gamesPlayedLabelLabel, unlockedAchievementsLabelLabel, playtimeLabel,
        playtimeValueLabel, separator);
    JavaFxUtil.bind(separator.visibleProperty(), gameDetailWrapper.visibleProperty());
  }

  private void initializePlayerListeners() {
    ratingInvalidationListener = observable -> chatUser.getPlayer().ifPresent(this::loadReceiverRatingInformation);
    gameInvalidationListener = observable -> chatUser.getPlayer().ifPresent(chatPlayer -> onPlayerGameChanged(chatPlayer.getGame()));
  }

  private void initializeChatUserListeners() {
    chatUserPropertiesInvalidationListener = observable -> JavaFxUtil.runLater(() -> {
      usernameLabel.setText(this.chatUser.getUsername());
      countryImageView.setImage(this.chatUser.getCountryFlag().orElse(null));
      countryLabel.setText(this.chatUser.getCountryName().orElse(""));
    });
  }

  public void setChatUser(@NotNull ChatChannelUser chatUser) {
    Assert.checkNotNullIllegalState(this.chatUser, "Chat user is already set");
    initializeChatUserListeners();
    this.chatUser = chatUser;
    this.chatUser.setDisplayed(true);
    JavaFxUtil.addAndTriggerListener(this.chatUser.playerProperty(), (observable) -> displayPlayerInfo());
    JavaFxUtil.addAndTriggerListener(this.chatUser.usernameProperty(), new WeakInvalidationListener(chatUserPropertiesInvalidationListener));
    JavaFxUtil.addListener(this.chatUser.countryFlagProperty(), new WeakInvalidationListener(chatUserPropertiesInvalidationListener));
    JavaFxUtil.addListener(this.chatUser.countryNameProperty(), new WeakInvalidationListener(chatUserPropertiesInvalidationListener));
  }

  private void displayChatUserInfo() {
    onPlayerGameChanged(null);
    setPlayerInfoVisible(false);
  }

  private void setPlayerInfoVisible(boolean visible) {
    userImageView.setVisible(visible);
    countryLabel.setVisible(visible);
    ratingsLabels.setVisible(visible);
    ratingsValues.setVisible(visible);
    gamesPlayedLabel.setVisible(visible);
    gamesPlayedLabelLabel.setVisible(visible);
    unlockedAchievementsLabel.setVisible(visible);
    unlockedAchievementsLabelLabel.setVisible(visible);
  }

  private void displayPlayerInfo() {
    initializePlayerListeners();
    chatUser.getPlayer().ifPresentOrElse(player -> {
      setPlayerInfoVisible(true);

      userImageView.setImage(IdenticonUtil.createIdenticon(player.getId()));
      userImageView.setVisible(true);

      JavaFxUtil.addAndTriggerListener(player.getLeaderboardRatings(), new WeakInvalidationListener(ratingInvalidationListener));
      JavaFxUtil.addAndTriggerListener(player.gameProperty(), new WeakInvalidationListener(gameInvalidationListener));

      populateUnlockedAchievementsLabel(player);
    }, this::displayChatUserInfo);
  }

  private void populateUnlockedAchievementsLabel(PlayerBean player) {
    achievementService.getAchievementDefinitions()
        .thenApply(achievementDefinitions -> {
          int totalAchievements = achievementDefinitions.size();
          return achievementService.getPlayerAchievements(player.getId())
              .thenAccept(playerAchievements -> {
                long unlockedAchievements = playerAchievements.stream()
                    .filter(playerAchievement -> playerAchievement.getState() == AchievementState.UNLOCKED)
                    .count();

                JavaFxUtil.runLater(() -> unlockedAchievementsLabel.setText(
                    i18n.get("chat.privateMessage.achievements.unlockedFormat", unlockedAchievements, totalAchievements))
                );
              })
              .exceptionally(throwable -> {
                log.error("Could not load achievements for player '" + player.getId(), throwable);
                return null;
              });
        });
  }

  private void onPlayerGameChanged(GameBean game) {
    gameDetailController.setGame(game);
    gameDetailWrapper.setVisible(game != null && game.getStatus() != GameStatus.CLOSED);
    if (game != null) {
      gameStatusInvalidationListener = observable -> calculatePlaytime(game);
      JavaFxUtil.addAndTriggerListener(game.statusProperty(), new WeakInvalidationListener(gameStatusInvalidationListener));
    }
  }

  private void calculatePlaytime(GameBean game) {
    if (game == null || game.getStatus() != GameStatus.PLAYING) {
      clearAndHidePlaytime();
    } else {
      playTimeTimeline = new Timeline(new KeyFrame(Duration.ZERO, event -> {
        if (game.getStatus() == GameStatus.PLAYING && game.getStartTime() != null) {
          updatePlaytimeValue(game.getStartTime());
        }
      }), new KeyFrame(Duration.seconds(1)));
      playTimeTimeline.setCycleCount(Timeline.INDEFINITE);
      playTimeTimeline.play();
      JavaFxUtil.runLater(() -> {
        playtimeLabel.setVisible(true);
        playtimeValueLabel.setVisible(true);
      });
    }
  }

  private void clearAndHidePlaytime() {
    if (playTimeTimeline != null) {
      playTimeTimeline.stop();
    }
    JavaFxUtil.runLater(() -> {
      playtimeLabel.setVisible(false);
      playtimeValueLabel.setVisible(false);
    });
  }

  private void updatePlaytimeValue(OffsetDateTime gameStartTime) {
    JavaFxUtil.runLater(() -> playtimeValueLabel.setText(timeService.shortDuration(java.time.Duration.between(gameStartTime, OffsetDateTime.now()))));
  }

  public void dispose() {
    if (playTimeTimeline != null) {
      playTimeTimeline.stop();
    }
  }

  private void loadReceiverRatingInformation(PlayerBean player) {
    leaderboardService.getLeaderboards().thenAccept(leaderboards -> {
      StringBuilder ratingNames = new StringBuilder();
      StringBuilder ratingNumbers = new StringBuilder();
      leaderboards.forEach(leaderboard -> {
        LeaderboardRatingBean leaderboardRating = player.getLeaderboardRatings().get(leaderboard.getTechnicalName());
        if (leaderboardRating != null) {
          String leaderboardName = i18n.getOrDefault(leaderboard.getTechnicalName(), leaderboard.getNameKey());
          ratingNames.append(i18n.get("leaderboard.rating", leaderboardName)).append("\n\n");
          ratingNumbers.append(i18n.number(RatingUtil.getLeaderboardRating(player, leaderboard))).append("\n\n");
        }
      });
      JavaFxUtil.runLater(() -> {
        ratingsLabels.setText(ratingNames.toString());
        ratingsValues.setText(ratingNumbers.toString());
        gamesPlayedLabel.setText(i18n.number(player.getNumberOfGames()));
      });
    });
  }

  @VisibleForTesting
  protected Timeline getPlayTimeTimeline() {
    return playTimeTimeline;
  }
}

