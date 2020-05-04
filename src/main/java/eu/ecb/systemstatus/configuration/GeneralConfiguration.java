package eu.ecb.systemstatus.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import eu.ecb.systemstatus.beans.Header;

@Component
@ConfigurationProperties("general")
public class GeneralConfiguration extends BaseClientProperties{
	
	public List<Header> getHeaderDetails() {
		List<Header> headers = new ArrayList<Header>();
		String headrSpclinks = getHeaderSpecificLink();
		String[] headerWithLnkDetails = headrSpclinks.split(",");
		Arrays.stream(headerWithLnkDetails).forEach(content -> headers.add(getAllHeaderDetails(content)));
		return headers;
	}

	private Header getAllHeaderDetails(String headerContent) {
		
		Header header = new Header();
		String[] headerWithEnvDetails = headerContent.split("\\|");
		header.setHeaderName(headerWithEnvDetails[0]);
		header.setRequestLink(headerWithEnvDetails[1]);
		return header;
	}
}
