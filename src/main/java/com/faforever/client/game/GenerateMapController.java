package com.faforever.client.game;

import com.faforever.client.fx.JavaFxUtil;
import com.faforever.client.fx.NodeController;
import com.faforever.client.i18n.I18n;
import com.faforever.client.map.generator.GenerationType;
import com.faforever.client.map.generator.GeneratorOptions;
import com.faforever.client.map.generator.MapGeneratorService;
import com.faforever.client.map.generator.OutdatedVersionException;
import com.faforever.client.map.generator.UnsupportedVersionException;
import com.faforever.client.notification.NotificationService;
import com.faforever.client.preferences.GeneratorPrefs;
import com.google.common.annotations.VisibleForTesting;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.ListSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.InvalidParameterException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Slf4j
public class GenerateMapController extends NodeController<Pane> {

  public static final double MIN_MAP_SIZE_STEP = 1.25;
  public static final double KM_TO_PIXEL_FACTOR = 51.2;

  private final NotificationService notificationService;
  private final MapGeneratorService mapGeneratorService;
  private final I18n i18n;
  private final GeneratorPrefs generatorPrefs;

  public CreateGameController createGameController;
  public Pane generateMapRoot;
  public Button generateMapButton;
  public TextField previousMapName;
  public Label commandLineLabel;
  public TextField commandLineArgsText;
  public ComboBox<GenerationType> generationTypeComboBox;
  public Label mapStyleLabel;
  public ComboBox<String> mapStyleComboBox;
  public Label biomeLabel;
  public ComboBox<String> biomeComboBox;
  public Spinner<Integer> spawnCountSpinner;
  public Spinner<Double> mapSizeSpinner;
  public Label symmetryLabel;
  public ComboBox<String> symmetryComboBox;
  public Label customStyleLabel;
  public CheckBox customStyleCheckBox;
  public Label fixedSeedLabel;
  public CheckBox fixedSeedCheckBox;
  public TextField seedTextField;
  public Button seedRerollButton;
  public Label terrainLabel;
  public ComboBox<String> terrainComboBox;
  public Label resourceLabel;
  public ComboBox<String> resourcesComboBox;
  public Label propLabel;
  public ComboBox<String> propsComboBox;

  private Runnable onCloseButtonClickedListener;
  private final ObservableList<Integer> validTeamSizes = FXCollections.observableList(IntStream.range(0, 17)
      .filter(value -> value != 1)
      .boxed()
      .collect(Collectors.toList()));
  private final FilteredList<Integer> selectableTeamSizes = new FilteredList<>(validTeamSizes);
  private final ObservableList<Integer> validSpawnCount = FXCollections.observableList(IntStream.range(2, 17)
      .boxed()
      .collect(Collectors.toList()));
  private final FilteredList<Integer> selectableSpawnCounts = new FilteredList<>(validSpawnCount);
  public Spinner<Integer> numTeamsSpinner;

  @Override
  protected void onInitialize() {
    JavaFxUtil.bindManagedToVisible(commandLineLabel, commandLineArgsText, mapStyleComboBox, mapStyleLabel, biomeComboBox, biomeLabel);
    initCommandlineArgs();
    initGenerationTypeComboBox();
    initSymmetryComboBox();
    initMapStyleComboBox();
    initCheckBoxes();
    initNumTeamsSpinner();
    initSpawnCountSpinner();
    initMapSizeSpinner();
    initSeedField();
    initGeneratorComboBox(terrainComboBox);
    initGeneratorComboBox(biomeComboBox);
    initGeneratorComboBox(resourcesComboBox);
    initGeneratorComboBox(propsComboBox);
  }

  private StringConverter<GenerationType> getGenerationTypeConverter() {
    return new StringConverter<>() {
      @Override
      public String toString(GenerationType generationType) {
        return i18n.get(generationType.getI18nKey());
      }

      @Override
      public GenerationType fromString(String s) {
        throw new UnsupportedOperationException();
      }
    };
  }

