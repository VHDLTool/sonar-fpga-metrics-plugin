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

public class MeasuresImporterTest {

  private static final SensorContextTester contextTester = SensorContextTester.create(new File("src/test/files/ctx"));

  @Test
  public void should_succeed_with_existing_file() {
    List<Metric> metrics = new MetricsImporter().getMetrics();
    MeasuresImporter measuresImporter = new MeasuresImporter(metrics, "src/test/files/");
    measuresImporter.execute(contextTester);
    Measure<Integer> intMeasure = contextTester.measure(contextTester.module().key(), "NX_Log_Remarks");
    Measure<Double> floatMeasure = contextTester.measure(contextTester.module().key(), "NX_CLK1_Max_Delay");
    assertEquals((Integer) 1, intMeasure.value());
    assertEquals((Double) 54.385, floatMeasure.value());
    // TODO: Add more checks
  }

  @Test
  public void should_throw_a_warning_with_non_existing_file() {
    MeasuresImporter measuresImporter = new MeasuresImporter(null, "src/test/files_folder_does_not_exist/");
    measuresImporter.execute(contextTester);
    // TODO: Add check on exception if we decide to go to exception. Otherwise try to capture warning message.
  }
}
