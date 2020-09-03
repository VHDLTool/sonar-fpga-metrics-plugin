# Sonar FPGA Metrics Plugin  

A SonarQube plugin allowing to create custom metrics and assign them values from external files. 

# Building and installing  

Build the plugin with :   
mvn clean package.  
Copy the generated jar file from /target to "Sonarqube server path"/extensions/plugins , then launch Sonarqube.   

# Usage

All custom metrics should be defined in a 'metrics.json' file.   
The absolute path to this file can be defined in Sonarqube web interface, in the plugin's properties (Administration tab).   
Sonarqube server needs to be restarted when this file is modified, in order for the changes to apply.   
Measures for each custom metric should be defined in a file named 'measures.json' at the root of the analyzed project, when they relate to a project.    
When measures relate to a single file, they should be in a json file named [Source file name without extension]_measures.json, in the same folder as the corresponding file.    
Measures will be imported in Sonarqube user interface after executing sonar-scanner.   


# Examples

metrics.json file :   
{
	"metrics": {
		"Metric1": {
			"name": "Metric 1",
			"type": "INT",
			"description":"metric 1 description",
			"qualitative":true
		},
		"Metric2": {
			"name": "Metric 2",
			"type": "STRING"
		},
		"Metric3": {
			"name": "Metric 3",
			"type": "FLOAT",
			"bestValue":1500,
			"worstValue":500,
			"qualitative":true
		},
		"Metric4": {
			"name": "Metric 4",
			"type": "PERCENT",
			"domain":"Maintainability",
			"direction":-1,
			"optimizedBestValue":true,
			"qualitative":true
		},
		"Metric5": {
			"name": "Metric 5",
			"type": "PERCENT",
			"domain":"custom",
			"direction":1,
			"optimizedBestValue":true,
			"qualitative":true
		}
	}
}


measures.json file :   

{
	"Metric1": 5257,   
	"Metric2": "aaaaaaaaaaaaz",   
	"Metric3": 1200.1,   
	"Metric4": 15,   
	"Metric5": 30
}    
