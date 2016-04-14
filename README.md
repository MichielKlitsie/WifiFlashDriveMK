# Nedap WiFi-Flash-drive
## 1. Synopsis
Nedap WiFi-Flash-drive is an Java-based file transfer system.

The small and portable Flash-Drive emits WiFi and the datatransfer takes place without relying on a specific router pre-installed. This new way of sending data locally improves security and is "optimised" (still under development).

## 2. Installation
### 2.1 Getting started
For setting op up the local ad-hoc network, see the instructions written in the document "Module 2: Wi­Fi Flash Drive" section "Raspberry Pi login" written by [Matthijs Langenberg](https://nl.linkedin.com/in/matthijslangenberg).

### 2.2 Setting up a file transfer
* Open two terminal (cmd) windows, (server and client)
	* Server: 
		* go to the folder where `FileServer.java` and `FileServerUtils.java` are located  
		* `ssh pi@172.17.2.3` to connect with the server.
		* `java -jar StartServer.jar` to start accepting file transfers.
	* Client: 
		* `javac ...` to compile a client.
		* `java /ServerClient.java <-port number> <filename> `
		





## 3. API Reference


## 4. Code structure
Besides reading the source code you can also check documentation in [docs/](www.google.nl).

## 5. Deployment


## 6. Dependencies / Repositories
### Project building and deployment

### In-house dependencies

## 7. Tests

## 8. License
All rights reserved, copyright ‘Nedap N.V.’, written by [Michiel Klitsie](Profilehttps://nl.linkedin.com/in/michielklitsie).

Last updated: 4 april 2016
