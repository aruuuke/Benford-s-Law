# Benford-s-Law
This project implements Benford's Law in Java, providing a server-client architecture to analyze numerical data sets and verify their conformity to Benford's Law. 
Benford's Law, also known as the first-digit law, states that in many naturally occurring collections of numbers, the leading digit is likely to be small.

## Features
Benford's Law Analysis: Analyze numerical datasets to check their adherence to Benford's Law.
Server-Client Architecture: A server handles the computation, while clients send data for analysis.
Easy to Use: Simple Java classes to set up and run the analysis.
Files
AppliedBenfordsLaw.java: The main application class to run the Benford's Law analysis.
BenfordClient.java: The client class to send data to the server for analysis.
BenfordServer.java: The server class that receives data from clients and performs the analysis.
BenfordsLaw.java: The core logic for Benford's Law computations.
How to Run
Compile the Java files:

### sh
Copy code
javac *.java
Start the server:

### sh
Copy code
java BenfordServer
Run the client with your data:

### sh
Copy code
java BenfordClient <datafile>
