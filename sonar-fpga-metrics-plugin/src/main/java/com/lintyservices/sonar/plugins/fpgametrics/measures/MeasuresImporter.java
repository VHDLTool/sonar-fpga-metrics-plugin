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
package com.lintyservices.sonar.plugins.fpgametrics.measures;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.FilenameUtils;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.Metric;
import org.sonar.api.scanner.sensor.ProjectSensor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MeasuresImporter implements ProjectSensor {

  private static final Logger LOG = Loggers.get(MeasuresImporter.class);

  private List<Metric> metrics = null;

  //For testing only
  private String basePath = "";


  public MeasuresImporter(Configuration configuration) {
    this(MetricsImporter.getMetricsResult(), "");
  }

  //For testing only
  public MeasuresImporter(List<Metric> metrics, String basePath) {
    this.metrics = metrics;
    this.basePath = basePath;
  }


  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.name("Import measures from Json files");
  }

  @Override
  public void execute(SensorContext context) {

    if (metrics == null)
      metrics = MetricsImporter.getMetricsResult();
    Gson gson = new Gson();
    Map<String, Object> measures = new HashMap<>();
    try {
      measures = gson.fromJson(new FileReader(basePath + "measures.json"), measures.getClass());
    } catch (FileNotFoundException e) {
      LOG.warn("No measures report found in this project directory");
    } catch (JsonSyntaxException e) {
      LOG.warn("Json measures report is not correctly formatted");
    } catch (JsonIOException e) {
      LOG.warn("Gson IO exception");
    }
    if (metrics != null) {
      for (Metric metric : metrics) {

        FileSystem fs = context.fileSystem();
        // only "main" files, but not "tests"
        Iterable<InputFile> files = fs.inputFiles(fs.predicates().hasType(InputFile.Type.MAIN));
        for (InputFile file : files) {
          Gson gsonFile = new Gson();
          Map<String, Object> measuresFile = new HashMap<>();
          try {
            measuresFile = gsonFile.fromJson(new FileReader(FilenameUtils.removeExtension(file.absolutePath()) + "_measures.json"), measuresFile.getClass());
          } catch (FileNotFoundException e) {
          } catch (JsonSyntaxException e) {
            LOG.warn("Json measures report is not correctly formatted");
          } catch (JsonIOException e) {
            LOG.warn("Gson IO exception");
          }

          Object rawValue = measures.get(metric.getKey());
          Double ratioMax = null;
          if (rawValue != null && rawValue.getClass().equals(ArrayList.class)) {
            if (((ArrayList) rawValue).size() == 2) {
              ratioMax = (Double) ((ArrayList) rawValue).get(1);
              rawValue = ((ArrayList) rawValue).get(0);
            } else {
              rawValue = null;
            }
          }

          if (rawValue != null) {
            String valueTypeStr = metric.getType().name();//ExampleMetrics.measures.metrics().get(metric.getKey()).getType();
            switch (valueTypeStr) {
              case "INT":
                context.newMeasure().forMetric(metric).on(file).withValue((int) Math.round((Double) rawValue)).save();
                break;
              case "FLOAT":
                if (ratioMax == null)
                  context.newMeasure().forMetric(metric).on(file).withValue((Double) rawValue).save();
                else
                  context.newMeasure().forMetric(metric).on(file).withValue(((Double) rawValue) / ratioMax).save();
                break;
              case "PERCENT":
                if (ratioMax == null)
                  context.newMeasure().forMetric(metric).on(file).withValue((Double) rawValue).save();
                else
                  context.newMeasure().forMetric(metric).on(file).withValue(((Double) rawValue) * 100.0 / ratioMax).save();
                break;
              case "BOOL":
                context.newMeasure().forMetric(metric).on(file).withValue((Boolean) rawValue).save();
                break;
              case "STRING":
                context.newMeasure().forMetric(metric).on(file).withValue((String) rawValue).save();
                break;
              case "MILLISEC":
                context.newMeasure().forMetric(metric).on(file).withValue((Long) Math.round((Double) rawValue)).save();
                break;
              case "DATA":
                context.newMeasure().forMetric(metric).on(file).withValue((String) rawValue).save();
                break;
              case "DISTRIB":
                context.newMeasure().forMetric(metric).on(file).withValue((String) rawValue).save();
                break;
              case "RATING":
                context.newMeasure().forMetric(metric).on(file).withValue((int) Math.round((Double) rawValue)).save();
                break;
              case "WORK_DUR":
                context.newMeasure().forMetric(metric).on(file).withValue((Long) Math.round((Double) rawValue)).save();
                break;
            }
          }
        }

        Object rawValue = measures.get(metric.getKey());
        Double ratioMax = null;
        if (rawValue != null && rawValue.getClass().equals(ArrayList.class)) {
          if (((ArrayList) rawValue).size() == 2) {
            ratioMax = (Double) ((ArrayList) rawValue).get(1);
            rawValue = ((ArrayList) rawValue).get(0);
          } else {
            rawValue = null;
          }
        }
        if (rawValue != null) {
          String valueTypeStr = metric.getType().name();
          switch (valueTypeStr) {
            case "INT":
              context.newMeasure().forMetric(metric).on(context.project()).withValue((int) Math.round((Double) rawValue)).save();
              break;
            case "FLOAT":
              if (ratioMax == null)
                context.newMeasure().forMetric(metric).on(context.project()).withValue((Double) rawValue).save();
              else
                context.newMeasure().forMetric(metric).on(context.project()).withValue(((Double) rawValue) / ratioMax).save();
              break;
            case "PERCENT":
              if (ratioMax == null)
                context.newMeasure().forMetric(metric).on(context.project()).withValue((Double) rawValue).save();
              else
                context.newMeasure().forMetric(metric).on(context.project()).withValue(((Double) rawValue) * 100.0 / ratioMax).save();
              break;
            case "BOOL":
              context.newMeasure().forMetric(metric).on(context.project()).withValue((Boolean) rawValue).save();
              break;
            case "STRING":
              context.newMeasure().forMetric(metric).on(context.project()).withValue((String) rawValue).save();
              break;
            case "MILLISEC":
              context.newMeasure().forMetric(metric).on(context.project()).withValue((Long) Math.round((Double) rawValue)).save();
              break;
            case "DATA":
              context.newMeasure().forMetric(metric).on(context.project()).withValue((String) rawValue).save();
              break;
            case "DISTRIB":
              context.newMeasure().forMetric(metric).on(context.project()).withValue((String) rawValue).save();
              break;
            case "RATING":
              context.newMeasure().forMetric(metric).on(context.project()).withValue((int) Math.round((Double) rawValue)).save();
              break;
            case "WORK_DUR":
              context.newMeasure().forMetric(metric).on(context.project()).withValue((Long) Math.round((Double) rawValue)).save();
              break;
          }
        }
      }
    }

  }

}
