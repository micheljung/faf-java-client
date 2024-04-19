package com.faforever.client.teammatchmaking;

import com.faforever.client.domain.api.MapVersion;
import com.faforever.client.domain.api.MatchmakerQueueMapPool;
import com.faforever.client.domain.server.PlayerInfo;
import com.faforever.client.fx.FxApplicationThreadExecutor;
import com.faforever.client.fx.JavaFxUtil;
import com.faforever.client.player.LeaderboardRating;
import com.faforever.client.player.PlayerService;
import com.faforever.client.theme.UiService;
import com.faforever.client.util.RatingUtil;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

import com.faforever.client.domain.server.MatchmakerQueueInfo;
import com.faforever.client.fx.NodeController;
import com.faforever.client.map.MapService;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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

  public Pane root;
  public FlowPane tilesContainer;
  public ScrollPane scrollContainer;
  public VBox loadingPane;
  private SortedMap<MatchmakerQueueMapPool, List<MapVersion>> sortedBrackets;
  private SortedMap<MatchmakerQueueMapPool, List<MapVersion>> sortedBracketsWithDuplicates;
  private Integer playerBracketIndex = null;

  private DoubleProperty maxWidthProperty = new SimpleDoubleProperty(0);

  private DoubleProperty maxHeightProperty = new SimpleDoubleProperty(0);


  @Override
  protected void onInitialize() {
    JavaFxUtil.bindManagedToVisible(loadingPane);
  }

  @Override
  public Pane getRoot() {
    return root;
  }

  public double getMaxWidth(){
    return this.maxWidthProperty.get();
  }
  public void setMaxWidth(double value) {
    this.maxWidthProperty.set(value);
  }
  public DoubleProperty maxWidthProperty() {
    return this.maxWidthProperty;
  }

  public double getMaxHeight(){
    return this.maxHeightProperty.get();
  }
  public void setMaxHeight(double value) {
    this.maxHeightProperty.set(value);
  }
  public DoubleProperty maxHeightProperty() {
    return this.maxHeightProperty;
  }


  public void setQueue(MatchmakerQueueInfo queue) {
    mapService.getMatchmakerBrackets(queue).subscribe(rawBrackets -> {
      loadingPane.setVisible(false);

      this.sortedBracketsWithDuplicates = this.getSortedBrackets(rawBrackets);

      this.sortedBrackets = this.removeDuplicates(this.sortedBracketsWithDuplicates);

      PlayerInfo player = playerService.getCurrentPlayer();
      Integer rating = RatingUtil.getLeaderboardRating(player, queue.getLeaderboard());

      this.playerBracketIndex = this.getPlayerBracketIndex(this.sortedBrackets, rating);

      List<MapVersion> values = this.sortedBrackets.values().stream().flatMap(List::stream).toList();
      this.resizeToContent(values.size(), TILE_SIZE);


      fxApplicationThreadExecutor.execute(() -> values.forEach(this::addMapTile));
    });

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

  private Integer getPlayerBracketIndex(SortedMap<MatchmakerQueueMapPool, List<MapVersion>> sortedBrackets, double rating) {
    int i = 0;
    for (Map.Entry<MatchmakerQueueMapPool, List<MapVersion>> entry : sortedBrackets.entrySet()) {
      MatchmakerQueueMapPool pool = entry.getKey();
      Double min = pool.minRating();
      Double max = pool.maxRating();
      if (min == null) {min = Double.NEGATIVE_INFINITY;}
      if (max == null) {max = Double.POSITIVE_INFINITY;}

      if (rating < max && rating > min) {
        return i;
      }
      i++;
    }
    return null;
  }

  private void addMapTile(MapVersion mapVersion) {
    int i = 0;
    List<Integer> indexes = new ArrayList<>();
    for (List<MapVersion> maps : this.sortedBracketsWithDuplicates.values()) {
      if (maps.contains(mapVersion)) {
        indexes.add(i);
      }
      i++;
    }
    int diff = Collections.min(indexes.stream().map(idx -> Math.abs(idx - this.playerBracketIndex)).toList());
    double relevanceLevel = switch (diff) {
      case 0 -> 1;
      case 1 -> 0.2;
      default -> 0;
    };

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