  private StringConverter<Double> getMapSizeConverter() {
    return new StringConverter<>() {
      @Override
      public String toString(Double mapSize) {
        return NumberFormat.getInstance().format(mapSize);
      }

      @Override
      public Double fromString(String s) {
        try {
          return Math.round(NumberFormat.getInstance().parse(s).doubleValue() / MIN_MAP_SIZE_STEP) * MIN_MAP_SIZE_STEP;
        } catch (ParseException e) {
          throw new IllegalArgumentException("Could not parse number", e);
        }
      }
    };
  }

  private void initCommandlineArgs() {
    String commandLineArgs = generatorPrefs.getCommandLineArgs();
    commandLineArgsText.setText(commandLineArgs);
    generatorPrefs.commandLineArgsProperty().bind(commandLineArgsText.textProperty());
    commandLineArgsText.disableProperty().bind(previousMapName.textProperty().isNotEmpty());
    if (!commandLineArgsText.getText().isBlank()) {
      commandLineArgsText.setVisible(true);
      commandLineLabel.setVisible(true);
    }
  }

  private void initGenerationTypeComboBox() {
    GenerationType generationType = generatorPrefs.getGenerationType();
    generationTypeComboBox.setItems(FXCollections.observableArrayList(GenerationType.values()));
    generationTypeComboBox.setConverter(getGenerationTypeConverter());
    generationTypeComboBox.setValue(generationType);
    generatorPrefs.generationTypeProperty().bind(generationTypeComboBox.valueProperty());
    generationTypeComboBox.disableProperty()
        .bind(previousMapName.textProperty().isNotEmpty()
            .or(commandLineArgsText.textProperty().isNotEmpty()));
  }

  private void initNumTeamsSpinner() {
    int numTeamsProperty = generatorPrefs.getNumTeams();
    numTeamsSpinner.setValueFactory(new ListSpinnerValueFactory<>(selectableTeamSizes));
    numTeamsSpinner.valueProperty().addListener((observable) -> {
      if (spawnCountSpinner.getValue() != null) {
        int spawnCount = spawnCountSpinner.getValue();
        int lastIndex = selectableSpawnCounts.indexOf(spawnCount);
        selectableSpawnCounts.setPredicate((value) -> {
          Integer numTeams = numTeamsSpinner.getValue();
          return numTeams == null || numTeams == 0 || value % numTeams == 0;
        });
        int newIndex = selectableSpawnCounts.indexOf(spawnCount);
        if (newIndex > 0) {
          int diff = newIndex - lastIndex;
          if (diff > 0) {
            spawnCountSpinner.increment(diff);
          } else {
            spawnCountSpinner.decrement(-diff);
          }
        }
      }
    });
    generatorPrefs.numTeamsProperty().bind(numTeamsSpinner.valueProperty());
    numTeamsSpinner.disableProperty()
        .bind(previousMapName.textProperty().isNotEmpty()
            .or(commandLineArgsText.textProperty().isNotEmpty()));
    int lastIndex = selectableTeamSizes.indexOf(numTeamsProperty);
    numTeamsSpinner.increment(lastIndex >= 0 ? lastIndex : 1);
  }

  private void initSpawnCountSpinner() {
    int spawnCountProperty = generatorPrefs.getSpawnCount();
    selectableSpawnCounts.setPredicate((value) -> {
      Integer numTeams = numTeamsSpinner.getValue();
      return numTeams == null || numTeams == 0 || value % numTeams == 0;
    });
    spawnCountSpinner.setValueFactory(new ListSpinnerValueFactory<>(selectableSpawnCounts));
    generatorPrefs.spawnCountProperty().bind(spawnCountSpinner.valueProperty());
    spawnCountSpinner.disableProperty()
        .bind(previousMapName.textProperty().isNotEmpty()
            .or(commandLineArgsText.textProperty().isNotEmpty()));
    int lastIndex = selectableSpawnCounts.indexOf(spawnCountProperty);
    spawnCountSpinner.increment(Math.max(lastIndex, 0));
  }

