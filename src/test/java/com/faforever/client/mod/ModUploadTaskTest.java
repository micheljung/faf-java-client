package com.faforever.client.mod;

import com.faforever.client.api.FafApiAccessor;
import com.faforever.client.i18n.I18n;
import com.faforever.client.preferences.DataPrefs;
import com.faforever.client.test.PlatformTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Spy;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsArrayWithSize.emptyArray;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

public class ModUploadTaskTest extends PlatformTest {

  @TempDir
  public Path tempDirectory;


  private ModUploadTask instance;

  @Mock
  private ModService modService;

  @Mock
  private FafApiAccessor fafApiAccessor;
  @Mock
  private I18n i18n;
  @Spy
  private DataPrefs dataPrefs;

  @BeforeEach
  public void setUp() throws Exception {
    instance = new ModUploadTask(modService, fafApiAccessor, i18n, dataPrefs);
    dataPrefs.setBaseDataDirectory(tempDirectory);

    Files.createDirectories(dataPrefs.getCacheDirectory());
    lenient().when(i18n.get(any())).thenReturn("");
    lenient().when(fafApiAccessor.uploadFile(any(), any(), any(), any())).thenReturn(Mono.empty());
  }

  @Test
  public void testModPathNull() throws Exception {
    assertThrows(NullPointerException.class, () -> instance.call());
  }

  @Test
  public void testProgressListenerNull() throws Exception {
    instance.setModPath(Path.of("."));
    assertThrows(NullPointerException.class, () -> instance.call());
  }

  @Test
  public void testCall() throws Exception {

    Path pathToMod =tempDirectory.resolve("test-mod");
    Path pathToModInfo = tempDirectory.resolve("test-mod/mod_info.lua");

    Files.createDirectories(pathToMod);
    Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/mods/eco_manager_mod_info.lua")), pathToModInfo);

    instance.setModPath(pathToMod);
    instance.call();

    verify(fafApiAccessor).uploadFile(any(), any(), any(), any());

    assertThat(Files.list(dataPrefs.getCacheDirectory()).toArray(), emptyArray());
  }
}
