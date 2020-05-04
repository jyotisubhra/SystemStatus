package eu.ecb.systemstatus.controller;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.ecb.systemstatus.beans.ExternalInterfaceConn;
import eu.ecb.systemstatus.beans.FileAttributes;
import eu.ecb.systemstatus.beans.HealthStatus;
import eu.ecb.systemstatus.configuration.GeneralConfiguration;
import eu.ecb.systemstatus.configuration.MdpConfiguration;
import eu.ecb.systemstatus.service.MonitorService;
import eu.ecb.systemstatus.service.ServiceHelper;
import eu.ecb.systemstatus.service.WinRemoteConnection;

@Controller
public class MonitorController {

	private static Logger LOGGER = LogManager.getLogger(MonitorController.class);
	@Autowired
	private MonitorService service;
	@Autowired
	ServiceHelper serviceHelper;
	@Autowired
	MdpConfiguration mdpConfiguration;	
	@Autowired
	GeneralConfiguration genConfiguration;
	@Autowired
	WinRemoteConnection winRemoteConn;

	
	@GetMapping("/systemstatus")
	public String getStatus(@RequestParam(name="time", required=false, 
	defaultValue="10:00") String time, Model model) {

		List<HealthStatus> healthStatusList = getGenericAllCompStatus();
		model.addAttribute("allHealths", healthStatusList);
		//model.addAttribute("loadTime", loadTime.toString());
		model.addAttribute("headers", genConfiguration.getHeaderDetails());
		service.convertIntoText(healthStatusList);
		return "index";
	}
	
	@GetMapping("/systemstatus/txt")
	public @ResponseBody String getStatusTxt(@RequestParam(name="time", required=false, 
	defaultValue="10:00") String time, Model model) {

		List<HealthStatus> healthStatusList = getGenericAllCompStatus();
		String value = service.convertIntoText(healthStatusList);
		return value;
	}
	
	@GetMapping("/systemstatus/html")
	public String getStatusHtml(@RequestParam(name="time", required=false, 
	defaultValue="10:00") String time, Model model) {

		List<HealthStatus> healthStatusList = getGenericAllCompStatus();
		String value = service.convertIntoText(healthStatusList);
		model.addAttribute("value", value);
		return "plainText";
	}
	
	private List<HealthStatus> getGenericAllCompStatus() {

		//get current timestamp - which will be used to create a folder with this name
		Long loadTime = Calendar.getInstance().getTimeInMillis();

		String destinationLoc = genConfiguration.getRootDir()
				.concat(File.separator)
				.concat(genConfiguration.getScriptLogFolder())
				.concat(File.separator)
				.concat(loadTime.toString());
		String srcLoc = genConfiguration.getRootDir()
				.concat(File.separator)
				.concat(genConfiguration.getScriptPropertiesLocation())
				.concat(File.separator)
				.concat("*");
		String hostname = genConfiguration.getMonitoringHostName();

		//serviceHelper.loadConfig();
		if (genConfiguration.getEnvType().equalsIgnoreCase("Windows")) {
			String scriptToBeexecuted = genConfiguration.getWinScriptTocall();
			winRemoteConn.callRemoteSystem(scriptToBeexecuted, hostname, srcLoc, destinationLoc);
		} else {
			//TODO
		}
		List<HealthStatus> healthStatusList = service.getStatus(destinationLoc);
		return healthStatusList;
	}
	
	
	@GetMapping("/oTPstatus")
	public String otpStatus(@RequestParam(name="time", required=false, 
	defaultValue="10:00") String time, Model model) {

		List<HealthStatus> healthStatusList = getOtPrimaryComponentStatus();
		model.addAttribute("allHealths", healthStatusList);
		//model.addAttribute("loadTime", loadTime.toString());
		model.addAttribute("headers", genConfiguration.getHeaderDetails());
		return "SystemStatus";
	}
	
	@GetMapping("/oTPstatus/txt")
	public @ResponseBody String otpStatusTxt(@RequestParam(name="time", required=false, 
	defaultValue="10:00") String time, Model model) {

		List<HealthStatus> healthStatusList = getOtPrimaryComponentStatus();
		String value = service.convertIntoText(healthStatusList);
		return value;
	}

	private List<HealthStatus> getOtPrimaryComponentStatus() {
		
		//get current timestamp - which will be used to create a folder with this name
		Long loadTime = Calendar.getInstance().getTimeInMillis();
		
		String destinationLoc = mdpConfiguration.getRootDir()
								.concat(File.separator)
								.concat(mdpConfiguration.getOtPrimaryFolder())
								.concat(File.separator)
								.concat(loadTime.toString());
		String srcLoc = mdpConfiguration.getRootDir()
								.concat(File.separator)
								.concat(mdpConfiguration.getOtPrimarySrcLocation())
								.concat(File.separator)
								.concat("*");
		String hostname = mdpConfiguration.getOtPrimaryHostname();
		
		//serviceHelper.loadConfig();
		String scriptToBeexecuted = mdpConfiguration.getOtPrimaryScriptTocall();
		winRemoteConn.callRemoteSystem(scriptToBeexecuted, hostname, srcLoc, destinationLoc);
		
		List<HealthStatus> healthStatusList = service.getStatus(destinationLoc);
		return healthStatusList;
	}
	
