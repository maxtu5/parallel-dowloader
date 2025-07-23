Program usage:
1. build jar
2. provide batch config file with relevant data
3. run java -jar out/artifacts/parallel-downloader.jar <path>

This is a very simple prototype, multiple functional and non-functional requirements to be implemented for production.
1. Now works only with simple URLs, like "https://getsamplefiles.com/download/zip/sample-5.zip". Links with redirects, etc, to be added.
2. Config json file has to be validated for relevant values in all fields.
3. I used buffer to control download duration and generate timeout exception. Need to think how to implement timeout using channels and .transferFrom() method. This didn't work at once, so I switched to buffered version.
4. I simply count files in the target folder, relying on the fact it's empty. Need smarter way to count.
5. Multiple additional functionalities are available. This includes downloading large files in parallel in chunks in case if we have free executors. Or folder management like create folder, clear folder, ask on file rewrite, etc.
6. Time measurement could be implemented as aspect 
