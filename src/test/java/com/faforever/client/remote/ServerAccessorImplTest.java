package com.faforever.client.remote;

import com.faforever.client.api.TokenService;
import com.faforever.client.config.ClientProperties;
import com.faforever.client.fa.relay.event.CloseGameEvent;
import com.faforever.client.game.GameInfoMessageTestBuilder;
import com.faforever.client.game.GameLaunchMessageTestBuilder;
import com.faforever.client.game.NewGameInfo;
import com.faforever.client.game.NewGameInfoBuilder;
import com.faforever.client.i18n.I18n;
import com.faforever.client.legacy.UidService;
import com.faforever.client.notification.ImmediateNotification;
import com.faforever.client.notification.NotificationService;
import com.faforever.client.notification.Severity;
import com.faforever.client.player.Player;
import com.faforever.client.player.PlayerBuilder;
import com.faforever.client.preferences.LoginPrefs;
import com.faforever.client.preferences.PreferencesService;
import com.faforever.client.teammatchmaking.MatchmakingQueue;
import com.faforever.client.teammatchmaking.MatchmakingQueueBuilder;
import com.faforever.client.test.ServiceTest;
import com.faforever.client.update.Version;
import com.faforever.commons.lobby.AvatarListInfo;
import com.faforever.commons.lobby.ConnectToPeerGpgCommand;
import com.faforever.commons.lobby.DisconnectFromPeerGpgCommand;
import com.faforever.commons.lobby.Faction;
import com.faforever.commons.lobby.GameInfo;
import com.faforever.commons.lobby.GameLaunchResponse;
import com.faforever.commons.lobby.GpgGameOutboundMessage;
import com.faforever.commons.lobby.HostGameGpgCommand;
import com.faforever.commons.lobby.IceMsgGpgCommand;
import com.faforever.commons.lobby.IceServerListResponse;
import com.faforever.commons.lobby.JoinGameGpgCommand;
import com.faforever.commons.lobby.LobbyMode;
import com.faforever.commons.lobby.LoginFailedResponse;
import com.faforever.commons.lobby.LoginSuccessResponse;
import com.faforever.commons.lobby.MatchmakerInfo;
import com.faforever.commons.lobby.MatchmakerInfo.MatchmakerQueue;
import com.faforever.commons.lobby.MatchmakerMatchCancelledResponse;
import com.faforever.commons.lobby.MatchmakerMatchFoundResponse;
import com.faforever.commons.lobby.MatchmakerState;
import com.faforever.commons.lobby.MessageTarget;
import com.faforever.commons.lobby.NoticeInfo;
import com.faforever.commons.lobby.PartyInfo;
import com.faforever.commons.lobby.PartyInfo.PartyMember;
import com.faforever.commons.lobby.PartyInvite;
import com.faforever.commons.lobby.PartyKick;
import com.faforever.commons.lobby.PlayerInfo;
import com.faforever.commons.lobby.SearchInfo;
import com.faforever.commons.lobby.ServerMessage;
import com.faforever.commons.lobby.SessionResponse;
import com.faforever.commons.lobby.SocialInfo;
import com.faforever.commons.lobby.UpdatedAchievementsInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.google.common.eventbus.EventBus;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.LineEncoder;
import io.netty.handler.codec.string.LineSeparator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.scheduling.TaskScheduler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
public class ServerAccessorImplTest extends ServiceTest {

  private static final long TIMEOUT = 5000;
  private static final TimeUnit TIMEOUT_UNIT = TimeUnit.MILLISECONDS;
  private static final InetAddress LOOPBACK_ADDRESS = InetAddress.getLoopbackAddress();

  @TempDir
  public Path faDirectory;

  @Mock
  private PreferencesService preferencesService;
  @Mock
  private UidService uidService;
  @Mock
  private NotificationService notificationService;
  @Mock
  private TokenService tokenService;
  @Mock
  private I18n i18n;
  @Mock
  private TaskScheduler taskScheduler;
  @Mock
  private EventBus eventBus;

