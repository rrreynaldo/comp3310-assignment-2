public class File {
    ContentType fileType;
    String name;
    String path;
    String host;
    int port;

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

    public String prettyPrint() {
        return switch (fileType) {
            case TEXT_FILE -> "(FILE)" + "\t" + name + "\t" + "[" + path + "]";
            case DIRECTORY -> "(DIR)" + "\t" + name + "\t" + "[" + path + "]";
            case ERROR_MESSAGE -> "(ERROR)" + "\t" + name;
            case BINARY_FILE -> "(BIN)" + "\t" + name + "\t" + "[" + path + "]";
            default -> name + "\t" + "[" + path + "]";
        };
    }

    @Override
    public String toString() {
        return "File{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof File)) {
            return false;
        } else {
            File file = (File) obj;
            return name.equals(file.getName());
        }
    }
}
