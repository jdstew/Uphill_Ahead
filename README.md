# Uphill Ahead (Up’ahead) – an Android App

![Uphill Ahead application icon](app/uphill_ahead_icon_512.png)

## Project description

This is not another mapping tool for Pacific Crest Trail (PCT) hikers.  There are plenty good mapping tools already available that display your latitude and longitude.  Instead, this app is an aide to answer the most pressing questions for PCT hikers:

* How much and how steep is the elevation gain ahead?
* Where are my next sources of water?
* Where can I camp for the night?

Gauging the difficulty of elevation gain and loss over distances using raw numbers is like taking photos without stopping – it’s easy to trip and make a mistake.  Evaluating elevation contour lines on maps isn’t trivial and takes practice.

So, what’s wrong with the available products?  Primarily how elevation exaggeration zoom levels are inconsistent and unrealistic.  On printed maps, elevation profiles are often represented at a too small of scale to depict difficulty.  App-based maps often provide vertical and horizontal zoom in combination, which too easily allows elevation profiles to be displayed at unrealistic scales.

What makes this app different?  First, this app focuses the user on what is ahead of them; because, who cares about the trail already hiked?  Second, it encourages the user to maintain a fixed level of elevation exaggeration as they zoom in and out (along the trail ahead).  Third, it uses the battery draining GPS locating function as little as possible.  Lastly, it’s free.

## Privacy policy

Privacy policy for this app is as follows.

* This app does not handle financial or payment information or government identification numbers.
* This app does not handle non-public phonebook or contact information.
* This app does not contain anti-virus or security functionality.
* This app does not target children.
* This app does not collect or link persistent device identifiers.

## Features

Below are highlights of using this app.  See the settings section for more complete list of configurable settings.

* **One-handed operation**.  No pinching or rubber-banding with two fingers need with this app.  Easy fat-fingered taps and buttons were part of the design criteria.
* **Visual difficulty levels**.  Green-yellow-red bands appear to indicate the trail difficulty by slope and available oxygen.  Trail slope uses a modified version of [Tobler’s hiking function](https://en.wikipedia.org/wiki/Tobler%27s_hiking_function)  Slope and oxygen content were selected because they are consistent and “good enough”.  Yes, there are dozens of other variables in actual hiking that impact difficulty.
* **Time estimations**.  This app estimates time to a distance ahead using slope and oxygen level calculations (time = distance / speed (i.e., pace)). 

## Known limitations:

A best-effort approach was applied in creating this app – I use this app while on the trail!  However, below are some known limitations.

* Elevation data is less accurate than coordinate positions.  The USGS data is merged from numerous sources, some of which are less accurate for very remote areas. And don’t forget that the Sierra Nevada and Cascade ranges are rising about 2 millimeters every year (…written with tongue in cheek) ;^).
* This is my first Android App.  This app was created primarily to solve my own need and build professional skills.  It is being shared for those who could also benefit from both the code and the app.

## Settings

The following is a list of configurable options within the app.

* **Route** - all of the PCT sections and side trails included in the Halfmile project data are included with this app.
* **Direction** – typically north or southbound, or to and from the trailhead for side trails.  These values were not part of the original data set.  They were added later by hand with custom GPX elements.
* **GPS usage** – whether the GPS should be used.  Note: even though you can set the app to not use the GPS, it will ask your device where it knew it was last (i.e., a last-known-position LKP).
* **How often to get a position update** – the app will attempt to get a fresh position when the app starts if the above setting is turned on; and then will turn off the GPS for the interval set.
* **Snap-to-trail distance** – is a user-set level of tolerance to determine if you are on the trail.
* **Elevation exaggeration** – how much elevation changes are represented compared with horizontal distances.
* **Pace adjustment** – to accommodate faster and slower hikers (or other conditions and limitations).
* **Units** – imperial (aka English) and metric units of measurement.

## How to install and run the project

This app is available on the [Google Play App Store](https://play.google.com)   There is no intent to create a similar version for Apple iOS – I simply don’t have the bandwidth of time for this. 
  
## How to use the app

This app is primarily intended to be used offline, without needing mobile data.  Preferably you grant the app access to a device’s precise location services (i.e., GPS or equivalent).  All of the PCT trail data is provided within the app.   So, the first step after starting the app is to select a route and direction within the settings activity (i.e., screen).  Below is the briefest tutorial.

* **Tailor your settings**.  By clicking on the ‘gear’ icon, you can begin configuring the settings listed in the settings section above.
* **Fling**.  Zooming in and out is performed by finger swipes.  While you, the hiker, remain fixed to the left side of the display, swiping right zooms the display in and swiping left zooms the display out.  The display can also be framed up and down slightly.  Zooming in and out has limits, so you may not be able to view and entire section at once.
* **Tap**.  Tapping on an icon provides the details for that type of icon, while tapping elsewhere provides simpler distance, time, gain, and loss information.
* **Go-to**.  Once you tap on an icon, you can simulate being at that location by clicking on the ‘Jump-to’ button.  If you do, you will simulate being at that location (until your GPS refresh period elapses, if GPS is turned on).

## Credits

Without the Halfmile’s PCT GPS data being available from [PCTMAP.NET](https://pctmap.net/gps/) this project would have never begun.  Specifically, thank you David Lippke and Lon Cooper (et. al) for their efforts to gather, process, improve, and share data.

## Licensing

All of the code for this project is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

The application icon is copyrighted by Jeffrey D. Stewart.

PCT Notes are copyrighted by [Halfmile](https://pctmap.net/trail-notes/)

Trail coordinates were sourced by the [Pacific Crest Trail Association](https://www.pcta.org/discover-the-trail/maps/pct-data/)

Elevations were sourced from the [U.S. Geological Survey elevation point query service](https://apps.nationalmap.gov/epqs/)

## Data quality and testing

How is trail data used by this app?  

1. Trail coordinates were extracted from PCTA's ArcGIS Online query service as GeoJSON data.
2. Trail coordinates were annotated to indicate (a) section end points and (b) closest point to Halfmile PCT Notes
3. Trail segments were merged up to, but not exceeding 100 meters; however, cumulative distances were retained for each remaining point.  This resulted in approximately 60,000 points, which included all section end points and Halfmile PCT Notes
4. Elevation data and Halfmile PCT Notes were appended to remaining trail coordinates.

Note: cumulative distances and time estimations are calculated dynamically within the application.

## How to contribute to the project

I’m currently not soliciting assistance with this project.  That said, if I were to enhance this application, I would pursue opportunities in the following areas:

* The ability to add GPX files for non-PCT trails.
* Incorporation or replacement with more current and higher quality PCT trail data.
* Conversion of Java into Kotlin code.
* Separating graphics rendering into its own thread.

Enjoy!
