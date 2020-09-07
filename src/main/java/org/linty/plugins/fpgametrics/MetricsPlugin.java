 /*
 * sonar-fpga-metrics plugin for Sonarqube
 * Copyright (C) 2020 Linty Services
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.linty.plugins.fpgametrics;

import org.linty.plugins.fpgametrics.measures.MetricsImporter;
import org.linty.plugins.fpgametrics.measures.MeasuresImporter;
import org.sonar.api.Plugin;

/**
 * This class is the entry point for all extensions. It is referenced in pom.xml.
 */


public class MetricsPlugin implements Plugin {
	
//For testing only
  public MetricsPlugin() {
		super();
	}

@Override
  public void define(Context context) {
    context.addExtensions(MetricsImporter.class, MeasuresImporter.class);
  }

}
