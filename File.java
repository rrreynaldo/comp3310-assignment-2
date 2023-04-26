import java.util.ArrayList;
import java.util.Objects;

public class File {
    ContentType fileType;
    String name;
    String path;
    String host;
    int port;
    int size;

    public File() {
    }

    public File(String name) {
        this.name = name;
    }

    public File(String name, String path, String host, int port) {
        this.name = name;
        this.path = path;
        this.host = host;
        this.port = port;
    }

    public File(ContentType fileType, String name, String path, String host, int port) {
        this.fileType = fileType;
        this.name = name;
        this.path = path;
        this.host = host;
        this.port = port;
    }

    public ContentType getFileType() {
        return fileType;
    }

    public void setFileType(ContentType fileType) {
        this.fileType = fileType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String prettyPrint() {
        return switch (fileType) {
            case TEXT_FILE -> "(FILE)" + "\t" + name + "\t" + "[" + path + "]";
            case DIRECTORY -> "(DIR)" + "\t" + name + "\t" + "[" + path + "]";
            case ERROR_MESSAGE -> "(ERROR)" + "\t" + name;
            case BINARY_FILE -> "(BIN)" + "\t" + name + "\t" + "[" + path + "]";
            default -> name + "\t" + "[" + path + "]";
        };
    }

    public boolean compareFilePath(ArrayList<File> fileList) {
        for (File file : fileList) {
            if (file.getPath().equals(this.getPath())) {
                return true;
            }
        }
        return false;
    }

    public String filePrint() {
        return "Name: \"" + name + "\", Path: " + path + ", Size: " + size;
    }

    @Override
    public String toString() {
        return "File{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path, host, port);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        File file = (File) obj;
        return Objects.equals(name, file.name) &&
                Objects.equals(path, file.path) &&
                Objects.equals(host, file.host) &&
                port == file.port;
    }
}