	@GetMapping("/oTSstatus")
	public String otsStatus(@RequestParam(name="time", required=false, 
	defaultValue="10:00") String time, Model model) {
		
		List<HealthStatus> healthStatusList = getOtSecondaryComponentStatus();
		model.addAttribute("allHealths", healthStatusList);
		model.addAttribute("headers", genConfiguration.getHeaderDetails());
		return "SystemStatus";
	}
	
	@GetMapping("/oTSstatus/txt")
	public @ResponseBody String otsStatusTxt(@RequestParam(name="time", required=false, 
	defaultValue="10:00") String time, Model model) {
		
		List<HealthStatus> healthStatusList = getOtSecondaryComponentStatus();
		String value = service.convertIntoText(healthStatusList);
		return value;
	}

	private List<HealthStatus> getOtSecondaryComponentStatus() {
		//get current timestamp - which will be used to create a folder with this name
		Long loadTime = Calendar.getInstance().getTimeInMillis();
		
		String destinationLoc = mdpConfiguration.getRootDir()
							.concat(File.separator)
							.concat(mdpConfiguration.getOtSecondaryFolder())
							.concat(File.separator)
							.concat(loadTime.toString());
		String srcLoc = mdpConfiguration.getRootDir().concat(File.separator).concat(mdpConfiguration.getOtSecondarySrcLocation()).concat(File.separator).concat("*");
		String hostname = mdpConfiguration.getOtSecondaryHostname();
		
		LOGGER.debug("Log Location " + destinationLoc);
		LOGGER.debug("hostname " + hostname);
		
		//serviceHelper.loadConfig();
		String scriptToBeexecuted = mdpConfiguration.getOtSecondaryScriptTocall();
		winRemoteConn.callRemoteSystem(scriptToBeexecuted, hostname, srcLoc, destinationLoc);
		
		List<HealthStatus> healthStatusList = service.getStatus(destinationLoc);
		return healthStatusList;
	}
	
	@GetMapping("/aCstatus")
	public String acStatus(@RequestParam(name="time", required=false, 
	defaultValue="10:00") String time, Model model) {

		List<HealthStatus> healthStatusList = getAcAllComponentStatus();
		model.addAttribute("allHealths", healthStatusList);
		model.addAttribute("headers", genConfiguration.getHeaderDetails());
		return "SystemStatus";
	}
	
	@GetMapping("/aCstatus/txt")
	public @ResponseBody String acStatusTxt(@RequestParam(name="time", required=false, 
	defaultValue="10:00") String time, Model model) {

		List<HealthStatus> healthStatusList = getAcAllComponentStatus();
		String value = service.convertIntoText(healthStatusList);
		return value;
	}

	private List<HealthStatus> getAcAllComponentStatus() {
		//get current timestamp - which will be used to create a folder with this name
		Long loadTime = Calendar.getInstance().getTimeInMillis();
		String destinationLoc = mdpConfiguration.getRootDir().concat(File.separator).concat(mdpConfiguration.getAcPrimaryFolder()).concat(File.separator).concat(loadTime.toString());
		String srcLoc = mdpConfiguration.getRootDir().concat(File.separator).concat(mdpConfiguration.getAcSrcLocation()).concat(File.separator).concat("*");
		String hostname = mdpConfiguration.getAcPrimaryHostname();
				
		//serviceHelper.loadConfig();
		String scriptToBeexecuted = mdpConfiguration.getAcScriptTocall();
		winRemoteConn.callRemoteSystem(scriptToBeexecuted, hostname, srcLoc, destinationLoc);
		
					
		List<HealthStatus> healthStatusList = service.getStatus(destinationLoc);
		return healthStatusList;
	}
	
	@GetMapping("/status/file")
	public String getIndividualStatus(@RequestParam Map<String,String> requestParams, Model model) {
		
		//String loadTime = requestParams.get("loadTime");
		String logFile = requestParams.get("logFile");
		
		List<FileAttributes> allFileExeDetails = service.getAllFileDetails(logFile);
		model.addAttribute("allFileExeDetails", allFileExeDetails);
		model.addAttribute("headers", genConfiguration.getHeaderDetails());
		return "FileExecutionDetails";
	}
	
	@GetMapping("/status/interface")
	public String otInterfaceConnStatus(@RequestParam Map<String,String> requestParams, Model model) {

		//String loadTime = requestParams.get("loadTime");
		String logFile = requestParams.get("logFile");
		
		List<ExternalInterfaceConn> otInterfaceConnDetails = service.getAllInterfaceDetails(logFile);
		model.addAttribute("otInterfaceConnDetails", otInterfaceConnDetails);
		model.addAttribute("headers", genConfiguration.getHeaderDetails());
		return "InterfaceDetailsStatus";
	}


}
