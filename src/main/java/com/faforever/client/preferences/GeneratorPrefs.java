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
  private final StringProperty seed = new SimpleStringProperty("");
  private final BooleanProperty fixedSeed = new SimpleBooleanProperty(false);
  private final BooleanProperty customStyle = new SimpleBooleanProperty(false);
  private final StringProperty terrainStyle = new SimpleStringProperty("");
  private final StringProperty textureStyle = new SimpleStringProperty("");
  private final StringProperty resourceStyle = new SimpleStringProperty("");
  private final StringProperty propStyle = new SimpleStringProperty("");
  private final IntegerProperty reclaimDensityMin = new SimpleIntegerProperty(0);
  private final IntegerProperty reclaimDensityMax = new SimpleIntegerProperty(127);
  private final IntegerProperty resourcesDensityMin = new SimpleIntegerProperty(0);
  private final IntegerProperty resourcesDensityMax = new SimpleIntegerProperty(127);

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

  public String getSeed() {
    return seed.get();
  }

  public void setSeed(String seed) {
    this.seed.set(seed);
  }

  public StringProperty seedProperty() {
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

  public String getTerrainStyle() {
    return terrainStyle.get();
  }

  public void setTerrainStyle(String terrainStyle) {
    this.terrainStyle.set(terrainStyle);
  }

  public StringProperty terrainStyleProperty() {
    return terrainStyle;
  }

  public String getTextureStyle() {
    return textureStyle.get();
  }

  public void setTextureStyle(String textureStyle) {
    this.textureStyle.set(textureStyle);
  }

  public StringProperty textureStyleProperty() {
    return textureStyle;
  }

  public String getResourceStyle() {
    return resourceStyle.get();
  }

  public void setResourceStyle(String resourceStyle) {
    this.resourceStyle.set(resourceStyle);
  }

  public StringProperty resourceStyleProperty() {
    return resourceStyle;
  }

  public String getPropStyle() {
    return propStyle.get();
  }

  public void setPropStyle(String propStyle) {
    this.propStyle.set(propStyle);
  }

  public StringProperty propStyleProperty() {
    return propStyle;
  }

  public boolean isFixedSeed() {
    return fixedSeed.get();
  }

  public boolean isCustomStyle() {
    return customStyle.get();
  }

  public int getReclaimDensityMin() {
    return reclaimDensityMin.get();
  }

  public IntegerProperty reclaimDensityMinProperty() {
    return reclaimDensityMin;
  }

  public void setReclaimDensityMin(int min) {
    this.reclaimDensityMin.set(min);
  }

  public int getReclaimDensityMax() {
    return reclaimDensityMax.get();
  }

  public IntegerProperty reclaimDensityMaxProperty() {
    return reclaimDensityMax;
  }

  public void setReclaimDensityMax(int max) {
    this.reclaimDensityMax.set(max);
  }

  public int getResourcesDensityMin() {
    return resourcesDensityMin.get();
  }

  public IntegerProperty resourcesDensityMinProperty() {
    return resourcesDensityMin;
  }

  public void setResourcesDensityMin(int min) {
    this.resourcesDensityMin.set(min);
  }

  public int getResourcesDensityMax() {
    return resourcesDensityMax.get();
  }

  public IntegerProperty resourcesDensityMaxProperty() {
    return resourcesDensityMax;
  }

  public void setResourcesDensityMax(int max) {
    this.resourcesDensityMax.set(max);
  }
}
