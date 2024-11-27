# Project

Course project for the
course [IDATA2304 Computer communication and network programming (2023)](https://www.ntnu.edu/studies/courses/IDATA2304/2023).

Project theme: a distributed smart greenhouse application, consisting of:

* Sensor-actuator nodes
* Control panel nodes

See protocol description in [protocol.md](protocol.md).

## Getting started

The projects contains several runnable classes, the order of which you run these is not important.
- `GreenhouseGuiStarter` - starts the greenhouse simulation
- `ControlPanelStarter` - starts a control panel, multiple instances can be run simultaneously (allow "Mutliple instances" in the run configuration)
- `GreenhouseServerStarter` - starts the greenhouse server
