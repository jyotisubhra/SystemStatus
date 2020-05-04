package eu.ecb.systemstatus.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import eu.ecb.systemstatus.configuration.DynamicConfiguration;
import eu.ecb.systemstatus.configuration.MdpConfiguration;

@Service
public class ServiceHelper {

	private static Logger LOGGER = LogManager.getLogger(ServiceHelper.class);
	//private static Map<String, String> map = null;
	private static int noValidRecord = 0;
	private static int noInvalidRecord = 0;
	
	@Autowired
	MdpConfiguration configuration;
	
	@Autowired
	DynamicConfiguration appConfig;

	
	private HealthStatus convertHealthStatus(String displatText, String status, String executionStatus, String logFile) {

		HealthStatus healthStatus = new HealthStatus();
		healthStatus.setDisplayText(displatText);	
		healthStatus.setDisplayTextResult(status);
		healthStatus.setHealthStatus(executionStatus);
		healthStatus.setLogFile(logFile);

		return healthStatus;
	}
	
	/*public HealthStatus getOtMtsEarlyRun() {

		String fileName = "C:/OT-Config/mts_early_runs.properties";	
		HealthStatus healthStatus = getComponentHealth(fileName, 7, true);		
		return healthStatus;
	}
	
	public HealthStatus getOtMtsRepoTradeRun() {
		String fileName = "C:/OT-Config/mts_repoTrade_run.properties";	
		HealthStatus healthStatus = getComponentHealth(fileName, 7, true);		
		return healthStatus;
	}
	
	public HealthStatus getOtIcapRun() {
		String fileName = "C:/OT-Config/icap_run.properties";	
		HealthStatus healthStatus = getComponentHealth(fileName, 7, true);		
		return healthStatus;
	}

	public HealthStatus getOtGdpDataSdwExportRun() {
		String fileName = "C:/OT-Config/gdp_data_sdw_export.properties";	
		HealthStatus healthStatus = getComponentHealth(fileName, 7, true);		
		return healthStatus;
	}
	
	public HealthStatus getOtProcessing() {

		String fileName = "C:/OT-Config/processing.properties";
		HealthStatus healthStatus = getComponentHealth(fileName, 7, true);	
		return healthStatus;
	}
	public HealthStatus getOtInterface() {

		String fileName = "C:/OT-Config/interface.properties";
		HealthStatus healthStatus = getComponentHealth(fileName, 4, false);		
		return healthStatus;
	}*/
	
	public HealthStatus getComponetStatus(String fileName) {

		HealthStatus healthStatus = null;
		if (fileName.contains("interface")) {
			healthStatus = getComponentHealth(fileName, 4, false);	
			healthStatus.setDisplayType("interface");
		} else {
			healthStatus = getComponentHealth(fileName, 7, false);	
			healthStatus.setDisplayType("file");
		}
		return healthStatus;
	}


	private HealthStatus getComponentHealth(String fileName, int pos, boolean chkValidRecordsReqd) {

		String displayText = new File(fileName).getName().replaceFirst("[.][^.]+$", "").toUpperCase().replace("_", " ");
		String executionStatus = null;
		String status = "NOK";
		if (new File(fileName).length() != 0) {
			status = getComponentStatus(fileName, pos, chkValidRecordsReqd);	
			executionStatus = status;
		} else {
			executionStatus = "No Data available";
		}
		HealthStatus healthStatus = convertHealthStatus(displayText, status, executionStatus, fileName);
		return healthStatus;
	}

