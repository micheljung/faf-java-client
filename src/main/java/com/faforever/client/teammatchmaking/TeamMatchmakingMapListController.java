package com.faforever.client.teammatchmaking;

import com.faforever.client.domain.api.MapVersion;
import com.faforever.client.domain.api.MatchmakerQueueMapPool;
import com.faforever.client.domain.server.MatchmakerQueueInfo;
import com.faforever.client.fx.FxApplicationThreadExecutor;
import com.faforever.client.fx.JavaFxUtil;
import com.faforever.client.fx.NodeController;
import com.faforever.client.map.MapService;
import com.faforever.client.player.PlayerService;
import com.faforever.client.theme.UiService;
import com.faforever.client.util.RatingUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor

public class TeamMatchmakingMapListController extends NodeController<Pane> {

  private static final int TILE_SIZE = 125;
  private static final int PADDING = 20;

  private static final Comparator<MapVersion> MAP_VERSION_COMPARATOR = Comparator.nullsFirst(
                                                                                     Comparator.comparing(MapVersion::size))
                                                                                 .thenComparing(
                                                                                     mapVersion -> mapVersion.map()
                                                                                                             .displayName(),
                                                                                     String.CASE_INSENSITIVE_ORDER);

  private static final Comparator<MatchmakerQueueMapPool> MAP_POOL_COMPARATOR = Comparator.comparing(
                                                                                              MatchmakerQueueMapPool::minRating, Comparator.nullsFirst(Double::compare))
                                                                                          .thenComparing(
                                                                                              MatchmakerQueueMapPool::maxRating,
                                                                                              Comparator.nullsLast(
                                                                                                  Double::compare));


  private final MapService mapService;
  private final UiService uiService;
  private final PlayerService playerService;
  private final FxApplicationThreadExecutor fxApplicationThreadExecutor;

  private final DoubleProperty maxWidth = new SimpleDoubleProperty(0);
  private final DoubleProperty maxHeight = new SimpleDoubleProperty(0);
  private final ObjectProperty<Map<MatchmakerQueueMapPool, List<MapVersion>>> brackets = new SimpleObjectProperty<>(
      Map.of());
  private final IntegerProperty playerRating = new SimpleIntegerProperty();
  private final ObjectProperty<MatchmakerQueueInfo> queue = new SimpleObjectProperty<>();

  private final ObservableValue<List<MatchmakerQueueMapPool>> sortedMapPools = brackets.map(this::getSortedMapPools)
                                                                                       .orElse(List.of());
  private final ObservableValue<List<MapVersion>> sortedMaps = brackets.map(this::getSortedMaps).orElse(List.of());
  private final ObservableValue<Integer> playerBracketIndex = Bindings.createObjectBinding(this::calculateBracketIndex,
                                                                                           sortedMapPools,
                                                                                           playerRating);

  public Pane root;
  public TilePane tilesContainer;
  public ScrollPane scrollContainer;
  public VBox loadingPane;

  @Override
  protected void onInitialize() {
    this.bindProperties();
  }

  private void bindProperties() {
    JavaFxUtil.bindManagedToVisible(loadingPane);
    tilesContainer.getChildren().subscribe(()->this.loadingPane.setVisible(false));

    this.queue.when(showing).subscribe(value -> {
      if (value == null) {
        return;
      }
      loadingPane.setVisible(true);
      mapService.getMatchmakerBrackets(value).subscribe(this.brackets::set);
    });

    playerRating.bind(playerService.currentPlayerProperty()
                                   .flatMap(player -> queue.flatMap(MatchmakerQueueInfo::leaderboardProperty)
                                                           .map(leaderboard -> RatingUtil.getLeaderboardRating(player,
                                                                                                               leaderboard)))
                                   .orElse(0)
                                   .when(showing));

    this.sortedMaps.when(showing).subscribe(this::updateContent);
    this.playerBracketIndex.when(showing).subscribe(this::updateContent);
  }

  @Override
  protected void onShow() {
    addShownSubscription(this.maxWidth.subscribe(this::resizeToContent));
    addShownSubscription(this.maxHeight.subscribe(this::resizeToContent));
  }

  @Override
  public Pane getRoot() {
    return root;
  }

  public double getMaxWidth() {
    return this.maxWidth.get();
  }

