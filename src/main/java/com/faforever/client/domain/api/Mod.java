package com.faforever.client.domain.api;

import com.faforever.client.domain.server.PlayerInfo;

import java.net.URL;

public record Mod(
    Integer id,
    String displayName,
    URL repositoryURL,
    boolean recommended,
    String author,
    PlayerInfo uploader,
    ReviewsSummary reviewsSummary
) {}
