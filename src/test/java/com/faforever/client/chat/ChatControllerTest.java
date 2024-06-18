package com.faforever.client.chat;

import com.faforever.client.net.ConnectionState;
import com.faforever.client.test.PlatformTest;
import com.faforever.client.theme.UiService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.Tab;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Set;

import static javafx.collections.FXCollections.observableHashMap;
import static javafx.collections.FXCollections.synchronizedObservableMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ChatControllerTest extends PlatformTest {

  @Mock
  private AbstractChatTabController tabController;
  @Mock
  private UiService uiService;
  @Mock
  private ChatService chatService;
  @Spy
  private ChatNavigation chatNavigation;
  @Captor
  private ArgumentCaptor<MapChangeListener<String, ChatChannel>> channelsListener;
  private final ObservableMap<String, ChatChannel> channels = synchronizedObservableMap(observableHashMap());

  private final ObjectProperty<ConnectionState> connectionState = new SimpleObjectProperty<>(ConnectionState.DISCONNECTED);
  @InjectMocks
  private ChatController instance;
  private ObservableList<Tab> openedTabs;

  @BeforeEach
  public void setUp() throws Exception {
    lenient().when(chatService.connectionStateProperty()).thenReturn(connectionState);
    lenient().when(uiService.loadFxml("theme/chat/private_chat_tab.fxml")).thenReturn(tabController);
    lenient().when(uiService.loadFxml("theme/chat/channel_tab.fxml")).thenReturn(tabController);
    lenient().when(tabController.getRoot()).thenAnswer(invocation -> new Tab());


    loadFxml("theme/chat/chat.fxml", clazz -> instance);
    openedTabs = instance.tabPane.getTabs();
  }

  private void setChannelsListener() {
    verify(chatService).addChannelsListener(channelsListener.capture());
    channels.addListener(channelsListener.getValue());
  }

  @Test
  public void testOnlyOneAddChannelTabAfterInitialized() {
    assertEquals(1, openedTabs.size());
    assertEquals(ChatController.ADD_CHANNEL_TAB_ID, openedTabs.getFirst().getId());
  }

  @Test
  public void testOnChannelJoined() {
    setChannelsListener();

    requestChannel("aeolus", true);
    requestChannel("channel");
    requestChannel("player");

    assertContainsTab("aeolus");
    assertContainsTab("channel");
    assertContainsTab("player");
  }

  @Test
  public void testAddedTabShouldBeSelected() {
    setChannelsListener();
    requestChannel("channel");

    assertEquals("channel", chatNavigation.getLastOpenedTabId());
    assertEquals("channel", instance.tabPane.getSelectionModel().getSelectedItem().getId());
  }

  @Test
  public void testSelectedTabShouldBeRemembered() {
    setChannelsListener();
    requestChannel("channel1");
    requestChannel("channel2");

    assertEquals("channel2", chatNavigation.getLastOpenedTabId());
    assertEquals("channel2", instance.tabPane.getSelectionModel().getSelectedItem().getId());

    runOnFxThreadAndWait(() -> instance.tabPane.getSelectionModel().selectPrevious());

    assertEquals("channel1", chatNavigation.getLastOpenedTabId());
    assertEquals("channel1", instance.tabPane.getSelectionModel().getSelectedItem().getId());
  }

  private void assertContainsTab(String channel) {
    assertTrue(openedTabs.stream().anyMatch(tab -> tab.getId().equals(channel)));
  }

  @Test
  public void testOnDisconnected() {
    setChannelsListener();

    connectionState.set(ConnectionState.CONNECTED);
    requestChannel("aeolus", true);

    runOnFxThreadAndWait(() -> connectionState.set(ConnectionState.DISCONNECTED));
    assertTrue(instance.disconnectedPane.isVisible());
    assertFalse(instance.connectingProgressPane.isVisible());
    assertFalse(instance.tabPane.isVisible());
    assertEquals(1, openedTabs.size());
    assertNull(chatNavigation.getLastOpenedTabId());
  }

  @Test
  public void testOnConnected() {
    assertEquals(ConnectionState.DISCONNECTED, connectionState.getValue());

    ChatChannel chatChannelMock1 = mock(ChatChannel.class);
    ChatChannel chatChannelMock2 = mock(ChatChannel.class);
    when(chatChannelMock1.getName()).thenReturn("1");
    when(chatChannelMock2.getName()).thenReturn("2");
    when(chatService.getChannels()).thenReturn(Set.of(chatChannelMock1, chatChannelMock2));

    runOnFxThreadAndWait(() -> connectionState.set(ConnectionState.CONNECTED));

    assertFalse(instance.disconnectedPane.isVisible());
    assertFalse(instance.connectingProgressPane.isVisible());
    assertTrue(instance.tabPane.isVisible());
    assertEquals(3, openedTabs.size());
  }

  @Test
  public void testOnConnecting() {
    assertEquals(ConnectionState.DISCONNECTED, connectionState.getValue());
    runOnFxThreadAndWait(() -> connectionState.set(ConnectionState.CONNECTING));
    assertFalse(instance.disconnectedPane.isVisible());
    assertTrue(instance.connectingProgressPane.isVisible());
    assertFalse(instance.tabPane.isVisible());
  }

  private void requestChannel(String channel) {
    requestChannel(channel, false);
  }

  private void requestChannel(String channel, boolean isDefaultChannel) {
    ChatChannel chatChannelMock = mock(ChatChannel.class);
    when(chatChannelMock.getName()).thenReturn(channel);
    when(chatService.isDefaultChannel(chatChannelMock)).thenReturn(isDefaultChannel);
    channels.putIfAbsent(channel, chatChannelMock);
    waitFxEvents();
  }

  @Test
  public void testOnChannelLeft() {
    setChannelsListener();
    requestChannel("aeolus", true);
    requestChannel("channel", false);

    Tab tab = openedTabs.stream().filter(tab1 -> tab1.getId().equals("aeolus")).findFirst().orElseThrow();
    runOnFxThreadAndWait(() -> openedTabs.remove(tab));
    assertEquals(2, openedTabs.size());
  }

  @Test
  public void testOnJoinChannelButtonClicked() {
    runOnFxThreadAndWait(() -> {
      instance.channelNameTextField.setText("newChannel");
      instance.onJoinChannelButtonClicked();
    });

    verify(chatService).joinChannel("#newChannel");
    assertTrue(instance.channelNameTextField.getText().isEmpty());
  }

  @Test
  public void testOnConnectButtonClicked() {
    runOnFxThreadAndWait(() -> instance.onConnectButtonClicked());
    verify(chatService).connect();
  }
}
