Author              : Chuyun Sun, Youyi Shi, and Yong Jian Quek
Created             : Jan 29 2020
Last Modified       : Feb 24 2020
Affiliation         : Georgia Institute of Technology

Description
-----------
A Discrete Event Simulator (DES) simulating the TWIN elevator system in the CODA building. The system replicates the real system with the
same number of shafts, cabs, and floors. Outputs the throughput, average waiting time, and average waiting and travelling time of users
within the system. By default, the system run for 1 hour (3600 seconds), simulating peak lunch hour.

Installation
-------------
A fully compiled program is available in /simulator and there is no need to recompile the program as Java is platform independent.
The source code is still included in /src. To compile from source, run

    javac *.java -d ../simulation

Tested and ran on openjdk 11.0.6. Java version 8 or higher is required to run the program.

Execution
---------
To execute DES, please navigate to /simulator and run

    java Driver [ALGORITHM] [VERBOSITY]

[ALGORITHM] must be 1 to run Algorithm 1, a collision avoidance algorithm, or 2 to run Algorithm 2, a collision resolution algorithm.
[VERBOSITY] must be 0 to run silently, or 1 to run in verbose mode.
Parameters for this simulation has been hardcoded into Driver.java. Modify these parameters to change the testing model. Instructions for modifications are in the source code.

