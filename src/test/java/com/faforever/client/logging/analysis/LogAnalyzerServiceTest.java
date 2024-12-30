package com.faforever.client.logging.analysis;

import com.faforever.client.config.ClientProperties;
import com.faforever.client.i18n.I18n;
import com.faforever.client.test.ServiceTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class LogAnalyzerServiceTest extends ServiceTest {

  private static final String SOUND_EXPECTED_TEXT = "Sound issue detected";
  private static final String MINIMIZED_EXPECTED_TEXT = "Game was minimized";
  private static final String MORE_INFO_BUTTON = "More Info";

  @Mock
  private I18n i18n;

  @Mock
  private ClientProperties clientProperties;

  @InjectMocks
  private LogAnalyzerService logAnalyzerService;

  @Test
  public void testAnalyzeLogContentsWhenGameMinimizedTrace() {
    final String logContents = "info: Minimized true";

    when(i18n.get("game.log.analysis.minimized")).thenReturn(MINIMIZED_EXPECTED_TEXT);

    AnalysisResult result = logAnalyzerService.analyzeLogContents(logContents);

    assertTrue(result.analysisMessages().contains(MINIMIZED_EXPECTED_TEXT));
    assertTrue(result.actions().isEmpty());
  }

  @Test
  public void testAnalyzeLogContentsWhenXactTrace() {
    final String logContents = "warning: SND\nXACT";

    when(i18n.get("game.log.analysis.moreInfo")).thenReturn(MORE_INFO_BUTTON);
    when(i18n.get("game.log.analysis.snd", MORE_INFO_BUTTON)).thenReturn(SOUND_EXPECTED_TEXT);

    AnalysisResult result = logAnalyzerService.analyzeLogContents(logContents);

    assertTrue(result.analysisMessages().contains(SOUND_EXPECTED_TEXT));
    assertEquals(1, result.actions().size());
  }

  @Test
  public void testAnalyzeLogContentsWhenGameMinimizedAndXactTrace() {
    final String logContents = "info: Minimized true\nwarning: SND\nXACT";

    when(i18n.get("game.log.analysis.minimized")).thenReturn(MINIMIZED_EXPECTED_TEXT);
    when(i18n.get("game.log.analysis.moreInfo")).thenReturn(MORE_INFO_BUTTON);
    when(i18n.get("game.log.analysis.snd", MORE_INFO_BUTTON)).thenReturn(SOUND_EXPECTED_TEXT);

    AnalysisResult result = logAnalyzerService.analyzeLogContents(logContents);

    Collection<String> results = result.analysisMessages();
    assertTrue(results.contains(MINIMIZED_EXPECTED_TEXT));
    assertTrue(results.contains(SOUND_EXPECTED_TEXT));
    assertEquals(1, result.actions().size());
  }

  @Test
  public void testAnalyzeLogContentsWhenNoRelevantTraces() {
    final String logContents = "Some other log content";

    AnalysisResult result = logAnalyzerService.analyzeLogContents(logContents);

    assertTrue(result.analysisMessages().isEmpty());
    assertTrue(result.actions().isEmpty());
  }
}
