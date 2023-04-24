import java.util.ArrayList;

public class Directory {
    Directory parentDirectory;
    String directoryName;
    Directory subDirectory;

    ArrayList<Object> content = new ArrayList<>();

    ArrayList<File> textFileList = new ArrayList<>();
    ArrayList<File> directoryList = new ArrayList<>();

    ArrayList<String> informationList = new ArrayList<>();

    public Directory() {
        this.directoryName = "";
    }
    public Directory(String directoryName) {
        this.directoryName = directoryName;
    }

    public Directory getParentDirectory() {
        return parentDirectory;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public Directory getSubDirectory() {
        return subDirectory;
    }

    public ArrayList<Object> getContent() {
        return content;
    }

    public ArrayList<File> getTextFileList() {
        return textFileList;
    }

    public ArrayList<File> getDirectoryList() {
        return directoryList;
    }

    public ArrayList<String> getInformationList() {
        return informationList;
    }

    public void setParentDirectory(Directory parentDirectory) {
        this.parentDirectory = parentDirectory;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public void setSubDirectory(Directory subDirectory) {
        this.subDirectory = subDirectory;
    }

    public void setContent(ArrayList<Object> content) {
        this.content = content;
    }

    public void setTextFileList(ArrayList<File> textFileList) {
        this.textFileList = textFileList;
    }

    public void setDirectoryList(ArrayList<File> directoryList) {
        this.directoryList = directoryList;
    }

    public void setInformationList(ArrayList<String> informationList) {
        this.informationList = informationList;
    }

    public boolean hasFilePath(String filePath) {
        for (File file : textFileList) {
            if (file.getPath().equals(filePath)) {
                return true;
            }
        }
        return false;
    }

    public File getFileFromPath(String filePath) {
        for (File file : textFileList) {
            if (file.getPath().equals(filePath)) {
                return file;
            }
        }
        return null;
    }

    private String buildFullPath() {
        if (parentDirectory == null) {
            return directoryName;
        }
        return parentDirectory.buildFullPath() + directoryName;
    }

    @Override
    public String toString() {
        if (subDirectory == null) {
            return buildFullPath();
        }
        return buildFullPath() + subDirectory;
    }

    public String getCurrentPath() {
        return directoryName;
    }
}
