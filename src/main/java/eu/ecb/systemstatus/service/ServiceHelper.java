package eu.ecb.systemstatus.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
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
import eu.ecb.systemstatus.configuration.GeneralConfiguration;
import eu.ecb.systemstatus.configuration.MdpConfiguration;

@Service
public class ServiceHelper {

	private static Logger LOGGER = LogManager.getLogger(ServiceHelper.class);
	
	@Autowired
	MdpConfiguration configuration;
	
	@Autowired
	DynamicConfiguration dynConfig;
	
	@Autowired
	GeneralConfiguration genConfig;

	
	private HealthStatus convertHealthStatus(String displatText, String status, String executionStatus, String logFile) {

		HealthStatus healthStatus = new HealthStatus();
		healthStatus.setDisplayText(displatText);	
		healthStatus.setDisplayTextResult(status);
		healthStatus.setHealthStatus(executionStatus);
		healthStatus.setLogFile(logFile);

		return healthStatus;
	}
	
	public HealthStatus getComponetStatus(String fileName) {

		HealthStatus healthStatus = null;
		if (fileName.contains("interface")) {
			healthStatus = getComponentHealth(fileName, 4, false);	
			healthStatus.setDisplayType("interface");
		} else {
			healthStatus = getComponentHealth(fileName, 7, true);	
			healthStatus.setDisplayType("file");
		}
		return healthStatus;
	}


	private HealthStatus getComponentHealth(String fileName, int pos, boolean fileCheckRequired) {

		String displayText = new File(fileName).getName().replaceFirst("[.][^.]+$", "").toUpperCase().replace("_", " ");
		String executionStatus = null;
		String status = genConfig.getNotOkStatusText();
		if (new File(fileName).length() != 0) {
			status = getComponentStatus(fileName, pos, fileCheckRequired);	
			executionStatus = status;
		} else {
			status = genConfig.getOkStatusText();
			executionStatus = genConfig.getNoValidDataText();
		}
		HealthStatus healthStatus = convertHealthStatus(displayText, status, executionStatus, fileName);
		return healthStatus;
	}

