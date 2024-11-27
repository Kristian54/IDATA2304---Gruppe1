# Communication protocol

[//]: # (TODO: Finish this document.)

This document describes the protocol used for communication between the different nodes of the
distributed application.

## Introduction

This document describes the communication protocol used in our solution for a functioning greenhouse 
containing sensors, actuators and control panels. The greenhouse is controlled and monitored by one or more control panels. 
The sensors and actuators are connected to a node that is responsible for handling sensor data and actuator state updates.

## Terminology

| Term | Description                                                                                                                                                                            |
|------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Sensor | A device which senses the environment and describes it with a value (a double value in the context of this project). Examples: temperature sensor, humidity sensor.                    |
| Actuator | A device which can influence the environment. Examples: a fan, a window opener/closer,door opener/closer, heater.                                                                      |
| Sensor and actuator node | A collection of actuators and sensors which is connected to the server via TCP socket. |
| Control-panel node | A device connected to the Internet which visualizes status of sensor and actuator nodes and sends control commands to them.                                                            |
| Graphical User Interface (GUI) | A graphical interface where users of the system can interact with it.                                                                                                                  |
| TCP | Transmission Control Protocol.                                                                                                                                                         |

## The underlying transport protocol

In this project we use Transmission Control Protocol (TCP) as the underlying transport protocol. We 
have chosen to use TCP in order to have reliable communication between the different elements of our 
solution. TCP ensures that data packets is received by the receiving unit. This is done by 
establishing a connection with a three-way handshake, which ensures that the sender and receiver 
have a reliable connection. The second stage of the protocol is the data transfer, which may happen 
in both directions between the sender and receiver. The receiver will send an acknowledgement after 
it has received data. If the sender doesn't receive an acknowledgement, it will resend the message. 
After all the data is sent, the connection will be terminated. This will also be done with a 
three-way handshake [[1](#Sources)].

To establish connection we have used port number 10020. There is no specific reason we have chosen 
this port number, but we have made sure to consistently use the same port number. Since the port 
number has to be a 16-bit integer, it can be any number between 0 and 65 535 [[1](#Sources)].

## The architecture

Our solution consists of a server and multiple sensor/actuator nodes and control panel nodes. The server is responsible
for allowing the nodes to communicate and acts like a central hub for the communication. The nodes are individually
connected to the server via TCP, where they will send and receive messages. The server will direct messages to the
correct nodes based on the information given.

`Sensor/Actuator Node` <--> `Server` <--> `Control Panel Node`

* `Server` - One server that acts as a middleman for all communication.
* `Sensor/Actuator Node` - One or multiple nodes that are responsible for handling sensor data and actuator state updates.
* `Control Panel Node` - One or multiple nodes that are responsible for visualizing the status of sensor and actuator nodes and sending control commands to them.

## The flow of information and events

The general flow of information in our application is for the nodes to feed (push) information to the server, where it
will be interpreted and directed to the correct nodes. However, in some cases the nodes will request information on
creation. If the nodes lose connection to the server, or the server becomes unreachable the nodes will periodically
attempt reconnection until successful, before resuming normal operation.

#### Sensor/Actuator Nodes
The sensor/actuator nodes are observer based, meaning they will push information about sensor or actuator updates
automatically to the server when the data or actuator states change. While running, they also constantly listen for 
incoming messages from the server, which will be handled differently based on the information given. On creation, they
will push information about their node type and id to the server, so it can direct messages to the correct node and
notify the control panel nodes to add the new node to the GUI.

#### Control Panel Nodes
The control panel nodes constantly listen for incoming messages, for example containing sensor data updates to keep the
GUI up to date. The control panel nodes are also observer based, meaning they will push information about actuator 
updates to the server when the user interacts with the GUI. On creation, they will request information about all nodes, 
so they can display the current state of the greenhouse.

#### Server
The server is the central unit of the application, acting as a middleman for all communication. It is constantly
listening to incoming commands from the connected nodes, which will be directed to other nodes accordingly. The server
also allow for broadcasting messages to all control panel nodes, or all sensor/actuator nodes.

## Connection and state

Since we are using TCP, the communication in this project is connection-oriented. This is described 
in more detail in the chapter about [the underlying transport protocol](#The-underlying-transport-protocol).
This is also a stateful protocol, since it keeps track of the data that is transmitted and if it is 
transmitted or not, so it is able to retransmit data if an error occurs [[2](#Sources)].

## Types, constants

#### Node Types
Node types are represented by ENUMs in the code. These are used by the server to differentiate between the node types
for broadcasting and directing messages. The different node types are:
- CONTROLPANEL
- SENSORACTUATOR
- UNDEFINED
  - Default value if not set.

#### Command Types
- setNodeType
  - Used to set the node type on connection. Node type will be used by the server to broadcast messages.
   Default value: Undefined
- setId
  - Used to set the id of the connected node. Used by the server to determine where to direct further commands. Default
  value: 0
- updateSensorData
  - Used to update the data of all sensors connected to the current node. Will be broadcast to all control panel nodes.
- nodeAdded
  - Used to notify the server that a new node has been initiated. Will be broadcast to all control panel nodes.
- controlPanelAdded
  - Used to notify the server that a new control panel node has been initiated. Will be broadcast to all sensor/actuator
  nodes.
- actuatorUpdated
  - Used to notify all control panels that an actuator has changed state.
- controlPanelUpdateActuator
  - Used to notify a specific sensor/actuator node that a single actuator has been updated by a control panel node.
- nodeRemoved
  - Used to notify all control panels that a node has been removed.
- checkConnection
  - Can be used as a "heartbeat" to check if the connection is active.

#### Sensor Types
- Temperature
  - Simulated temperature sensor, generating periodic values. Values are affected by the different actuators.
- Humidity
  - Simulated humidity sensor, generating periodic values. Values are affected by the different actuators.

#### Actuator Types
- Fan
  - Can be turned on or off. Will affect the humidity.
- Window
  - Can be opened or closed. Will affect the temperature. 
- Heater
  - Can be turned on or off. Will affect the temperature.

#### Units
- Temperature: Celsius (°C)
- Humidity: Percentage (%)

## Message format

The messages are sent as Strings where each line represents a single command. Our messages are on the general format:
"command-nodeID;arguments". Command represents how the server and possibly nodes will execute the following information.
The nodeID represents the ID of the node that the command was sent from, or will be directed to. The arguments are
different for each command, but follow similar rules. Some example commands are:
- controlPaneUpdateActuator-5;41=false
  - This command is sent from a control panel, directed at node 5, telling node 5 to turn off actuator with id 41.
- actuatorUpdated-5;41=true
  - This is a response from the previous command and will be broadcast to all control panel nodes, telling them that
    actuator 41 on node 5 is now turned on.
- updateSensorData-2;Temperature=27.41 °C,Humidity=80.33 %,Humidity=78.6 %
  - This command is broadcast to all control panel nodes, telling them the sensor values of node 2.
- controlPanelAdded
  - This command is broadcast to all nodes, telling them to start to feed information to the new control panel node.

### Error messages

Error messages are sent by the server as a response to a command it could not interpret. The error message will be sent
to the node that sent the command. The error message is as follows:
- unknownCommandError

## An example scenario

1. The server is started and is ready to accept incoming clients.
2. The greenhouse simulation is started, and 2 sensor/actuator nodes are started and connect to the server individually.
3. The nodes attempt to connect to the server until successful.
3. The server receives the connections and processes them on separate threads.
4. When the nodes are connected, they send a message to the server with their node type and ID.
5. The server receives the commands and assigns each client the given ID and node type.
6. A control panel node is started and attempts to connect to the server.
7. The server receives the connection and processes it on a separate thread.
8. The control panel node sends a message to the server with its node type and ID.
9. The server receives the command and assigns the control panel node the given ID and node type.
10. The control panel node sends a message to the server requesting information about all nodes.
11. The server receives the command and sends a message to all sensor/actuator requesting information about them.
12. The sensor/actuator nodes receive the command and send a message to the server with their sensor data.
13. The server receives the sensor data and sends it to the control panel node.
14. An outage happens, and the server is unreachable.
15. All network nodes attempt reconnecting to the server until successful, before resuming normal operation.

## Reliability and security

#### Reliability
For reliability, we have implemented outage protection for the network nodes. If the server becomes unreachable,
unavailable or offline the network nodes will attempt to reconnect to the server until successful. After reconnecting,
the nodes will resume normal operation. This ensures that the application will continue to function if a power or
network outage happens. 

#### Encryption
TODO

[//]: # (TODO: Finish.)

## Sources

[1] B.A. Forouzan, *Data Communication & Networking with TCP/IP Protocol Suite*, 6th ed. New York: McGraw Hill LLC, 2022.

[2] S. Datta. (2024, Mar. 18). *Networking: Stateless and Stateful Protocols* [Online]. Available: https://www.baeldung.com/cs/networking-stateless-stateful-protocols 