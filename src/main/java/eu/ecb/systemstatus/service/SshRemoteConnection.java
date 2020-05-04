package eu.ecb.systemstatus.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream.GetField;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import eu.ecb.systemstatus.beans.FileAttributes;
import eu.ecb.systemstatus.configuration.MdpConfiguration;

@Component
public class SshRemoteConnection {
	
	@Autowired
	MdpConfiguration mdpConfig;
	
	public static void main(String[] args) {
		System.out.println(getFileDate(new Date()));
		System.out.println(getFileTime(new Date()));
	}
	
	public void executeRemoteSSH(String hostName) {
		
		String userName = mdpConfig.getEnvAccessUserName();
		String password = mdpConfig.getEnvAccessPassword();
		String oneToOneLoc = mdpConfig.getOneToOneSrcLocation();
		String oneToManyLoc = mdpConfig.getOneToManySrcLocation();
		
		Session session = null;
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		try {
			session = getSession(hostName, userName, password);			
			session.setConfig(config);
			session.connect();
			createLogFrOneToOneFiles(session, oneToOneLoc);
			createLogFrOneToManyFiles(session, oneToManyLoc);
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  finally {
			session.disconnect();
		}
	}
	
	public Session getSession(String hostname, String userName, String password) throws JSchException {
		JSch jsch = new JSch();
		Session session = null;
		//jsch.addIdentity(pvtKey);
		session = jsch.getSession(userName,hostname, 22);
		session.setPassword(password);

		return session;

	}
	
	private void createLogFrOneToOneFiles(Session session, String oneToOneLoc) throws IOException {
		
		Files.list(Paths.get(oneToOneLoc))
		.filter(Files::isRegularFile)
		.forEach(path -> {
			try {
				processOneToOneEachFile(path.toAbsolutePath().toString(), session);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	private void createLogFrOneToManyFiles(Session session, String logLocation) throws IOException {
		
		Files.list(Paths.get(logLocation))
		.filter(Files::isRegularFile)
		.forEach(path -> {
			try {
				processOneToManyEachFile(path.toAbsolutePath().toString(), session);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	
	private Session processOneToManyEachFile(String filePath, Session session) throws IOException {
		
		System.out.println(filePath);
		ChannelSftp sftp;
		Channel sftpChannel = null;
		try {
			sftpChannel = session.openChannel("sftp");
			sftp = (ChannelSftp) sftpChannel;		
			Files.lines(Paths.get(filePath)).forEach(line -> processEachLineOneToMany(line, sftp));
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			sftpChannel.disconnect();
		}
		return session;
		
	}
	
	private ChannelSftp processEachLineOneToOne(String line, ChannelSftp sftp) throws SftpException, ParseException, IOException {

		FileAttributes fileAttributes = new FileAttributes();

		String[] lineDetails = line.split(","); 
		fileAttributes.setCaseId(lineDetails[0]);
		String folderName = lineDetails[1];
		String folderLocation = getFolderName(folderName);
		String fileName = lineDetails[2];

		fileAttributes.setSize("0");
		fileAttributes.setNoOfLines("0");
		fileAttributes.setStatus("NAVL");
		fileAttributes.setFileLocation(folderLocation);

		checkEachFile(fileName, folderLocation, sftp, fileAttributes);
		return sftp;
	}

	private ChannelSftp processEachLineOneToMany(String line, ChannelSftp sftp) {
		
		FileAttributes fileAttributes = new FileAttributes();
		
		String[] lineDetails = line.split(","); 
		fileAttributes.setCaseId(lineDetails[0]);
		String folderName = lineDetails[1];
		String folderLocation = getFolderName(folderName);
		String[] fileNames = lineDetails[2].split("//|");
		
		fileAttributes.setSize("0");
		fileAttributes.setNoOfLines("0");
		fileAttributes.setStatus("NAVL");
		fileAttributes.setFileLocation(folderLocation);
		
		Arrays.stream(fileNames).forEach(fileName -> {
			try {
				checkEachFile(fileName, folderLocation, sftp, fileAttributes);
			} catch (SftpException | IOException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		return sftp;
		
	}

	private ChannelSftp checkEachFile(String fileName, String outFolderLocation, ChannelSftp sftp, FileAttributes fileAttributes) throws SftpException, IOException, ParseException {
		
		Date currentDate = new Date();
		String outFileName = fileName;
		Vector<LsEntry> files = sftp.ls(outFolderLocation);
		
		List<LsEntry> resultFiles = files.stream().filter(file -> file.getFilename().equalsIgnoreCase(fileName)).collect(Collectors.toList());
		if (resultFiles.size() == 1) {
			LsEntry resultFile = resultFiles.get(0);
			fileAttributes.setFileLocation(sftp.pwd());
			fileAttributes.setStatus("AVL");		
			outFileName = resultFile.getFilename();
			Date lastModified = new Date(resultFile.getAttrs().getMTime() * 1000L);
			if (!isLastModEqualsCurrentDat(lastModified, currentDate)) {
				fileAttributes.setStatus("NAVL");
			}
			fileAttributes.setSize(String.valueOf(resultFile.getAttrs().getSize()));
			fileAttributes.setNoOfLines(String.valueOf(getNoOfCountsInFile(sftp, resultFile.getFilename())));			
			fileAttributes.setDate(getFileDate(currentDate));
			fileAttributes.setTime(getFileTime(currentDate));
		} else {
			fileAttributes.setDate(getFileDate(currentDate));
			fileAttributes.setTime(getFileTime(currentDate));
		}
		fileAttributes.setFileName(outFileName);
		generateOutputLog(mdpConfig.getAcPrimaryFolder(), fileAttributes);
		return sftp;
	}

	private Session processOneToOneEachFile(String filePath, Session session) throws IOException {
		
		System.out.println(filePath);
		ChannelSftp sftp;
		Channel sftpChannel = null;
		try {
			sftpChannel = session.openChannel("sftp");
			sftp = (ChannelSftp) sftpChannel;		
			Files.lines(Paths.get(filePath)).forEach(line -> {
				try {
					processEachLineOneToOne(line, sftp);
				} catch (SftpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					sftp.disconnect();
				}
			});
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			sftpChannel.disconnect();
		}
		return session;
	}

	

	private String getFolderName(String folderName) {
		String derivedFolderName = folderName.concat(getFileDate(new Date())).concat("*");
		return derivedFolderName;
	}

	private long getNoOfCountsInFile(ChannelSftp sftp, String fileName) throws SftpException, IOException {
		long size = 0;
		InputStream inpStream = sftp.get(sftp.pwd().concat(File.separator).concat(fileName));
		BufferedReader bReader = new BufferedReader(new InputStreamReader(inpStream));
		size = bReader.lines().collect(Collectors.toList()).size();
		inpStream.close();
		return size;
	}

	private void generateOutputLog(String logLocation, FileAttributes fileAttributes) throws IOException {
		
		String text = fileAttributes.getCaseId().concat("|").concat(
				fileAttributes.getFileLocation().concat("|")).concat(
				fileAttributes.getFileName()).concat("|").concat(
				fileAttributes.getDate()).concat("|").concat(
				fileAttributes.getTime()).concat("|").concat(
				fileAttributes.getSize()).concat("|").concat(
				fileAttributes.getNoOfLines()).concat("|").concat(
				fileAttributes.getStatus());
		
		if (! new File(logLocation).exists()){
			new File(logLocation).mkdir();	
			File outputLog = new File(fileAttributes.getFileLocation().concat(File.separator).concat(fileAttributes.getFileName()));
			Files.write(Paths.get(outputLog.getAbsolutePath()), text.getBytes(), StandardOpenOption.APPEND);
	    }

		
	}

	public static String getFileTime(Date date) {
		Format formatter = new SimpleDateFormat("HH:mm:ss");
		String str = formatter.format(date);
		return str;
	}

	public static String getFileDate(Date date) {
		Format formatter = new SimpleDateFormat("yyyyMMdd");
		String str = formatter.format(date);
		return str;
	}

	private boolean isLastModEqualsCurrentDat(Date lastModified, Date currentDate) throws ParseException {
		
		boolean isEqals = false;
		Format formatter = new SimpleDateFormat("yyyy-MM-dd");
		String str = formatter.format(lastModified);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    Date dtWithoutTime = sdf.parse(str);
	    if (dtWithoutTime.compareTo(currentDate) == 0) {
	    	isEqals = true;
	    }
	    return isEqals;
		
	}
}
