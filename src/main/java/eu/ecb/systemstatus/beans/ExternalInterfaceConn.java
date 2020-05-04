package eu.ecb.systemstatus.beans;

public class ExternalInterfaceConn {

		private String source;
		private String destination;
		private String port;
		private String interfaceName;
		private String URL;
		private String status;
		protected String caseId;
		
		public String getCaseId() {
			return caseId;
		}
		public void setCaseId(String caseId) {
			this.caseId = caseId;
		}
		public String getSource() {
			return source;
		}
		public void setSource(String source) {
			this.source = source;
		}
		public String getDestination() {
			return destination;
		}
		public void setDestination(String destination) {
			this.destination = destination;
		}
		public String getPort() {
			return port;
		}
		public void setPort(String port) {
			this.port = port;
		}
		public String getInterfaceName() {
			return interfaceName;
		}
		public void setInterfaceName(String interfaceName) {
			this.interfaceName = interfaceName;
		}
		public String getURL() {
			return URL;
		}
		public void setURL(String uRL) {
			URL = uRL;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		
		
		
}