  private FafServerAccessorImpl instance;
  private CountDownLatch serverToClientReadyLatch;
  private CountDownLatch messageReceivedByClientLatch;
  private ServerMessage receivedMessage;
  private ObjectMapper objectMapper;
  private final String token = "abc";
  private final Sinks.Many<String> serverReceivedSink = Sinks.many().multicast().directBestEffort();
  private final Flux<String> serverMessagesReceived = serverReceivedSink.asFlux();
  private final Sinks.Many<String> serverSentSink = Sinks.many().unicast().onBackpressureBuffer();
  private DisposableServer disposableServer;

  @BeforeEach
  public void setUp() throws Exception {
    when(tokenService.getRefreshedTokenValue()).thenReturn(token);
    serverToClientReadyLatch = new CountDownLatch(1);
    objectMapper = new ObjectMapper()
        .registerModule(new KotlinModule())
        .registerModule(new JavaTimeModule())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

    startFakeFafLobbyServer();

    ClientProperties clientProperties = new ClientProperties();
    clientProperties.getServer()
        .setHost(LOOPBACK_ADDRESS.getHostAddress())
        .setPort(disposableServer.port() - 1);

    instance = new FafServerAccessorImpl(notificationService, i18n, taskScheduler, clientProperties, preferencesService, uidService,
        tokenService, eventBus, objectMapper);
    instance.afterPropertiesSet();
    instance.addEventListener(ServerMessage.class, serverMessage -> {
      receivedMessage = serverMessage;
      messageReceivedByClientLatch.countDown();
    });
    LoginPrefs loginPrefs = new LoginPrefs();
    loginPrefs.setRefreshToken("junit");

    when(preferencesService.getFafDataDirectory()).thenReturn(faDirectory);
    when(uidService.generate(any(), any())).thenReturn("encrypteduidstring");

    connectAndLogIn();
  }

  private ServerMessage parseServerString(String json) throws JsonProcessingException {
    return objectMapper.readValue(json, ServerMessage.class);
  }

  private void startFakeFafLobbyServer() {
    this.disposableServer = TcpServer.create()
        .doOnConnection(connection -> {
          log.info("New Client connected to server");
          connection.addHandler(new LineEncoder(LineSeparator.UNIX)) // TODO: This is not working. Raise a bug ticket! Workaround below
              .addHandler(new LineBasedFrameDecoder(1024*1024));
        })
        .doOnBound(disposableServer -> log.info("Fake server listening at {} on port {}", disposableServer.host(), disposableServer.port()))
        .noSSL()
        .host(LOOPBACK_ADDRESS.getHostAddress())
        .handle((inbound, outbound) -> {
          Mono<Void> inboundMono = inbound.receive()
              .asString(StandardCharsets.UTF_8)
              .map(message -> {
                log.info("Received message at server {}", message);
                return serverReceivedSink.tryEmitNext(message);
              })
              .then();

          Mono<Void> outboundMono = outbound.send(serverSentSink.asFlux().map(message -> {
            log.info("Sending message from fake server {}", message);
            return Unpooled.copiedBuffer(message + "\n", StandardCharsets.UTF_8);
          })).then();

          return inboundMono.mergeWith(outboundMono);
        })
        .bindNow();

    serverToClientReadyLatch.countDown();
  }

  @SneakyThrows
  private Mono<Void> assertMessageContainsComponents(String... values) {
    return serverMessagesReceived.next().doOnNext(json -> {
      assertThat(json, containsString("command"));
      for (String string : values) {
        assertThat(json, containsString(string));
      }
    })
    .then();
  }

  @AfterEach
  public void tearDown() {
    disposableServer.disposeNow();
    instance.disconnect();
  }

