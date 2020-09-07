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


import org.junit.Test;
import org.linty.plugins.fpgametrics.measures.MeasuresImporter;
import org.linty.plugins.fpgametrics.measures.MetricsImporter;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.PluginContextImpl;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.utils.Version;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;


public class ImportersTest {


    
    @Test
    public void execute_WithMatchingPattern_ExpectValueRegistered() throws Exception {SonarRuntimeImpl a;

    	//Instantiating MetricsPlugin class
    
    	MetricsPlugin metricsPlugin = new MetricsPlugin();
    	metricsPlugin.define((new PluginContextImpl.Builder().setSonarRuntime(SonarRuntimeImpl.forSonarLint(Version.parse("1.1.1")))).build());
    
    	
    	//Testing MetricsImporter
    	
        MetricsImporter metricsImporter = new MetricsImporter(new MapSettings().asConfig());
        List<Metric> metricsList = metricsImporter.getMetrics();
        MetricsImporter.getJsonMetrics();        
        assertEquals(10,MetricsImporter.getMetricsResult().size());
        assertEquals("Metric 1",MetricsImporter.getMetricsResult().get(0).getName());
        assertEquals(ValueType.INT,MetricsImporter.getMetricsResult().get(1).getType());
        assertEquals(false,MetricsImporter.getMetricsResult().get(2).getQualitative());     
        
        
        //Testing MeasuresImporter
        
        final SensorContextTester contextTester = SensorContextTester.create(new File("src\\test\\files\\ctx")); //Blank file only used for context simulation
        MeasuresImporter measuresImporter = new MeasuresImporter(metricsList,"src\\test\\files\\");
        measuresImporter.execute(contextTester);
        final Measure<Double> floatMeasure = contextTester.measure(contextTester.module().key(), "Metric1");
        final Measure<Integer> intMeasure = contextTester.measure(contextTester.module().key(), "Metric2");
        final Measure<String> stringMeasure = contextTester.measure(contextTester.module().key(), "Metric3");
        assertEquals((Double) 80.0, floatMeasure.value());
        assertEquals((Integer)10, intMeasure.value());
        assertEquals("hello", stringMeasure.value());
        
        measuresImporter = new MeasuresImporter(null,"src\\test\\files_folder_does_not_exist\\");
        measuresImporter.execute(contextTester);
    }


}