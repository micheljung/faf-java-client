package com.faforever.client.fx.contextmenu;

import com.faforever.client.chat.ChatChannelUser;
import com.faforever.client.chat.ChatColorMode;
import com.faforever.client.chat.ChatUserCategory;
import com.faforever.client.chat.event.ChatUserColorChangeEvent;
import com.faforever.client.fx.JavaFxUtil;
import com.faforever.client.preferences.ChatPrefs;
import com.faforever.client.preferences.PreferencesService;
import com.faforever.client.util.Assert;
import com.google.common.eventbus.EventBus;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Locale;

import static com.faforever.client.chat.ChatColorMode.RANDOM;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Slf4j
public class ChatUserColorPickerCustomMenuItemController extends AbstractCustomMenuItemController<ChatChannelUser> {

  public ColorPicker colorPicker;
  public Button removeCustomColorButton;

  private final PreferencesService preferencesService;
  private final EventBus eventBus;

  private ChatPrefs chatPrefs;

  @Override
  public void initialize() {
    chatPrefs = preferencesService.getPreferences().getChat();
    removeCustomColorButton.setOnAction(event -> colorPicker.setValue(null));
    JavaFxUtil.bindManagedToVisible(removeCustomColorButton);
    WeakInvalidationListener weakChatColorModePropertyListener = new WeakInvalidationListener((observable) -> JavaFxUtil.runLater(() -> {
      ChatColorMode chatColorMode = chatPrefs.getChatColorMode();
      removeCustomColorButton.setVisible(!chatColorMode.equals(RANDOM) && colorPicker.getValue() != null);
      getRoot().setVisible(!chatColorMode.equals(RANDOM));
    }));
    JavaFxUtil.addListener(colorPicker.valueProperty(), weakChatColorModePropertyListener);
    JavaFxUtil.addAndTriggerListener(chatPrefs.chatColorModeProperty(), weakChatColorModePropertyListener);
  }

  @Override
  public void afterSetObject() {
    Assert.checkNullIllegalState(object, "no chat user has been set");
    ChatChannelUser chatUser = object;
    colorPicker.setValue(chatPrefs.getUserToColor().getOrDefault(getLowerUsername(chatUser), null));
    JavaFxUtil.addListener(colorPicker.valueProperty(), (observable, oldValue, newValue) -> {
      ChatUserCategory userCategory;
      if (chatUser.isModerator()) {
        userCategory = ChatUserCategory.MODERATOR;
      } else {
        userCategory = chatUser.getSocialStatus().map(status -> switch (status) {
          case FRIEND -> ChatUserCategory.FRIEND;
          case FOE -> ChatUserCategory.FOE;
          default -> ChatUserCategory.OTHER;
        }).orElse(ChatUserCategory.OTHER);
      }
      if (newValue == null) {
        chatPrefs.getUserToColor().remove(getLowerUsername(chatUser));
        chatUser.setColor(chatPrefs.getGroupToColor().getOrDefault(userCategory, null));
      } else {
        chatPrefs.getUserToColor().put(getLowerUsername(chatUser), newValue);
        chatUser.setColor(newValue);
      }
      eventBus.post(new ChatUserColorChangeEvent(chatUser));
    });
  }

  private String getLowerUsername(ChatChannelUser chatUser) {
    return chatUser.getUsername().toLowerCase(Locale.ROOT);
  }
}

