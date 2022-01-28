package com.faforever.client.game;

import com.faforever.client.avatar.AvatarService;
import com.faforever.client.builders.PlayerBeanBuilder;
import com.faforever.client.domain.PlayerBean;
import com.faforever.client.i18n.I18n;
import com.faforever.client.player.CountryFlagService;
import com.faforever.client.player.SocialStatus;
import com.faforever.client.test.UITest;
import com.faforever.client.theme.UiService;
import com.faforever.commons.api.dto.Faction;
import javafx.scene.image.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlayerCardTooltipControllerTest extends UITest {
  @Mock
  private I18n i18n;
  @Mock
  private CountryFlagService countryFlagService;
  @Mock
  private AvatarService avatarService;

  @InjectMocks
  private PlayerCardTooltipController instance;

  @BeforeEach
  public void setUp() throws Exception {
    loadFxml("theme/player_card_tooltip.fxml", clazz -> instance);
  }

  @Test
  public void testSetFoe() {
    when(i18n.get("userInfo.tooltipFormat.withRating", "foe", 1000)).thenReturn("foe(1000)");

    PlayerBeanBuilder playerBeanBuilder = PlayerBeanBuilder.create()
        .defaultValues()
        .username("foe")
        .socialStatus(SocialStatus.FOE);
    PlayerBean player = playerBeanBuilder.get();
    runOnFxThreadAndWait(() -> instance.setPlayer(player, 1000, Faction.CYBRAN));

    assertTrue(instance.factionIcon.getStyleClass().contains(UiService.CYBRAN_STYLE_CLASS));
    assertTrue(instance.factionIcon.isVisible());
    assertFalse(instance.factionImage.isVisible());
    assertTrue(instance.foeIconText.isVisible());
    assertFalse(instance.friendIconText.isVisible());
    assertEquals("foe(1000)", instance.playerInfo.getText());
  }

  @Test
  public void testSetFriend() {
    when(i18n.get("userInfo.tooltipFormat.withRating", "user", 1000)).thenReturn("user(1000)");

    PlayerBeanBuilder playerBeanBuilder = PlayerBeanBuilder.create()
        .defaultValues()
        .username("user")
        .socialStatus(SocialStatus.FRIEND);
    PlayerBean player = playerBeanBuilder.get();
    runOnFxThreadAndWait(() -> instance.setPlayer(player, 1000, Faction.SERAPHIM));

    assertTrue(instance.factionIcon.getStyleClass().contains(UiService.SERAPHIM_STYLE_CLASS));
    assertTrue(instance.factionIcon.isVisible());
    assertFalse(instance.factionImage.isVisible());
    assertFalse(instance.foeIconText.isVisible());
    assertTrue(instance.friendIconText.isVisible());
    assertEquals("user(1000)", instance.playerInfo.getText());
  }

  @Test
  public void testSetOther() {
    when(i18n.get("userInfo.tooltipFormat.withRating", "user", 1000)).thenReturn("user(1000)");

    PlayerBeanBuilder playerBeanBuilder = PlayerBeanBuilder.create()
        .defaultValues()
        .username("user")
        .socialStatus(SocialStatus.OTHER);
    PlayerBean player = playerBeanBuilder.get();
    runOnFxThreadAndWait(() -> instance.setPlayer(player, 1000, Faction.RANDOM));

    assertTrue(instance.factionImage.getImage().getUrl().contains(UiService.RANDOM_IMAGE));
    assertFalse(instance.factionIcon.isVisible());
    assertTrue(instance.factionImage.isVisible());
    assertFalse(instance.foeIconText.isVisible());
    assertFalse(instance.friendIconText.isVisible());
    assertEquals("user(1000)", instance.playerInfo.getText());
  }

  @Test
  public void testSetPlayerAvatar() {
    Image avatarImage = mock(Image.class);
    PlayerBean playerBean = PlayerBeanBuilder.create().defaultValues().get();
    when(avatarService.getCurrentAvatarByPlayerName(playerBean.getUsername())).thenReturn(Optional.of(avatarImage));

    runOnFxThreadAndWait(() -> instance.setPlayer(playerBean, 1000, Faction.RANDOM));

    assertTrue(instance.avatarImageView.isVisible());
    assertEquals(avatarImage, instance.avatarImageView.getImage());
  }

  @Test
  public void testInvisibleAvatarImageView() {
    PlayerBean playerBean = PlayerBeanBuilder.create().defaultValues().get();
    when(avatarService.getCurrentAvatarByPlayerName(playerBean.getUsername())).thenReturn(Optional.empty());

    runOnFxThreadAndWait(() -> instance.setPlayer(playerBean, 1000, Faction.RANDOM));

    assertFalse(instance.avatarImageView.isVisible());
  }

  @Test
  public void testSetCountryImage() {
    Image countryImage = mock(Image.class);
    PlayerBean playerBean = PlayerBeanBuilder.create().defaultValues().get();
    when(countryFlagService.loadCountryFlag(playerBean.getCountry())).thenReturn(Optional.of(countryImage));

    runOnFxThreadAndWait(() -> instance.setPlayer(playerBean, 1000, Faction.RANDOM));

    assertTrue(instance.countryImageView.isVisible());
    assertEquals(countryImage, instance.countryImageView.getImage());
  }

  @Test
  public void testInvisibleCountryImageView() {
    PlayerBean playerBean = PlayerBeanBuilder.create().defaultValues().get();
    when(countryFlagService.loadCountryFlag(playerBean.getCountry())).thenReturn(Optional.empty());

    runOnFxThreadAndWait(() -> instance.setPlayer(playerBean, 1000, Faction.RANDOM));

    assertFalse(instance.countryImageView.isVisible());
  }
}