package com.mycompany.javafxapplication1;

import java.io.File;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;

public class ZipOutput {

    public static void zipWithPassword(String sourceFolder, String destinationZip, String password) throws ZipException {
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setEncryptFiles(true);
        zipParameters.setCompressionLevel(CompressionLevel.MAXIMUM);
        zipParameters.setEncryptionMethod(EncryptionMethod.AES);

        try {
            ZipFile zipFile = new ZipFile(destinationZip, password.toCharArray());
            zipFile.addFolder(new File(sourceFolder), zipParameters);
            System.out.println("File zipped with success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unzipWithPassword(String sourceZip, String destinationFolder, String password) throws ZipException {
        try {
            ZipFile zipFile = new ZipFile(sourceZip, password.toCharArray());
            zipFile.extractAll(destinationFolder);
            System.out.println("File unzipped with success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String sourceFolder = "/home/ntu-user/folder_to_add";
        String destinationZip = "/home/ntu-user/filename.zip";
        String destinationFolder = "/home/ntu-user/destination_directory";
        String password = "password";

        try {
            zipWithPassword(sourceFolder, destinationZip, password);
            unzipWithPassword(destinationZip, destinationFolder, password);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }
}
