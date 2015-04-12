# jarvis
Elite Dangerous Trade Assistant 
Requires EliteOCR

Powered by OrientDb, Spring, and JavaFx

## Installation
* Install Elite:Dangerous .. or all of this will really be boring
* Install EliteOCR and learn how to use it. This app uses the csv exports. Mind what directory they get written to.

* Install Java 8 (JDK to do the compile). Its is the language this app uses.

* clone the git project from https://github.com/jrosocha/jarvisFx.git, you can just download the zip from github.
* cd to the cloned directory
* run gradlew or gradlew.bat (*nix or windows)

(At this point you can copy the build/install/jarvis folder somewhere else. It contains all the artifacts you require to run this app.

* cd ../bin (or build/install/jarvis/bin for those of you who are lost)

* run jarvis (or jarvis.bat)

## Example Usage

Set up your app via the settings tab. The 2 most important settings are the directory EliteOcr is writing your CSV files, and the Elite:Dangerous app directory. 
You'll also need verbose logging enabled in Elite:Dangerous for both EliteOCR and Jarvis.
![Jarvis Settings](jarvis-settings.png "Jarvis Settings")

Critical to Jarvis doing anything useful is EliteOCR. You'll need to import station you visit so that Jarvis has data to work with. Share them with your friends.
![Elite Ocr Import](jarvis-import-from-eliteocr.png "Elite Ocr Import")

This is an example of a basic trade. Pick where you are, and how far you are willing to go, and Jarvis will brute force a solution for you.
![Basic Exchange](jarvis-basic-exchange-big.png "A basic from station to somewhere close exchange")

This is a station's exchange data from the last time you imorted the data.
![Station Overview](jarvis-station-overview.png "Station overview")

This is an example of a multistop trade solution. The profit is incremental for each stop.
![Multistop Exchange](jarvis-3-trades-1-jump-distance-exchange.png "Multistop Exchange")

This is an example of specific commodity trade solutions.
![Buy Anywhere](jarvis-buy-anywhere-exchange.png "Buy Anywhere")

More commodity specific trade solutions.
![Sell Near](jarvis-sell-commodity-near-station-exchange.png "Sell Near")

Station to station exchange example.
![Station to Station Exchange](jarvis-station-to-station-exchange.png "Station to Station Exchange")

Jarvis is multithreaded and CPU starved. he tries to brute force these trade solutions, and It will bring your CPU to a halt. I'll eventually provide a CPU throttle (its really easy, just not done yet). The 1 and 2 stop solutions come in fast. The 3 stop solutions will take a minute.
![Jarvis Multi-threaded](jarvis-is-really-multithreaded.png "Jarvis Multi-threaded")



