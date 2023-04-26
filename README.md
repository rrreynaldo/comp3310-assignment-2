# COMP3310-Assignment-2: Indexing a Gopher
## Introduction
This is a simple Java-based Gopher crawler. 
The main purpose of this program is to crawl Gopher servers and fetch resources using the Gopher protocol. 
The primary source code file is ClientGopher.java.

## Requirements
Java Development Kit (JDK) 8 or above. It is recommended to compile the source code with JDK 17 or 18.

## Compilation
To compile the program, navigate to the directory containing the ClientGopher.java file and run the following command:

`javac ClientGopher.java`

## Running the Crawler
To run the crawler, use the following command:

`java ClientGopher.java [address] [port] crawl`

Replace [address] and [port] with the target Gopher server's address and port number.

### Example

`java ClientGopher.java comp3310.ddns.net 70 crawl`

## Using the Crawling Script
Alternatively, you can run the crawler using the provided crawl-script.sh script. 
This script is preconfigured to crawl the Gopher server at comp3310.ddns.net on port 70.

To run the script, execute the following command:

`./crawl-script.sh`

You may need to grant execute permission to the script before running it:

`chmod +x crawl-script.sh`

Run the command only in the case that the code requires further permission.

## Code Algorithm to Crawl the Server
The codes implement a depth first search algorithm which transverse down to every directory in the server.

While transversing each directory, it kept a list of the unique path for a directory in the server. 
This allows the algorithm prevent loops of visiting the same directory over and over again.
Similarly, it will also download any downloadable files that it encounter while transversing the directory.

## Files Downloaded
The files are downloaded into the "crawl-download" folder on the main code repository.
The names of the downloaded files follows the original path from the server.

For example, for a file with path "/rfc1436.txt", and a description/name of "RFC 1436 (describes the Gopher protocol)",
the crawler will download the file and named it "rfc1436.txt" inside the "crawl-download" folder.

In the case that the file are located in multiple directory such as /acme/about, then the file naming would replace every "/"
with "-".

For example, a file with the path "/acme/about", would be downloaded with the name "acme-about".

File extension for the downloaded files follow the extension in the server.
For binary file, if there is no extension specification from the server, then the downloaded file would have 
a default ".bin"