package eu.ecb.systemstatus.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;


@Configuration
@ComponentScan(basePackages = { "eu.ecb.systemstatus" })
@PropertySources({
    @PropertySource("classpath:environments.properties"),
    @PropertySource(value="file:environments.properties", ignoreResourceNotFound=true)
})
public class DynamicConfiguration {
	
	@Autowired
	Environment env;
	
	@Autowired
	GeneralConfiguration genConfiguration;
	
	private static Map<String, String> map = null;
	private static Logger LOGGER = LogManager.getLogger(DynamicConfiguration.class);
	
	public Map<String, String> getMap() {
		
		LOGGER.debug("inside method getMap");
		
		Properties properties = new Properties();
		FileInputStream inputStream = null;
		// add  some properties  here
		try {
			String scheduleFileName = genConfiguration.getScheduleFolderRoot()
									.concat(File.separator)
									.concat(genConfiguration.getScheduleFolderName())
									.concat(File.separator)
									.concat(genConfiguration.getScheduleFileName());
			
			inputStream = new FileInputStream(scheduleFileName);
			properties.load(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Some issue finding or loading file....!!! " + e.getMessage());

		} finally {
			try {
				inputStream.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		map = new HashMap<String, String>();
		map.putAll(properties.entrySet()
				.stream()
				.collect(Collectors.toMap(e -> e.getKey().toString(), 
						e -> e.getValue().toString())));	
		//map.forEach((k, v) -> System.out.println((k + "----" + v)));
		
		return map;

	}
}
