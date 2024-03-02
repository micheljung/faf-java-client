package com.faforever.client.map.management;

import com.faforever.client.domain.MapVersionBean;
import com.faforever.client.i18n.I18n;
import com.faforever.client.map.MapService;
import com.faforever.client.test.PlatformTest;
import com.faforever.client.theme.UiService;
import javafx.collections.FXCollections;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

public class MapsManagementControllerTest extends PlatformTest {

  @Mock
  private MapService mapService;
  @Mock
  private I18n i18n;
  @Mock
  private UiService uiService;

  private final MapVersionBean officialMap = Instancio.create(MapVersionBean.class);
  private final MapVersionBean customMap1 = Instancio.create(MapVersionBean.class);
  private final MapVersionBean customMap2 = Instancio.create(MapVersionBean.class);

  @InjectMocks
  private MapsManagementController instance;

  @BeforeEach
  public void setUp() throws Exception {
    when(mapService.isOfficialMap(officialMap)).thenReturn(true);
    when(mapService.isCustomMap(officialMap)).thenReturn(false);
    when(mapService.isCustomMap(customMap1)).thenReturn(true);
    when(mapService.isCustomMap(customMap2)).thenReturn(true);
    when(mapService.isOfficialMap(customMap1)).thenReturn(false);
    when(mapService.isOfficialMap(customMap2)).thenReturn(false);
    when(mapService.getInstalledMaps()).thenReturn(FXCollections.observableArrayList(officialMap, customMap1, customMap2));

    loadFxml("theme/vault/map/maps_management.fxml", param -> instance);
  }

  @Test
  public void testFilterMaps() {
    switchFilterTo(MapFilter.CUSTOM_MAPS);
    assertThat(instance.listView.getItems().size(), is(2));
    verifyItemsInList(customMap1, customMap2);

    switchFilterTo(MapFilter.OFFICIAL_MAPS);
    assertThat(instance.listView.getItems().size(), is(1));
    verifyItemsInList(officialMap);

    switchFilterTo(MapFilter.ALL_MAPS);
    assertThat(instance.listView.getItems().size(), is(3));
    verifyItemsInList(customMap1, customMap2, officialMap);
  }

  private void verifyItemsInList(MapVersionBean... items) {
    Arrays.stream(items).forEach(item -> assertThat(instance.listView.getItems().contains(item), is(true)));
  }

  private void switchFilterTo(MapFilter filter) {
    runOnFxThreadAndWait(() -> instance.filterMapsChoiceBox.setValue(filter));
  }
}
