package eu.ecb.systemstatus.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("mdp")
public class MdpConfiguration extends BaseClientProperties {
	
				
}
