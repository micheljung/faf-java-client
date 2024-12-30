package com.faforever.client.logging.analysis;

import com.faforever.client.notification.Action;

import java.util.Collection;

public record AnalysisResult(Collection<String> analysisMessages, Collection<Action> actions) {
  public boolean isOk() {
    return null == analysisMessages || analysisMessages.isEmpty();
  }
}