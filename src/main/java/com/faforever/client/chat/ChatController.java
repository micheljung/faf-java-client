package com.faforever.client.chat;

import com.faforever.client.exception.ProgrammingError;
import com.faforever.client.fx.FxApplicationThreadExecutor;
import com.faforever.client.fx.NodeController;
import com.faforever.client.net.ConnectionState;
import com.faforever.client.theme.UiService;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakMapChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ChatController extends NodeController<AnchorPane> {

  private final ChatService chatService;
  private final UiService uiService;
  private final FxApplicationThreadExecutor fxApplicationThreadExecutor;
  private final ChatNavigation chatNavigation;

  private final MapChangeListener<String, ChatChannel> channelChangeListener = change -> {
    if (change.wasRemoved()) {
      onChannelLeft(change.getValueRemoved());
    }
    if (change.wasAdded()) {
      onChannelJoined(change.getValueAdded());
    }
  };

  public AnchorPane chatRoot;
  public TabPane tabPane;
  public Pane connectingProgressPane;
  public VBox noOpenTabsContainer;
  public TextField channelNameTextField;
  public VBox disconnectedPane;

  private ObservableList<Tab> openedTabs;

  @Override
  protected void onInitialize() {
    super.onInitialize();
    openedTabs = tabPane.getTabs();

    chatService.addChannelsListener(new WeakMapChangeListener<>(channelChangeListener));
    chatService.getChannels().forEach(this::onChannelJoined);

    chatService.connectionStateProperty().when(showing).subscribe(this::onConnectionStateChange);
    tabPane.getSelectionModel().selectedItemProperty().subscribe(tab -> {
      if (tab.isClosable()) {
        chatNavigation.setLastOpenedTabId(tab.getId());
      }
    });
  }

  private void onChannelLeft(ChatChannel chatChannel) {
    if (chatChannel.isPartyChannel()) {
      return;
    }

    removeTab(chatChannel);
  }

  private void onChannelJoined(ChatChannel chatChannel) {
    if (chatChannel.isPartyChannel()) {
      return;
    }

    addAndSelectTab(chatChannel);
  }

  private void onDisconnected() {
    fxApplicationThreadExecutor.execute(() -> {
      disconnectedPane.setVisible(true);
      connectingProgressPane.setVisible(false);
      tabPane.setVisible(false);
      openedTabs.removeIf(Tab::isClosable);
      chatNavigation.clear();
    });
  }

  private void onConnected() {
    chatService.getChannels().forEach(this::onChannelJoined);
    fxApplicationThreadExecutor.execute(() -> {
      disconnectedPane.setVisible(false);
      connectingProgressPane.setVisible(false);
      tabPane.setVisible(true);
    });
  }

  private void onConnecting() {
    fxApplicationThreadExecutor.execute(() -> {
      disconnectedPane.setVisible(false);
      connectingProgressPane.setVisible(true);
      tabPane.setVisible(false);
    });
  }

  private void removeTab(ChatChannel chatChannel) {
    fxApplicationThreadExecutor.execute(() -> {
      String channelName = chatChannel.getName();
      openedTabs.removeIf(tab -> channelName.equals(tab.getId()));
      chatNavigation.removeTab(channelName);
    });
  }

  private void addAndSelectTab(ChatChannel chatChannel) {
    if (!containsTab(chatChannel)) {
      fxApplicationThreadExecutor.execute(() -> {
        Tab tab = createTab(chatChannel);
        int index = chatService.isDefaultChannel(chatChannel) ? 0 : openedTabs.size() - 1; // Last index is `add channel` tab
        openedTabs.add(index, tab);

        String channelName = chatChannel.getName();
        if (chatNavigation.addTabIfMissing(channelName) || channelName.equals(chatNavigation.getLastOpenedTabId())) {
          tabPane.getSelectionModel().select(tab);
        }
      });
    }
  }

  private boolean containsTab(ChatChannel chatChannel) {
    return openedTabs.stream().anyMatch(tab -> chatChannel.getName().equals(tab.getId()));
  }

  private Tab createTab(ChatChannel chatChannel) {
    String fxmlFile = chatChannel.isPrivateChannel() ? "theme/chat/private_chat_tab.fxml" : "theme/chat/channel_tab.fxml";
    AbstractChatTabController tabController = uiService.loadFxml(fxmlFile);
    tabController.setChatChannel(chatChannel);
    Tab tab = tabController.getRoot();
    tab.setId(chatChannel.getName());
    return tab;
  }

  private void onConnectionStateChange(ConnectionState newValue) {
    switch (newValue) {
      case DISCONNECTED -> onDisconnected();
      case CONNECTED -> onConnected();
      case CONNECTING -> onConnecting();
      default -> throw new ProgrammingError("Uncovered connection state: " + newValue);
    }
  }

  @Override
  public AnchorPane getRoot() {
    return chatRoot;
  }

  public void onJoinChannelButtonClicked() {
    String channelName = channelNameTextField.getText();
    if (!channelName.startsWith("#")) {
      channelName = "#" + channelName;
    }

    chatService.joinChannel(channelName);
    channelNameTextField.clear();
  }

  public void onConnectButtonClicked() {
    chatService.connect();
  }
}
