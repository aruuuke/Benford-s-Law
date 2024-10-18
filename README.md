### Benford's Law Analysis 

#### Overview
This project implements a robust system for detecting anomalies in datasets using Benford's Law, which is often utilized to identify fraudulent data patterns. The tool leverages a multi-threaded Java server-client architecture to efficiently handle multiple client requests simultaneously and perform data analysis.

#### Features
- **Server-Side Analysis:** Executes the Benford’s Law calculation to determine the natural occurrence of leading digits in the data. Utilizes multithreading to manage multiple analysis requests concurrently, improving throughput and response times.
- **Client-Server Communication:** Employs Java sockets to enable dynamic interaction between the client and server. Clients can send data files to the server, receive the analysis, and get results displayed in a graphical interface.
- **Graphical User Interface:** The client side features a GUI where users can upload their datasets, initiate analysis, and view the results in the form of a bar chart displaying the frequency distribution of leading digits alongside the expected distribution according to Benford's Law.
- **Fraud Detection:** Integrates a chi-squared test to compare observed digit frequencies with expected frequencies to assess the likelihood of data manipulation.

#### Technologies Used
- Java: Core programming language for building the server and client functionality.
- Swing: Utilized for creating the graphical user interface on the client side.
- Sockets: Facilitates communication between client and server for data transmission and result sharing.

#### How It Works
1. **Data Submission:** Users upload their dataset through the client interface.
2. **Processing:** The server receives the data and processes multiple requests concurrently using multithreading, computing the frequency of each leading digit, converting these frequencies to percentages, and performing a chi-squared test to detect anomalies.
3. **Result Presentation:** The analysis results are sent back to the client, where they are displayed visually. The system also indicates whether the analyzed data is likely to be fraudulent based on the chi-squared test.

