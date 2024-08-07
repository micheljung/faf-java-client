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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LiveReplaySearchPrefs {

  private final BooleanProperty hideModdedGames = new SimpleBooleanProperty(false);
  private final BooleanProperty hideSingleGames = new SimpleBooleanProperty(false);
  private final BooleanProperty onlyGamesWithFriends = new SimpleBooleanProperty(false);
  private final BooleanProperty onlyGeneratedMaps = new SimpleBooleanProperty(false);

  private final ListProperty<GameType> gameTypes = new SimpleListProperty<>(FXCollections.observableArrayList());
  private final ListProperty<FeaturedMod> modName = new SimpleListProperty<>(FXCollections.observableArrayList());

  private final StringProperty playerName = new SimpleStringProperty("");

}
