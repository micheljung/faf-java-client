package com.faforever.client.query;

import com.faforever.client.i18n.I18n;
import com.faforever.client.test.PlatformTest;
import com.github.rutledgepaulv.qbuilders.builders.QBuilder;
import com.github.rutledgepaulv.qbuilders.conditions.Condition;
import com.github.rutledgepaulv.qbuilders.properties.concrete.DoubleProperty;
import com.github.rutledgepaulv.qbuilders.visitors.RSQLVisitor;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.MenuButton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RangeFilterControllerTest extends PlatformTest {

  private final String propertyName = "testProp";
  private final double max = 100;
  private final double min = 0;
  private final double increment = 1.0;

  @InjectMocks
  private RangeFilterController instance;
  @Mock
  private I18n i18n;
  @Mock
  private InvalidationListener queryListener;

  @BeforeEach
  public void setUp() throws Exception {
    loadFxml("theme/vault/search/rangeFilter.fxml", clazz -> instance);

    instance.setPropertyName(propertyName);
    instance.setRange(min, max, 10, 0);
    instance.setIncrement(increment);
    instance.setSnapToTicks(true);
    instance.setTickUnit(increment);
    instance.setValueTransform((value) -> value);
    instance.bind();
  }

  @Test
  public void testTextBinding() throws Exception {
    instance.rangeSlider.setLowValue(10.0);
    instance.rangeSlider.setHighValue(90.0);

    assertEquals(instance.lowValue.getText(), "10");
    assertEquals(instance.highValue.getText(), "90");

    instance.rangeSlider.setLowValue(min);
    instance.rangeSlider.setHighValue(max);

    assertEquals(instance.lowValue.getText(), "");
    assertEquals(instance.highValue.getText(), "");
  }

  @Test
  public void testSliderBinding() throws Exception {
    instance.lowValue.setText("20");
    instance.highValue.setText("80");

    assertEquals(instance.rangeSlider.getLowValue(), 20, 0);
    assertEquals(instance.rangeSlider.getHighValue(), 80, 0);

    instance.lowValue.setText("a");
    instance.highValue.setText("a");

    assertEquals(min, instance.rangeSlider.getLowValue(), 0);
    assertEquals(max, instance.rangeSlider.getHighValue(), 0);
  }

  @Test
  public void testAddListener() throws Exception {
    instance.addQueryListener(queryListener);
    instance.rangeSlider.setLowValue(10.0);
    instance.rangeSlider.setHighValue(90.0);
    instance.lowValue.setText("20");
    instance.highValue.setText("80");
    verify(queryListener, times(20)).invalidated(any());
  }

  @Test
  public void testClear() throws Exception {
    instance.rangeSlider.setLowValue(10.0);
    instance.rangeSlider.setHighValue(90.0);
    instance.clear();

    assertEquals(instance.rangeSlider.getLowValue(), min, 0);
    assertEquals(instance.rangeSlider.getHighValue(), max, 0);
  }

  @Test
  public void testSetTitle() throws Exception {
    when(i18n.get(anyString(), anyString(), anyString(), anyString())).thenReturn("Test");
    instance.setTitle("Test");

    assertTrue(instance.menu.textProperty().isBound());
    assertEquals("Test", instance.menu.getText());
  }

  @Test
  public void testGetRoot() throws Exception {
    assertTrue(instance.getRoot() instanceof MenuButton);
  }

  @Test
  public void testGetConditionMaxRange() throws Exception {
    assertTrue(instance.getCondition().isEmpty());
    assertFalse(instance.menu.getStyleClass().contains("query-filter-selected"));
  }

  @Test
  public void testGetConditionMaximum() throws Exception {
    DoubleProperty property = new QBuilder<>().doubleNum(propertyName);
    instance.rangeSlider.setHighValue(50.0);

    Optional<List<Condition>> result = instance.getCondition();

    assertTrue(result.isPresent());
    assertEquals(result.get().getFirst().query(new RSQLVisitor()), property.lte(50.0).query(new RSQLVisitor()));
    assertTrue(instance.menu.getStyleClass().contains("query-filter-selected"));
  }

  @Test
  public void testGetConditionMinimum() throws Exception {
    DoubleProperty property = new QBuilder<>().doubleNum(propertyName);
    instance.rangeSlider.setLowValue(50.0);

    Optional<List<Condition>> result = instance.getCondition();

    assertTrue(result.isPresent());
    assertEquals(result.get().getFirst().query(new RSQLVisitor()), property.gte(50.0).query(new RSQLVisitor()));
    assertTrue(instance.menu.getStyleClass().contains("query-filter-selected"));
  }

  @Test
  public void testGetConditionRange() throws Exception {
    instance.rangeSlider.setLowValue(50.0);
    instance.rangeSlider.setHighValue(50.0);

    Optional<List<Condition>> result = instance.getCondition();

    assertTrue(result.isPresent());
    assertEquals(result.get().getFirst().query(new RSQLVisitor()),
                 new QBuilder<>().doubleNum(propertyName).gte(50.0).query(new RSQLVisitor()));
    assertEquals(result.get().get(1).query(new RSQLVisitor()), new QBuilder<>().doubleNum(propertyName).lte(50.0).query(new RSQLVisitor()));
    assertTrue(instance.menu.getStyleClass().contains("query-filter-selected"));
  }

  @Test
  public void testTicks() {
    instance.setRange(-10.0, 90.0, 5, 1);

    assertEquals(20.0, instance.rangeSlider.getMajorTickUnit());
    assertEquals(1, instance.rangeSlider.getMinorTickCount());
    assertEquals(10.0, instance.rangeSlider.getBlockIncrement());
  }

  @Test
  public void testPersistentPropertiesGetsValuesFromRangeFilter() {
    javafx.beans.property.DoubleProperty lowerValue = new SimpleDoubleProperty();
    javafx.beans.property.DoubleProperty higherValue = new SimpleDoubleProperty();
    lowerValue.bind(instance.lowValueProperty());
    higherValue.bind(instance.highValueProperty());
    instance.lowValue.setText("20");
    instance.highValue.setText("80");

    assertEquals(lowerValue.get(), 20, 0);
    assertEquals(higherValue.get(), 80, 0);

    instance.lowValue.setText("a");
    instance.highValue.setText("a");

    assertEquals(min, lowerValue.get(), 0);
    assertEquals(max, higherValue.get(), 0);
  }
}
