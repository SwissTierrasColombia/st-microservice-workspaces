package com.ai.st.microservice.workspaces.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.FTPFeignClient;
import com.ai.st.microservice.workspaces.dto.ftp.MicroserviceCreateUserFTPDto;

@Component
public class FTPBusiness {

    private final Logger log = LoggerFactory.getLogger(FTPBusiness.class);

    @Value("${st.ftp.host}")
    private String hostFTP;

    @Value("${st.ftp.port}")
    private int portFTP;

    @Autowired
    private FTPFeignClient ftpFeignClient;

    public boolean createFTPCredentials(String username, String password) {

        boolean result;

        try {

            MicroserviceCreateUserFTPDto data = new MicroserviceCreateUserFTPDto();
            data.setUsername(username);
            data.setPassword(password);

            ftpFeignClient.createUserFTP(data);
            result = true;

        } catch (Exception e) {
            log.error("Error creating user ftp: " + e.getMessage());
            result = false;
        }

        return result;
    }

    public boolean uploadFileToFTP(String fileUrl, String nameFile, String ftpUser, String ftpPassword) {

        boolean result = false;

        FTPClient ftpClient = new FTPClient();

        try {

            ftpClient.connect(hostFTP, portFTP);
            showServerReply(ftpClient);

            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                log.error("Operation failed. Server reply code: " + replyCode);
            } else {

                boolean success = ftpClient.login(ftpUser, ftpPassword);
                showServerReply(ftpClient);
                if (!success) {
                    log.error("Could not login to the server");
                } else {

                    ftpClient.enterLocalPassiveMode();
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                    File firstLocalFile = new File(fileUrl);
                    InputStream inputStream = new FileInputStream(firstLocalFile);

                    log.info("Start uploading file " + fileUrl);
                    result = ftpClient.storeFile(nameFile, inputStream);
                    inputStream.close();
                    showServerReply(ftpClient);
                    if (result) {
                        log.info("The file is uploaded successfully.");
                    }
                }
            }
        } catch (IOException ex) {
            log.error("Error uploading file to FTP server: " + ex.getMessage());
            result = false;
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                log.error("Error closing connection: " + ex.getMessage());
                result = false;
            }
        }

        return result;
    }

    private void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                log.info("SERVER: " + aReply);
            }
        }
    }

}
