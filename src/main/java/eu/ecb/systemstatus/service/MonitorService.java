package eu.ecb.systemstatus.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.ecb.systemstatus.beans.ExternalInterfaceConn;
import eu.ecb.systemstatus.beans.FileAttributes;
import eu.ecb.systemstatus.beans.HealthStatus;

@Service
public class MonitorService {
	

	private static Logger LOGGER = LogManager.getLogger(MonitorService.class);
	@Autowired
	ServiceHelper serviceHelper;
	
	
	public List<HealthStatus> getStatus(String logLocation) {
		
		List<HealthStatus> healthStatusList = new ArrayList<HealthStatus>();
		//TODO - use logLocation below
		try {
			Files.list(Paths.get(logLocation))
				.filter(Files::isRegularFile)
				.sorted()
				.forEach(file ->  healthStatusList.add(serviceHelper.getComponetStatus(file.toAbsolutePath().toString())));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//healthStatusList.add(serviceHelper.getOtMtsEarlyRun() );
		//healthStatusList.add(serviceHelper.getOtMtsRepoTradeRun() );
		//healthStatusList.add(serviceHelper.getOtIcapRun() );
		//healthStatusList.add(serviceHelper.getOtGdpDataSdwExportRun() );
		//healthStatusList.add(serviceHelper.getOtProcessing());;
		//healthStatusList.add(serviceHelper.getOtInterface());
		
		return healthStatusList;
	}

	public List<FileAttributes> getAllFileDetails(String fileName) {
		
		List<FileAttributes> allFileDetails = null;
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
			//List<String> alltStatus = stream.collect(Collectors.toList());
			List<String> alltStatus = stream.filter(line -> serviceHelper.chkValidRecords(line)).collect(Collectors.toList());
			//LOGGER.debug(alltStatus);
			allFileDetails = serviceHelper.convertIntoFileAttributes(alltStatus);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return allFileDetails;
	}

	public List<ExternalInterfaceConn> getAllInterfaceDetails(String fileName) {
		
		List<ExternalInterfaceConn> allInterfaceDetails = null;
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
			List<String> alltStatus = stream.collect(Collectors.toList());
			//List<String> alltStatus = stream.filter(line -> serviceHelper.chkValidRecords(line)).collect(Collectors.toList());
			//LOGGER.debug(alltStatus);
			allInterfaceDetails = serviceHelper.convertIntoExterIntrfcConn(alltStatus);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return allInterfaceDetails;
	}

	public String convertIntoText(List<HealthStatus> healthStatusList) {
		
		String str = healthStatusList.stream().map(health -> health.getDisplayText() + ": " + health.getHealthStatus()).collect( Collectors.joining( ", \n" ) );
		LOGGER.debug("Text format ---> " + str);
		return str;
	}

		

}
