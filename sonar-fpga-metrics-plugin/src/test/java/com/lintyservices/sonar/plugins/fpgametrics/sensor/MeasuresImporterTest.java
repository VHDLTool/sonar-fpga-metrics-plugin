/*
 * SonarQube Linty FPGA Metrics :: Plugin
 * Copyright (C) 2020-2020 Linty Services
 * mailto:contact@linty-services.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.lintyservices.sonar.plugins.fpgametrics.sensor;

import org.junit.Test;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.measures.Metric;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class MeasuresImporterTest {

  private final List<Metric> metrics = new MetricsImporter().getMetrics();

  @Test
  public void should_properly_load_project_measures() {
    SensorContextTester contextTester = loadMeasuresFromFile(
      "src/test/files/measures/valid/",
      metrics
    );

    Measure<Integer> intMeasure = contextTester.measure(contextTester.module().key(), "NX_Log_Remarks");
    Measure<Double> floatMeasure = contextTester.measure(contextTester.module().key(), "NX_CLK1_Max_Delay");
    Measure<Double> percentage = contextTester.measure(contextTester.module().key(), "NX_4LUT_PERCENT");

    assertEquals(Integer.valueOf(1), intMeasure.value());
    assertEquals(Double.valueOf(54.385), floatMeasure.value());
    assertEquals(Double.valueOf(16636.0 * 100.0 / 129024.0), percentage.value());
  }

  // TODO
  //@Test
  //public void should_properly_load_file_measures() {
  //}

  @Test
  public void should_log_an_info_message_stating_that_json_measures_file_does_not_exist() {
    loadMeasuresFromFile("src/test/files/measures/does-not-exist/", metrics);
    // TODO: Try to catch log message
  }

  @Test
  public void should_throw_an_exception_with_an_invalid_json_file() {
    Exception thrown = assertThrows(
      IllegalStateException.class,
      () -> loadMeasuresFromFile("src/test/files/measures/invalid/", metrics)
    );
    assertEquals("[FPGA Metrics] Cannot parse JSON measures report: measures.json", thrown.getMessage());
  }

  @Test
  public void should_throw_an_exception_while_an_unknown_metric_is_found_in_measures_json_file() {
    Exception thrown = assertThrows(
      IllegalStateException.class,
      () -> loadMeasuresFromFile("src/test/files/measures/unknown-metric/", metrics)
    );
    assertEquals("[FPGA Metrics] Metric with 'UNKNOWN_METRIC' key cannot be found", thrown.getMessage());
  }

  private SensorContextTester loadMeasuresFromFile(String filePath, List<Metric> metrics) {
    MeasuresImporter measuresImporter = new MeasuresImporter(metrics, filePath);
    SensorContextTester contextTester = SensorContextTester.create(new File("src/test/files/measures/ctx"));
    measuresImporter.execute(contextTester);
    return contextTester;
  }
}
