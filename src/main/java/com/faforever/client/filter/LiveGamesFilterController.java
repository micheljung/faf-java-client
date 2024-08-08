package com.faforever.client.filter;

import com.faforever.client.domain.api.FeaturedMod;
import com.faforever.client.domain.server.GameInfo;
import com.faforever.client.domain.server.PlayerInfo;
import com.faforever.client.featuredmod.FeaturedModService;
import com.faforever.client.filter.function.FeaturedModFilterFunction;
import com.faforever.client.filter.function.SimModsFilterFunction;
import com.faforever.client.fx.FxApplicationThreadExecutor;
import com.faforever.client.fx.ToStringOnlyConverter;
import com.faforever.client.i18n.I18n;
import com.faforever.client.map.generator.MapGeneratorService;
import com.faforever.client.player.PlayerService;
import com.faforever.client.preferences.LiveReplaySearchPrefs;
import com.faforever.client.social.SocialService;
import com.faforever.client.theme.UiService;
import com.faforever.commons.lobby.GameType;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LiveGamesFilterController extends AbstractFilterController<GameInfo> {

  private final SocialService socialService;
  private final PlayerService playerService;
  private final FeaturedModService featuredModService;
  private final MapGeneratorService mapGeneratorService;

  private final LiveReplaySearchPrefs liveReplaySearchPrefs;

  private FilterCheckboxController<GameInfo> hideModdedGamesFilter;
  private FilterCheckboxController<GameInfo> hideSingleGamesFilter;
  private FilterCheckboxController<GameInfo> onlyGamesWithFriendsFilter;
  private FilterCheckboxController<GameInfo> onlyGeneratedMapsFilter;

  private FilterMultiCheckboxController<GameType,GameInfo> gameTypeFilter;
  private FilterMultiCheckboxController<FeaturedMod, GameInfo> featuredModFilter;

  private FilterTextFieldController<GameInfo> playerFilter;


  public LiveGamesFilterController(UiService uiService, I18n i18n, FeaturedModService featuredModService,
                                   PlayerService playerService,
                                   MapGeneratorService mapGeneratorService,
                                   FxApplicationThreadExecutor fxApplicationThreadExecutor,
                                   SocialService socialService,
                                   LiveReplaySearchPrefs liveReplaySearchPrefs) {
    super(uiService, i18n, fxApplicationThreadExecutor);
    this.featuredModService = featuredModService;
    this.playerService = playerService;
    this.mapGeneratorService = mapGeneratorService;
    this.socialService = socialService;
    this.liveReplaySearchPrefs = liveReplaySearchPrefs;
  }

  @Override
  protected void build(FilterBuilder<GameInfo> filterBuilder) {
    hideModdedGamesFilter = filterBuilder.checkbox(i18n.get("moddedGames"), new SimModsFilterFunction());

    hideSingleGamesFilter = filterBuilder.checkbox(i18n.get("hideSingleGames"),
                                                   (selected, game) -> !selected || game.getNumActivePlayers() != 1);

    onlyGamesWithFriendsFilter = filterBuilder.checkbox(i18n.get("showGamesWithFriends"),
                                                        (selected, game) -> !selected || socialService.areFriendsInGame(
                                                        game));

    onlyGeneratedMapsFilter = filterBuilder.checkbox(i18n.get("showGeneratedMaps"),
                                                     (selected, game) -> !selected || mapGeneratorService.isGeneratedMap(
                                                   game.getMapFolderName()));

    gameTypeFilter = filterBuilder.multiCheckbox(i18n.get("gameType"), List.of(GameType.CUSTOM, GameType.MATCHMAKER, GameType.COOP), gameTypeConverter,
        (selectedGameTypes, game) -> selectedGameTypes.isEmpty() || selectedGameTypes.contains(game.getGameType()));

    featuredModFilter = filterBuilder.multiCheckbox(
        i18n.get("featuredMod.displayName"), new ToStringOnlyConverter<>(FeaturedMod::displayName),
        new FeaturedModFilterFunction());

    featuredModService.getFeaturedMods().collectList().subscribe(featuredModFilter::setItems);

    playerFilter = filterBuilder.textField(i18n.get("game.player.username"), (text, game) -> text.isEmpty() || game.getTeams()
        .values()
        .stream()
        .flatMap(Collection::stream)
        .map(playerService::getPlayerByIdIfOnline)
        .flatMap(Optional::stream).map(PlayerInfo::getUsername)
        .anyMatch(name -> StringUtils.containsIgnoreCase(name, text)));
  }

  private final StringConverter<GameType> gameTypeConverter = new StringConverter<>() {
    @Override
    public String toString(GameType object) {
      return i18n.get(switch (object) {
        case CUSTOM -> "customGame";
        case MATCHMAKER -> "matchmaker";
        case COOP -> "coopGame";
        default -> throw new IllegalArgumentException(object + " should not be used");
      });
    }

    @Override
    public GameType fromString(String string) {
      throw new UnsupportedOperationException("Not supported");
    }
  };

  @Override
  protected void afterBuilt() {
    hideModdedGamesFilter.valueProperty().bindBidirectional(liveReplaySearchPrefs.hideModdedGamesProperty());
    hideSingleGamesFilter.valueProperty().bindBidirectional(liveReplaySearchPrefs.hideSingleGamesProperty());
    onlyGamesWithFriendsFilter.valueProperty().bindBidirectional(liveReplaySearchPrefs.onlyGamesWithFriendsProperty());
    onlyGeneratedMapsFilter.valueProperty().bindBidirectional(liveReplaySearchPrefs.onlyGeneratedMapsProperty());
    gameTypeFilter.valueProperty().bindBidirectional(liveReplaySearchPrefs.gameTypesProperty());
    featuredModFilter.valueProperty().bindBidirectional(liveReplaySearchPrefs.modNameProperty());
    playerFilter.valueProperty().bindBidirectional(liveReplaySearchPrefs.playerNameProperty());
  }

}
