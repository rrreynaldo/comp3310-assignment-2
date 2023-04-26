# COMP3310-Assignment-2: Indexing a Gopher
## Introduction
This is a simple Java-based Gopher crawler. 
The main purpose of this program is to crawl Gopher servers and fetch resources using the Gopher protocol. 
The primary source code file is ClientGopher.java.
You can view the content and clone the repository from https://github.com/rrreynaldo/comp3310-assignment-2

## Requirements
- Java Development Kit (JDK) 8 or above (compiling with JDK 17 or 18 is recommended).

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

# Report
## Wireshark
The report for the Wireshark analysis are provided on the `wireshark-report.pdf`.
On the occasion that the image is not sufficiently clear/too small, the image are available on `wireshark-1.png` and `wireshark-2.png`

## Crawl Report
The Crawling analysis report are provided when the code is run. Since the content for the largest text file size is too large too be displayed
(it will overlap all the other information), the content are provided under the `text-file-content.txt` on the `crawl-report` folder.
The resulting text analysis are produced by the crawling code.

## What is in the report
The report contains several components:
- Unique Path - List all the unique selector path within the Gopher Server
- Directory - All the resources that are encoded with the code `1` based on RFC1436 definition
- Text File - All the text file resources that are encoded with the code `0` based on the RFC1436 definition
- Binary File - All the binary file resources that are encoded with the code `9` based on the RFC1436 definition
- Error Message - All the error message resources that are encoded with the code `3` based on the RFC1436 definition.

Similarly, the directory are divided into two category:
- Internal - Directories with the same address and port specified when running the crawling code
- External - Directories with different address and port compared to those specified when running the crawling code

Only internal directories are traversed; external directories are not crawled.

# Code Concept and Logic
## Code Algorithm to Crawl the Server
The codes implement a depth first search algorithm which transverse down to every directory in the server.

While transversing each directory, it kept a list of the unique path for a directory in the server.
This allows the algorithm prevent loops of visiting the same directory over and over again.
Similarly, it will also download any downloadable files that it encounter while transversing the directory, 
and keeps in record for all the resources including error message, text, binary, and directory information

## Why Depth First Search
The reason why a depth-first search (DFS) algorithm is used is because it enables efficient exploration of the server's 
directory structure by visiting all the subdirectories of a particular directory before backtracking. 
This allows for a comprehensive traversal of the entire directory hierarchy, 
ensuring that no directories or files are missed during the crawling process. 
Furthermore, DFS is memory-efficient compared to breadth-first search (BFS) since it doesn't require the storage of 
all nodes at a given level in the hierarchy.

DFS algorithms work by starting at the root directory and exploring as far as possible along each branch before backtracking. 
This allows the algorithm to dive deep into the directory structure and retrieve all the contents of each subdirectory, 
including files and other resources. By exhaustively visiting all subdirectories in a depth-first manner, 
the algorithm ensures that no part of the directory hierarchy is left unexplored.

## Files Downloaded
The files are downloaded into the `crawl-download` folder on the main code repository.
The names of the downloaded files follows the original path from the server.

For example, for a file with path `/rfc1436.txt`, and a description/name of `RFC 1436 (describes the Gopher protocol)`,
the crawler will download the file and named it `rfc1436.txt` inside the `crawl-download` folder.

If a file is located in multiple directories, such as `/acme/about`, the file naming replaces every `/` with `-`. 
For example, a file with the path `/acme/about` would be downloaded with the name `acme-about`.

File extension for the downloaded files follow the extension in the server.

If a file name exceeds 255 characters (the maximum file name length in Linux), it will be truncated to 255 with the extension preserved.

## Extension (Outside of Assignment Requirement)
In addition to being a Gopher Crawler, the code can support an interactive Gopher Client for users to connect to a server. 
To do this, run the command:

`java ClientGopher.java [address] [port]`

This will connect to the client and allow the user to navigate through the resources and directories by entering the 
path of the desired directory or file into the terminal.

For example, the client might display directory information as follows:

`(DIR)	ACME Rocket-Powered Products Pty. Ltd.	[/acme]`

The user can navigate to the directory by entering the path `/acme` in the prompted terminal.

Similarly, the user can download text or binary file by entering the path of that file.

For example, to download the content of the text file:

`(FILE)	RFC 1436 (describes the Gopher protocol)	[/rfc1436.txt]`

The user can enter the path `/rfc1436.txt` to the terminal.

In case the user wants to crawl inside the interactive Gopher Client, they can use the command crawl to perform a crawling method. 
To quit the Gopher client, the user can enter the command q.