package com.faforever.client.chat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChatTab {

  @EqualsAndHashCode.Include
  private final String id;
  @Setter
  private boolean selected;
}
