package com.faforever.client.teammatchmaking;

import com.faforever.client.domain.api.Map;
import com.faforever.client.domain.api.MapVersion;
import com.faforever.client.fx.ImageViewHelper;
import com.faforever.client.fx.NodeController;
import com.faforever.client.i18n.I18n;
import com.faforever.client.map.MapService;
import com.faforever.client.map.MapService.PreviewSize;
import com.faforever.client.map.generator.MapGeneratorService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor

public class TeamMatchmakingMapTileController extends NodeController<Pane> {

  private final MapService mapService;
  private final I18n i18n;
  private final ImageViewHelper imageViewHelper;
  private final MapGeneratorService mapGeneratorService;

  public Pane root;
  public ImageView thumbnailImageView;
  public Label nameLabel;
  public Label authorLabel;
  public Label sizeLabel;
  public VBox authorBox;
  public Region relevanceIcon;

  protected final ObjectProperty<MapVersion> entity = new SimpleObjectProperty<>();
  private final BooleanProperty isRelevant = new SimpleBooleanProperty(false);


  public void setIsRelevant(boolean isRelevant) {
    this.isRelevant.set(isRelevant);
  }


  @Override
  public Pane getRoot() {
    return root;
  }


  public void setMapVersion(MapVersion mapVersion) {
    this.entity.set(mapVersion);
  }


  @Override
  protected void onInitialize() {
    thumbnailImageView.imageProperty().bind(entity.map(mapVersionBean -> mapService.loadPreview(mapVersionBean, PreviewSize.SMALL))
                                                  .flatMap(imageViewHelper::createPlaceholderImageOnErrorObservable));
    thumbnailImageView.effectProperty().bind(isRelevant.map(isRel -> {
      ColorAdjust grayscaleEffect = new ColorAdjust();
      grayscaleEffect.setSaturation(isRel ? 0 : -1);
      return grayscaleEffect;
    }));
    isRelevant.addListener((observable, oldValue, newValue) -> updateRelevanceIcon(newValue));

    ObservableValue<Map> mapObservable = entity.map(MapVersion::map);

    nameLabel.textProperty().bind(mapObservable.map(map -> {
      String name = map.displayName();
      if (mapGeneratorService.isGeneratedMap(name)) {
        return "map generator";
      }
      return name;
    }));

    authorBox.visibleProperty().bind(mapObservable.map(map -> (map.author() != null) || (mapGeneratorService.isGeneratedMap(map.displayName()))));
    authorLabel.textProperty().bind(mapObservable.map(map -> {
      if (map.author() != null) {
        return map.author().getUsername();
      } else if (mapGeneratorService.isGeneratedMap(map.displayName())) {
        return "Neroxis";
      } else {
        return i18n.get("map.unknownAuthor");
      }
    }));
    sizeLabel.textProperty().bind(entity.map(MapVersion::size).map(size -> i18n.get("mapPreview.size", size.widthInKm(), size.heightInKm())));
  }


  private void updateRelevanceIcon(boolean isRelevant) {
    if (isRelevant) {
      relevanceIcon.getStyleClass().setAll("icon", "icon16x16", "check-icon", "icon-map-available");
    } else {
      relevanceIcon.getStyleClass().setAll("icon", "icon16x16", "x-icon", "icon-map-not-available");
    }
  }
}