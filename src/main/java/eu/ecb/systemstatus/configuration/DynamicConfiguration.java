package eu.ecb.systemstatus.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @PropertySource(value="${external.app.properties.file}", ignoreResourceNotFound=true),
    @PropertySource("classpath:environments.properties")
})
public class DynamicConfiguration {
	
	@Autowired
	Environment env;
	
	@Autowired
	GeneralConfiguration genConfiguration;
	
	private static Map<String, String> schduleMap = null;
	private static Map<String, String> requirementsMap = null;
	private static Logger LOGGER = LogManager.getLogger(DynamicConfiguration.class);
	
	public Map<String, String> getMap() {
		if (schduleMap == null) {
			schduleMap = loadSchedule();
		}
		//map.forEach((k, v) -> System.out.println((k + "----" + v)));
		return schduleMap;

	}
	
	public Map<String, String> getRequirementsMap() {
		if (requirementsMap == null) {
			requirementsMap = loadReqMap();
		}
		//requirementsMap.forEach((k, v) -> LOGGER.debug((k + "----" + v)));
		return requirementsMap;

	}
	
	private Map<String, String> loadSchedule() {
		
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

		schduleMap = new HashMap<String, String>();
		schduleMap.putAll(properties.entrySet()
				.stream()
				.collect(Collectors.toMap(e -> e.getKey().toString(), 
						e -> e.getValue().toString())));
		
		return schduleMap;
	}
	
	private Map<String, String> loadReqMap() {

		String scheduleFileName = genConfiguration.getRootDir().concat(File.separator).concat(genConfiguration.getRequirementsMapFile());
		
		requirementsMap = new HashMap<String, String>();
		try (Stream<String> stream = Files.lines(Paths.get(scheduleFileName))) {
			stream.forEach(line ->processLine(line));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return requirementsMap;
	}

	private Map<String, String> processLine(String line) {
		
		String[] values = line.split("\\|");
		requirementsMap.put(values[0], values[1]);
		return requirementsMap;
	}
}
