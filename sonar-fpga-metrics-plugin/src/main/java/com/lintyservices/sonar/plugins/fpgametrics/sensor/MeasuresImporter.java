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

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.measures.Metric;
import org.sonar.api.scanner.sensor.ProjectSensor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class MeasuresImporter implements ProjectSensor {

  private static final Logger LOG = Loggers.get(MeasuresImporter.class);
  private List<Metric> metrics;
  private String basePath;

  @VisibleForTesting
  MeasuresImporter(List<Metric> metrics, String basePath) {
    this.metrics = metrics;
    this.basePath = basePath;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.name("Import custom FPGA measures from JSON files");
  }

  @Override
  public void execute(SensorContext context) {
    metrics = new MetricsImporter().getMetrics();
    Map<String, Object> measures = getMeasuresFromJsonFile();

    for (Metric metric : metrics) {
      FileSystem fs = context.fileSystem();
      Iterable<InputFile> files = fs.inputFiles(fs.predicates().hasType(InputFile.Type.MAIN));
      for (InputFile file : files) {
        addNewMeasureToFile(context, measures, file, metric);
      }
      addNewMeasureToProject(context, measures, metric);
    }
  }

  private Map<String, Object> getMeasuresFromJsonFile() {
    try {
      return new Gson().fromJson(new FileReader(basePath + "measures.json"), Map.class);
    } catch (FileNotFoundException e) {
      LOG.warn("No FPGA measures report found in this project directory");
      return Collections.emptyMap();
    } catch (JsonSyntaxException e) {
      // TODO: Why don't we make it crash in this case?
      LOG.warn("JSON measures report is not correctly formatted");
      return Collections.emptyMap();
    } catch (JsonIOException e) {
      // TODO: Why don't we make it crash in this case?
      LOG.warn("Gson IO exception");
      return Collections.emptyMap();
    }
  }

  private void addNewMeasureToProject(SensorContext context, Map<String, Object> measures, Metric metric) {
    Object rawMeasure = getRawMeasure(measures.get(metric.getKey()));
    if (rawMeasure != null) {
      Double ratioMax = getRatioMax(measures.get(metric.getKey()));
      Serializable typedMeasure = getTypedMeasure(rawMeasure, metric.getType().name(), ratioMax);
      saveMeasureOnProject(context, metric, typedMeasure);
    }
  }

  private void addNewMeasureToFile(SensorContext context, Map<String, Object> measures, InputFile file, Metric metric) {
    Object rawMeasure = getRawMeasure(measures.get(metric.getKey()));
    if (rawMeasure != null) {
      Double ratioMax = getRatioMax(measures.get(metric.getKey()));
      Serializable typedMeasure = getTypedMeasure(rawMeasure, metric.getType().name(), ratioMax);
      saveMeasureOnFile(context, file, metric, typedMeasure);
    }
  }

  private void saveMeasureOnProject(SensorContext context, Metric metric, Serializable measure) {
    context.newMeasure().forMetric(metric).on(context.project()).withValue(measure).save();
  }

  private void saveMeasureOnFile(SensorContext context, InputFile file, Metric metric, Serializable measure) {
    context.newMeasure().forMetric(metric).on(file).withValue(measure).save();
  }

  private Object getRawMeasure(Object rawValue) {
    if (rawValue != null && rawValue.getClass().equals(ArrayList.class)) {
      if (((ArrayList) rawValue).size() == 2) {
        rawValue = ((ArrayList) rawValue).get(0);
      } else {
        rawValue = null;
      }
    }
    return rawValue;
  }

  private Double getRatioMax(Object rawValue) {
    if (rawValue != null && rawValue.getClass().equals(ArrayList.class) && (((ArrayList) rawValue).size() == 2)) {
      return (Double) ((ArrayList) rawValue).get(1);
    }
    return null;
  }

  private Serializable getTypedMeasure(Object rawValue, String metricType, Double ratioMax) {
    switch (metricType) {
      case "INT":
        return (int) Math.round((Double) rawValue);
      case "FLOAT":
        if (ratioMax == null) {
          return (Double) rawValue;
        } else {
          return ((Double) rawValue) / ratioMax;
        }
      case "PERCENT":
        if (ratioMax == null) {
          return (Double) rawValue;
        } else {
          return ((Double) rawValue) * 100.0 / ratioMax;
        }
      case "BOOL":
        return (Boolean) rawValue;
      case "STRING":
      case "DATA":
      case "DISTRIB":
        return (String) rawValue;
      case "MILLISEC":
      case "RATING":
      case "WORK_DUR":
        return Math.round((Double) rawValue);
      default:
        throw new IllegalStateException(metricType + " metric type not recognized.");
    }
  }
}