  private void connectAndLogIn() throws Exception {
    int playerUid = 123;
    long sessionId = 456;

    CompletableFuture<LoginSuccessResponse> loginFuture = instance.connectAndLogIn();

    assertMessageContainsComponents(
        "downlords-faf-client",
        "version",
        "user_agent",
        Version.getCurrentVersion(),
        "ask_session"
    ).block();

    SessionResponse sessionMessage = new SessionResponse(sessionId);
    sendFromServer(sessionMessage);

    assertMessageContainsComponents(
        token,
        String.valueOf(sessionId),
        "encrypteduidstring",
        "token",
        "session",
        "unique_id",
        "auth"
    ).block();

    com.faforever.commons.lobby.Player me = new com.faforever.commons.lobby.Player(playerUid, "Junit", null, null, "", new HashMap<>(), new HashMap<>());
    LoginSuccessResponse loginServerMessage = new LoginSuccessResponse(me);

    sendFromServer(loginServerMessage);

    LoginSuccessResponse result = loginFuture.get(TIMEOUT, TIMEOUT_UNIT);

    assertThat(result.getMe().getId(), is(playerUid));
    assertThat(result.getMe().getLogin(), is("Junit"));
  }

  /**
   * Writes the specified message to the client as if it was sent by the FAF server.
   */
  @SneakyThrows
  private void sendFromServer(ServerMessage fafServerMessage) {
    serverToClientReadyLatch.await();
    messageReceivedByClientLatch = new CountDownLatch(1);
    serverSentSink.tryEmitNext(objectMapper.writeValueAsString(fafServerMessage));
  }

  @Test
  public void testRankedMatchNotification() throws Exception {
    OffsetDateTime popTime = OffsetDateTime.ofInstant(Instant.now().plusSeconds(65), ZoneOffset.UTC);
    MatchmakerQueue queue = new MatchmakerQueue("ladder1v1", popTime, 65, 1, 0, List.of(List.of(100, 200)), List.of(List.of(100, 200)));
    MatchmakerInfo matchmakerMessage = new MatchmakerInfo(
        List.of(queue));

    CompletableFuture<MatchmakerInfo> serviceStateDoneFuture = new CompletableFuture<>();

    instance.addEventListener(MatchmakerInfo.class, serviceStateDoneFuture::complete);

    sendFromServer(matchmakerMessage);

    MatchmakerInfo matchmakerServerMessage = serviceStateDoneFuture.get(TIMEOUT, TIMEOUT_UNIT);
    assertThat(matchmakerServerMessage.getQueues(), hasSize(1));
    assertThat(matchmakerServerMessage.getQueues(), contains(queue));
  }


  @Test
  public void testOnNotice() throws Exception {
    NoticeInfo noticeMessage = new NoticeInfo("warning", "foo bar");

    when(i18n.get("messageFromServer")).thenReturn("Message from Server");

    sendFromServer(noticeMessage);

    ArgumentCaptor<ImmediateNotification> captor = ArgumentCaptor.forClass(ImmediateNotification.class);
    verify(notificationService, timeout(1000)).addServerNotification(captor.capture());

    ImmediateNotification notification = captor.getValue();
    assertThat(notification.getSeverity(), is(Severity.WARN));
    assertThat(notification.getText(), is("foo bar"));
    assertThat(notification.getTitle(), is("Message from Server"));
    verify(i18n).get("messageFromServer");
  }

  @Test
  public void onKillNoticeStopsGame() throws Exception {
    NoticeInfo noticeMessage = new NoticeInfo("kill", null);

    sendFromServer(noticeMessage);

    verify(eventBus, timeout(10000)).post(any(CloseGameEvent.class));
  }

  @Test
  public void onKickNoticeStopsApplication() throws Exception {
    NoticeInfo noticeMessage = new NoticeInfo("kick", null);

    sendFromServer(noticeMessage);

    verify(taskScheduler, timeout(10000)).scheduleWithFixedDelay(any(Runnable.class), any(Duration.class));
  }

