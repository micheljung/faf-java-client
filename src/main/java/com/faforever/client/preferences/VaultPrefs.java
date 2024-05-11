package com.faforever.client.preferences;

import com.faforever.client.vault.search.SearchController.SortConfig;
import com.faforever.client.vault.search.SearchController.SortOrder;
import java.time.LocalDate;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class VaultPrefs {
  private final ObjectProperty<SortConfig> onlineReplaySortConfig = new SimpleObjectProperty<>(
      new SortConfig("startTime", SortOrder.DESC));
  private final ObjectProperty<SortConfig> mapSortConfig = new SimpleObjectProperty<>(
      new SortConfig("gamesPlayed", SortOrder.DESC));
  private final ObjectProperty<SortConfig> modVaultConfig = new SimpleObjectProperty<>(
      new SortConfig("latestVersion.createTime", SortOrder.DESC));
  private final MapProperty<String, String> savedReplayQueries = new SimpleMapProperty<>(
      FXCollections.observableHashMap());
  private final MapProperty<String, String> savedMapQueries = new SimpleMapProperty<>(
      FXCollections.observableHashMap());
  private final MapProperty<String, String> savedModQueries = new SimpleMapProperty<>(
      FXCollections.observableHashMap());
  private final StringProperty playerNameField = new SimpleStringProperty();
  private final StringProperty mapNameField = new SimpleStringProperty();
  private final StringProperty mapAuthorField = new SimpleStringProperty();
  private final StringProperty titleField = new SimpleStringProperty();
  private final StringProperty replayIDField = new SimpleStringProperty();
  private final ObjectProperty<ObservableList<String>> featuredModFilter = new SimpleObjectProperty<ObservableList<String>>(
    FXCollections.emptyObservableList());
  private final ObjectProperty<ObservableList<String>> leaderboardFilter = new SimpleObjectProperty<ObservableList<String>>(
    FXCollections.emptyObservableList());
  private final DoubleProperty ratingMin = new SimpleDoubleProperty();
  private final DoubleProperty ratingMax = new SimpleDoubleProperty();
  private final DoubleProperty averageReviewScoresMin = new SimpleDoubleProperty();
  private final DoubleProperty averageReviewScoresMax = new SimpleDoubleProperty();
  private final ObjectProperty<LocalDate> gameBeforeDate = new SimpleObjectProperty<LocalDate>();
  private final ObjectProperty<LocalDate> gameAfterDate = new SimpleObjectProperty<LocalDate>();
  private final DoubleProperty gameDurationMin = new SimpleDoubleProperty();
  private final DoubleProperty gameDurationMax = new SimpleDoubleProperty();
  private final BooleanProperty onlyRanked = new SimpleBooleanProperty();

  public SortConfig getOnlineReplaySortConfig() {
    return onlineReplaySortConfig.get();
  }

  public void setOnlineReplaySortConfig(SortConfig onlineReplaySortConfig) {
    this.onlineReplaySortConfig.set(onlineReplaySortConfig);
  }

  public ObjectProperty<SortConfig> onlineReplaySortConfigProperty() {
    return onlineReplaySortConfig;
  }

  public SortConfig getMapSortConfig() {
    return mapSortConfig.get();
  }

  public void setMapSortConfig(SortConfig mapSortConfig) {
    this.mapSortConfig.set(mapSortConfig);
  }

  public ObjectProperty<SortConfig> mapSortConfigProperty() {
    return mapSortConfig;
  }

  public SortConfig getModVaultConfig() {
    return modVaultConfig.get();
  }

  public void setModVaultConfig(SortConfig modVaultConfig) {
    this.modVaultConfig.set(modVaultConfig);
  }

  public ObjectProperty<SortConfig> modVaultConfigProperty() {
    return modVaultConfig;
  }

  public ObservableMap<String, String> getSavedReplayQueries() {
    return savedReplayQueries.get();
  }

  public void setSavedReplayQueries(ObservableMap<String, String> savedReplayQueries) {
    this.savedReplayQueries.set(savedReplayQueries);
  }

  public MapProperty<String, String> savedReplayQueriesProperty() {
    return savedReplayQueries;
  }

  public ObservableMap<String, String> getSavedMapQueries() {
    return savedMapQueries.get();
  }

  public void setSavedMapQueries(ObservableMap<String, String> savedMapQueries) {
    this.savedMapQueries.set(savedMapQueries);
  }

  public MapProperty<String, String> savedMapQueriesProperty() {
    return savedMapQueries;
  }

  public ObservableMap<String, String> getSavedModQueries() {
    return savedModQueries.get();
  }

  public void setSavedModQueries(ObservableMap<String, String> savedModQueries) {
    this.savedModQueries.set(savedModQueries);
  }

  public MapProperty<String, String> savedModQueriesProperty() {
    return savedModQueries;
  }

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

  public ObjectProperty<ObservableList<String>> featuredModFilterProperty() {
    return featuredModFilter;
  }

  public ObservableList<String> getLeaderboardFilter() {
    return leaderboardFilter.get();
  }

  public void setLeaderboardFilter(ObservableList<String> leaderboardFilter) {
    this.leaderboardFilter.set(leaderboardFilter);
  }

  public ObjectProperty<ObservableList<String>> leaderboardFilterProperty() {
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

  public LocalDate getGameBeforeDate() {
    return gameBeforeDate.get();
  }

  public void setGameBeforeDate(LocalDate gameBeforeDate) {
    this.gameBeforeDate.set(gameBeforeDate);
  }

  public ObjectProperty<LocalDate> gameBeforeDateProperty() {
    return gameBeforeDate;
  }

  public LocalDate getGameAfterDate() {
    return gameAfterDate.get();
  }

  public void setGameAfterDate(LocalDate gameAfterDate) {
    this.gameAfterDate.set(gameAfterDate);
  }

  public ObjectProperty<LocalDate> gameAfterDateProperty() {
    return gameAfterDate;
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
