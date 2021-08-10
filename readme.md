# Mango-Companion

Companion project for [Mango](https://github.com/hkalexling/Mango) (a self-hosted manga server and web reader) that takes care of automatically downloading new manga chapters from different sites.

This project doesnt directly depent on Mango and could technically be used to keep mangas up-to-date in one place for any other purpose.

This is just a quick side project to make it simpler for myself to keep track of new chapter releases over many different mangas and sites. Feel free to report issues/request other sites to be added.

## Installation

The configuration file is generated in the library directory.

### Standalone

Run the jar with a parameter to where the config/library should be stored. For example: ``java -jar mango-companion-0.0.1-SNAPSHOT-jar-with-dependencies.jar ./mango/library``

### Docker

Build the jar using ``mvn clean package`` and then the image with Docker.

The Docker image is configured for the Mango library folder to mounted at ``/library`` inside the image.

## Configuration

- ``sleepInMinutes``: How many minutes should the process wait for the next update check. Default 60mins
- ``urls``: List of urls to mangas on supported sites.
- ``folderOverwrites``: Mapping of url to folder name. Usefull if you already have folders under different names/structures than the site has for the manga. Without an entry here, the name on the site will be used as target folder name.

## Currently supported sites:

- [``Mangadex``](https://mangadex.org)
- [``Manganato``](https://manganato.com/)
- [``MANGATX``](https://mangatx.com/manga/)