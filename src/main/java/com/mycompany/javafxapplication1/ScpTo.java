package com.mycompany.javafxapplication1;

import com.jcraft.jsch.*;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;

public class ScpTo {
    private static final String
            USERNAME = "root";
    private static final String PASSWORD = "ntu-user";
    private static final int REMOTE_PORT = 22;
    private static final int SESSION_TIMEOUT = 10000;
    private static final int CHANNEL_TIMEOUT = 5000;
    public static final  int numberOfChunks = 4;


    public static void dockerConnect(String localFile, String remoteFile, String remoteHost, String Process) {
       
        Session jschSession = null;

        try {
            JSch jsch = new JSch();
            jsch.setKnownHosts("/home/mkyong/.ssh/known_hosts");

            jschSession = jsch.getSession(USERNAME, remoteHost, REMOTE_PORT);
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
                channelSftp.put("temp/" + localFile, remoteFile);
                System.out.println("Remote file created.");
            } 
            if ("get".equals(Process)) {
                channelSftp.get(remoteFile, "temp/" + localFile);
                System.out.println("Remote file retrieved.");
            } 

            channelSftp.exit();

        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        } finally {
            if (jschSession != null) {
                jschSession.disconnect();
            }
        }

    }
}
