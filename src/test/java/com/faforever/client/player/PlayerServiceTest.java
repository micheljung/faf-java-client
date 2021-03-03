package com.faforever.client.player;

import com.faforever.client.game.Game;
import com.faforever.client.game.GameBuilder;
import com.faforever.client.remote.FafService;
import com.faforever.client.remote.domain.GameStatus;
import com.faforever.client.remote.domain.PlayerInfo;
import com.faforever.client.remote.domain.PlayersMessage;
import com.faforever.client.remote.domain.SocialMessage;
import com.faforever.client.user.UserService;
import com.google.common.eventbus.EventBus;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static com.faforever.client.player.SocialStatus.FOE;
import static com.faforever.client.player.SocialStatus.FRIEND;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PlayerServiceTest {

  @Mock
  private FafService fafService;
  @Mock
  private UserService userService;
  @Mock
  private EventBus eventBus;

  private PlayerService instance;
  private PlayerInfo playerInfo1;
  private PlayerInfo playerInfo2;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(userService.getOwnPlayerInfo()).thenReturn(new PlayerInfo(1, "junit", null, null, null, 0, null, null));
    when(userService.getUsername()).thenReturn("junit");
    playerInfo1 = new PlayerInfo(2, "junit2", null, null, null, 0, null, null);
    playerInfo2 = new PlayerInfo(3, "junit3", null, null, null, 0, null, null);

    instance = new PlayerService(fafService, userService, eventBus);

    when(fafService.connectionStateProperty()).thenReturn(new SimpleObjectProperty<>());

    instance.afterPropertiesSet();
    instance.createOrUpdatePlayerForPlayerInfo(playerInfo1);
    instance.createOrUpdatePlayerForPlayerInfo(playerInfo2);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPostConstruct() {
    verify(fafService).addOnMessageListener(eq(PlayersMessage.class), any(Consumer.class));
    verify(fafService).addOnMessageListener(eq(SocialMessage.class), any(Consumer.class));
  }

  @Test
  public void testGetPlayerForUsernameUsernameDoesNotExist() {
    Optional<Player> player = instance.getPlayerByNameIfOnline("junit");
    assertFalse(player.isPresent());
  }

  @Test
  public void testGetPlayerForUsernameUsernameExists() {
    Player player = instance.getPlayerByNameIfOnline("junit2").orElseThrow();

    assertEquals("junit2", player.getUsername());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRegisterAndGetPlayerForUsernameNull() {
    instance.createOrUpdatePlayerForPlayerInfo(null);
  }

  @Test
  public void testPlayerUpdatedFromPlayerInfo() {
    Player player = instance.getPlayerByNameIfOnline(playerInfo1.getLogin()).orElseThrow();

    assertEquals((int) playerInfo1.getNumberOfGames(), player.getNumberOfGames());
    assertEquals(playerInfo1.getClan(), player.getClan());
    assertEquals(playerInfo1.getCountry(), player.getCountry());

    instance.createOrUpdatePlayerForPlayerInfo(new PlayerInfo(2, "junit2", "ABC", null, "DE", 100, null, null));

    assertEquals(100, player.getNumberOfGames());
    assertEquals("ABC", player.getClan());
    assertEquals("DE", player.getCountry());
  }

  @Test
  public void testGetPlayerNamesPopulated() {
    Set<String> playerNames = instance.getPlayerNames();
    assertThat(playerNames, hasSize(2));
  }

  @Test
  public void testGetPlayerNamesSomeInstances() {
    Set<String> playerNames = instance.getPlayerNames();

    assertThat(playerNames, hasSize(2));
    assertThat(playerNames, containsInAnyOrder(playerInfo1.getLogin(), playerInfo2.getLogin()));
  }

  @Test
  public void testAddFriend() {
    Player lisa = instance.getPlayerByNameIfOnline(playerInfo1.getLogin()).orElseThrow();
    Player ashley = instance.getPlayerByNameIfOnline(playerInfo2.getLogin()).orElseThrow();

    instance.addFriend(lisa);
    instance.addFriend(ashley);

    verify(fafService).addFriend(lisa);
    verify(fafService).addFriend(ashley);

    assertSame("Social Status Property not set to 'friend'", lisa.getSocialStatus(), FRIEND);
    assertSame("Social Status Property not set to 'friend'", ashley.getSocialStatus(), FRIEND);
  }

  @Test
  public void testAddFriendIsFoe() {
    Player player = instance.getPlayerByNameIfOnline(playerInfo1.getLogin()).orElseThrow();

    player.setSocialStatus(FOE);

    instance.addFriend(player);

    assertNotSame("Social Status Property is still 'foe'", player.getSocialStatus(), FOE);
  }

  @Test
  public void testRemoveFriend() {
    Player player1 = instance.getPlayerByNameIfOnline(playerInfo1.getLogin()).orElseThrow();
    Player player2 = instance.getPlayerByNameIfOnline(playerInfo2.getLogin()).orElseThrow();

    instance.addFriend(player1);
    verify(fafService).addFriend(player1);

    instance.addFriend(player2);
    verify(fafService).addFriend(player2);

    instance.removeFriend(player1);
    verify(fafService).removeFriend(player1);

    assertNotSame("Property 'friend' was not set to false", player1.getSocialStatus(), FRIEND);
    assertSame("Property 'friend' was not set to true", player2.getSocialStatus(), FRIEND);
  }

  @Test
  public void testAddFoe() {
    Player player1 = instance.getPlayerByNameIfOnline(playerInfo1.getLogin()).orElseThrow();
    Player player2 = instance.getPlayerByNameIfOnline(playerInfo2.getLogin()).orElseThrow();

    instance.addFoe(player1);
    instance.addFoe(player2);

    verify(fafService).addFoe(player1);
    verify(fafService).addFoe(player2);
    assertSame("Social Status Property not set to 'foe'", player1.getSocialStatus(), FOE);
    assertSame("Social Status Property not set to 'foe'", player2.getSocialStatus(), FOE);
  }

  @Test
  public void testAddFoeIsFriend() {
    Player player = instance.getPlayerByNameIfOnline(playerInfo1.getLogin()).orElseThrow();
    player.setSocialStatus(FRIEND);

    instance.addFoe(player);

    assertNotSame("Social Status Property is still 'friend'", player.getSocialStatus(), FRIEND);
  }

  @Test
  public void testRemoveFoe() {
    Player player = instance.getPlayerByNameIfOnline(playerInfo1.getLogin()).orElseThrow();

    instance.addFriend(player);
    instance.removeFriend(player);

    assertNotSame("Property 'friend' was not set to false", player.getSocialStatus(), FRIEND);
  }

  @Test
  public void testGetCurrentPlayer() {
    Player currentPlayer = instance.getCurrentPlayer();

    assertThat(currentPlayer.getUsername(), is("junit"));
    assertThat(currentPlayer.getId(), is(1));
  }

  @Test(expected = IllegalStateException.class)
  public void testGetCurrentPlayerNull() {
    when(userService.getOwnPlayerInfo()).thenReturn(null);

    Player currentPlayer = instance.getCurrentPlayer();

    assertNull(currentPlayer);
  }

  @Test
  public void testGetPlayerByName() {
    instance.getPlayerByName("junit");

    verify(fafService).queryPlayerByName("junit");
  }

  @Test
  public void testEventBusRegistered() {
    verify(eventBus).register(instance);
  }

  @Test
  public void testGameRemovedFromPlayerIfGameClosed() {
    Map<String, List<String>> teams = Map.of("1", List.of(playerInfo1.getLogin()), "2", List.of(playerInfo2.getLogin()));
    Game game = GameBuilder.create().defaultValues().teams(teams).get();

    Player player1 = instance.getPlayerByNameIfOnline(playerInfo1.getLogin()).orElseThrow();
    Player player2 = instance.getPlayerByNameIfOnline(playerInfo2.getLogin()).orElseThrow();

    instance.updatePlayersInGame(game);

    assertThat(player1.getGame(), is(game));
    assertThat(player2.getGame(), is(game));

    game.setStatus(GameStatus.CLOSED);

    instance.updatePlayersInGame(game);

    assertThat(player1.getGame(), is(nullValue()));
    assertThat(player2.getGame(), is(nullValue()));
  }

  @Test
  public void testPlayerLeftOpenGame() {
    Map<String, List<String>> teams = new HashMap<>(Map.of("1", List.of(playerInfo1.getLogin()), "2", List.of(playerInfo2.getLogin())));
    Game game = GameBuilder.create().defaultValues().teams(teams).get();

    Player player1 = instance.getPlayerByNameIfOnline(playerInfo1.getLogin()).orElseThrow();
    Player player2 = instance.getPlayerByNameIfOnline(playerInfo2.getLogin()).orElseThrow();

    instance.updatePlayersInGame(game);

    assertThat(player1.getGame(), is(game));
    assertThat(player2.getGame(), is(game));

    teams.remove("1");

    instance.updatePlayersInGame(game);

    assertThat(player1.getGame(), is(nullValue()));
    assertThat(player2.getGame(), is(game));
  }

  @Test
  public void testThereIsFriendInGame() {
    ObservableMap<String, List<String>> teams = FXCollections.observableMap(Map.of("team1", List.of(playerInfo1.getLogin(), playerInfo2.getLogin())));
    Game game = GameBuilder.create().defaultValues().teams(teams).get();
    instance.createOrUpdatePlayerForPlayerInfo(playerInfo1);
    Player player1 = instance.getPlayerByNameIfOnline(playerInfo1.getLogin()).orElseThrow();
    instance.createOrUpdatePlayerForPlayerInfo(playerInfo2);
    instance.addFriend(player1);

    assertTrue(instance.areFriendsInGame(game));
  }

  @Test
  public void testNoFriendInGame() {
    ObservableMap<String, List<String>> teams = FXCollections.observableMap(Map.of("team1", List.of(playerInfo1.getLogin())));
    Game game = GameBuilder.create().defaultValues().teams(teams).get();
    Player player2 = instance.getPlayerByNameIfOnline(playerInfo2.getLogin()).orElseThrow();
    player2.setId(100);
    instance.addFriend(player2);

    assertFalse(instance.areFriendsInGame(game));
    assertFalse(instance.areFriendsInGame(null));
  }

  @Test
  public void testCurrentPlayerInGame() {
    Game game = GameBuilder.create().defaultValues().teams(Map.of("1", List.of("junit"))).get();

    assertTrue(instance.isCurrentPlayerInGame(game));
  }

  @Test
  public void testCurrentPlayerNotInGame() {
    Game game = GameBuilder.create().defaultValues().teams(Map.of("1", List.of("other"))).get();

    assertFalse(instance.isCurrentPlayerInGame(game));
  }
}
