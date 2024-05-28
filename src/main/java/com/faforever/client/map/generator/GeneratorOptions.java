package com.faforever.client.map.generator;

import lombok.Builder;

@Builder
public record GeneratorOptions(
    Integer spawnCount,
    Integer numTeams,
    Integer mapSize,
    String seed,
    GenerationType generationType,
    String symmetry,
    String style,
    String terrainGenerator,
    String textureGenerator,
    String resourceGenerator,
    String propGenerator,
    String commandLineArgs
) {}
