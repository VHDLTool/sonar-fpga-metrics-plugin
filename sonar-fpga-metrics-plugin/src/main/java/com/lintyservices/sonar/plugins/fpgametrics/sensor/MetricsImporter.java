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
import com.lintyservices.sonar.plugins.fpgametrics.gsondata.JsonMetric;
import com.lintyservices.sonar.plugins.fpgametrics.gsondata.JsonMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.measures.Metrics;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MetricsImporter implements Metrics {

  @Override
  public List<Metric> getMetrics() {
    return getMetricsFromJsonFile("fpgametrics/format-metrics.json", "production");
  }

  @VisibleForTesting
  List<Metric> getMetricsFromJsonFile(String jsonFilePath, String type) {
    InputStreamReader inputStreamReader;
    if ("test".equals(type)) {
      try {
        inputStreamReader = new FileReader(jsonFilePath);
      } catch (FileNotFoundException e) {
        throw new IllegalStateException("[FPGA] Cannot find JSON metrics file", e);
      }
    } else {
      inputStreamReader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(jsonFilePath));
    }
    JsonMetrics jsonMetrics = new Gson().fromJson(
      inputStreamReader,
      JsonMetrics.class
    );

    List<Metric> metrics = new ArrayList<>();
    for (Map.Entry<String, JsonMetric> metric : jsonMetrics.metrics().entrySet()) {
      try {
        metrics.add(convertToSonarQubeMetric(metric));
      } catch (Exception e) {
        throw new IllegalStateException("[FPGA Metrics] " + metric.getKey() + " metric cannot be created since it is not properly formatted", e);
      }
    }
    return metrics;
  }

  private Metric convertToSonarQubeMetric(Map.Entry<String, JsonMetric> metric) {
    String key = metric.getKey();
    JsonMetric value = metric.getValue();
    return new Metric.Builder(
      key,
      value.getName(),
      ValueType.valueOf(value.getType())
    )
      .setDescription(value.getDescription())
      .setDirection(value.getDirection())
      .setQualitative(value.isQualitative())
      .setDomain(value.getDomain())
      .setWorstValue(value.getWorstValue())
      .setBestValue(value.getBestValue())
      .setOptimizedBestValue(value.isOptimizedBestValue())
      .setDecimalScale(value.getDecimalScale())
      .setDeleteHistoricalData(value.isDeleteHistoricalData())
      .setHidden(value.isHidden())
      .setUserManaged(value.isUserManaged())
      .create();
  }
}
