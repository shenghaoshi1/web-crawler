web crawler based on storm

Indexer
The indexer is based on hadoop map reduce, which take the docId in input.txt and fetch the raw document on S3 bucket. The output of mapreduce is (word, docId, tf,idf,titlecontain). The code is included in indexer.
instruction for running indexer 
first create a ~/.mrjob.config via terminal (see mrjob.config file)
run command like
python indexer.py -r emr input.txt --conf-path ~/.mrjob.conf --output-dir=s3://shaozilan555test-bucket/res

Web Interface & Search Engine
The web search & search Engine code is in web interface file. Copy the webinterface file, /www file and pom.xml  to Eclipse Che and run main class.
# web-crawler
