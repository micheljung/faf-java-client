package com.faforever.client.preferences;

import com.faforever.client.map.generator.GenerationType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class GeneratorPrefs {
  private final ObjectProperty<GenerationType> generationType = new SimpleObjectProperty<>(GenerationType.CASUAL);
  private final StringProperty commandLineArgs = new SimpleStringProperty("");
  private final IntegerProperty spawnCount = new SimpleIntegerProperty(6);
  private final IntegerProperty numTeams = new SimpleIntegerProperty(2);
  private final DoubleProperty mapSizeInKm = new SimpleDoubleProperty(10);
  private final StringProperty mapStyle = new SimpleStringProperty("");
  private final StringProperty symmetry = new SimpleStringProperty("");
  private final IntegerProperty seed = new SimpleIntegerProperty(0);
  private final BooleanProperty fixedSeed = new SimpleBooleanProperty(true);
  private final BooleanProperty customStyle = new SimpleBooleanProperty(true);
  private final StringProperty terrainGenerator = new SimpleStringProperty("");
  private final StringProperty textureGenerator = new SimpleStringProperty("");
  private final StringProperty resourceGenerator = new SimpleStringProperty("");
  private final StringProperty propGenerator = new SimpleStringProperty("");

  public GenerationType getGenerationType() {
    return generationType.get();
  }

  public void setGenerationType(GenerationType generationType) {
    this.generationType.set(generationType);
  }

  public ObjectProperty<GenerationType> generationTypeProperty() {
    return generationType;
  }

  public String getCommandLineArgs() {
    return commandLineArgs.get();
  }

  public void setCommandLineArgs(String commandLineArgs) {
    this.commandLineArgs.set(commandLineArgs);
  }

  public StringProperty commandLineArgsProperty() {
    return commandLineArgs;
  }

  public int getSpawnCount() {
    return spawnCount.get();
  }

  public void setSpawnCount(int spawnCount) {
    this.spawnCount.set(spawnCount);
  }

  public IntegerProperty spawnCountProperty() {
    return spawnCount;
  }

  public int getNumTeams() {
    return numTeams.get();
  }

  public void setNumTeams(int numTeams) {
    this.numTeams.set(numTeams);
  }

  public IntegerProperty numTeamsProperty() {
    return numTeams;
  }

  public double getMapSizeInKm() {
    return mapSizeInKm.get();
  }

  public void setMapSizeInKm(Double mapSizeInKm) {
    this.mapSizeInKm.set(mapSizeInKm);
  }

  public DoubleProperty mapSizeInKmProperty() {
    return mapSizeInKm;
  }

  public String getMapStyle() {
    return mapStyle.get();
  }

  public void setMapStyle(String mapStyle) {
    this.mapStyle.set(mapStyle);
  }

  public StringProperty mapStyleProperty() {
    return mapStyle;
  }

  public String getSymmetry() {
    return symmetry.get();
  }

  public void setSymmetry(String symmetry) {
    this.symmetry.set(symmetry);
  }

  public StringProperty symmetryProperty() {
    return symmetry;
  }

  public int getSeed() {
    return seed.get();
  }

  public void setSeed(int seed) {
    this.seed.set(seed);
  }

  public IntegerProperty seedProperty() {
    return seed;
  }

  public boolean getFixedSeed() {
    return fixedSeed.get();
  }

  public void setFixedSeed(boolean fixedSeed) {
    this.fixedSeed.set(fixedSeed);
  }

  public BooleanProperty fixedSeedProperty() {
    return fixedSeed;
  }

  public boolean getCustomStyle() {
    return customStyle.get();
  }

  public void setCustomStyle(boolean customStyle) {
    this.customStyle.set(customStyle);
  }

  public BooleanProperty customStyleProperty() {
    return customStyle;
  }

  public String getTerrainGenerator() {
    return terrainGenerator.get();
  }

  public void setTerrainGenerator(String terrainGenerator) {
    this.terrainGenerator.set(terrainGenerator);
  }

  public StringProperty terrainGeneratorProperty() {
    return terrainGenerator;
  }

  public String getTextureGenerator() {
    return textureGenerator.get();
  }

  public void setTextureGenerator(String textureGenerator) {
    this.textureGenerator.set(textureGenerator);
  }

  public StringProperty textureGeneratorProperty() {
    return textureGenerator;
  }

  public String getResourceGenerator() {
    return resourceGenerator.get();
  }

  public void setResourceGenerator(String resourceGenerator) {
    this.resourceGenerator.set(resourceGenerator);
  }

  public StringProperty resourceGeneratorProperty() {
    return resourceGenerator;
  }

  public String getPropGenerator() {
    return propGenerator.get();
  }

  public void setPropGenerator(String propGenerator) {
    this.propGenerator.set(propGenerator);
  }

  public StringProperty propGeneratorProperty() {
    return propGenerator;
  }
}
