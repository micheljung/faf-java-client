package com.faforever.client.teammatchmaking;

import com.faforever.client.domain.api.MapVersion;
import com.faforever.client.domain.api.MatchmakerQueueMapPool;
import com.faforever.client.domain.server.PlayerInfo;
import com.faforever.client.fx.FxApplicationThreadExecutor;
import com.faforever.client.fx.JavaFxUtil;
import com.faforever.client.player.PlayerService;
import com.faforever.client.theme.UiService;
import com.faforever.client.util.RatingUtil;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

import com.faforever.client.domain.server.MatchmakerQueueInfo;
import com.faforever.client.fx.NodeController;
import com.faforever.client.map.MapService;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor

public class TeamMatchmakingMapListController extends NodeController<Pane> {

  private static final int TILE_SIZE = 125;
  private static final int PADDING = 20;

  private static final Comparator<MapVersion> mapVersionComparator = Comparator.nullsFirst(Comparator.comparing(MapVersion::size))
                                                          .thenComparing(mapVersion -> mapVersion.map().displayName(),
                                                                         String.CASE_INSENSITIVE_ORDER);

  private static final Comparator<MatchmakerQueueMapPool> mapPoolComparator = Comparator.comparing(MatchmakerQueueMapPool::minRating, Comparator.nullsFirst(Double::compare))
                                                                   .thenComparing(MatchmakerQueueMapPool::maxRating, Comparator.nullsLast(Double::compare));


  private final MapService mapService;
  private final UiService uiService;
  private final PlayerService playerService;
  private final FxApplicationThreadExecutor fxApplicationThreadExecutor;
  private final DoubleProperty maxWidth = new SimpleDoubleProperty(0);
  private final DoubleProperty maxHeight = new SimpleDoubleProperty(0);
  private final ObjectProperty<SortedMap<MatchmakerQueueMapPool, List<MapVersion>>> sortedBracketsWithDuplicates = new SimpleObjectProperty<>();
  private final ObjectProperty<SortedMap<MatchmakerQueueMapPool, List<MapVersion>>> sortedBrackets = new SimpleObjectProperty<>();
  private final ObjectProperty<Integer> playerBracketIndex = new SimpleObjectProperty<>(null);
  private final ObjectProperty<MatchmakerQueueInfo> queue = new SimpleObjectProperty<>();

  public Pane root;
  public FlowPane tilesContainer;
  public ScrollPane scrollContainer;
  public VBox loadingPane;

  @Override
  protected void onInitialize() {
    this.bindProperties();
  }

  private void bindProperties() {
    JavaFxUtil.bindManagedToVisible(loadingPane);

    this.queue.addListener((obs, oldVal, newVal) -> {
      loadingPane.setVisible(true);

      mapService.getMatchmakerBrackets(newVal).subscribe(rawBrackets -> {
        loadingPane.setVisible(false);
        this.sortedBracketsWithDuplicates.set(getSortedBrackets(rawBrackets));
        this.sortedBrackets.set(removeDuplicates(this.sortedBracketsWithDuplicates.get()));
      });
    });

    this.sortedBrackets.addListener((obs, oldVal, newVal) -> {
      PlayerInfo player = playerService.getCurrentPlayer();
      Integer rating = RatingUtil.getLeaderboardRating(player, getQueue().getLeaderboard());
      this.updatePlayerBracketIndex(newVal, rating);
    });

    this.playerBracketIndex.addListener((obs, oldVal, newVal) -> {
      List<MapVersion> values = this.sortedBrackets.get().values().stream().flatMap(List::stream).toList();
      this.resizeToContent(values.size(), TILE_SIZE);
      fxApplicationThreadExecutor.execute(() -> values.forEach((mapVersion) -> this.addMapTile(mapVersion, newVal)));
    });
  }

  @Override
  public Pane getRoot() {
    return root;
  }

  public double getMaxWidth(){
    return this.maxWidth.get();
  }
  public void setMaxWidth(double value) {
    this.maxWidth.set(value);
  }
  public DoubleProperty maxWidthProperty() {
    return this.maxWidth;
  }

  public double getMaxHeight(){
    return this.maxHeight.get();
  }
  public void setMaxHeight(double value) {
    this.maxHeight.set(value);
  }
  public DoubleProperty maxHeightProperty() {
    return this.maxHeight;
  }

  public MatchmakerQueueInfo getQueue(){
    return this.queue.get();
  }
  public void setQueue(MatchmakerQueueInfo queue) {
    this.queue.set(queue);
  }
  public ObjectProperty<MatchmakerQueueInfo> queueProperty() {
    return this.queue;
  }

