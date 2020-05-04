package eu.ecb.systemstatus.ssh;

import com.jcraft.jsch.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SshRemoteConnect {

    public static void main(String[] args) {
        String host = "localhost";
        String user = "test";
        String password = "test";
        String command = "hostname\ndf -h\nexit\n";
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user,host, 22);
            session.setUserInfo(new sshRemoteExampleUserInfo(user, password));
            session.connect();
            Channel channel = session.openChannel("shell");
            channel.setInputStream(new ByteArrayInputStream(command.getBytes(StandardCharsets.UTF_8)));
            channel.setOutputStream(System.out);
            InputStream in = channel.getInputStream();
            StringBuilder outBuff = new StringBuilder();
            int exitStatus = -1;
            
            channel.connect();
            
            while (true) {                
                for (int c; ((c = in.read()) >= 0);) {
                    outBuff.append((char) c);
                }
                
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    exitStatus = channel.getExitStatus();
                    break;
                }
            }
            channel.disconnect();
            session.disconnect();
            
        // print the buffer's contents
        System.out.print (outBuff.toString());
        // print exit status
        System.out.print ("Exit status of the execution: " + exitStatus);
        if ( exitStatus == 0 ) {
            System.out.print (" (OK)\n");
        } else {
            System.out.print (" (NOK)\n");
        }
        
        } catch (IOException | JSchException ioEx) {
            System.err.println(ioEx.toString());
        }
    }   
}
