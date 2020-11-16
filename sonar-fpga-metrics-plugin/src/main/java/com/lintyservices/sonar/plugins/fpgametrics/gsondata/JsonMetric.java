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
package com.lintyservices.sonar.plugins.fpgametrics.gsondata;

public class JsonMetric {

  private String name;
  private String type;
  private String description;
  private String domain;
  private double worstValue;
  private double bestValue;
  private boolean qualitative;
  private int direction;
  private boolean optimizedBestValue;
  private boolean userManaged;
  private boolean hidden;
  private boolean deleteHistoricalData;
  private int decimalScale;

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getDescription() {
    return description;
  }

  public boolean isQualitative() {
    return qualitative;
  }

  public String getDomain() {
    return domain;
  }

  public double getWorstValue() {
    return worstValue;
  }

  public double getBestValue() {
    return bestValue;
  }

  public boolean isOptimizedBestValue() {
    return optimizedBestValue;
  }

  public int getDirection() {
    return direction;
  }

  public boolean isUserManaged() {
    return userManaged;
  }

  public boolean isHidden() {
    return hidden;
  }

  public boolean isDeleteHistoricalData() {
    return deleteHistoricalData;
  }

  public int getDecimalScale() {
    return decimalScale;
  }

}
