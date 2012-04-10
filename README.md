# Serval Maps - Version 0.2 #

This is version 0.2 of the Serval Maps application. The previous prototype version was undertaken as part of the requirements for this [honours thesis](http://bytechxplorer.com/studies/honours-thesis/).

The purpose of the application is to provide a collaborative mapping application which is infrastructure independent. It uses the resilient AdHoc mesh network provided by the [Serval Project](http://www.servalproject.org/) to transfer information between instances of the application. 

Map data is sourced from the [OpenStreetMap](http://www.openstreetmap.org/) and managed using the [mapsforge](http://code.google.com/p/mapsforge/) library. 

As development progresses more information will be made available on the [Thoughts by Techxplorer blog](http://techxplorer.com) and on the [Serval Project Wiki](http://developer.servalproject.org/dokuwiki/doku.php?id=content:servalmaps:main_page).

## Change Log ##

### Version 0.2 ###

Version 0.2 is a complete rewrite from version 0.1. New features and functionality include:

* Using the Rhizome service provided by the Serval Software to share location and point of interest information across the network
* A new binary format using [Google Protocol Buffers](http://code.google.com/apis/protocolbuffers/) for storing and sharing information across the network
* An improved Content Provider for sharing information with other applications
* User interface for listing information stored in points of interest in alphabetical order
* Innumerable other changes and tweaks

### Version 0.1 ###

No public release, development undertaken to satisfy the requirements for the honours thesis.

This is version 0.2 of the Serval Maps application. Version 0.1 was constructed as part of this honours thesis. 

This is the development branch for version 0.2 of the application. This version will take advantage of the MeshMS functionality provided by the main Serval Project application. 

It was decided that a complete rewrite, using the lessons learned from the development of the prototype version, was easier than retrofitting the existing prototype. 
