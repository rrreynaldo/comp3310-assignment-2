# COMP3310-Assignment-2
## Indexing a Gopher

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