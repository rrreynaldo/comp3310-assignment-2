//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ClientGopher {
    static int port = 70;   // The default port for Gopher Protocol
    static String address = "localhost";

    static ArrayList<String> exitCommand = new ArrayList<>(Arrays.asList("q", "Q"));
    static ArrayList<String> backCommand = new ArrayList<>(Arrays.asList("b", "back"));
    static ArrayList<String> forwardCommand = new ArrayList<>(Arrays.asList("f", "forward"));
    static ArrayList<String> crawlCommand = new ArrayList<>(Arrays.asList("crawl", "crw"));

    static Socket socket;
    static BufferedWriter outputStream;
    static BufferedReader inputStream;
    static BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

    static String downloadFolderName = "crawl-download";

    public ClientGopher() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("The usage of the command is ./ClientTCP {address} {port} [crawl]");
            return;
        }
        address = args[0];
        port = Integer.parseInt(args[1]);

        // Check if the crawl argument is provided
        boolean shouldCrawl = args.length == 3 && args[2].equalsIgnoreCase("crawl");

        // Call the crawl function if the crawl argument is provided
        if (shouldCrawl) {
            crawl();
            return;
        }

        // Opening a socket to the server
        connectSocket();
        Directory directoryNavigation = new Directory();

        // Getting the initial default response from the Gopher Server
        outputStream.write(directoryNavigation + "\r\n");
        outputStream.flush();

        // Displaying the message from the server
        parseResponse(inputStream, directoryNavigation);
        displayMessage(directoryNavigation);
        // Closing the socket connection
        socket.close();
        while(true){
            // Opening a new socket connection
            connectSocket();

            // Prompting a new selector from the user
            System.out.print("Enter selector: ");
            String userCommand = consoleReader.readLine();

            // A command to parse-in crawl to the current directory
            if (crawlCommand.contains(userCommand.toLowerCase())) {
                crawl();
                break;
            }

            // A command to parse-in the exit command from the application
            if (exitCommand.contains(userCommand.toLowerCase())) {
                break;
            }

            // A command to parse-in the back navigation properties from a directory
            if (backCommand.contains(userCommand.toLowerCase())) {
                if (directoryNavigation.getParentDirectory() != null) {
                    directoryNavigation = directoryNavigation.getParentDirectory();
                }
            } else if (forwardCommand.contains(userCommand.toLowerCase())) {
                if (directoryNavigation.getSubDirectory() != null) {
                    directoryNavigation = directoryNavigation.getSubDirectory();
                }
            } else if (directoryNavigation.hasTextFilePath(userCommand)) {
                downloadFile(directoryNavigation.getTextFileFromPath(userCommand));
                continue;
            } else {
                Directory subDirectory = new Directory(userCommand);
                subDirectory.setParentDirectory(directoryNavigation);
                directoryNavigation.setSubDirectory(subDirectory);
                directoryNavigation = subDirectory;
            }

            // Calling the new directory navigation to the server
            outputStream.write(directoryNavigation.getCurrentPath() + "\r\n");
            outputStream.flush();

            // To ensure that there is no remaining data from the previous server's response
            while(inputStream.ready()){
                inputStream.readLine();
            }
            // Reading the response from the server
            parseResponse(inputStream, directoryNavigation);
            // Displaying the message from the server
            displayMessage(directoryNavigation);
            // Closing the old socket
            socket.close();
        }
        socket.close();
        System.out.println("client: closed socket and terminating");
    }

    // A sets of array list that is used for the crawler to keep track of the different resources
    static ArrayList<File> visitedPath = new ArrayList<>();     // List of all the visited path selector
    static ArrayList<File> directoryCrawlList = new ArrayList<>();      // List of all the directory
    static ArrayList<File> internalDirCrawList = new ArrayList<>();     // List of all internal directory
    static ArrayList<File> externalDirCrawlList = new ArrayList<>();    // List of all external directory
    static ArrayList<File> textFileCrawlList = new ArrayList<>();       // List of all text file
    static ArrayList<File> binaryFileCrawlList = new ArrayList<>();     // List of all binary file
    static ArrayList<File> errorCrawList = new ArrayList<>();           // List of all the error code

    public static void crawl() throws IOException {
        System.out.println("--".repeat(5) + " Started Crawling " + "--".repeat(5));
        // Opening a new socket connection
        connectSocket();
        // Starts with an empty directory with a empty request to get the initial directory list
        Directory directoryNavigation = new Directory();

        // Getting the initial default response from the Gopher Server
        outputStream.write(directoryNavigation + "\r\n");
        outputStream.flush();

        // Parsing the message from the server at the root/initial response
        parseResponse(inputStream, directoryNavigation);
        // Adding the root path to the visitedDirectories List
        visitedPath.add(new File(ContentType.DIRECTORY, "root",
                                    directoryNavigation.getCurrentPath(), address, port));
        // Recurse through the sub-directory
        crawlHelper(directoryNavigation);

        // Removing all the duplicate directory by using HashSet
        directoryCrawlList = new ArrayList<>(new HashSet<>(directoryCrawlList));

        // Separating the internal and external directory list
        for (File file : directoryCrawlList) {
            if (!file.getHost().equals(address)) {
                externalDirCrawlList.add(file);
            } else {
                internalDirCrawList.add(file);
            }
        }

        // Providing the report for each of the resources in the Gopher Server
        crawlerReport("Summary of the Unique Path", "Total Unique Path", visitedPath, "unique-path.txt");
        crawlerReport("Summary of all the Directory (Internal and External)",
                        "Total Internal and External Directory Count", directoryCrawlList,
                        "internal-external-directory.txt");
        crawlerReport("Summary of all the Internal Directory", "Total Internal Directory Count",
                        internalDirCrawList, "internal-directory.txt");
        crawlerReport("Summary of all the External Directory", "Total External Directory Count",
                        externalDirCrawlList, "external-directory.txt");
        crawlerReport("Summary of all the Text File", "Total Text File Count", textFileCrawlList, "text-file.txt");
        crawlerReport("Summary of all the Binary File", "Total Binary File Count", binaryFileCrawlList, "binary-file.txt");
        crawlerReport("Summary of all the Error Message", "Total Error Message", errorCrawList, "error-mess.txt");
        crawlFileReport("Summary of the Smallest and Largest Text File", "Text File", textFileCrawlList);
        crawlFileReport("Summary of the Smallest and Largest Binary File", "Binary File", binaryFileCrawlList);

        // Closing the connection of the so
        socket.close();
    }

    /**
     * This is a helper method for the crawler that is used to recurse through all the sub directory in the server
     * @param directory The current directory to crawl
     * @throws IOException
     */
    public static void crawlHelper(Directory directory) throws IOException {
        // Download all the text file in the current directory path and update the file size
        for (File textFile : directory.getTextFileList()) {
            downloadFile(textFile);
            textFileCrawlList.add(textFile);
        }

        // Download all the binary file in the current directory path and update the file size
        for (File binaryFile : directory.getBinaryFileList()) {
            downloadFile(binaryFile);
            binaryFileCrawlList.add(binaryFile);
        }

        // Adding all the directory from the current path
        directoryCrawlList.addAll(directory.getDirectoryList());

        // Adding all the error message present in the current path
        errorCrawList.addAll(directory.getErrorList());

        // Transversing through all the sub-directory
        for (File dir : directory.getDirectoryList()) {
            if (!(dir.compareFilePath(visitedPath))) {
                System.out.println("Visiting Path: " + dir.getPath());

                // Opening a new socket connection
                connectSocket();

                // Adding the current directory in the visitedDirectories List
                visitedPath.add(dir);
                Directory subDirectory = new Directory(dir.getPath());

                // Request the content of the new directory
                outputStream.write(subDirectory.getCurrentPath() + "\r\n");
                outputStream.flush();

                // To ensure that there is no remaining data from the previous server's response
                while (inputStream.ready()) {
                    inputStream.readLine();
                }
                // Parsing the response from the server
                parseResponse(inputStream, subDirectory);
                // Recurse through the sub-directory
                crawlHelper(subDirectory);
            }
        }
    }

    /**
     * Provide a summarization for a resources in the server
     * @param title The title of the Summary
     * @param countName The variable name of the resources (Resource object)
     * @param data An arraylist of the data
     * @param outputPath The output path for the text file of the report
     */
    public static void crawlerReport(String title, String countName, ArrayList<File> data, String outputPath) {
        String directory = "crawl-report";
        try {
            Files.createDirectories(Paths.get(directory));
        } catch (IOException e) {
            System.out.println("Error creating directory: " + e.getMessage());
            return;
        }

        String outputFilePath = Paths.get(directory, outputPath).toString();
        try (PrintWriter fileWriter = new PrintWriter(Files.newBufferedWriter(Paths.get(outputFilePath)))) {
            printToBoth(fileWriter, "-".repeat(3) + " " + title + " " + "-".repeat(3));
            int count = 1;
            for (File file : data) {
                if (file.getFileType() == ContentType.TEXT_FILE ||
                        file.getFileType() == ContentType.BINARY_FILE) {
                    printToBoth(fileWriter, count + "-> Name: \"" + file.getName() +
                            "\", Path: " + file.getPath() +
                            ", Size: " + file.getSize() + " Bytes");
                } else if (file.getFileType() == ContentType.ERROR_MESSAGE) {
                    printToBoth(fileWriter, count + "-> Name: \"" + file.getName() +
                            "\", From Path: " + file.getPath());
                } else {
                    printToBoth(fileWriter, count + "-> Name: \"" + file.getName() + "\", Path: " + file.getPath() +
                            ", Address: " + file.getHost() + ", Port: " + file.getPort());
                }
                count++;
            }
            printToBoth(fileWriter, "-".repeat(8 + title.length()));
            printToBoth(fileWriter, countName + ": " + data.size());
            printToBoth(fileWriter, "-".repeat(8 + title.length()));
            printToBoth(fileWriter, "-".repeat(8 + title.length()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Provide a summarization for a file or binary resource in the server (Analysing the content and file size)
     * @param title The title of the Summary
     * @param countName The variable name of the resources (Resource object)
     * @param data An arraylist of the data
     */
    public static void crawlFileReport(String title, String countName, ArrayList<File> data) {
        // Find the smallest and largest files using the max function and a comparator
        File smallest = data.stream()
                .min(Comparator.comparingInt(File::getSize))
                .orElse(null);

        File largest = data.stream()
                .max(Comparator.comparingInt(File::getSize))
                .orElse(null);

        // Parsing the file name for the report
        String fileName = countName.toLowerCase().replace(" ", "-");

        // Creating a fancy summary for the file report
        System.out.println("-".repeat(3) + " " + title + " " + "-".repeat(3));
        System.out.println("The Smallest " + countName + ": " + smallest.filePrint());
        System.out.println("The Smallest " + countName + " file size: " + smallest.getSize() + " Bytes");
        System.out.println("The Largest " + countName + ": " + largest.filePrint());
        System.out.println("The Largest " + countName + " file size: " + largest.getSize() + " Bytes");
        System.out.println("-".repeat(8 + title.length()));
        System.out.println("The content of the file are not displayed to the terminal output due to the large text.");
        System.out.println("Instead, the content can be found on the " + fileName + "-content-report.txt under the crawl-report folder." );
        System.out.println("-".repeat(8 + title.length()));
        System.out.println("-".repeat(8 + title.length()));

        // Writing the content of the file to an external file
        printFileContent(smallest, largest, title, fileName + "-content-report.txt", countName);
    }

    /**
     * A method to both print to the terminal and write to an output file
     * @param fileWriter File writer for the output file
     * @param message The content to be written
     */
    private static void printToBoth(PrintWriter fileWriter, String message) {
        System.out.println(message);
        fileWriter.println(message);
    }

    /**
     * A method to print the content of a binary or text file into a report
     * (A helper method for the crawlFileReport)
     * @param smallestFile The smallest file for the type
     * @param largestFile The largest file for the type
     * @param title The title of the report
     * @param outputFileName The output file name
     * @param reportObject The variable name of the resources (Resource object)
     */
    private static void printFileContent(File smallestFile, File largestFile, String title, String outputFileName, String reportObject) {
        // Parse and load the smallest file name
        String smallestFileName = smallestFile.getPath().substring(1).replace("/", "-");
        smallestFileName = truncateFilename(smallestFileName, 255);
        java.io.File smallestLocalFile = new java.io.File("./crawl-download", smallestFileName);

        // Parse and load the largest file name
        String largestFileName = largestFile.getPath().substring(1).replace("/", "-");
        largestFileName = truncateFilename(largestFileName, 255);
        java.io.File largestLocalFile = new java.io.File("./crawl-download", largestFileName);

        try {
            // Create the "crawl-report" directory if it doesn't exist
            Files.createDirectories(Paths.get("crawl-report"));

            // Save the output file in the "crawl-report" folder
            Path outputFile = Paths.get("crawl-report", outputFileName);

            // A variable containing -
            String lineBreaker = "-".repeat(8 + title.length()) + "\n";

            // Reading the content of the smallest and largest file
            String smallestContent = new String(Files.readAllBytes(Paths.get(smallestLocalFile.toURI()))).replace("\r", "");
            String largestContent = new String(Files.readAllBytes(Paths.get(largestLocalFile.toURI()))).replace("\r", "");

            // Use try-with-resources to ensure the PrintWriter is closed
            try (PrintWriter fileWriter = new PrintWriter(Files.newBufferedWriter(outputFile))) {
                // Writing a fancy report to the file
                fileWriter.write("-".repeat(3) + " " + title + " " + "-".repeat(3) + "\n");
                fileWriter.write("Smallest " + reportObject + ": " + smallestFile.getPath() + "\n");
                fileWriter.write("Content: " + smallestContent + "\n");
                fileWriter.write(lineBreaker);
                fileWriter.write("Largest " + reportObject + ": " + largestFile.getPath() + "\n");
                fileWriter.write("Content: " + largestContent + "\n");
                fileWriter.write(lineBreaker);
                fileWriter.write(lineBreaker);
            }
        } catch (IOException e) {
            System.out.println("Failed to read the content of the file: " + smallestFileName);
            e.printStackTrace();
        }
    }

    public static void downloadFile(File file) throws IOException {
        // Open a new socket connection
        connectSocket();

        // Request the content of the file
        outputStream.write(file.getPath() + "\r\n");
        outputStream.flush();

        // Create the crawl download directory if it doesn't exist
        java.io.File downloadDirectory = new java.io.File(downloadFolderName);
        if (!downloadDirectory.exists()) {
            downloadDirectory.mkdir();
        }

        // Parsing the file name of the downloaded file
        String fileName = file.getPath().substring(1).replace("/", "-");
        // Truncate the name of the file to a maximum of 255 if it is too long
        fileName = truncateFilename(fileName, 255);

        // Create a local file to save the content
        java.io.File localFile = new java.io.File(downloadDirectory, fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(localFile);
        InputStream socketInputStream = socket.getInputStream();


        // Read the content of the file from the Gopher server and save it to the local file
        byte[] buffer = new byte[4096];     // A default buffer size of 4096 bytes
        int bytesRead;      // Keeping track of the bytes read from the server
        while ((bytesRead = socketInputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead);
        }

        // Update the size attribute of the File object
        file.setSize((int) localFile.length());

        // Close the local file, reader and the socket connection
        socketInputStream.close();
        fileOutputStream.close();
        socket.close();
    }

    public static String truncateFilename(String fileName, int maxLength) {
        if (fileName.length() <= maxLength) {
            return fileName;
        }

        int extensionIndex = fileName.lastIndexOf(".");
        String nameWithoutExtension = fileName;
        String extension = "";

        if (extensionIndex != -1) {
            nameWithoutExtension = fileName.substring(0, extensionIndex);
            extension = fileName.substring(extensionIndex);
        }

        int nameLength = maxLength - extension.length();
        String truncatedName = nameWithoutExtension.substring(0, Math.min(nameWithoutExtension.length(), nameLength));
        return truncatedName + extension;
    }

    public static void connectSocket() {
        try {
            // Creating a new socket instance
            socket = new Socket(address, port);
            // Redefining the stream for the new socket
            outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Recording the current timestamp of the connection
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
            LocalDateTime timestamp = LocalDateTime.now();
            String formattedTimestamp = timestamp.format(formatter);
            // Providing a reference for the new socket
            System.out.println("[" + formattedTimestamp +"] client: created socket connected to local port " + socket.getLocalPort() +
                                " and to remote address " + socket.getInetAddress() +
                                " and port " + socket.getPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void parseResponse(BufferedReader inputStream, Directory directory) {
        String serverResponse;
        try {
            while ((serverResponse = inputStream.readLine()) != null) {
                if (serverResponse.equals(".")) {
                    break;
                }
                if (serverResponse.length() > 0) {
                    char firstChar = serverResponse.charAt(0);
                    if (firstChar == 'i') {
                        String[] fileContent = serverResponse.split("\t");
                        directory.getContent().add(fileContent[0].substring(1));
                        directory.getInformationList().add(fileContent[0].substring(1));
                    } else if (firstChar == '0') {
                        String[] fileContent = serverResponse.split("\t");
                        File textFile = new File(fileContent[0].substring(1),
                                            fileContent[1], fileContent[2],
                                            Integer.parseInt(fileContent[3]));
                        textFile.setFileType(ContentType.TEXT_FILE);
                        directory.getContent().add(textFile);
                        directory.getTextFileList().add(textFile);
                    } else if (firstChar == '1') {
                        String[] fileContent = serverResponse.split("\t");
                        File directoryFile = new File(fileContent[0].substring(1),
                                            fileContent[1], fileContent[2],
                                            Integer.parseInt(fileContent[3]));
                        directoryFile.setFileType(ContentType.DIRECTORY);
                        directory.getContent().add(directoryFile);
                        directory.getDirectoryList().add(directoryFile);
                    } else if (firstChar == '9') {
                        String[] fileContent = serverResponse.split("\t");
                        File binaryFile = new File(fileContent[0].substring(1),
                                            fileContent[1], fileContent[2],
                                            Integer.parseInt(fileContent[3]));
                        binaryFile.setFileType(ContentType.BINARY_FILE);
                        directory.getContent().add(binaryFile);
                        directory.getBinaryFileList().add(binaryFile);
                    } else if (firstChar == '3') {
                        String[] fileContent = serverResponse.split("\t");
                        File errorFile = new File(fileContent[0].substring(1));
                        errorFile.setFileType(ContentType.ERROR_MESSAGE);
                        errorFile.setPath(directory.getCurrentPath());
                        directory.getContent().add(errorFile);
                        directory.getErrorList().add(errorFile);
                    } else {
                        System.out.println("Code not taken care of: " + firstChar);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Server closed the connection, reconnecting...");
        }
    }

    public static void displayMessage(Directory directory) {
        for (Object content : directory.getContent()) {
            if (content instanceof String) {
                System.out.println(content);
            } else {
                File fileContent = (File) content;
                System.out.println(fileContent.prettyPrint());
            }
        }
    }
}
