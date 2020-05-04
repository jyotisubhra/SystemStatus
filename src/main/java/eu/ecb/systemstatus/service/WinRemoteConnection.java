package eu.ecb.systemstatus.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellNotAvailableException;
import com.profesorfalken.jpowershell.PowerShellResponse;

import eu.ecb.systemstatus.configuration.MdpConfiguration;

@Configuration
public class WinRemoteConnection {
	
	private static Logger LOGGER = LogManager.getLogger(WinRemoteConnection.class);
	@Autowired
	MdpConfiguration mdpConfig;
	
	public void callRemoteSystem(String scriptToBeexecuted, String hostname, String srcLoc, String destinationLoc) {


		File file = new File(getClass().getClassLoader().getResource(scriptToBeexecuted).getFile());

		/*Process proc;
		 * Runtime runtime = Runtime.getRuntime();
		try {
			proc = runtime.exec("powershell " + file.getAbsolutePath() + " -names " + "hello");
			proc.getOutputStream().close();
	        InputStream is = proc.getInputStream();
	        InputStreamReader isr = new InputStreamReader(is);
	        BufferedReader reader = new BufferedReader(isr);
	        String line;
	        while ((line = reader.readLine()) != null)
	        {
	            LOGGER.debug(line);
	        }
	        reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		try (PowerShell powerShell = PowerShell.openSession()) {       
			//Increase timeout to give enough time to the script to finish
			Map<String, String> config = new HashMap<String, String>();
			config.put("maxWait", mdpConfig.getScriptWaitTime());

			String params = hostname.concat(",").concat(srcLoc).concat(",").concat(destinationLoc);
			//Execute script
			PowerShellResponse response = powerShell.configuration(config)
					.executeScript(file.getAbsolutePath(), params);

			//Print results if the script
			LOGGER.debug("Script output:" + response.getCommandOutput());
		} catch(PowerShellNotAvailableException ex) {
			//Handle error when PowerShell is not available in the system
			//Maybe try in another way?
		}
	}
	
	public static void executeRemoteWin(String inputLoc) throws IOException {
		
		Files.list(Paths.get(inputLoc)).filter(Files::isRegularFile).forEach(path -> {
			try {
				processEachFile(path.toAbsolutePath().toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	
	private static Object processEachFile(String filePath) throws IOException {
		
		Files.lines(Paths.get(filePath)).forEach(line -> processEachLine(line));
		return null;
	}

	private static Object processEachLine(String line) {
		
		String[] lineDetails = line.split(",");
		String fileId = lineDetails[0];
		String folderName = lineDetails[1];
		String fileName = lineDetails[2];
		
		try (PowerShell powerShell = PowerShell.openSession()) {       

			Map<String, String> config = new HashMap<String, String>();
			config.put("maxWait", "80000");
			String cmd = createDynamicQuery(folderName.concat(File.separator).concat(fileName));
			
			System.out.println(cmd);
			
			PowerShellResponse response = powerShell.configuration(config).executeCommand(cmd);
			String[] lines = response.getCommandOutput().split("-");
			//LOGGER.debug("Script output:" + response.getCommandOutput());
			Arrays.stream(lines).forEach(System.out::println);
		} catch(PowerShellNotAvailableException ex) {
			//Handle error when PowerShell is not available in the system
			//Maybe try in another way?
		} finally {
			
		}
		return null;
	}

	private static String createDynamicQuery(String filepath) {
		
		String command = "Invoke-Command -ScriptBlock {"
				+ "$fileStatus = Test-Path -Path '" + filepath + "';"
				+ "$size = [math]::ceiling((Get-ChildItem " + filepath + ").length/1kb);"
				+ "$creationTime = (Get-item " + filepath + ").creationtime;"
				+ "$nlines = 0;"
				+ "gc " + filepath + " -read 1000 | % { $nlines += $_.Length };"
				+ "Write-Host FileStatus:$fileStatus - FileSize:$size - CreationDate:$creationTime - NoOfLines:$nlines"
				+ "}";
		
		return command;
	}

	public static void main(String[] args) {
		try {
			executeRemoteWin("C:\\MDP_CONFIG\\OT_Inputs");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