  private void initMapSizeSpinner() {
    double mapSize = generatorPrefs.getMapSizeInKm();
    mapSizeSpinner.setValueFactory(new DoubleSpinnerValueFactory(5, 20, mapSize, MIN_MAP_SIZE_STEP));
    mapSizeSpinner.getValueFactory().setConverter(getMapSizeConverter());
    generatorPrefs.mapSizeInKmProperty().bind(mapSizeSpinner.getValueFactory().valueProperty());
    mapSizeSpinner.disableProperty()
        .bind(previousMapName.textProperty().isNotEmpty()
            .or(commandLineArgsText.textProperty().isNotEmpty()));
  }

  private void initSymmetryComboBox() {
    symmetryComboBox.disableProperty()
        .bind(previousMapName.textProperty().isNotEmpty()
            .or(generationTypeComboBox.valueProperty().isNotEqualTo(GenerationType.CASUAL))
            .or(commandLineArgsText.textProperty().isNotEmpty()));
  }

  private void initMapStyleComboBox() {
    mapStyleComboBox.disableProperty()
        .bind(previousMapName.textProperty().isNotEmpty()
            .or(generationTypeComboBox.valueProperty().isNotEqualTo(GenerationType.CASUAL))
            .or(commandLineArgsText.textProperty().isNotEmpty())
            .or(customStyleCheckBox.selectedProperty()));
  }

  private void initCheckBoxes() {
    customStyleCheckBox.setSelected(generatorPrefs.getCustomStyle());
    customStyleCheckBox.disableProperty()
        .bind(previousMapName.textProperty().isNotEmpty()
            .or(generationTypeComboBox.valueProperty().isNotEqualTo(GenerationType.CASUAL))
            .or(commandLineArgsText.textProperty().isNotEmpty()));
    fixedSeedCheckBox.setSelected(generatorPrefs.getFixedSeed());
    fixedSeedCheckBox.disableProperty()
        .bind(previousMapName.textProperty().isNotEmpty()
            .or(generationTypeComboBox.valueProperty().isNotEqualTo(GenerationType.CASUAL))
            .or(commandLineArgsText.textProperty().isNotEmpty()));
  }

  private void initSeedField() {
    seedTextField.setText(String.valueOf(generatorPrefs.getSeed()));
    seedTextField.disableProperty()
        .bind(previousMapName.textProperty().isNotEmpty()
            .or(generationTypeComboBox.valueProperty().isNotEqualTo(GenerationType.CASUAL))
            .or(commandLineArgsText.textProperty().isNotEmpty())
            .or(fixedSeedCheckBox.selectedProperty().not()));
    seedRerollButton.disableProperty()
        .bind(previousMapName.textProperty().isNotEmpty()
            .or(generationTypeComboBox.valueProperty().isNotEqualTo(GenerationType.CASUAL))
            .or(commandLineArgsText.textProperty().isNotEmpty())
            .or(fixedSeedCheckBox.selectedProperty().not()));
  }

  private void initGeneratorComboBox(ComboBox<String> comboBox) {
    comboBox.disableProperty()
        .bind(previousMapName.textProperty().isNotEmpty()
            .or(generationTypeComboBox.valueProperty().isNotEqualTo(GenerationType.CASUAL))
            .or(commandLineArgsText.textProperty().isNotEmpty())
            .or(customStyleCheckBox.selectedProperty().not()));
  }

