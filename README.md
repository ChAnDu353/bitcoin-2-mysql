# bitcoin-2-mysql
Export Bitcoin transactions to Mysql using bitcoinj

JAVA 8
Mysql 8

A Demo work to export data from bitcoin to mysql. High performance 2 minutes per one blk***.dat file.

# Required java libraries
You can download from <a href="https://mvnrepository.com/artifact/org.bitcoinj/bitcoinj-core/0.15.3"> Maven Repository</a>
Place them in BlockParser\library folder
*  	bitcoinj-core-0.15.3.jar 
* 	mysql-connector-java-8.0.16.jar
* 	bcprov-jdk15on-1.60.jar
* 	protobuf-java-3.6.1.jar
* 	guava-27.0.1-android.jar
* 	slf4j-api-1.7.25.jar
* 	slf4j-simple-1.7.26.jar
  

# How to use 
  To use you will need fully synced <a href="https://bitcoin.org/en/bitcoin-core/">bitcoin core</a> client. 
  Close bitcoin core while running this program. 
  Open db.properties 
  Fill database connection details and path to  blk***.dat files in bitcoin core folder. (blocks_path)
  open trid.txt file type 1 and close
  import the sql file
  num_files value in db.properties will process that many blk***.dat files in bitcoin core folder. Depending on your system and how long you can run continously, enter a numeric value.(500 or max 2000 to process all blk files in single run)


Download and extract the blockparser.jar to BlockParser folder and run loadtoDb.bat or 
```
  java -cp "blockparser.jar;library/*"   com.zanet.btcsql.BTCSqlParser
```
  Every subsequent run will start program from where it last left and continue until all files in bitcoin core folder are processed 
  After first run do not alter the value in trid.txt file.
 
Lastly, add keys to your tables for indexing. you can use provided alterkeys.sql file or make changes to tables as needed.
#### Performance
  Currently on a average it takes (max) 2 minutes to process one  blk***.dat file.

# Notice
This project is developed for educational purpose only and to test the performance.
There are less chances of any further development.

## Donations
IF this work helps you , feel free to share a cup of TEA
> 1GWHmyKM2TFDy1Tymgn8eskGfxDWVv1roX

# Acknowledgement
Thanks to <a href="https://bitcoinj.github.io/">bitcoinj</a>
