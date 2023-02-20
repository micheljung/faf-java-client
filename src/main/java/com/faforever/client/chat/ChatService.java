package com.faforever.client.chat;

import com.faforever.client.net.ConnectionState;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.MapChangeListener;

import java.util.concurrent.CompletableFuture;

public interface ChatService {

  String PARTY_CHANNEL_SUFFIX = "'sParty";

  void connect();

  void disconnect();

  CompletableFuture<String> sendMessageInBackground(String target, String message);

  boolean userExistsInAnyChannel(String username);

  ChatChannel getOrCreateChannel(String channelName);

  ChatChannelUser getOrCreateChatUser(String username, String channel);

  void addUsersListener(String channelName, MapChangeListener<String, ChatChannelUser> listener);

  void addChannelsListener(MapChangeListener<String, ChatChannel> listener);

  void removeUsersListener(String channelName, MapChangeListener<String, ChatChannelUser> listener);

  void leaveChannel(String channelName);

  CompletableFuture<String> sendActionInBackground(String target, String action);

  void joinChannel(String channelName);

  boolean isDefaultChannel(String channelName);

  void close();

  ReadOnlyObjectProperty<ConnectionState> connectionStateProperty();

  ConnectionState getConnectionState();

  void reconnect();

  void whois(String username);

  void setChannelTopic(String channelName, String text);

  /**
   * Increase or decrease the number of unread messages.
   *
   * @param delta a positive or negative number
   */
  void incrementUnreadMessagesCount(int delta);
}
