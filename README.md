# README
__This repository contains a REST webservice which interacts with neo4j Database.__
So, briefly, this is the content:
- src folder contains the source code in Java.
- lib folder contains all necessary jars for this project.
- schema folder contains xsd schema of the xml elements used in this service.
- the pom.xml file is useful for maven dependencies in eclipse project.
- documentation folder contains a report which describe more accurately the webservice.
- testcases folder contains all xml files used for testing.
- there are five Ant files:
	- build-nffg, useful for building and deploying the webservice.
	- test, useful for performing tests.
	- whereas service-build, tomcat-build, generate-classes are used indirectly by build-nffg Ant file.
- finally server.properties contains useful properties used by the server, in particular contains the relative path of database in Catalina home directory.

__How to deploy and test Neo4jManager on Apache Tomcat:__
- install Apache Tomcat.
- clone this repository and import it in eclipse.
- change the tomcat credentials inside tomcat-build Ant file (so the server location, username and password).
- now, launch these targets on build-nffg Ant file:
	- start-tomcat
	- deployWS
- finally, for test the web service launch runFuncTest of test Ant file.
