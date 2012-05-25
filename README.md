# Serval Maps - Version 0.3 #

This is version 0.3 of the Serval Maps application. 

The purpose of the application is to provide a collaborative mapping application which is infrastructure independent. It uses the resilient AdHoc mesh network provided by the [Serval Project](http://www.servalproject.org/) to transfer information between instances of the application. 

Map data is sourced from the [OpenStreetMap](http://www.openstreetmap.org/) and managed using the [mapsforge](http://code.google.com/p/mapsforge/) library. 

As development progresses more information will be made available on the [Thoughts by Techxplorer blog](http://techxplorer.com) and on the [Serval Project Wiki](http://developer.servalproject.org/dokuwiki/doku.php?id=content:servalmaps:main_page).

## Change Log ##

### Version 0.3 ###

Version 0.3 builds on the previous version and adds a number of new areas of functionality. Including the implementation of feature requests as a result of feedback from a field trial of the software during the KiwiEx exercise conducted in February 2012 in New Zealand. 

New functionality includes:

* Improvements and bug fixes to the efficiency of importing of new location and Point of Interest (POI) information
* Distance between the user and other peers / points of interest is calculated and displayed
* The user can display their own GPS trace on the map
* Update to version 0.3.0 of the [mapsforge](http://code.google.com/p/mapsforge/) library
* The list of POIs can be sorted by time and alphabetically by title
* A picture can now be associated with a POI
* Location and POI data can be exported in the native binary format and in plain CSV format
* Statistics on the use of Serval Maps is collected and can be sent anonymously to the developers if the user chooses
* Redesign of the UI to improve the User Experience and bring it inline with the overall Serval Project style
* A variety of other minor bug fixes and tweaks

### Version 0.2 ###

Version 0.2 is a complete rewrite from version 0.1. New features and functionality include:

* Using the Rhizome service provided by the Serval Software to share location and point of interest information across the network
* A new binary format using [Google Protocol Buffers](http://code.google.com/apis/protocolbuffers/) for storing and sharing information across the network
* An improved Content Provider for sharing information with other applications
* User interface for listing information stored in points of interest in alphabetical order
* Innumerable other changes and tweaks

It was decided that a complete rewrite, using the lessons learned from the development of the prototype version, was easier than retrofitting the existing prototype. 

### Version 0.1 ###

No public release, development undertaken as part of the requirements for this [honours thesis](http://bytechxplorer.com/studies/honours-thesis/).