	private String getComponentStatus(String fileName, int pos, boolean fileCheckRequired) {
		String status = genConfig.getNotOkStatusText();

		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
			List<String> alltStatus = stream.filter(line -> !processLine(line, pos, fileCheckRequired)).collect(Collectors.toList());
			LOGGER.debug(alltStatus);
			if (alltStatus.size() == 0) {
				status = genConfig.getOkStatusText();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return status;
	}

	private boolean processLine(String line, int pos, boolean fileCheckRequired) {


		boolean status = false;
		boolean isValidSize = true;
		String[] values = line.split("\\|");
		
		if (fileCheckRequired) {
			isValidSize = isValidSize(values[5].trim());
		}
		if (isValidSize && values[pos].trim().equalsIgnoreCase("AVL")) {
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

	private boolean isValidEntry(String reqId, boolean isExtensive) {

		boolean isValidRecord = false;
		String scheduledDetail = null;
		if (dynConfig.getMap().containsKey(reqId)) {
			scheduledDetail = dynConfig.getMap().get(reqId);
		}		
		if (null != scheduledDetail) {

			Date[] scheduledHrs = getScheduleTime(scheduledDetail.trim());
			String scheduleCondition = getScheduleCondition(scheduledDetail.trim());

			//Current Time
			Calendar cal = Calendar.getInstance();
			Date currentHrs = cal.getTime();
			//LOGGER.debug("Current Time = " + currentHrs);

			if (scheduledHrs == null ) {
				scheduledHrs = new Date[1];
				scheduledHrs[0] = getDefaultValue();
			}
			//Arrays.stream(scheduledHrs).forEach(LOGGER::debug);
			//LOGGER.debug("scheduleCondition = " + scheduleCondition);
			isValidRecord = getSceduleData(scheduledHrs, scheduleCondition, currentHrs, isExtensive);
			//LOGGER.debug("Requirement Id : " + reqId + ", Status: " + isValidRecord);
		} else {
			LOGGER.warn("No Scheduler set fot this requirement Id: " + reqId);
		}
		return isValidRecord;
	}

	private boolean getSceduleData(Date[] scheduledHrs, String scheduleCondition, Date currentHrs, boolean isExtensive) {

		boolean isValidRecord = false;

		if (isExtensive) {

			if (scheduledHrs[0].before(currentHrs)) {
				//LOGGER.debug("Current time Matches with BEFORE Time");
				isValidRecord = true;
			} 

		} else {
			if (scheduleCondition.equalsIgnoreCase("BEFORE")) {
				if (scheduledHrs[0].before(currentHrs)) {
					//LOGGER.debug("Current time Matches with BEFORE Time");
					isValidRecord = true;
				} 
			} else if (scheduleCondition.equalsIgnoreCase("AFTER")) {
				if (currentHrs.after(scheduledHrs[0])) {
					//LOGGER.debug("Current time Matches with AFTER Time");
					isValidRecord = true;
				} 

			} else if (scheduleCondition.equalsIgnoreCase("BETWEEN")) {
				if (currentHrs.after(scheduledHrs[0]) && currentHrs.before(scheduledHrs[1])) {
					//LOGGER.debug("Current time Matches with BETWEEN Time");
					isValidRecord = true;
				} 
			} else {
				Calendar currentDate = Calendar.getInstance();
				currentDate.add(Calendar.HOUR, - Integer.valueOf(genConfig.getDefaultInterval()));
				Date previousHrBack = currentDate.getTime();

				if (scheduledHrs[0].after(previousHrBack) && scheduledHrs[0].before(Calendar.getInstance().getTime())) {
					//LOGGER.debug("Current time taking DEFAULT AFTER Time");
					isValidRecord = true;
				} 
			}
		}
		return isValidRecord;
	}

	private Date getDefaultValue() {
		
		Calendar caln = Calendar.getInstance();
		return caln.getTime();
	}

	private String getScheduleCondition(String scheduledDetail) {
		String[] details = scheduledDetail.split("_");
		return details[0].trim();
	}

	private Date[] getScheduleTime(String scheduledDetail) {
		
		String[] details = scheduledDetail.split("_");
		Date[] resultValue = null;
		
		if (details.length > 0) {			
			resultValue = new Date[details.length];
			
			if (details.length == 1) {
				resultValue[0] = getTime(details[0].trim().split(":"));
			} else if (details.length == 2) {
				resultValue[0] = getTime(details[1].trim().split(":"));
			} else if (details.length == 3) {
				resultValue[0] = getTime(details[1].trim().split(":"));
				resultValue[1] = getTime(details[2].trim().split(":"));
			} else {
				//TODO - throw error - scheduler not set properly
				LOGGER.warn("scheduler not set properly, setting DEFAULT Schedule");
			}
		} else {
			//TODO - throw error - scheduler not set properly
			LOGGER.warn("scheduler not set properly, setting DEFAULT Schedule");
		}
		return resultValue;
	}

	private Date getTime(String[] scheduleTimeDetails) {
		
		String hour = scheduleTimeDetails[0].trim();
		String mint = scheduleTimeDetails[1].trim();
		String secs = scheduleTimeDetails[2].trim();
		Calendar caln = Calendar.getInstance();
		caln.set(Calendar.HOUR_OF_DAY,Integer.valueOf(hour));
		caln.set(Calendar.MINUTE,Integer.valueOf(mint));
		caln.set(Calendar.SECOND,Integer.valueOf(secs));
		caln.set(Calendar.MILLISECOND,0);

		Date scheduledHrs = caln.getTime();
		//LOGGER.debug("Scheduled Time = " + scheduledHrs);
		return scheduledHrs;
	}

	public List<FileAttributes> convertIntoFileAttributes(List<String> alltStatus) {

		List<FileAttributes> allFileStatus = new ArrayList<FileAttributes>();
		for (String indvStatus : alltStatus) {
			FileAttributes fileAttributes = new FileAttributes();
			String[] values = indvStatus.split("\\|");
			
			fileAttributes.setCaseId(values[0].trim());
			fileAttributes.setFileLocation(values[1].trim());
			fileAttributes.setFileName(values[2].trim());
			fileAttributes.setDate(values[3].trim());
			fileAttributes.setTime(values[4].trim());
			fileAttributes.setSize(values[5].trim() + "KB");
			fileAttributes.setNoOfLines(values[6].trim());
			if (values[7].trim().equalsIgnoreCase("NAVL")) {
				fileAttributes.setStatus(genConfig.getNotOkStatusText());
			} else if (Integer.valueOf(values[5].trim()) == 0 && values[7].trim().equalsIgnoreCase("AVL")) {
				fileAttributes.setStatus(genConfig.getNotOkStatusText());
			} else if (values[7].trim().equalsIgnoreCase("ERR")) {
				fileAttributes.setStatus(genConfig.getNotOkStatusText());
			} else {
				fileAttributes.setStatus(genConfig.getOkStatusText());
			}
			allFileStatus.add(fileAttributes);
		}
		return allFileStatus;
	}
	
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
				externalInterfaceConn.setStatus(genConfig.getOkStatusText());
			} else {
				externalInterfaceConn.setStatus(genConfig.getNotOkStatusText());
			}
			allExternalIntrfcConn.add(externalInterfaceConn);
		}
		return allExternalIntrfcConn;
	}

