package com.faforever.client.preferences;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;

public class ReplaySearchPrefs {
  private final StringProperty playerNameField = new SimpleStringProperty("");
  private final StringProperty mapNameField = new SimpleStringProperty("");
  private final StringProperty mapAuthorField = new SimpleStringProperty("");
  private final StringProperty titleField = new SimpleStringProperty("");
  private final StringProperty replayIDField = new SimpleStringProperty("");
  private final ListProperty<String> featuredModFilter = new SimpleListProperty<>(
    FXCollections.observableArrayList()
  );
  private final ListProperty<String> leaderboardFilter = new SimpleListProperty<>(
    FXCollections.observableArrayList());
  private final DoubleProperty ratingMin = new SimpleDoubleProperty();
  private final DoubleProperty ratingMax = new SimpleDoubleProperty();
  private final DoubleProperty averageReviewScoresMin = new SimpleDoubleProperty();
  private final DoubleProperty averageReviewScoresMax = new SimpleDoubleProperty();
  private final DoubleProperty gameDurationMin = new SimpleDoubleProperty();
  private final DoubleProperty gameDurationMax = new SimpleDoubleProperty();
  private final BooleanProperty onlyRanked = new SimpleBooleanProperty();

  public String getPlayerNameField() {
    return playerNameField.get();
  }

  public void setPlayerNameField(String playerName) {
    this.playerNameField.set(playerName);
  }

  public StringProperty playerNameFieldProperty() {
    return playerNameField;
  }

  public String getMapNameField() {
    return mapNameField.get();
  }

  public void setMapNameField(String mapName) {
    this.mapNameField.set(mapName);
  }

  public StringProperty mapNameFieldProperty() {
    return mapNameField;
  }

  public String getMapAuthorField() {
    return mapAuthorField.get();
  }

  public void setMapAuthorField(String mapAuthor) {
    this.mapAuthorField.set(mapAuthor);
  }

  public StringProperty mapAuthorFieldProperty() {
    return mapAuthorField;
  }

  public String getTitleField() {
    return titleField.get();
  }

  public void setTitleField(String titleField) {
    this.titleField.set(titleField);
  }

  public StringProperty titleFieldProperty() {
    return titleField;
  }

  public String getReplayIDField() {
    return replayIDField.get();
  }

  public void setReplayIDField(String replayIDField) {
    this.replayIDField.set(replayIDField);
  }

  public StringProperty replayIDFieldProperty() {
    return replayIDField;
  }

  public ObservableList<String> getFeaturedModFilter() {
    return featuredModFilter.get();
  }

  public void setFeaturedModFilter(ObservableList<String> featuredModFilter) {
    this.featuredModFilter.set(featuredModFilter);
  }

  public ListProperty<String> featuredModFilterProperty() {
    return featuredModFilter;
  }

  public ObservableList<String> getLeaderboardFilter() {
    return leaderboardFilter.get();
  }

  public void setLeaderboardFilter(ObservableList<String> leaderboardFilter) {
    this.leaderboardFilter.set(leaderboardFilter);
  }

  public ListProperty<String> leaderboardFilterProperty() {
    return leaderboardFilter;
  }

  public Double getRatingMin() {
    return ratingMin.get();
  }

  public void setRatingMin(Double ratingMin) {
    this.ratingMin.set(ratingMin);
  }

  public DoubleProperty ratingMinProperty() {
    return ratingMin;
  }

  public Double getRatingMax() {
    return ratingMax.get();
  }

  public void setRatingMax(Double ratingMax) {
    this.ratingMax.set(ratingMax);
  }

  public DoubleProperty ratingMaxProperty() {
    return ratingMax;
  }

  public Double getAverageReviewScoresMin() {
    return averageReviewScoresMin.get();
  }

  public void setAverageReviewScoresMin(Double averageReviewScoresMin) {
    this.averageReviewScoresMin.set(averageReviewScoresMin);
  }

  public DoubleProperty averageReviewScoresMinProperty() {
    return averageReviewScoresMin;
  }

  public Double getAverageReviewScoresMax() {
    return averageReviewScoresMax.get();
  }

  public void setAverageReviewScoresMax(Double averageReviewScoresMax) {
    this.averageReviewScoresMax.set(averageReviewScoresMax);
  }

  public DoubleProperty averageReviewScoresMaxProperty() {
    return averageReviewScoresMax;
  }

  public Double getGameDurationMin() {
    return gameDurationMin.get();
  }

  public void setGameDurationMin(Double gameDurationMin) {
    this.gameDurationMin.set(gameDurationMin);
  }

  public DoubleProperty gameDurationMinProperty() {
    return gameDurationMin;
  }

  public Double getGameDurationMax() {
    return gameDurationMax.get();
  }

  public void setGameDurationMax(Double gameDurationMax) {
    this.gameDurationMax.set(gameDurationMax);
  }

  public DoubleProperty gameDurationMaxProperty() {
    return gameDurationMax;
  }

  public Boolean getOnlyRanked() {
    return onlyRanked.get();
  }

  public void setOnlyRanked(Boolean onlyRanked) {
    this.onlyRanked.set(onlyRanked);
  }

  public BooleanProperty onlyRankedProperty() {
    return onlyRanked;
  }
}
