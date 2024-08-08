package com.faforever.client.preferences;

import com.faforever.client.domain.api.FeaturedMod;
import com.faforever.commons.lobby.GameType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class LiveReplaySearchPrefs {

  private final BooleanProperty hideModdedGames = new SimpleBooleanProperty(false);
  private final BooleanProperty hideSingleGames = new SimpleBooleanProperty(false);
  private final BooleanProperty onlyGamesWithFriends = new SimpleBooleanProperty(false);
  private final BooleanProperty onlyGeneratedMaps = new SimpleBooleanProperty(false);

  private final ListProperty<GameType> gameTypes = new SimpleListProperty<>(FXCollections.observableArrayList());
  private final ListProperty<FeaturedMod> modName = new SimpleListProperty<>(FXCollections.observableArrayList());

  private final StringProperty playerName = new SimpleStringProperty("");

  public boolean getHideModdedGames() {return hideModdedGames.get();}

  public void setHideModdedGames(Boolean hideModdedGames) {this.hideModdedGames.set(hideModdedGames);}

  public BooleanProperty hideModdedGamesProperty() {return hideModdedGames;}


  public boolean getHideSingleGames() {return hideSingleGames.get();}

  public void setHideSingleGames(Boolean hideSingleGames) {this.hideSingleGames.set(hideSingleGames);}

  public BooleanProperty hideSingleGamesProperty() {return hideSingleGames;}


  public boolean getOnlyGamesWithFriends() {return onlyGamesWithFriends.get();}

  public void setOnlyGamesWithFriends(Boolean onlyGamesWithFriends) {this.onlyGamesWithFriends.set(onlyGamesWithFriends);}

  public BooleanProperty onlyGamesWithFriendsProperty() {return onlyGamesWithFriends;}


  public boolean getOnlyGeneratedMaps() {return onlyGeneratedMaps.get();}

  public void setOnlyGeneratedMaps(Boolean onlyGeneratedMaps) {this.onlyGeneratedMaps.set(onlyGeneratedMaps);}

  public BooleanProperty onlyGeneratedMapsProperty() {return onlyGeneratedMaps;}


  public ObservableList<GameType> getGameTypes() {return gameTypes.get();}

  public void setGameTypes(ObservableList<GameType> gameTypes) {this.gameTypes.set(gameTypes);}

  public ListProperty<GameType> gameTypesProperty() {return gameTypes;}


  public ObservableList<FeaturedMod> getModName() {return modName.get();}

  public void setModName(ObservableList<FeaturedMod> modName) {this.modName.set(modName);}

  public ListProperty<FeaturedMod> modNameProperty() {return modName;}


  public String getPlayerName() {return playerName.get();}

  public void setPlayerName(String playerName) {this.playerName.set(playerName);}

  public StringProperty playerNameProperty() {return playerName;}
}