  @Test
  public void testRequestHostGame() {
    NewGameInfo newGameInfo = NewGameInfoBuilder.create()
        .defaultValues()
        .enforceRatingRange(true)
        .ratingMax(3000)
        .ratingMin(0)
        .get();

    Mono<Void> assertMono = assertMessageContainsComponents("access",
        "mapname",
        "title",
        "options",
        "mod",
        "password",
        "version",
        "visibility",
        "rating_min",
        "rating_max",
        "enforce_rating_range",
        "game_host",
        "password",
        newGameInfo.getMap(),
        newGameInfo.getTitle(),
        newGameInfo.getFeaturedMod().getTechnicalName(),
        newGameInfo.getPassword(),
        "public",
        String.valueOf(newGameInfo.getRatingMax()),
        String.valueOf(newGameInfo.getRatingMin()),
        "true"
    );

    instance.requestHostGame(newGameInfo);

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testRequestJoinGame() {
    Mono<Void> assertMono = assertMessageContainsComponents(
        "uid",
        "password",
        "game_join",
        "pass",
        String.valueOf(1)
    );

    instance.requestJoinGame(1, "pass");

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testAddFriend() {
    Mono<Void> assertMono = assertMessageContainsComponents(
        "friend",
        "social_add",
        String.valueOf(1)
    );

    instance.addFriend(1);

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testAddFoe() {
    Mono<Void> assertMono = assertMessageContainsComponents(
        "foe",
        "social_add",
        String.valueOf(1)
    );

    instance.addFoe(1);

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testRemoveFriend() {
    Mono<Void> assertMono = assertMessageContainsComponents(
        "friend",
        "social_remove",
        String.valueOf(1)
    );

    instance.removeFriend(1);

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testRemoveFoe() {
    Mono<Void> assertMono = assertMessageContainsComponents(
        "foe",
        "social_remove",
        String.valueOf(1)
    );

    instance.removeFoe(1);

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testRequestMatchmakerInfo() {
    Mono<Void> assertMono = assertMessageContainsComponents("matchmaker_info");

    instance.requestMatchmakerInfo();

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testSendGpgMessage() {
    Mono<Void> assertMono = assertMessageContainsComponents(
        "command",
        "args",
        "Test",
        "arg1",
        "arg2");

    instance.sendGpgMessage(new GpgGameOutboundMessage("Test", List.of("arg1", "arg2"), MessageTarget.GAME));

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testClosePlayersGame() {
    Mono<Void> assertMono = assertMessageContainsComponents(
        "user_id",
        "admin",
        "action",
        String.valueOf(1));

    instance.closePlayersGame(1);

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testClosePlayersLobby() {
    Mono<Void> assertMono = assertMessageContainsComponents(
        "user_id",
        "admin",
        "action",
        String.valueOf(1));

    instance.closePlayersLobby(1);

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testBroadcastMessage() {
    Mono<Void> assertMono = assertMessageContainsComponents(
        "message",
        "admin",
        "action",
        "Test");

    instance.broadcastMessage("Test");

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testGetAvailableAvatars() throws Exception {
    Mono<Void> assertMono = assertMessageContainsComponents(
        "avatar",
        "list_avatar",
        "action"
    );

    instance.getAvailableAvatars();

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testGetIceServers() throws Exception {
    Mono<Void> assertMono = assertMessageContainsComponents("ice_servers");

    instance.getIceServers();

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testRestoreGameSession() {
    Mono<Void> assertMono = assertMessageContainsComponents(
        "game_id",
        "restore_game_session",
        String.valueOf(1));

    instance.restoreGameSession(1);

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testGameMatchmaking() {
    MatchmakingQueue queue = MatchmakingQueueBuilder.create().defaultValues().get();

    Mono<Void> assertMono = assertMessageContainsComponents(
        "queue_name",
        "state",
        "game_matchmaking",
        queue.getTechnicalName(),
        "start");

    instance.gameMatchmaking(queue, MatchmakerState.START);

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testInviteToParty() {
    Player player = PlayerBuilder.create("junit").defaultValues().get();

    Mono<Void> assertMono = assertMessageContainsComponents(
        "recipient_id",
        "invite_to_party",
        String.valueOf(player.getId()));

    instance.inviteToParty(player);

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testAcceptPartyInvite() {
    Player player = PlayerBuilder.create("junit").defaultValues().get();

    Mono<Void> assertMono = assertMessageContainsComponents(
        "sender_id",
        "accept_party_invite",
        String.valueOf(player.getId()));

    instance.acceptPartyInvite(player);

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testKickPlayerFromParty() {
    Player player = PlayerBuilder.create("junit").defaultValues().get();

    Mono<Void> assertMono = assertMessageContainsComponents(
        "kicked_player_id",
        "kick_player_from_party",
        String.valueOf(player.getId()));

    instance.kickPlayerFromParty(player);

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testReadyParty() {
    Mono<Void> assertMono = assertMessageContainsComponents("ready_party");

    instance.readyParty();

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testUnreadyParty() {
    Mono<Void> assertMono = assertMessageContainsComponents("unready_party");

    instance.unreadyParty();

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testLeaveParty() {
    Mono<Void> assertMono = assertMessageContainsComponents("leave_party");

    instance.leaveParty();

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testSetPartyFactions() {
    Mono<Void> assertMono = assertMessageContainsComponents(
        "factions",
        "set_party_factions",
        "aeon", "uef", "cybran", "seraphim");

    instance.setPartyFactions(List.of(Faction.AEON, Faction.UEF, Faction.CYBRAN, Faction.SERAPHIM));

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testSelectAvatar() throws MalformedURLException {
    URL url = new URL("http://google.com");

    Mono<Void> assertMono = assertMessageContainsComponents(
        "avatar",
        "action",
        url.toString()
    );

    instance.selectAvatar(url);

    assertMono.block(Duration.ofSeconds(10));
  }

  @Test
  public void testOnGameInfo() throws InterruptedException, JsonProcessingException {
    GameInfo gameInfoMessage = GameInfoMessageTestBuilder.create(1)
        .defaultValues()
        .get();

    sendFromServer(gameInfoMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(gameInfoMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
          "command" : "game_info",
          "host" : "Some host",
          "password_protected" : false,
          "visibility" : null,
          "state" : "open",
          "num_players" : 1,
          "teams" : { },
          "featured_mod" : "faf",
          "uid" : 1,
          "max_players" : 4,
          "title" : "Test preferences",
          "sim_mods" : null,
          "mapname" : "scmp_007",
          "map_file_path" : "scmp_007",
          "launched_at" : null,
          "rating_type" : null,
          "rating_min" : 0,
          "rating_max" : 3000,
          "enforce_rating_range" : false,
          "game_type" : null,
          "games" : null
        }""");

    assertThat(parsedMessage, equalTo(gameInfoMessage));
  }

  @Test
  public void testOnGameLaunch() throws InterruptedException, JsonProcessingException {
    GameLaunchResponse gameLaunchMessage = GameLaunchMessageTestBuilder.create()
        .defaultValues()
        .faction(Faction.AEON)
        .initMode(LobbyMode.AUTO_LOBBY)
        .get();

    instance.startSearchMatchmaker();
    sendFromServer(gameLaunchMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(gameLaunchMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
          "command" : "game_launch",
          "args" : [ ],
          "uid" : 1,
          "mod" : "faf",
          "mapname" : null,
          "name" : "test",
          "expected_players" : null,
          "team" : null,
          "map_position" : null,
          "faction" : "aeon",
          "init_mode" : 1,
          "rating_type" : "global"
        }""");

    assertThat(parsedMessage, equalTo(gameLaunchMessage));

  }

  @Test
  public void testOnPlayerInfo() throws InterruptedException, JsonProcessingException {
    PlayerInfo playerInfoMessage = new PlayerInfo(List.of());

    sendFromServer(playerInfoMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(playerInfoMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
          "command" : "player_info",
          "players" : [ ]
        }""");

    assertThat(parsedMessage, equalTo(playerInfoMessage));
  }

  @Test
  public void testOnMatchmakerInfo() throws InterruptedException, JsonProcessingException {
    MatchmakerInfo matchmakerInfoMessage = new MatchmakerInfo(List.of());

    sendFromServer(matchmakerInfoMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(matchmakerInfoMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
          "command" : "matchmaker_info",
          "queues" : [ ]
        }""");

    assertThat(parsedMessage, equalTo(matchmakerInfoMessage));
  }

  @Test
  public void testOnMatchFound() throws InterruptedException, JsonProcessingException {
    MatchmakerMatchFoundResponse matchFoundMessage = new MatchmakerMatchFoundResponse("test");

    sendFromServer(matchFoundMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(matchFoundMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
          "command" : "match_found",
          "queue_name" : "test"
        }""");

    assertThat(parsedMessage, equalTo(matchFoundMessage));
  }

  @Test
  public void testOnMatchCancelled() throws InterruptedException, JsonProcessingException {
    MatchmakerMatchCancelledResponse matchCancelledMessage = new MatchmakerMatchCancelledResponse();

    sendFromServer(matchCancelledMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage.getClass(), equalTo(matchCancelledMessage.getClass()));

    ServerMessage parsedMessage = parseServerString("""
        {
          "command" : "match_cancelled"
        }""");

    assertThat(parsedMessage.getClass(), equalTo(matchCancelledMessage.getClass()));
  }

  @Test
  public void testOnSocialMessage() throws InterruptedException, JsonProcessingException {
    SocialInfo socialMessage = new SocialInfo(List.of("aeolus"), List.of("aeolus"), List.of(123, 124), List.of(456, 457),  0);

    sendFromServer(socialMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(socialMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
          "command" : "social",
          "friends" : [ 123, 124 ],
          "foes" : [ 456, 457 ],
          "channels" : [ "aeolus" ],
          "autojoin" : [ "aeolus" ]
        }""");

    assertThat(parsedMessage, equalTo(socialMessage));
  }

  //Causes an infinite loop on github actions
  @Disabled
  @Test
  public void testOnAuthenticationFailed() throws InterruptedException, JsonProcessingException {
    LoginFailedResponse authenticationFailedMessage = new LoginFailedResponse("boo");

    instance.connectAndLogIn();
    sendFromServer(authenticationFailedMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(authenticationFailedMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
         "command" : "authentication_failed",
         "text" : "boo"
        }""");

    assertThat(parsedMessage, equalTo(authenticationFailedMessage));
  }

  @Test
  public void testOnUpdatedAchievements() throws InterruptedException, JsonProcessingException {
    UpdatedAchievementsInfo updatedAchievementsMessage = new UpdatedAchievementsInfo(List.of());

    sendFromServer(updatedAchievementsMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(updatedAchievementsMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
          "command" : "updated_achievements",
          "updated_achievements" : [ ]
        }""");

    assertThat(parsedMessage, equalTo(updatedAchievementsMessage));
  }

  @Test
  public void testOnIceServers() throws InterruptedException, JsonProcessingException {
    IceServerListResponse iceServersMessage = new IceServerListResponse(List.of(), 0);

    instance.getIceServers();
    sendFromServer(iceServersMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(iceServersMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
          "command" : "ice_servers",
          "ice_servers" : [ ],
          "ttl" : 0
        }""");

    assertThat(parsedMessage, equalTo(iceServersMessage));
  }

  @Test
  public void testOnAvatarMessage() throws InterruptedException, JsonProcessingException {
    AvatarListInfo avatarMessage = new AvatarListInfo(List.of());

    instance.getAvailableAvatars();
    sendFromServer(avatarMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(avatarMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
          "command" : "avatar",
          "avatarlist" : [ ]
        }""");

    assertThat(parsedMessage, equalTo(avatarMessage));
  }

  @Test
  public void testOnUpdatePartyMessage() throws InterruptedException, JsonProcessingException {
    PartyInfo updatePartyMessage = new PartyInfo(1, List.of(new PartyMember(123, List.of(Faction.UEF, Faction.CYBRAN, Faction.AEON, Faction.SERAPHIM))));

    sendFromServer(updatePartyMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(updatePartyMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
          "command" : "update_party",
          "owner" : 1,
          "members" : [{"player":123,"factions":["uef","cybran","aeon","seraphim"]} ]
        }""");

    assertThat(parsedMessage, equalTo(updatePartyMessage));
  }

  @Test
  public void testOnPartyInviteMessage() throws InterruptedException, JsonProcessingException {
    PartyInvite partyInviteMessage = new PartyInvite(1);

    sendFromServer(partyInviteMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(partyInviteMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
          "command" : "party_invite",
          "sender" : 1
        }""");

    assertThat(parsedMessage, equalTo(partyInviteMessage));
  }

  @Test
  public void testOnPartyKickedMessage() throws InterruptedException, JsonProcessingException {
    PartyKick partyKickedMessage = new PartyKick();

    sendFromServer(partyKickedMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage.getClass(), equalTo(partyKickedMessage.getClass()));

    ServerMessage parsedMessage = parseServerString("""
        {
          "command" : "kicked_from_party"
        }""");

    assertThat(parsedMessage.getClass(), equalTo(partyKickedMessage.getClass()));
  }

  @Test
  public void testOnSearchInfoMessage() throws InterruptedException, JsonProcessingException {
    SearchInfo searchInfoMessage = new SearchInfo("test", MatchmakerState.START);

    sendFromServer(searchInfoMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(searchInfoMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
          "command" : "search_info",
          "queue_name": "test",
          "state": "start"
        }""");

    assertThat(parsedMessage, equalTo(searchInfoMessage));
  }

  @Test
  public void testOnGpgHostMessage() throws InterruptedException, JsonProcessingException {
    HostGameGpgCommand gpgHostGameMessage = new HostGameGpgCommand(MessageTarget.GAME, List.of("test"));

    sendFromServer(gpgHostGameMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(gpgHostGameMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
          "command" : "HostGame",
          "target" : "game",
          "args" : [ "test" ]
        }""");

    assertThat(parsedMessage, equalTo(gpgHostGameMessage));
  }

  @Test
  public void testOnGpgJoinMessage() throws InterruptedException, JsonProcessingException {
    JoinGameGpgCommand gpgJoinGameMessage = new JoinGameGpgCommand(MessageTarget.GAME, List.of("test", 1));

    sendFromServer(gpgJoinGameMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(gpgJoinGameMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
          "command" : "JoinGame",
          "target" : "game",
          "args" : [ "test", 1 ]
        }""");

    assertThat(parsedMessage, equalTo(gpgJoinGameMessage));
  }

  @Test
  public void testOnConnectToPeerMessage() throws InterruptedException, JsonProcessingException {
    ConnectToPeerGpgCommand connectToPeerMessage = new ConnectToPeerGpgCommand(MessageTarget.GAME, List.of("test", 1, true));

    sendFromServer(connectToPeerMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(connectToPeerMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
          "command" : "ConnectToPeer",
          "target" : "game",
          "args" : [ "test", 1, true ]
        }""");

    assertThat(parsedMessage, equalTo(connectToPeerMessage));
  }

  @Test
  public void testOnIceServerMessage() throws InterruptedException, JsonProcessingException {
    IceMsgGpgCommand iceServerMessage = new IceMsgGpgCommand(MessageTarget.GAME, List.of(1, 3));

    sendFromServer(iceServerMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(iceServerMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
           "command" : "IceMsg",
           "target" : "game",
           "args" : [ 1, 3 ]
         }""");

    assertThat(parsedMessage, equalTo(iceServerMessage));
  }

  @Test
  public void testOnDisconnectFromPeerMessage() throws InterruptedException, JsonProcessingException {
    DisconnectFromPeerGpgCommand disconnectFromPeerMessage = new DisconnectFromPeerGpgCommand(MessageTarget.GAME, List.of(1));

    sendFromServer(disconnectFromPeerMessage);
    messageReceivedByClientLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(receivedMessage, is(disconnectFromPeerMessage));

    ServerMessage parsedMessage = parseServerString("""
        {
            "command" : "DisconnectFromPeer",
            "target" : "game",
            "args" : [ 1 ]
          }""");

    assertThat(parsedMessage, equalTo(disconnectFromPeerMessage));
  }
}
