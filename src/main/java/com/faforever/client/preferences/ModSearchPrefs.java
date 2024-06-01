package com.faforever.client.preferences;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;

public class ModSearchPrefs {
  private StringProperty modNameField = new SimpleStringProperty("");
  private StringProperty modAuthorField = new SimpleStringProperty("");
  private final ObjectProperty<LocalDate> uploadedBeforeDate = new SimpleObjectProperty<LocalDate>();
  private final ObjectProperty<LocalDate> uploadedAfterDate = new SimpleObjectProperty<LocalDate>();
  private final ObjectProperty<Double> averageReviewScoresMin = new SimpleObjectProperty<Double>();
  private final ObjectProperty<Double> averageReviewScoresMax = new SimpleObjectProperty<Double>();
  private final BooleanProperty uiMod = new SimpleBooleanProperty();
  private final BooleanProperty simMod = new SimpleBooleanProperty();
  private final BooleanProperty onlyRanked = new SimpleBooleanProperty();

  public String getModNameField() {
    return modNameField.get();
  }

  public void setModNameField(String modNameField) {
    this.modNameField.set(modNameField);
  }

  public StringProperty modNameFieldProperty() {
    return modNameField;
  }

  public String getModAuthorField() {
    return modAuthorField.get();
  }

  public void setModAuthorField(String modAuthorField) {
    this.modAuthorField.set(modAuthorField);
  }

  public StringProperty modAuthorFieldProperty() {
    return modAuthorField;
  }

  public LocalDate getUploadedBeforeDate() {
    return uploadedBeforeDate.get();
  }

  public void setUploadedBeforeDate(LocalDate uploadedBeforeDate) {
    this.uploadedBeforeDate.set(uploadedBeforeDate);
  }

  public ObjectProperty<LocalDate> uploadedBeforeDateProperty() {
    return uploadedBeforeDate;
  }

  public LocalDate getUploadedAfterDate() {
    return uploadedAfterDate.get();
  }

  public void setUploadedAfterDate(LocalDate uploadedAfterDate) {
    this.uploadedAfterDate.set(uploadedAfterDate);
  }

  public ObjectProperty<LocalDate> uploadedAfterDateProperty() {
    return uploadedAfterDate;
  }

  public Double getAverageReviewScoresMin() {
    return averageReviewScoresMin.get();
  }

  public void setAverageReviewScoresMin(Double averageReviewScoresMin) {
    this.averageReviewScoresMin.set(averageReviewScoresMin);
  }

  public ObjectProperty<Double> averageReviewScoresMinProperty() {
    return averageReviewScoresMin;
  }

  public Double getAverageReviewScoresMax() {
    return averageReviewScoresMax.get();
  }

  public void setAverageReviewScoresMax(Double averageReviewScoresMax) {
    this.averageReviewScoresMax.set(averageReviewScoresMax);
  }

  public ObjectProperty<Double> averageReviewScoresMaxProperty() {
    return averageReviewScoresMax;
  }

  public Boolean getUiMod() {
    return uiMod.get();
  }

  public void setUiMod(Boolean uiMod) {
    this.uiMod.set(uiMod);
  }

  public BooleanProperty uiModProperty() {
    return uiMod;
  }

  public Boolean getSimMod() {
    return simMod.get();
  }

  public void setSimMod(Boolean simMod) {
    this.simMod.set(simMod);
  }

  public BooleanProperty simModProperty() {
    return simMod;
  }

  public Boolean getOnlyRanked() {
    return onlyRanked.get();
  }

  public void setOnlyRanked(Boolean onlyRanked) {
    this.onlyRanked.set(onlyRanked);
  }

  public BooleanProperty onlyRankedProperty() {
    return onlyRanked;
  }
}
