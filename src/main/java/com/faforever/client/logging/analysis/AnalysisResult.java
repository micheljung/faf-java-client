package com.faforever.client.logging.analysis;

import com.faforever.client.notification.Action;

import java.util.Map;

public record AnalysisResult(Map<String, Action> result) {
  public boolean isOk() {
    return result.isEmpty();
  }
}