  private GeneratorOptions getGeneratorOptions() {
    GeneratorOptions.GeneratorOptionsBuilder optionsBuilder = GeneratorOptions.builder();
    if (!commandLineArgsText.getText().isBlank()) {
      optionsBuilder.commandLineArgs(commandLineArgsText.getText());
    }

    optionsBuilder.spawnCount(spawnCountSpinner.getValue());
    optionsBuilder.mapSize((int) (mapSizeSpinner.getValue() * KM_TO_PIXEL_FACTOR));
    optionsBuilder.numTeams(numTeamsSpinner.getValue());
    optionsBuilder.generationType(generationTypeComboBox.getValue());
    optionsBuilder.symmetry(symmetryComboBox.getValue());
    optionsBuilder.style(mapStyleComboBox.getValue());
    if (fixedSeedCheckBox.isSelected() && generationTypeComboBox.getValue() == GenerationType.CASUAL) {
      optionsBuilder.seed(seedTextField.getText());
    }
    if (customStyleCheckBox.isSelected() && generationTypeComboBox.getValue() == GenerationType.CASUAL) {
      optionsBuilder.terrainGenerator(terrainComboBox.getValue());
      optionsBuilder.textureGenerator(biomeComboBox.getValue());
      optionsBuilder.resourceGenerator(resourcesComboBox.getValue());
      optionsBuilder.propGenerator(propsComboBox.getValue());
    }
    return optionsBuilder.build();
  }

  public void onCloseButtonClicked() {
    if (onCloseButtonClickedListener != null) {
      onCloseButtonClickedListener.run();
    }
  }

  public void onGenerateMapButtonClicked() {
    onGenerateMap();
  }

  public void onGenerateMap() {
    Mono<String> generateFuture;
    if (!previousMapName.getText().isEmpty()) {
      if (!mapGeneratorService.isGeneratedMap(previousMapName.getText())) {
        log.warn(String.format("Invalid Generated Map Name %s", previousMapName.getText()));
        notificationService.addImmediateWarnNotification("mapGenerator.invalidName");
        return;
      }
      generateFuture = mapGeneratorService.generateMap(previousMapName.getText());
    } else {
      generateFuture = mapGeneratorService.generateMap(getGeneratorOptions());
    }

    generateFuture.subscribe(null, this::handleGenerationException);
    onCloseButtonClicked();
  }

  private void handleGenerationException(Throwable e) {
    switch (e) {
      case InvalidParameterException ignored -> {
        log.error("Map generation failed due to invalid parameter", e);
        notificationService.addImmediateErrorNotification(e, "mapGenerator.invalidName");
      }
      case UnsupportedVersionException ignored -> {
        log.warn("Map generation failed due to unsupported version", e);
        notificationService.addImmediateWarnNotification("mapGenerator.tooNewVersion");
      }
      case OutdatedVersionException ignored -> {
        log.warn("Map generation failed due to outdated version", e);
        notificationService.addImmediateWarnNotification("mapGenerator.tooOldVersion");
      }
      case null, default -> {
        log.error("Map generation failed", e);
        notificationService.addImmediateErrorNotification(e, "mapGenerator.generationFailed");
      }
    }
  }

  @VisibleForTesting
  void toggleCommandlineInput() {
    commandLineLabel.setVisible(!commandLineLabel.isVisible());
    commandLineArgsText.setVisible(!commandLineArgsText.isVisible());
  }

  protected void setCreateGameController(CreateGameController controller) {
    createGameController = controller;
  }

  protected void setSymmetries(List<String> symmetries) {
    ArrayList<String> symmetryList = new ArrayList<>(List.of(MapGeneratorService.GENERATOR_RANDOM_OPTION));
    symmetryList.addAll(symmetries);
    symmetryComboBox.setItems(FXCollections.observableList(symmetryList));
    String symmetry = generatorPrefs.getSymmetry();
    if (symmetryComboBox.getItems().contains(symmetry)) {
      symmetryComboBox.getSelectionModel().select(symmetry);
    } else {
      symmetryComboBox.getSelectionModel().select(MapGeneratorService.GENERATOR_RANDOM_OPTION);
    }
    generatorPrefs.symmetryProperty().bind(symmetryComboBox.valueProperty());
  }

  protected void setStyles(List<String> styles) {
    ArrayList<String> styleList = new ArrayList<>(List.of(MapGeneratorService.GENERATOR_RANDOM_OPTION));
    styleList.addAll(styles);
    mapStyleComboBox.setItems(FXCollections.observableList(styleList));
    String mapStyle = generatorPrefs.getMapStyle();
    if (mapStyleComboBox.getItems().contains(mapStyle)) {
      mapStyleComboBox.getSelectionModel().select(mapStyle);
    } else {
      mapStyleComboBox.getSelectionModel().select(MapGeneratorService.GENERATOR_RANDOM_OPTION);
    }
    generatorPrefs.mapStyleProperty().bind(mapStyleComboBox.valueProperty());
  }