  private SortedMap<MatchmakerQueueMapPool, List<MapVersion>> getSortedBrackets(Map<MatchmakerQueueMapPool, List<MapVersion>> brackets) {
    SortedMap<MatchmakerQueueMapPool, List<MapVersion>> sortedMap = new TreeMap<>(mapPoolComparator);

    brackets.forEach((mapPool, mapVersions) -> {
      List<MapVersion> sortedList = mapVersions.stream().sorted(mapVersionComparator).toList();
      sortedMap.put(mapPool, sortedList);
    });

    return sortedMap;
  }

  private SortedMap<MatchmakerQueueMapPool, List<MapVersion>> removeDuplicates(SortedMap<MatchmakerQueueMapPool, List<MapVersion>> sortedBrackets) {
    Set<String> usedMaps = new HashSet<>();
    return sortedBrackets.entrySet().stream()
                         .collect(Collectors.toMap(
                             Map.Entry::getKey,
                             entry -> entry.getValue().stream()
                                           .filter(mapVersion -> {
                                             String name = mapVersion.map().displayName();
                                             return usedMaps.add(name);
                                           })
                                           .collect(Collectors.toList()),
                             (list1, list2) -> list1,
                             () -> new TreeMap<>(mapPoolComparator)
                         ));
  }

  private void updatePlayerBracketIndex(SortedMap<MatchmakerQueueMapPool, List<MapVersion>> sortedBrackets, double rating) {
    if (sortedBrackets == null) {
      this.playerBracketIndex.set(null);
      return;
    }
    int i = 0;
    for (Map.Entry<MatchmakerQueueMapPool, List<MapVersion>> entry : sortedBrackets.entrySet()) {
      MatchmakerQueueMapPool pool = entry.getKey();
      Double min = pool.minRating();
      Double max = pool.maxRating();
      if (min == null) {min = Double.NEGATIVE_INFINITY;}
      if (max == null) {max = Double.POSITIVE_INFINITY;}

      if (rating < max && rating > min) {
        this.playerBracketIndex.set(i);
        return;
      }
      i++;
    }
    this.playerBracketIndex.set(null);
  }

  private void addMapTile(MapVersion mapVersion, Integer playerBracketIndex) {
    int i = 0;
    double relevanceLevel = 1;
    if (playerBracketIndex != null) {
      List<Integer> indexes = new ArrayList<>();
      for (List<MapVersion> maps : this.sortedBracketsWithDuplicates.get().values()) {
        if (maps.contains(mapVersion)) {
          indexes.add(i);
        }
        i++;
      }
      int diff = Collections.min(indexes.stream().map(idx -> Math.abs(idx - playerBracketIndex)).toList());
      relevanceLevel = switch (diff) {
        case 0 -> 1;
        case 1 -> 0.2;
        default -> 0;
      };
    }

    TeamMatchmakingMapTileController controller = uiService.loadFxml(
        "theme/play/teammatchmaking/matchmaking_map_tile.fxml");
    controller.setRelevanceLevel(relevanceLevel);
    controller.setMapVersion(mapVersion);
    this.tilesContainer.getChildren().add(controller.getRoot());
  }

  private void resizeToContent(int tilecount, int tileSize) {
    double hgap = tilesContainer.getHgap();
    double vgap = tilesContainer.getVgap();

    int maxTilesInLine = (int) Math.min(10, Math.floor((getMaxWidth() * 0.95 - PADDING * 2 + hgap) / (tileSize + hgap)));

    int maxLinesWithoutScroll = (int) Math.floor((getMaxHeight() * 0.95 - PADDING * 2 + vgap) / (tileSize + vgap));
    int scrollWidth = 18;
    double maxScrollPaneHeight = maxLinesWithoutScroll * (tileSize + vgap) - vgap;
    this.scrollContainer.setMaxHeight(maxScrollPaneHeight);

    int tilesInOneLine = Math.min(maxTilesInLine, Math.max(Math.max(4, Math.ceilDiv(tilecount, Math.max(1, maxLinesWithoutScroll))), (int) Math.ceil(Math.sqrt(tilecount))));
    int numberOfLines = Math.ceilDiv(tilecount, tilesInOneLine);

    double preferredWidth = (tileSize + hgap) * tilesInOneLine - hgap;
    double gridHeight = (tileSize + vgap) * numberOfLines - vgap;

    if (gridHeight > maxScrollPaneHeight) {
      scrollContainer.setPrefWidth(preferredWidth + scrollWidth);
      scrollContainer.setPrefHeight(maxScrollPaneHeight);
    }
    else {
      scrollContainer.setPrefWidth(preferredWidth);
      scrollContainer.setPrefHeight(gridHeight);
    }

    tilesContainer.setPrefWidth(preferredWidth);
  }

}