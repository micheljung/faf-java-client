package com.faforever.client.logging.analysis;

import com.faforever.client.config.ClientProperties;
import com.faforever.client.i18n.I18n;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LogAnalyzerService {
  private static final String GAME_MINIMIZED_TRACE = "info: Minimized true";
  private static final String SND_WARNING_TRACE = "warning: SND";
  private static final String SND_XACT_TRACE = "XACT";

  private final I18n i18n;
  private final ClientProperties clientProperties;

  @NotNull
  public AnalysisResult analyzeLogContents(final String logContents) {
    final Set<String> analysisResult = new HashSet<>();

    if (StringUtils.contains(logContents, GAME_MINIMIZED_TRACE)) {
      analysisResult.add(i18n.get("game.log.analysis.minimized"));
    }

    if (StringUtils.contains(logContents, SND_WARNING_TRACE) && StringUtils.contains(logContents, SND_XACT_TRACE)) {
      analysisResult.add(i18n.get("game.log.analysis.snd",
                                  clientProperties.getLinks().get("linksSoundIssues")));
    }

    return new AnalysisResult(analysisResult);
  }
}
