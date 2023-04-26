package com.securecoding.securemailer.util;

import java.io.File;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;

public class Tools {

    public static String loadTemplateFile(String filename) {
        try {

        	//취약점 1 - scary 단계 -> 경로를 찾아가는 문자 열 제거
        	//File file = new File(String.format("%s\\%s.txt", Constants.HTML_TEMPLATE_LOCATION, filename, ".txt"));

        	//솔루션1 - spotBug - org.apache.commons.io.FilenameUtils 사용, 해당 라이브러리 build 처리
        	String pathName = "C:\\SecureCoding_Expert\\workspace\\SecureMailer\\src\\resources\\template";
        	File file = new File(pathName,FilenameUtils.getName(filename)+".txt");
        	
        	StringBuilder fileContents = new StringBuilder((int) file.length());

            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    fileContents.append(scanner.nextLine() + System.lineSeparator());
                }
                return fileContents.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String replaceString(String input, String... replace) {
        for(int i = 0; i < replace.length; i++)
            input = input.replaceFirst("%s", replace[i]);

        return input;
    }
}
