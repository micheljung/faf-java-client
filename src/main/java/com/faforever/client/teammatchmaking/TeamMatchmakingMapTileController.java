package com.faforever.client.teammatchmaking;

import com.faforever.client.domain.api.Map;
import com.faforever.client.domain.api.MapVersion;
import com.faforever.client.fx.ImageViewHelper;
import com.faforever.client.fx.NodeController;
import com.faforever.client.i18n.I18n;
import com.faforever.client.map.MapService;
import com.faforever.client.map.MapService.PreviewSize;
import com.faforever.client.map.generator.MapGeneratorService;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
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

  protected final ObjectProperty<MapVersion> entity = new SimpleObjectProperty<>();
  private DoubleProperty relevanceLevel = new SimpleDoubleProperty(0);;

  public double getRelevanceLevel(){
    return this.relevanceLevel.get();
  }
  public void setRelevanceLevel(double value) {
    this.relevanceLevel.set(value);
  }
  public DoubleProperty relevanceLevelProperty() {
    return this.relevanceLevel;
  }

  @Override
  public Pane getRoot() {
    return root;
  }


  public void setMapVersion(MapVersion mapVersion) {
    this.entity.set(mapVersion);
  }


  @Override
  protected void onInitialize(){
    thumbnailImageView.imageProperty().bind(entity.map(mapVersionBean -> mapService.loadPreview(mapVersionBean, PreviewSize.SMALL))
                                                  .flatMap(imageViewHelper::createPlaceholderImageOnErrorObservable));
    thumbnailImageView.effectProperty().bind(relevanceLevel.map(relevanceLevel -> {
      ColorAdjust grayscaleEffect = new ColorAdjust();
      grayscaleEffect.setSaturation(-1 + relevanceLevel.intValue());
      return grayscaleEffect;
    }));
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
}