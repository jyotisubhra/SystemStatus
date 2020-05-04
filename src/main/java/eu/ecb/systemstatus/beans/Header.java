package eu.ecb.systemstatus.beans;

public class Header {
	private String headerName;
	private String requestLink;
	private String envType;
	
	public String getHeaderName() {
		return headerName;
	}
	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}
	public String getEnvType() {
		return envType;
	}
	public void setEnvType(String envType) {
		this.envType = envType;
	}
	public String getRequestLink() {
		return requestLink;
	}
	public void setRequestLink(String requestLink) {
		this.requestLink = requestLink;
	}
	
	
}
