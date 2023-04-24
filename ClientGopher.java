//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

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
        if (args.length != 2) {
            System.out.println("The usage of the command is ./ClientTCP {address} {port}");
            return;
        } else {
            address = args[0];
            port = Integer.parseInt(args[1]);
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

    static ArrayList<File> visitedPath = new ArrayList<>();
    static ArrayList<File> uniquePath = new ArrayList<>();
    static ArrayList<File> uniqueName = new ArrayList<>();
    static ArrayList<File> uniqueNameAndPath = new ArrayList<>();
    static ArrayList<File> textFileListTotal = new ArrayList<>();
    static ArrayList<File> binaryFileListTotal = new ArrayList<>();

    static ArrayList<String> uniqueCode = new ArrayList<>();

    public static void crawl() throws IOException {
        // Opening a new socket connection
        connectSocket();
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

        int count = 1;
        for (File file : visitedPath) {
            System.out.println(count + "-> Name: " + file.getName() + ", Path: " + file.getPath());
            count++;
        }

        System.out.println("total count: " + visitedPath.size());
        socket.close();
    }

    public static void crawlHelper(Directory directory) throws IOException {
        // Download all the text file in the current directory path
        for (File textFile : directory.getTextFileList()) {

            downloadFile(textFile);
        }

        // Download all the binary file in the current directory path
        for (File binaryFile : directory.getBinaryFileList()) {
            downloadFile(binaryFile);
        }

        // Transversing through all the sub-directory
        for (File dir : directory.getDirectoryList()) {
            if (!visitedPath.contains(dir)) {
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
        fileName = fileName.substring(0, Math.min(fileName.length(), 255));

        // Create a local file to save the content
        java.io.File localFile = new java.io.File(downloadDirectory, fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(localFile);
        InputStream socketInputStream = socket.getInputStream();


        // Read the content of the file from the Gopher server and save it to the local file
        byte[] buffer = new byte[4096];     // A default buffer size of 4096 bytes
        int bytesRead;      // Keeping track of the bytes read from the server
        while ((bytesRead = socketInputStream.read(buffer)) != -1) {
            System.out.println(bytesRead);
            fileOutputStream.write(buffer, 0, bytesRead);
        }

        // Close the local file, reader and the socket connection
        socketInputStream.close();
        fileOutputStream.close();
        socket.close();
    }

    public static void connectSocket() {
        try {
            // Creating a new socket instance
            socket = new Socket(address, port);
            // Redefining the stream for the new socket
            outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Providing a reference for the new socket
            System.out.println("client: created socket connected to local port " + socket.getLocalPort() +
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
                        directory.getContent().add(errorFile);
                        directory.getErrorList().add(errorFile);
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
