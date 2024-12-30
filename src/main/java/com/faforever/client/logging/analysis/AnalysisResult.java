package com.faforever.client.logging.analysis;

import java.util.Set;

public record AnalysisResult(Set<String> analysisMessages) {
  public boolean isOk() {
    return null == analysisMessages || analysisMessages.isEmpty();
  }
}