  protected void setTerrainGenerators(List<String> terrainGenerators) {
    ArrayList<String> terrainGeneratorList = new ArrayList<>(List.of(MapGeneratorService.GENERATOR_RANDOM_OPTION));
    terrainGeneratorList.addAll(terrainGenerators);
    terrainComboBox.setItems(FXCollections.observableList(terrainGeneratorList));
    String terrainGenerator = generatorPrefs.getTerrainGenerator();
    if (terrainComboBox.getItems().contains(terrainGenerator)) {
      terrainComboBox.getSelectionModel().select(terrainGenerator);
    } else {
      terrainComboBox.getSelectionModel().select(MapGeneratorService.GENERATOR_RANDOM_OPTION);
    }
    generatorPrefs.terrainGeneratorProperty().bind(terrainComboBox.valueProperty());
  }

  protected void setTextureGenerators(List<String> textureGenerators) {
    ArrayList<String> textureGeneratorList = new ArrayList<>(List.of(MapGeneratorService.GENERATOR_RANDOM_OPTION));
    textureGeneratorList.addAll(textureGenerators);
    biomeComboBox.setItems(FXCollections.observableList(textureGeneratorList));
    String textureGenerator = generatorPrefs.getTextureGenerator();
    if (biomeComboBox.getItems().contains(textureGenerator)) {
      biomeComboBox.getSelectionModel().select(textureGenerator);
    } else {
      biomeComboBox.getSelectionModel().select(MapGeneratorService.GENERATOR_RANDOM_OPTION);
    }
    generatorPrefs.textureGeneratorProperty().bind(biomeComboBox.valueProperty());
  }

  protected void setResourceGenerators(List<String> resourceGenerators) {
    ArrayList<String> resourceGeneratorList = new ArrayList<>(List.of(MapGeneratorService.GENERATOR_RANDOM_OPTION));
    resourceGeneratorList.addAll(resourceGenerators);
    resourcesComboBox.setItems(FXCollections.observableList(resourceGeneratorList));
    String resourceGenerator = generatorPrefs.getResourceGenerator();
    if (resourcesComboBox.getItems().contains(resourceGenerator)) {
      resourcesComboBox.getSelectionModel().select(resourceGenerator);
    } else {
      resourcesComboBox.getSelectionModel().select(MapGeneratorService.GENERATOR_RANDOM_OPTION);
    }
    generatorPrefs.resourceGeneratorProperty().bind(resourcesComboBox.valueProperty());
  }

  protected void setPropGenerators(List<String> propGenerators) {
    ArrayList<String> propGeneratorList = new ArrayList<>(List.of(MapGeneratorService.GENERATOR_RANDOM_OPTION));
    propGeneratorList.addAll(propGenerators);
    propsComboBox.setItems(FXCollections.observableList(propGeneratorList));
    String propGenerator = generatorPrefs.getPropGenerator();
    if (propsComboBox.getItems().contains(propGenerator)) {
      propsComboBox.getSelectionModel().select(propGenerator);
    } else {
      propsComboBox.getSelectionModel().select(MapGeneratorService.GENERATOR_RANDOM_OPTION);
    }
    generatorPrefs.propGeneratorProperty().bind(propsComboBox.valueProperty());
  }

  public void onNewLabelClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
      toggleCommandlineInput();
    }
  }

  @Override
  public Pane getRoot() {
    return generateMapRoot;
  }

  void setOnCloseButtonClickedListener(Runnable onCloseButtonClickedListener) {
    this.onCloseButtonClickedListener = onCloseButtonClickedListener;
  }

  public void onSeedRerollButtonClicked() {
    seedTextField.setText(String.valueOf(new Random().nextLong()));
  }
}
