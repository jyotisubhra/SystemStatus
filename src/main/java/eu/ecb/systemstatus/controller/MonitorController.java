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
	public String getStatus(@RequestParam Map<String,String> requestParams, Model model) {

		List<HealthStatus> healthStatusList = getGenericAllCompStatus();
		model.addAttribute("allHealths", healthStatusList);
		//model.addAttribute("loadTime", loadTime.toString());
		model.addAttribute("headers", genConfiguration.getHeaderDetails());
		service.convertIntoText(healthStatusList);
		return "index";
	}
	
	@GetMapping("/systemstatus/txt")
	public @ResponseBody String getStatusTxt(@RequestParam Map<String,String> requestParams, Model model) {

		List<HealthStatus> healthStatusList = getGenericAllCompStatus();
		String value = service.convertIntoText(healthStatusList);
		return value;
	}
	
	@GetMapping("/systemstatus/html")
	public String getStatusHtml(@RequestParam Map<String,String> requestParams, Model model) {

		List<HealthStatus> healthStatusList = getGenericAllCompStatus();
		String value = service.convertIntoText(healthStatusList);
		model.addAttribute("value", value);
		return "plainText";
	}
	
	private List<HealthStatus> getGenericAllCompStatus() {

		Long loadTime = Calendar.getInstance().getTimeInMillis();

		String destinationLoc = genConfiguration.getRootDir()
				.concat(File.separator)
				.concat(genConfiguration.getScriptLogFolder())
				.concat(File.separator)
				.concat(loadTime.toString());
		//String srcLoc = genConfiguration.getRootDir().concat(File.separator).concat(genConfiguration.getScriptPropertiesLocation()).concat(File.separator).concat("*");
		String hostname = genConfiguration.getMonitoringHostName();
		
		String fileSrcLoc = genConfiguration.getRootDir().concat(File.separator).concat(
				genConfiguration.getScriptPropertiesLocation()).concat(File.separator).concat(loadTime.toString());
		String fileMapping = genConfiguration.getRootDir().concat(File.separator).concat(genConfiguration.getFileMapFile());
		serviceHelper.createFileInputForScripts(fileMapping, fileSrcLoc, false);
		
		if (genConfiguration.getEnvType().equalsIgnoreCase("Windows")) {
			String scriptToBeexecuted = genConfiguration.getWinScriptTocall();
			winRemoteConn.callRemoteSystem(scriptToBeexecuted, hostname, fileSrcLoc, destinationLoc);
		} else {
			//TODO
		}
		List<HealthStatus> healthStatusList = service.getStatus(destinationLoc);
		return healthStatusList;
	}
	
	
	@GetMapping("/oTPstatus")
	public String otpStatus(@RequestParam Map<String,String> requestParams, Model model) {

		List<HealthStatus> healthStatusList = getOtPrimaryComponentStatus(requestParams.get("extensiveReport"));
		model.addAttribute("allHealths", healthStatusList);
		//model.addAttribute("loadTime", loadTime.toString());
		model.addAttribute("headers", genConfiguration.getHeaderDetails());
		model.addAttribute("extensiveRepURL", "/oTPstatus");
		return "SystemStatus";
	}
	
	@GetMapping("/oTPstatus/txt")
	public @ResponseBody String otpStatusTxt(@RequestParam Map<String,String> requestParams, Model model) {

		List<HealthStatus> healthStatusList = getOtPrimaryComponentStatus(requestParams.get("extensiveReport"));
		String value = service.convertIntoText(healthStatusList);
		return value;
	}

	private List<HealthStatus> getOtPrimaryComponentStatus(String extensiveReport) {
		
		Long loadTime = Calendar.getInstance().getTimeInMillis();
		boolean isExtensive = false;
		if (extensiveReport != null && extensiveReport.equalsIgnoreCase("true")) {
			isExtensive = true;
		}
		String destinationLoc = mdpConfiguration.getRootDir()
								.concat(File.separator)
								.concat(mdpConfiguration.getOtPrimaryFolder())
								.concat(File.separator)
								.concat(loadTime.toString());
									
		String hostname = mdpConfiguration.getOtPrimaryHostname();
		
		String fileSrcLoc = mdpConfiguration.getRootDir().concat(File.separator).concat(
				mdpConfiguration.getOtPrimarySrcLocation()).concat(File.separator).concat(loadTime.toString());
		String fileMapping = mdpConfiguration.getRootDir().concat(File.separator).concat(mdpConfiguration.getOtPrimaryMapFolder());
		serviceHelper.createFileInputForScripts(fileMapping, fileSrcLoc, isExtensive);
		
		String scriptToBeexecuted = mdpConfiguration.getOtPrimaryScriptTocall();
		if (mdpConfiguration.getOtPrimaryCallApproach().equalsIgnoreCase("INDV_FILE")) {
			winRemoteConn.callRemotePerFile(scriptToBeexecuted, hostname, fileSrcLoc, destinationLoc);
		} else {
			winRemoteConn.callRemoteSystem(scriptToBeexecuted, hostname, fileSrcLoc, destinationLoc);
		}
		
		List<HealthStatus> healthStatusList = service.getStatus(destinationLoc);
		return healthStatusList;
	}
	
	@GetMapping("/oTSstatus")
	public String otsStatus(@RequestParam Map<String,String> requestParams, Model model) {
		
		List<HealthStatus> healthStatusList = getOtSecondaryComponentStatus(requestParams.get("extensiveReport"));
		model.addAttribute("allHealths", healthStatusList);
		model.addAttribute("headers", genConfiguration.getHeaderDetails());
		model.addAttribute("extensiveRepURL", "/oTSstatus");
		return "SystemStatus";
	}
	
	@GetMapping("/oTSstatus/txt")
	public @ResponseBody String otsStatusTxt(@RequestParam Map<String,String> requestParams, Model model) {
		
		List<HealthStatus> healthStatusList = getOtSecondaryComponentStatus(requestParams.get("extensiveReport"));
		String value = service.convertIntoText(healthStatusList);
		return value;
	}

	private List<HealthStatus> getOtSecondaryComponentStatus(String extensiveReport) {
		Long loadTime = Calendar.getInstance().getTimeInMillis();
		
		boolean isExtensive = false;
		if (extensiveReport != null && extensiveReport.equalsIgnoreCase("true")) {
			isExtensive = true;
		}
		
		String destinationLoc = mdpConfiguration.getRootDir()
							.concat(File.separator)
							.concat(mdpConfiguration.getOtSecondaryFolder())
							.concat(File.separator)
							.concat(loadTime.toString());
		
		String hostname = mdpConfiguration.getOtSecondaryHostname();
		
		String fileSrcLoc = mdpConfiguration.getRootDir().concat(File.separator).concat(
				mdpConfiguration.getOtSecondarySrcLocation()).concat(File.separator).concat(loadTime.toString());
		String fileMapping = mdpConfiguration.getRootDir().concat(File.separator).concat(mdpConfiguration.getOtSecondaryMapFolder());
		serviceHelper.createFileInputForScripts(fileMapping, fileSrcLoc, isExtensive);
		
		String scriptToBeexecuted = mdpConfiguration.getOtSecondaryScriptTocall();
		if (mdpConfiguration.getOtSecondaryCallApproach().equalsIgnoreCase("INDV_FILE")) {
			winRemoteConn.callRemotePerFile(scriptToBeexecuted, hostname, fileSrcLoc, destinationLoc);
		} else {
			winRemoteConn.callRemoteSystem(scriptToBeexecuted, hostname, fileSrcLoc, destinationLoc);
		}
		List<HealthStatus> healthStatusList = service.getStatus(destinationLoc);
		return healthStatusList;
	}
	
	@GetMapping("/aCstatus")
	public String acStatus(@RequestParam Map<String,String> requestParams, Model model) {

		List<HealthStatus> healthStatusList = getAcAllComponentStatus(requestParams.get("extensiveReport"));
		model.addAttribute("allHealths", healthStatusList);
		model.addAttribute("headers", genConfiguration.getHeaderDetails());
		model.addAttribute("extensiveRepURL", "/aCstatus");
		return "SystemStatus";
	}
	
	@GetMapping("/aCstatus/txt")
	public @ResponseBody String acStatusTxt(@RequestParam Map<String,String> requestParams, Model model) {

		List<HealthStatus> healthStatusList = getAcAllComponentStatus(requestParams.get("extensiveReport"));
		String value = service.convertIntoText(healthStatusList);
		return value;
	}

	private List<HealthStatus> getAcAllComponentStatus(String extensiveReport) {
		
		boolean isExtensive = false;
		if (extensiveReport != null && extensiveReport.equalsIgnoreCase("true")) {
			isExtensive = true;
		}
		Long loadTime = Calendar.getInstance().getTimeInMillis();
		String destinationLoc = mdpConfiguration.getRootDir().concat(File.separator).concat(mdpConfiguration.getAcPrimaryFolder()).concat(File.separator).concat(loadTime.toString());
		String srcLoc = mdpConfiguration.getRootDir().concat(File.separator).concat(mdpConfiguration.getAcSrcLocation()).concat(File.separator).concat("*");
		
		String oneToOneFileMapping = mdpConfiguration.getRootDir().concat(File.separator).concat(mdpConfiguration.getOneToOneFileMappingLoc());		
		String oneToOneSrcLoc = mdpConfiguration.getRootDir().concat(File.separator).concat(mdpConfiguration.getOneToOneSrcLocation()).concat(File.separator).concat(loadTime.toString());;
		serviceHelper.createFileInputForScripts(oneToOneFileMapping, oneToOneSrcLoc, isExtensive);
		
		String oneToManyFileMappingLoc = mdpConfiguration.getRootDir().concat(File.separator).concat(mdpConfiguration.getOneToManyFileMappingLoc());	
		String oneToManySrcLoc = mdpConfiguration.getRootDir().concat(File.separator).concat(mdpConfiguration.getOneToManySrcLocation()).concat(File.separator).concat(loadTime.toString());
		serviceHelper.createFileInputForScripts(oneToManyFileMappingLoc, oneToManySrcLoc, isExtensive);
		
		String hostname = mdpConfiguration.getAcPrimaryHostname();
				
		String scriptToBeexecuted = mdpConfiguration.getAcScriptTocall();
		winRemoteConn.callRemoteSystem(scriptToBeexecuted, hostname, srcLoc, destinationLoc);
		
					
		List<HealthStatus> healthStatusList = service.getStatus(destinationLoc);
		return healthStatusList;
	}
	
	@GetMapping("/status/file")
	public String getIndividualStatus(@RequestParam Map<String,String> requestParams, Model model) {
		
		//String loadTime = requestParams.get("loadTime");
		String logFile = requestParams.get("logFile");
		String pageHeader = new File(logFile).getName().replaceFirst("[.][^.]+$", "").toUpperCase().replace("_", " ");
		
		List<FileAttributes> allFileExeDetails = service.getAllFileDetails(logFile);
		model.addAttribute("allFileExeDetails", allFileExeDetails);
		model.addAttribute("headers", genConfiguration.getHeaderDetails());
		model.addAttribute("detailText" , genConfiguration.getFileDetailsPageText());
		model.addAttribute("pageHeader", pageHeader);
		
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