	private String getComponentStatus(String fileName, int pos, boolean chkScheduledTime) {
		String status = "NOK";
		noValidRecord = 0;
		noInvalidRecord = 0;

		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
			List<String> alltStatus = stream.filter(line -> !processLine(line, pos, chkScheduledTime)).collect(Collectors.toList());
			LOGGER.debug(alltStatus);
			if (alltStatus.size() == 0) {
				status = "OK";
			}
			//File not available since jobs not triggered yet
			if (noValidRecord == 0 && noInvalidRecord > 0) {
				status = "OK";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return status;
	}

	private boolean processLine(String line, int pos, boolean chkScheduledTime) {


		boolean status = false;
		boolean isValidRecord = true;
		boolean isValidSize = true;
		String[] values = line.split("\\|");

		if (chkScheduledTime) {
			//compare scheduled time
			isValidRecord = isValidEntry(values[0].trim());
			if (isValidRecord) {
				isValidSize = isValidSize(values[5].trim());
			}
		}
		if (isValidRecord && isValidSize && values[pos].trim().equalsIgnoreCase("AVL")) {
			status = true;
		}
		return status;
	}

	public boolean chkValidRecords(String line) {


		boolean status = false;
		boolean isValidRecord = true;
		String[] values = line.split("\\|");

		//compare scheduled time
		isValidRecord = isValidEntry(values[0].trim());
		if (isValidRecord) {
			status = true;
		}
		return status;
	}

	private boolean isValidSize(String fileSize) {

		int size = Integer.valueOf(fileSize);
		boolean isValidSize = false;
		if (size > 0) {
			isValidSize = true;
		}
		return isValidSize;
	}

	private boolean isValidEntry(String reqId) {

		boolean isValidRecord = false;
		String scheduledTime = null;
		if (appConfig.getMap().containsKey(reqId)) {
			scheduledTime = appConfig.getMap().get(reqId);
		}		
		if (null != scheduledTime) {

			String[] scheduleTimeDetails = scheduledTime.split(":");
			String hour = scheduleTimeDetails[0];
			String mint = scheduleTimeDetails[1];
			Calendar caln = Calendar.getInstance();
			caln.set(Calendar.HOUR_OF_DAY,Integer.valueOf(hour));
			caln.set(Calendar.MINUTE,Integer.valueOf(mint));
			caln.set(Calendar.SECOND,0);
			caln.set(Calendar.MILLISECOND,0);

			Date scheduledHrs = caln.getTime();
			//LOGGER.debug("Scheduled Time = " + scheduledHrs);

			//Current Time
			Calendar cal = Calendar.getInstance();
			Date currentHrs = cal.getTime();
			//LOGGER.debug("Current Time = " + currentHrs);

			if (currentHrs.after(scheduledHrs)) {
				//LOGGER.debug("Current time is after scheduled time");
				isValidRecord = true;
				noValidRecord++;
			} else {
				noInvalidRecord++;
			}

		}
		return isValidRecord;
	}

	public List<FileAttributes> convertIntoFileAttributes(List<String> alltStatus) {

		List<FileAttributes> allFileStatus = new ArrayList<FileAttributes>();
		for (String indvStatus : alltStatus) {
			FileAttributes fileAttributes = new FileAttributes();
			String[] values = indvStatus.split("\\|");
			if (isValidEntry(values[0].trim())) {
				fileAttributes.setCaseId(values[0].trim());
				fileAttributes.setFileLocation(values[1].trim());
				fileAttributes.setFileName(values[2].trim());
				fileAttributes.setDate(values[3].trim());
				fileAttributes.setTime(values[4].trim());
				fileAttributes.setSize(values[5].trim() + "KB");
				fileAttributes.setNoOfLines(values[6].trim());
				if (values[7].trim().equalsIgnoreCase("NAVL")) {
					fileAttributes.setStatus("NOK");
				} else if (Integer.valueOf(values[5].trim()) == 0 && values[7].trim().equalsIgnoreCase("AVL")) {
					fileAttributes.setStatus("NOK");
				} else if (values[7].trim().equalsIgnoreCase("ERR")) {
					fileAttributes.setStatus("NOK");
				} else {
					fileAttributes.setStatus("OK");
				}

				allFileStatus.add(fileAttributes);
			}			
		}
		return allFileStatus;
	}
	
	/*public void loadConfig() {

		Properties properties = new Properties();
		FileInputStream inputStream = null;
		// add  some properties  here
		try {
			String scheduleFileName = configuration.getScheduleFolderRoot()
									.concat(File.separator)
									.concat(configuration.getScheduleFolderName())
									.concat(File.separator)
									.concat(configuration.getScheduleFileName());
			
			inputStream = new FileInputStream(scheduleFileName);
			properties.load(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.debug("Some issue finding or loading file....!!! " + e.getMessage());

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
		//map.forEach((k, v) -> LOGGER.debug((k + "----" + v)));

	}*/

	public List<ExternalInterfaceConn> convertIntoExterIntrfcConn(List<String> alltStatus) {

		List<ExternalInterfaceConn> allExternalIntrfcConn = new ArrayList<ExternalInterfaceConn>();
		for (String indvStatus : alltStatus) {
			ExternalInterfaceConn externalInterfaceConn = new ExternalInterfaceConn();
			String[] values = indvStatus.split("\\|");
			externalInterfaceConn.setCaseId(values[0].trim());
			externalInterfaceConn.setSource(values[1].trim());
			externalInterfaceConn.setDestination(values[2].trim());
			externalInterfaceConn.setPort(values[3].trim());

			if (values[4].trim().equalsIgnoreCase("AVL")) {
				externalInterfaceConn.setStatus("OK");
			} else {
				externalInterfaceConn.setStatus("NOK");
			}
			allExternalIntrfcConn.add(externalInterfaceConn);
		}
		return allExternalIntrfcConn;
	}

	public String getFolderLocation(String folder, String loadTime) {
		String logLocation = configuration.getRootDir().concat(File.separator).concat(folder).concat(File.separator).concat(loadTime);
		return logLocation;
	}
	
}
