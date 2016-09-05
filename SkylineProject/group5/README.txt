############## README ############
Project Title :: K-dominant skyline in distributed system
Project by : Ritika Garg(15111036) and Swapnil Mhamane(15111044)
Project group id : 5

###### Execution Help #####
#Prerequisites ::
## For each server in cluster, you will have to list out the siteDetails i.e ipaddress and port.
This file will be accessible to all servers. This will give initial details about cluster present.
## Prepare sample data file :
	Program is hard coded to following assumption over input data file data file will have format matching pattern initailized by tuple id and then space separated numerical 
	attribute value.
	
## Query file :: 
	query file will have three lines 
	1. intger value k
	2. Set of dimension to indexes to choose for query
	3. P parallasim bound 

#Execution :
For each server either you can run java process with main class view.SiteUI or provided binary KSkyFinder.jar 
Arguments to class in order are 
1. Current site IP address 
2. Port assigned
3. Cluster config file (eample file available in src\config directory)
4. Input_data file : sample input is avaible  

So for example :
java - jar KSkyFinder.jar 192.168.255.1 7000 ..\config\ClusterDetails.txt data_cor.txt

After executing process, program will start a server at given port to listen for request from other sites.
Once process is started, it will prompt for query file path.
But please don't fire query untill all sites are ready.


###Logging ::
We have used log4j java libarary for logging message.
so one you fire query all messages will be logged at file KDOMFinder.log

###Dependencies
Directory : /lib
We have depency on log4j on jackson libaray
We are using jackson library to efficiently ransfer data on network in json format.

### supoorting batch scripts####
Directory : /script
#dataSplitter.txt
	provided a dat file, it will split file into multiple tuples eenly with each of provided size. File name is enumerated name of data file.
######Cluster file generator ###
Another helping script to auto genrate clusterdetails file entry for multiple simulating process on single machine
arg0 : clusterfilename
arg1 : ipaddress
arg2 : count of porcess


>ClusterDetailsGenerator.bat ..\config\ClusterB412.txt 172.24.132.57 20

#####ProcessGenerator.bat
Spwans the multiple process with different cmd 
arg0 : <sample_dat> ###note don't give suffix
arg1 : clusterFilePath
arg2 : count of cluster
arg3 : ip address

>processGenerator.bat sample_cor ..\config\ClusterB412.txt 20 172.24.132.57