  public void setMaxWidth(double value) {
    this.maxWidth.set(value);
  }

  public DoubleProperty maxWidthProperty() {
    return this.maxWidth;
  }

  public double getMaxHeight() {
    return this.maxHeight.get();
  }

  public void setMaxHeight(double value) {
    this.maxHeight.set(value);
  }

  public DoubleProperty maxHeightProperty() {
    return this.maxHeight;
  }

  public MatchmakerQueueInfo getQueue() {
    return this.queue.get();
  }

  public void setQueue(MatchmakerQueueInfo queue) {
    this.queue.set(queue);
  }

  public ObjectProperty<MatchmakerQueueInfo> queueProperty() {
    return this.queue;
  }

  private List<MatchmakerQueueMapPool> getSortedMapPools(Map<MatchmakerQueueMapPool, List<MapVersion>> brackets) {
    return brackets.keySet().stream().sorted(MAP_POOL_COMPARATOR).toList();
  }

  private List<MapVersion> getSortedMaps(Map<MatchmakerQueueMapPool, List<MapVersion>> map) {
    return map.entrySet()
              .stream()
              .sorted(Entry.comparingByKey(MAP_POOL_COMPARATOR))
              .flatMap(entry -> entry.getValue().stream().sorted(MAP_VERSION_COMPARATOR))
              .distinct()
              .toList();
  }

  private Integer calculateBracketIndex() {
    int rating = playerRating.get();
    List<MatchmakerQueueMapPool> pools = this.sortedMapPools.getValue();
    return pools.stream()
                .filter(pool -> pool.minRating() == null || pool.minRating() < rating)
                .filter(pool -> pool.maxRating() == null || pool.maxRating() > rating)
                .findFirst()
                .map(pools::indexOf)
                .orElse(null);
  }

  private Pane createMapTile(MapVersion mapVersion) {
    Integer playerBracketIndex = this.playerBracketIndex.getValue();
    List<MatchmakerQueueMapPool> pools = this.sortedMapPools.getValue();
    Map<MatchmakerQueueMapPool, List<MapVersion>> brackets = this.brackets.get();
    double relevanceLevel = 1;
    if (playerBracketIndex != null) {
      int bracketDistance = brackets.entrySet()
                                    .stream()
                                    .filter(entry -> entry.getValue().contains(mapVersion))
                                    .map(Entry::getKey)
                                    .mapToInt(pools::indexOf)
                                    .filter(index -> index >= 0)
                                    .map(index -> Math.abs(index - playerBracketIndex))
                                    .min()
                                    .orElseThrow();

      relevanceLevel = switch (bracketDistance) {
        case 0 -> 1;
        case 1 -> 0.2;
        default -> 0;
      };
    }

    TeamMatchmakingMapTileController controller = uiService.loadFxml(
        "theme/play/teammatchmaking/matchmaking_map_tile.fxml");
    controller.setRelevanceLevel(relevanceLevel);
    controller.setMapVersion(mapVersion);
    return controller.getRoot();
  }

  private void resizeToContent() {
    int tilecount = this.sortedMaps.getValue().size();
    if (tilecount == 0) {
      return;
    }

    double hgap = tilesContainer.getHgap();
    double vgap = tilesContainer.getVgap();

    double tileHSize = TILE_SIZE + hgap;
    double tileVSize = TILE_SIZE + vgap;

    int maxTilesInLine = (int) Math.min(10, Math.floor((getMaxWidth() * 0.95 - PADDING * 2 + hgap) / tileHSize));
    int maxLinesWithoutScroll = (int) Math.floor((getMaxHeight() * 0.95 - PADDING * 2 + vgap) / tileVSize);

    int tilesInOneLine = Math.min(maxTilesInLine,
                                  Math.max(Math.max(4, Math.ceilDiv(tilecount, Math.max(1, maxLinesWithoutScroll))),
                                           (int) Math.ceil(Math.sqrt(tilecount))));

    tilesContainer.setPrefColumns(tilesInOneLine);
  }

  private void updateContent() {
    List<Pane> mapTiles = sortedMaps.getValue().stream().map(this::createMapTile).toList();

    fxApplicationThreadExecutor.execute(() -> {
      this.tilesContainer.getChildren().setAll(mapTiles);
      this.resizeToContent();
    });
  }
}