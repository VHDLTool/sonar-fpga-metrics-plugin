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

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.FilenameUtils;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.measures.Metric;
import org.sonar.api.scanner.sensor.ProjectSensor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class MeasuresImporter implements ProjectSensor {

  private static final Logger LOG = Loggers.get(MeasuresImporter.class);
  private Map<String, Metric> metrics;

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.name("Import custom FPGA measures from JSON files");
  }

  @Override
  public void execute(SensorContext context) {
    metrics = Maps.uniqueIndex(new MetricsImporter().getMetrics(), Metric::getKey);
    addAllMeasuresToProject(context);

    FileSystem fs = context.fileSystem();
    Iterable<InputFile> files = fs.inputFiles(fs.predicates().and(
      fs.predicates().hasLanguage("vhdl"),
      fs.predicates().hasType(InputFile.Type.MAIN)
    ));
    for (InputFile file : files) {
      addAllMeasuresToFile(context, file);
    }
  }

  private Map<String, Object> getMeasuresFromJsonFile(String filePath) {
    try {
      return new Gson().fromJson(new FileReader(filePath), Map.class);
    } catch (FileNotFoundException e) {
      LOG.info("[FPGA Metrics] No measures report found: " + filePath);
      return Collections.emptyMap();
    } catch (JsonSyntaxException | JsonIOException e) {
      throw new IllegalStateException("[FPGA Metrics] Cannot parse JSON measures report: " + filePath);
    }
  }

  private void addAllMeasuresToProject(SensorContext context) {
    Map<String, Object> measures = getMeasuresFromJsonFile(context.fileSystem().baseDir().getPath() + File.separator + "measures.json");
    for (Map.Entry<String, Object> measure : measures.entrySet()) {
      addNewMeasure(context, null, getMetricFromKey(measure.getKey()), measure.getValue());
    }
  }

  private void addAllMeasuresToFile(SensorContext context, InputFile file) {
    Map<String, Object> measures = getMeasuresFromJsonFile(
      context.fileSystem().baseDir().getPath() + File.separator
        + FilenameUtils.removeExtension(file.filename()) + "_measures.json"
    );

    for (Map.Entry<String, Object> measure : measures.entrySet()) {
      addNewMeasure(context, file, getMetricFromKey(measure.getKey()), measure.getValue());
    }
  }

  private void addNewMeasure(SensorContext context, InputFile file, Metric metric, Object rawMeasure) {
    Serializable measure = getTypedMeasure(
      metric.getType().name(),
      getMeasure(rawMeasure),
      getRatioMax(rawMeasure)
    );

    if (file != null) {
      context.newMeasure().forMetric(metric).on(file).withValue(measure).save();
    } else {
      context.newMeasure().forMetric(metric).on(context.project()).withValue(measure).save();
    }
  }

  private Object getMeasure(Object rawValue) {
    if (rawValue.getClass().equals(ArrayList.class)) {
      return ((ArrayList) rawValue).get(0);
    }
    return rawValue;
  }

  private Double getRatioMax(Object rawValue) {
    if (rawValue.getClass().equals(ArrayList.class)) {
      return (Double) ((ArrayList) rawValue).get(1);
    }
    return null;
  }

  private Metric getMetricFromKey(String metricKey) {
    Metric metric = metrics.get(metricKey);
    if (metric == null) {
      throw new IllegalStateException("[FPGA Metrics] Metric with '" + metricKey + "' key cannot be found");
    }
    return metric;
  }

  private Serializable getTypedMeasure(String metricType, Object measureValue, Double ratioMax) {
    switch (metricType) {
      case "INT":
        return (int) Math.round((Double) measureValue);
      case "FLOAT":
        return (Double) measureValue;
      case "PERCENT":
        return ((Double) measureValue) * 100.0 / ratioMax;
      case "BOOL":
        return (Boolean) measureValue;
      case "STRING":
      case "DATA":
      case "DISTRIB":
        return (String) measureValue;
      case "MILLISEC":
      case "RATING":
      case "WORK_DUR":
        return Math.round((Double) measureValue);
      default:
        throw new IllegalStateException("[FPGA Metrics] '" + metricType + "' metric type not recognized.");
    }
  }
}
