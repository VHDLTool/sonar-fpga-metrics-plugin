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

  private static final SensorContextTester contextTester = SensorContextTester.create(new File("src/test/files/measures/ctx"));

  @Test
  public void should_succeed_with_existing_file() {
    List<Metric> metrics = new MetricsImporter().getMetrics();
    MeasuresImporter measuresImporter = new MeasuresImporter(metrics, "src/test/files/measures/valid/");
    measuresImporter.execute(contextTester);
    Measure<Integer> intMeasure = contextTester.measure(contextTester.module().key(), "NX_Log_Remarks");
    Measure<Double> floatMeasure = contextTester.measure(contextTester.module().key(), "NX_CLK1_Max_Delay");

    assertEquals((Integer) 1, intMeasure.value());
    assertEquals((Double) 54.385, floatMeasure.value());
    // TODO: Add more checks for any type of data
  }

  @Test
  public void should_log_an_info_message_stating_that_json_measures_file_does_not_exist() {
    MeasuresImporter measuresImporter = new MeasuresImporter(null, "src/test/measures/does-not-exist/");
    measuresImporter.execute(contextTester);
    // TODO: Try to catch log message
  }

  @Test
  public void should_throw_an_exception_with_an_invalid_json_file() {
    Exception thrown = assertThrows(IllegalStateException.class, this::loadMeasuresFromInvalidFile);
    assertEquals("[FPGA Metrics] Cannot parse JSON report", thrown.getMessage());
  }

  private void loadMeasuresFromInvalidFile() {
    MeasuresImporter measuresImporter = new MeasuresImporter(null, "src/test/files/measures/invalid/");
    measuresImporter.execute(contextTester);
  }
}
