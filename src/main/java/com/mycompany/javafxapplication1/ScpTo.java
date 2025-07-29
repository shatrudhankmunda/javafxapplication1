package com.mycompany.javafxapplication1;

import com.jcraft.jsch.*;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;

public class ScpTo {
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    private static final int REMOTE_PORT = 2222;
    private static final int SESSION_TIMEOUT = 10000;
    private static final int CHANNEL_TIMEOUT = 5000;
    public static final  int numberOfChunks = 4;


    public static boolean dockerConnect(String localFile, String remoteFile, String remoteHost,int remotePort, String Process) throws Exception {

        Session jschSession = null;

        try {
            JSch jsch = new JSch();
            jsch.setKnownHosts(System.getProperty("user.home") + "/.ssh/known_hosts");
            jschSession = jsch.getSession(USERNAME, remoteHost, remotePort);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            jschSession.setConfig(config);

            jschSession.setPassword(PASSWORD);

            jschSession.connect(SESSION_TIMEOUT);

            Channel sftp = jschSession.openChannel("sftp");

            sftp.connect(CHANNEL_TIMEOUT);

            ChannelSftp channelSftp = (ChannelSftp) sftp;

            if ("delete".equals(Process)) {
                channelSftp.rm(remoteFile);
                System.out.println("Remote file deleted.");
            }
            if ("create".equals(Process)) {
                channelSftp.put( localFile, remoteFile);
                System.out.println("Remote file created.");
            }
            if ("get".equals(Process)) {
                channelSftp.get(remoteFile, "temp/" + localFile);
                System.out.println("Remote file retrieved.");
            }

            channelSftp.exit();

        }finally {
            if (jschSession != null) {
                jschSession.disconnect();
            }
        }
     return true;
    }

    public static void main(String[] args) {
//      boolean test =  dockerConnect("test.txt", "test.txt", "localhost",2221, "delete");
//       System.out.println("Operation successful: " + test);
        for(int i = 1; i < 10 ; i++){
            System.out.println(2220+i);
        }
    }
}
