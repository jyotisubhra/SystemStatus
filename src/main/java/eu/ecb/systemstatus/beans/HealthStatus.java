package eu.ecb.systemstatus.beans;

public class HealthStatus {
	
	private String healthStatus;
	private String displayText;
	private String displayTextResult;
	private String displayType;
	private String logFile;
	
	public String getHealthStatus() {
		return healthStatus;
	}
	public void setHealthStatus(String healthStatus) {
		this.healthStatus = healthStatus;
	}
	public String getDisplayText() {
		return displayText;
	}
	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}
	public String getDisplayTextResult() {
		return displayTextResult;
	}
	public void setDisplayTextResult(String displayTextResult) {
		this.displayTextResult = displayTextResult;
	}
	public String getLogFile() {
		return logFile;
	}
	public String getDisplayType() {
		return displayType;
	}
	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}
	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}
	
}
