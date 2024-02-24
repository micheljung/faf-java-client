package com.faforever.client.leaderboard;

import com.faforever.client.domain.LeagueEntryBean;
import com.faforever.client.domain.PlayerBean;
import com.faforever.client.domain.SubdivisionBean;
import com.faforever.client.fx.FxApplicationThreadExecutor;
import com.faforever.client.fx.StringCell;
import com.faforever.client.fx.TabController;
import com.faforever.client.fx.contextmenu.AddFoeMenuItem;
import com.faforever.client.fx.contextmenu.AddFriendMenuItem;
import com.faforever.client.fx.contextmenu.ContextMenuBuilder;
import com.faforever.client.fx.contextmenu.CopyUsernameMenuItem;
import com.faforever.client.fx.contextmenu.RemoveFoeMenuItem;
import com.faforever.client.fx.contextmenu.RemoveFriendMenuItem;
import com.faforever.client.fx.contextmenu.ShowPlayerInfoMenuItem;
import com.faforever.client.fx.contextmenu.ViewReplaysMenuItem;
import com.faforever.client.i18n.I18n;
import com.faforever.client.notification.NotificationService;
import javafx.collections.FXCollections;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple3;

import java.util.function.Function;


@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class SubDivisionTabController extends TabController {

  private final ContextMenuBuilder contextMenuBuilder;
  private final LeaderboardService leaderboardService;
  private final NotificationService notificationService;
  private final I18n i18n;
  private final FxApplicationThreadExecutor fxApplicationThreadExecutor;

  public Tab subDivisionTab;
  public TableColumn<LeagueEntryBean, Number> rankColumn;
  public TableColumn<LeagueEntryBean, String> nameColumn;
  public TableColumn<LeagueEntryBean, Number> gamesPlayedColumn;
  public TableColumn<LeagueEntryBean, Number> scoreColumn;
  public TableView<LeagueEntryBean> ratingTable;

  @Override
  public Tab getRoot() {
    return subDivisionTab;
  }

  @Override
  protected void onInitialize() {
    ratingTable.setRowFactory(param -> entriesRowFactory());

    rankColumn.setCellValueFactory(param -> param.getValue().rankProperty());
    rankColumn.setCellFactory(param -> new StringCell<>(rank -> i18n.number(rank.intValue())));

    nameColumn.setCellValueFactory(param -> param.getValue().getPlayer().usernameProperty());
    nameColumn.setCellFactory(param -> new StringCell<>(name -> name));
    nameColumn.prefWidthProperty().bind(ratingTable.widthProperty().subtract(250));

    gamesPlayedColumn.setCellValueFactory(param -> param.getValue().gamesPlayedProperty());
    gamesPlayedColumn.setCellFactory(param -> new StringCell<>(count -> i18n.number(count.intValue())));

    scoreColumn.setCellValueFactory(param -> param.getValue().scoreProperty());
    scoreColumn.setCellFactory(param -> new StringCell<>(score -> i18n.number(score.intValue())));
  }

  @NotNull
  private TableRow<LeagueEntryBean> entriesRowFactory() {
    TableRow<LeagueEntryBean> row = new TableRow<>();
    row.setOnContextMenuRequested(event -> {
      if (row.getItem() == null) {
        return;
      }
      LeagueEntryBean entry = row.getItem();
      PlayerBean player = entry.getPlayer();
      contextMenuBuilder.newBuilder()
                        .addItem(ShowPlayerInfoMenuItem.class, player)
                        .addItem(CopyUsernameMenuItem.class, player.getUsername())
                        .addSeparator()
                        .addItem(AddFriendMenuItem.class, player)
                        .addItem(RemoveFriendMenuItem.class, player)
                        .addItem(AddFoeMenuItem.class, player)
                        .addItem(RemoveFoeMenuItem.class, player)
                        .addSeparator()
                        .addItem(ViewReplaysMenuItem.class, player)
                        .build()
                        .show(subDivisionTab.getTabPane().getScene().getWindow(), event.getScreenX(),
                              event.getScreenY());
    });

    return row;
  }

  public void populate(SubdivisionBean subdivision) {
    fxApplicationThreadExecutor.execute(() -> subDivisionTab.setText(subdivision.getNameKey()));

    Mono<Integer> playersInHigherDivisions = leaderboardService.getPlayerNumberInHigherDivisions(subdivision).cache();
    leaderboardService.getEntries(subdivision)
                      .index((index, entry) -> Mono.zip(Mono.just(entry), Mono.just(index), playersInHigherDivisions))
                      .flatMap(Function.identity())
                      .doOnNext(TupleUtils.consumer(
                          (entry, index, numHigherPlayers) -> entry.setRank(numHigherPlayers + 1 + index.intValue())))
                      .map(Tuple3::getT1)
                      .collectList()
                      .map(FXCollections::observableList)
                      .publishOn(fxApplicationThreadExecutor.asScheduler())
                      .subscribe(ratingTable::setItems, throwable -> {
                        log.error("Error while loading leaderboard entries for subdivision `{}`", subdivision,
                                  throwable);
                        notificationService.addImmediateErrorNotification(throwable, "leaderboard.failedToLoad");
                      });
  }
}
