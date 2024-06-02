package com.faforever.client.mod;

import com.faforever.client.api.FafApiAccessor;
import com.faforever.client.domain.api.ModVersion;
import com.faforever.client.i18n.I18n;
import com.faforever.client.preferences.DataPrefs;
import com.faforever.client.task.CompletableTask;
import com.faforever.client.task.ResourceLocks;
import com.faforever.client.util.Validator;
import com.faforever.commons.io.ByteCountListener;
import com.faforever.commons.io.Zipper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.faforever.commons.io.Bytes.formatSize;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.newOutputStream;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ModUploadTask extends CompletableTask<Void> {

  private final ModService modService;
  private final FafApiAccessor fafApiAccessor;
  private final I18n i18n;
  private final DataPrefs dataPrefs;

  private Path modPath;

  @Autowired
  public ModUploadTask(ModService modService, FafApiAccessor fafApiAccessor, I18n i18n, DataPrefs dataPrefs) {
    super(Priority.HIGH);
    this.modService = modService;
    this.dataPrefs = dataPrefs;
    this.fafApiAccessor = fafApiAccessor;
    this.i18n = i18n;
  }

  @Override
  protected Void call() throws Exception {
    Validator.notNull(modPath, "modPath must not be null");

    ResourceLocks.acquireUploadLock();
    Path cacheDirectory = dataPrefs.getCacheDirectory();
    Files.createDirectories(cacheDirectory);
    Path tmpFile = createTempFile(cacheDirectory, "mod", ".zip");

    try {

      log.debug("Retrieve information from mod_info.lua at: {}", modPath);
      ModVersion modVersionInfo = modService.extractModInfo(modPath);
      URL repositoryURL = modVersionInfo.mod().repositoryURL();

      HashMap<String, String> parameters = new HashMap<>();
      if (repositoryURL != null) {
        parameters.put("repositoryUrl", repositoryURL.toString());
      }

      log.debug("Zipping mod `{}` to `{}`", modPath, tmpFile);
      updateTitle(i18n.get("modVault.upload.compressing"));

      Locale locale = i18n.getUserSpecificLocale();
      ByteCountListener byteListener = (written, total) -> {
        updateMessage(i18n.get("bytesProgress", formatSize(written, locale), formatSize(total, locale)));
        updateProgress(written, total);
      };

      try (OutputStream outputStream = newOutputStream(tmpFile)) {
        Zipper.of(modPath)
            .to(outputStream)
            .listener(byteListener)
            .zip();
      }

      log.debug("Uploading mod `{}` as `{}`", modPath, tmpFile);
      updateTitle(i18n.get("modVault.upload.uploading"));

      return fafApiAccessor.uploadFile("/mods/upload", tmpFile, byteListener, Map.of("metadata", parameters)).block();
    } finally {
      Files.delete(tmpFile);
      ResourceLocks.freeUploadLock();
    }
  }

  public void setModPath(Path modPath) {
    this.modPath = modPath;
  }
}