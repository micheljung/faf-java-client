package com.faforever.client.fx.contextmenu;

import com.faforever.client.chat.ChatChannelUser;
import com.faforever.client.preferences.PreferencesService;
import com.faforever.client.theme.UiService;
import com.faforever.client.ui.StageHolder;
import com.faforever.client.util.Assert;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.faforever.client.chat.ChatColorMode.RANDOM;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ChangeUsernameColorMenuItem extends AbstractMenuItem<ChatChannelUser> {

  private final PreferencesService preferencesService;
  private final UiService uiService;
  private final ContextMenuBuilder contextMenuBuilder;

  @Override
  protected void onClicked() {
    Assert.checkNullIllegalState(object, "no chat user has been set");
    Node node = getStyleableNode();
    Bounds screenBounds = node.localToScreen(node.getBoundsInLocal());
    contextMenuBuilder.newBuilder()
        .addCustomItem(uiService.loadFxml("theme/chat/color_picker_menu_item.fxml", ChatUserColorPickerCustomMenuItemController.class), object)
        .build()
        .show(StageHolder.getStage(), screenBounds.getMinX(), screenBounds.getMinY());
  }

  @Override
  protected boolean isItemVisible() {
    return object != null && !preferencesService.getPreferences().getChat().getChatColorMode().equals(RANDOM);
  }

  @Override
  protected String getItemText() {
    return "Change color";
  }
}
