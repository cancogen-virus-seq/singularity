package org.cancogenvirusseq.singularity.components.utils;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestCompression {

    public static void main(String args[]){

        /*try {
         *//*InputStream in = Files.newInputStream(Paths.get("/Users/urangwala/muse-5d48dddf45-rvt5q.log"));
            OutputStream fout = Files.newOutputStream(Paths.get("/Users/urangwala/LogFile.gz"));*//*

            FileInputStream in = new FileInputStream("/Users/urangwala/BigFat.fasta");
            FileOutputStream fout = new FileOutputStream(new File("/Users/urangwala/BigFat.fasta.gz"));
            BufferedOutputStream out = new BufferedOutputStream(fout);
            GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(out);
            final byte[] buffer = new byte[1024];
            int n = 0;
            while (-1 != (n = in.read(buffer))) {
                gzOut.write(buffer, 0, n);
            }
            gzOut.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        /*try{
            testLargeArchive();
        }catch (Exception e){
            e.printStackTrace();
        }*/

        generateFile();

        //create gzip out stream
        //create tar out stream


    }



    public static void testLargeArchive() throws Exception {

        File file = new File("/Users/urangwala/TestCompression/Output/BIGTARPOSIX30.tar.gz");
        FileOutputStream fout = new FileOutputStream(file);

        BufferedOutputStream out = new BufferedOutputStream(fout);
        GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(out);

        TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(gzOut);
        tarArchiveOutputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        //tarArchiveOutputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);

        File inputFile1 = new File("/Users/urangwala/TestCompression/testfiledownload.txt");
        File inputFile2 = new File("/Users/urangwala/TestCompression/tiny.tsv");

        //file1
        System.out.println("Putting in tar archive file1");
        tarArchiveOutputStream.putArchiveEntry(new TarArchiveEntry(inputFile1, inputFile1.getName()));
        System.out.println("Copying to tar archive output Stream file1");
        IOUtils.copy(new BufferedInputStream(new FileInputStream(inputFile1)), tarArchiveOutputStream);
        tarArchiveOutputStream.closeArchiveEntry();

        //file2
        System.out.println("Putting in tar archive file2");
        tarArchiveOutputStream.putArchiveEntry(new TarArchiveEntry(inputFile2, inputFile2.getName()));
        System.out.println("Copying to tar archive output Stream file2");
        IOUtils.copy(new BufferedInputStream(new FileInputStream(inputFile2)), tarArchiveOutputStream);
        tarArchiveOutputStream.closeArchiveEntry();

        tarArchiveOutputStream.close();
    }


    public static void generateFile(){
        try {

            System.out.println("Creating input file");
            File file = new File("/Users/urangwala/TestCompression/BigFatOne.fasta");

            System.out.println("Reading from input file");
            FileInputStream fin = new FileInputStream(file);

            int i = 0;
            String s = "";

            while((i=fin.read())!=-1) {
                s = s + String.valueOf((char)i);
            }
            System.out.println("Creating output file");
            File outFile = new File("/Users/urangwala/TestCompression/BIGFILE.fasta");
            FileOutputStream fout = new
                    FileOutputStream(outFile);
            byte[] b = s.getBytes();

            System.out.println("Writing to output file");
            fout.write(b);
            fout.close();

            System.out.println("Done reading and writing!!");

        } catch(Exception e){
            System.out.println(e);
        }

    }



}
