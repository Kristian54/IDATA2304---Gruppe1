# Communication protocol

[//]: # (TODO: Finish this document.)

This document describes the protocol used for communication between the different nodes of the
distributed application.

## Introduction

(Remove) A short introduction of the contents of this document.

This document describes the commmunication protocol used in our solution to a functioning greenhouse 
containing sensors and actuators. The greenhouse is controlled by one or more control panels **(write more about 
the control panel when finished)**. The sensors and actuators are connected to a node that communicates with TCP to 
the server.

## Terminology

| Term | Description                                                                                                                                                            |
|------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Sensor | A device which senses the environment and describes it with a value (an integer value in the context of this project). Examples: temperature sensor, humidity sensor.|
| Actuator | A device which can influence the environment. Examples: a fan, a window opener/closer,door opener/closer, heater.                                                  |
| Sensor and actuator node | A computer which has direct access to a set of sensors, a set of actuators and is connected to the Internet.                                       |
| Control-panel node | A device connected to the Internet which visualizes status of sensor and actuator nodes and sends control commands to them.                              |
| Graphical User Interface (GUI) | A graphical interface where users of the system can interact with it.                                                                        |
| TCP | Transmission Control Protocol.                                                                                                                                          |

## The underlying transport protocol

TODO - what transport-layer protocol do you use? TCP? UDP? What port number(s)? Why did you 
choose this transport layer protocol?

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

TODO - show the general architecture of your network. Which part is a server? Who are clients? 
Do you have one or several servers? Perhaps include a picture here. 


## The flow of information and events

TODO - describe what each network node does and when. Some periodic events? Some reaction on 
incoming packets? Perhaps split into several subsections, where each subsection describes one 
node type (For example: one subsection for sensor/actuator nodes, one for control panel nodes).

## Connection and state

TODO - is your communication protocol connection-oriented or connection-less? Is it stateful or 
stateless? 

Since we are using TCP, the communication in this project is connection-oriented. This is described 
in more detail in the chapter about [the underlying transport protocol](#The-underlying-transport-protocol).
This is also a stateful protocol, since it keeps track of the data that is transmitted and if it is 
transmitted or not, so it is able to retransmit data if an error occurs [[2](#Sources)].

## Types, constants

TODO - Do you have some specific value types you use in several messages? Then you can describe 
them here.

## Message format

TODO - describe the general format of all messages. Then describe specific format for each 
message type in your protocol.

### Error messages

TODO - describe the possible error messages that nodes can send in your system.

## An example scenario

TODO - describe a typical scenario. How would it look like from communication perspective? When 
are connections established? Which packets are sent? How do nodes react on the packets? An 
example scenario could be as follows:
1. A sensor node with ID=1 is started. It has a temperature sensor, two humidity sensors. It can
   also open a window.
2. A sensor node with ID=2 is started. It has a single temperature sensor and can control two fans
   and a heater.
3. A control panel node is started.
4. Another control panel node is started.
5. A sensor node with ID=3 is started. It has a two temperature sensors and no actuators.
6. After 5 seconds all three sensor/actuator nodes broadcast their sensor data.
7. The user of the first-control panel presses on the button "ON" for the first fan of
   sensor/actuator node with ID=2.
8. The user of the second control-panel node presses on the button "turn off all actuators".

## Reliability and security

TODO - describe the reliability and security mechanisms your solution supports.

## Sources

[1] B.A. Forouzan, *Data Communication & Networking with TCP/IP Protocol Suite*, 6th ed. New York: McGraw Hill LLC, 2022.

[2] S. Datta. (2024, Mar. 18). *Networking: Stateless and Stateful Protocols* [Online]. Available: https://www.baeldung.com/cs/networking-stateless-stateful-protocols 