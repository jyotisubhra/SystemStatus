package eu.ecb.systemstatus.configuration;

public class BaseClientProperties {
	
	
	private String headerSpecificLink;
	private String viewPowershellOutput;
	private String scheduleFolderRoot;
	private String scheduleFolderName;
	private String scheduleFileName;
	
	private String scriptLogFolder;
	private String scriptPropertiesLocation;
	private String monitoringHostName;
	private String winScriptTocall;
	private String sshScriptTocall;
	private String envType;
	
	private String rootDir;
	private String otEnvType;
	
	private String otPrimaryFolder;
	private String otPrimaryHostname;
	private String otPrimarySrcLocation;
	private String otPrimaryScriptTocall;
	private String otPrimaryCallApproach;
	
	private String otSecondaryFolder;
	private String otSecondaryHostname;
	private String otSecondarySrcLocation;
	private String otSecondaryScriptTocall;
	private String otSecondaryCallApproach;
	private String scriptWaitTime;
	
	
	private String acPrimaryFolder;
	private String acSrcLocation;
	private String oneToOneSrcLocation;
	private String oneToManySrcLocation;
	private String interfaceSrcLocation;
	private String acPrimaryHostname;
	private String acScriptTocall;
	private String acEnvType;
	
	private String envAccessUserName;
	private String envAccessPassword;
	
	
	public String getViewPowershellOutput() {
		return viewPowershellOutput;
	}
	public void setViewPowershellOutput(String viewPowershellOutput) {
		this.viewPowershellOutput = viewPowershellOutput;
	}
	public String getScriptLogFolder() {
		return scriptLogFolder;
	}
	public void setScriptLogFolder(String scriptLogFolder) {
		this.scriptLogFolder = scriptLogFolder;
	}
	public String getScriptPropertiesLocation() {
		return scriptPropertiesLocation;
	}
	public void setScriptPropertiesLocation(String scriptPropertiesLocation) {
		this.scriptPropertiesLocation = scriptPropertiesLocation;
	}
	public String getMonitoringHostName() {
		return monitoringHostName;
	}
	public void setMonitoringHostName(String monitoringHostName) {
		this.monitoringHostName = monitoringHostName;
	}
	public String getWinScriptTocall() {
		return winScriptTocall;
	}
	public void setWinScriptTocall(String winScriptTocall) {
		this.winScriptTocall = winScriptTocall;
	}
	public String getSshScriptTocall() {
		return sshScriptTocall;
	}
	public void setSshScriptTocall(String sshScriptTocall) {
		this.sshScriptTocall = sshScriptTocall;
	}
	public String getEnvType() {
		return envType;
	}
	public void setEnvType(String envType) {
		this.envType = envType;
	}
	public String getOtEnvType() {
		return otEnvType;
	}
	public void setOtEnvType(String otEnvType) {
		this.otEnvType = otEnvType;
	}
	public String getAcEnvType() {
		return acEnvType;
	}
	public void setAcEnvType(String acEnvType) {
		this.acEnvType = acEnvType;
	}
	public String getRootDir() {
		return rootDir;
	}
	public void setRootDir(String rootDir) {
		this.rootDir = rootDir;
	}
	public String getOtPrimaryFolder() {
		return otPrimaryFolder;
	}
	public void setOtPrimaryFolder(String otPrimaryFolder) {
		this.otPrimaryFolder = otPrimaryFolder;
	}
	public String getOtSecondaryFolder() {
		return otSecondaryFolder;
	}
	public void setOtSecondaryFolder(String otSecondaryFolder) {
		this.otSecondaryFolder = otSecondaryFolder;
	}
	public String getOtPrimaryHostname() {
		return otPrimaryHostname;
	}
	public void setOtPrimaryHostname(String otPrimaryHostname) {
		this.otPrimaryHostname = otPrimaryHostname;
	}
	public String getOtSecondaryHostname() {
		return otSecondaryHostname;
	}
	public void setOtSecondaryHostname(String otSecondaryHostname) {
		this.otSecondaryHostname = otSecondaryHostname;
	}
	public String getAcPrimaryFolder() {
		return acPrimaryFolder;
	}
	public void setAcPrimaryFolder(String acPrimaryFolder) {
		this.acPrimaryFolder = acPrimaryFolder;
	}
	public String getAcPrimaryHostname() {
		return acPrimaryHostname;
	}
	public void setAcPrimaryHostname(String acPrimaryHostname) {
		this.acPrimaryHostname = acPrimaryHostname;
	}
	public String getAcSrcLocation() {
		return acSrcLocation;
	}
	public void setAcSrcLocation(String acSrcLocation) {
		this.acSrcLocation = acSrcLocation;
	}
	public String getScheduleFolderRoot() {
		return scheduleFolderRoot;
	}
	public void setScheduleFolderRoot(String scheduleFolderRoot) {
		this.scheduleFolderRoot = scheduleFolderRoot;
	}
	public String getScheduleFolderName() {
		return scheduleFolderName;
	}
	public void setScheduleFolderName(String scheduleFolderName) {
		this.scheduleFolderName = scheduleFolderName;
	}
	public String getScheduleFileName() {
		return scheduleFileName;
	}
	public void setScheduleFileName(String scheduleFileName) {
		this.scheduleFileName = scheduleFileName;
	}
	public String getAcScriptTocall() {
		return acScriptTocall;
	}
	public void setAcScriptTocall(String acScriptTocall) {
		this.acScriptTocall = acScriptTocall;
	}
	public String getHeaderSpecificLink() {
		return headerSpecificLink;
	}
	public void setHeaderSpecificLink(String headerSpecificLink) {
		this.headerSpecificLink = headerSpecificLink;
	}
	public String getEnvAccessUserName() {
		return envAccessUserName;
	}
	public void setEnvAccessUserName(String envAccessUserName) {
		this.envAccessUserName = envAccessUserName;
	}
	public String getEnvAccessPassword() {
		return envAccessPassword;
	}
	public void setEnvAccessPassword(String envAccessPassword) {
		this.envAccessPassword = envAccessPassword;
	}
	public String getOneToOneSrcLocation() {
		return oneToOneSrcLocation;
	}
	public void setOneToOneSrcLocation(String oneToOneSrcLocation) {
		this.oneToOneSrcLocation = oneToOneSrcLocation;
	}
	public String getOneToManySrcLocation() {
		return oneToManySrcLocation;
	}
	public void setOneToManySrcLocation(String oneToManySrcLocation) {
		this.oneToManySrcLocation = oneToManySrcLocation;
	}
	public String getInterfaceSrcLocation() {
		return interfaceSrcLocation;
	}
	public void setInterfaceSrcLocation(String interfaceSrcLocation) {
		this.interfaceSrcLocation = interfaceSrcLocation;
	}
	public String getScriptWaitTime() {
		return scriptWaitTime;
	}
	public void setScriptWaitTime(String scriptWaitTime) {
		this.scriptWaitTime = scriptWaitTime;
	}
	public String getOtPrimarySrcLocation() {
		return otPrimarySrcLocation;
	}
	public void setOtPrimarySrcLocation(String otPrimarySrcLocation) {
		this.otPrimarySrcLocation = otPrimarySrcLocation;
	}
	public String getOtPrimaryScriptTocall() {
		return otPrimaryScriptTocall;
	}
	public void setOtPrimaryScriptTocall(String otPrimaryScriptTocall) {
		this.otPrimaryScriptTocall = otPrimaryScriptTocall;
	}
	public String getOtSecondarySrcLocation() {
		return otSecondarySrcLocation;
	}
	public void setOtSecondarySrcLocation(String otSecondarySrcLocation) {
		this.otSecondarySrcLocation = otSecondarySrcLocation;
	}
	public String getOtSecondaryScriptTocall() {
		return otSecondaryScriptTocall;
	}
	public void setOtSecondaryScriptTocall(String otSecondaryScriptTocall) {
		this.otSecondaryScriptTocall = otSecondaryScriptTocall;
	}
	public String getOtPrimaryCallApproach() {
		return otPrimaryCallApproach;
	}
	public void setOtPrimaryCallApproach(String otPrimaryCallApproach) {
		this.otPrimaryCallApproach = otPrimaryCallApproach;
	}
	public String getOtSecondaryCallApproach() {
		return otSecondaryCallApproach;
	}
	public void setOtSecondaryCallApproach(String otSecondaryCallApproach) {
		this.otSecondaryCallApproach = otSecondaryCallApproach;
	}
	
	
}