	public String getFolderLocation(String folder, String loadTime) {
		String logLocation = configuration.getRootDir().concat(File.separator).concat(folder).concat(File.separator).concat(loadTime);
		return logLocation;
	}

	public void createFileInputForScripts(String fileMapping, String srcLocation, boolean isExtensive) {
		
		
		File f = new File(srcLocation);
		f.getAbsoluteFile().mkdirs(); 
		
		try (Stream<String> stream = Files.lines(Paths.get(fileMapping))) {
			stream.forEach(line ->{
				try {
					processFileMappingLine(line.trim(), srcLocation, isExtensive);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private String processFileMappingLine(String line, String srcLocation, boolean isExtensive) throws IOException {
		
		//LOGGER.debug("line : " + line);
		
		String[] values = line.split("\\|");
		Path path = Paths.get(srcLocation.concat(File.separator).concat(values[0].trim()));
		createEmptyFile(values[0].trim(), srcLocation);
		Charset charset = StandardCharsets.UTF_8;
		List<String> content = new ArrayList<String>();
		
		String[] requirementIds = values[1].trim().split(",");
		
		Arrays.stream(requirementIds).forEach(reqId -> {
			String fileContent = getContentByReqId(reqId.trim(), isExtensive);
			if (fileContent != null) {
				content.add(fileContent);
			}
					});
		
		if (content.size() > 0) {
			//content.stream().forEach(LOGGER::debug);
			Files.write(path, content, charset, StandardOpenOption.APPEND);
		} 
		return null;
	}

	private void createEmptyFile(String fileName, String srcLocation) throws IOException {
		
		File f = new File(srcLocation.concat(File.separator).concat(fileName));	
		f.createNewFile();
		
	}

	private String getContentByReqId(String reqId, boolean isExtensive) {
		
		String value = null;
		if (reqId.contains("INF")) {
			if (dynConfig.getRequirementsMap().get(reqId) != null) {
				value = reqId.concat(",").concat(dynConfig.getRequirementsMap().get(reqId).trim());
			} else {
				//TODO - throw error
				LOGGER.warn("Application configuration not set up properly For RequirementId: " + reqId);
			}
		} else {
			if (isValidEntry(reqId, isExtensive)) {
				if (dynConfig.getRequirementsMap().get(reqId) != null) {
					value = reqId.concat(",").concat(dynConfig.getRequirementsMap().get(reqId).trim());
				} else {
					//TODO - throw error
					LOGGER.warn("Application configuration not set up properly For RequirementId: " + reqId);
				}
			}
		}
		return value;
	}
		
}
