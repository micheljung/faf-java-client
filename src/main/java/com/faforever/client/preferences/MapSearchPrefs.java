package com.faforever.client.preferences;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;

public class MapSearchPrefs {
  private final StringProperty mapNameField = new SimpleStringProperty("");
  private final StringProperty mapAuthorField = new SimpleStringProperty("");
  private final ObjectProperty<LocalDate> uploadedBeforeDate = new SimpleObjectProperty<LocalDate>();
  private final ObjectProperty<LocalDate> uploadedAfterDate = new SimpleObjectProperty<LocalDate>();
  private final ListProperty<String> mapWidthFilter = new SimpleListProperty<>(
    FXCollections.observableArrayList()
  );
  private final ListProperty<String> mapHeightFilter = new SimpleListProperty<>(
    FXCollections.observableArrayList()
  );
  private final ObjectProperty<Double> maxPlayersMin = new SimpleObjectProperty<Double>();
  private final ObjectProperty<Double> maxPlayersMax = new SimpleObjectProperty<Double>();
  private final ObjectProperty<Double> averageReviewScoresMin = new SimpleObjectProperty<Double>();
  private final ObjectProperty<Double> averageReviewScoresMax = new SimpleObjectProperty<Double>();
  private final BooleanProperty onlyRanked = new SimpleBooleanProperty();

  public String getMapNameField() {
    return mapNameField.get();
  }
  
  public void setMapNameField(String mapNameField) {
    this.mapNameField.set(mapNameField);
  }
  
  public StringProperty mapNameFieldProperty() {
    return mapNameField;
  }

  public String getMapAuthorField() {
		return mapAuthorField.get();
	}

	public void setMapAuthorField(String mapAuthorField) {
		this.mapAuthorField.set(mapAuthorField);
	}

	public StringProperty mapAuthorFieldProperty() {
		return mapAuthorField;
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
  
  public ObservableList<String> getMapWidthFilter() {
		return mapWidthFilter.get();
	}

	public void setMapWidthFilter(ObservableList<String> mapWidthFilter) {
		this.mapWidthFilter.set(mapWidthFilter);
	}

	public ListProperty<String> mapWidthFilterProperty() {
		return mapWidthFilter;
	}

  public ObservableList<String> getMapHeightFilter() {
		return mapHeightFilter.get();
	}

	public void setMapHeightFilter(ObservableList<String> mapHeightFilter) {
		this.mapHeightFilter.set(mapHeightFilter);
	}

	public ListProperty<String> mapHeightFilterProperty() {
		return mapHeightFilter;
	}

  public Double getMaxPlayersMin() {
	  return maxPlayersMin.get();
  }

	public void setMaxPlayersMin(Double maxPlayersMin) {
		this.maxPlayersMin.set(maxPlayersMin);
	}

	public ObjectProperty<Double> maxPlayersMinProperty() {
		return maxPlayersMin;
	}

  public Double getMaxPlayersMax() {
		return maxPlayersMax.get();
	}

	public void setMaxPlayersMax(Double maxPlayersMax) {
		this.maxPlayersMax.set(maxPlayersMax);
	}

	public ObjectProperty<Double> maxPlayersMaxProperty() {
		return maxPlayersMax;
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
