# DHConvalidator

**Offical Repository:**
[ADHO/dhconvalidator](https://github.com/ADHO/dhconvalidator)

The goal of the DHConvalidator is a simplification of the creation of a
consistent [TEI](http://www.tei-c.org) text base of Digital Humanities
(DH) conference abstracts for further processing like the generation of
the book of abstracts. The idea was to offer the user the possibility to
edit the final version of the submission with her tool of choice
starting with two of the most popular systems Libre Office and Microsoft
Word. 

The DHConvalidator works together with the conference management tool
[ConfTool](http://www.conftool.net/) and 
uses [TEIGarage](https://teigarage.tei-c.org/) (formerly known as OxGarage) 
to do the bulk conversion.

The submission of the final version is composed of four steps after
logging in:

1. Generates a template for each of his submissions for one of the
   supported editing systems via the Template Generation Service.
2. Adding subtitle (optional), content, notes (optional) and references
   (optional) to the template.
3. Converting the template to a .dhc package via the Conversion and
   Validation Service
4. Uploading the .dhc package to ConfTool

The .dhc package contains the original edited template file (docx or
odt), the TEI version, integrated media files and a HTML version of the
submission.

## Overview of the process

1. ConfTool is set up by the Local Organizers
2. Contributors log into ConfTool to make their initial submission for review.
3. On acceptance, contributors prepare the final version of their submission in a word processor of their choice
4. ConValidator is set up by the Local Organizers to run on a server, and connected to ConfTool; see here: https://github.com/ADHO/dhconvalidator and here: https://www.conftool.net/ctforum/index.php?topic=368.0;wap2
5. Then, contributors log onto the ConValidator webservice with their ConfTool credentials. They need to be given a link to the entry point. 
6. There, they select the submission they would like to prepare and download the DOCX or ODT template file they need. The template contains some metadata (title, authors) and as formatting styles (DH-Heading1, DH-Blockquote, DH-Default, etc.)
7. They transfer their text into the template as plain text, applying the formatting styles as applicable. They insert images. They insert the bibliographical references in the appropriate section of the template.
8. They upload their ODT or DOCX file into the ConValidator, where they can check the conversion and execute the conversion to the DHC format. They download the DHC file from the ConValidator. This is essentially a ZIP archive containing their original OCT/DOCX file, an HTML rendering and an XML-TEI rendering, plus a folder with the image files, properly renamed).
9. The DHC file is what contributors upload into ConfTool as their final submission.
10. Local Organizers receive the DHC files, use the HTML version for the online publication of the abstracts and the XML-TEI files for archiving and for production of the Book of Abstracts, using this service: https://github.com/ADHO/TEI-to-PDF

## Installation

Take the latest [WAR file release]
(https://github.com/mpetris/dhconvalidator/blob/master/release/) or
create a WAR file yourself and extract it to a Servlet container (Jetty
9 or higher, Tomcat 8 or higher). 

Copy the dhconvalidator.properties.example to dhconvalidator.properties
and edit it as needed:
- Set the ConfTool REST interface settings like URL and shared password. 
- Set the TEIGarage webservice URL (formerly OxGarage)
- Edit settings for validation, min resolution, publication statement
- and so on.

## Optional integration into a conference host page

Integration of the DHConvalidator is done via integrating an IFRAME into
the host site:

```
<iframe src="http://mydomain/dhconvalidator/" height="230" width="250" frameborder="0">
</iframe>`
```

To avoid cross site scripting issues the domain should be the same as
the domain of the host site. 

## Docker

### Building the image

With Docker installed, simply enter
```bash
docker build -t dhconvalidator .
```

### Running a container

To run the 'dhconvalidator' image you created in the step above enter 
```bash
docker run -d --rm -p8080:8080 --name dhconvalidator dhconvalidator 
```
This will spin up a container with the default settings, making 
the DHConvalidator instance available at `http://localhost:8080/dhconv`.

#### Parameters (and default values) available to the Docker container

* dhconvalidator_base_url=`http://localhost:8080/dhconv`
* dhconvalidator_conftool_login_url=`https://www.conftool.pro/dh2018/`
* dhconvalidator_conftool_rest_url=`https://www.conftool.pro/dh2018/rest.php`
* dhconvalidator_conftool_shared_pass=`some_password`
* dhconvalidator_defaultSubmissionLanguage=`ENGLISH`
* dhconvalidator_encodingDesc=`'<encodingDesc xmlns="http://www.tei-c.org/ns/1.0"><appInfo><application ident="DHCONVALIDATOR" version="{VERSION}"><label>DHConvalidator</label></application></appInfo></encodingDesc>'`
* dhconvalidator_html_address_generation=`true`
* dhconvalidator_html_to_xml_link=`true`
* dhconvalidator_image_min_resolution_height=`50`
* dhconvalidator_image_min_resolution_width=`800`
* dhconvalidator_logConversionStepOutput=`false`
* dhconvalidator_oxgarage_url=`https://teigarage.tei-c.org/ege-webservice/`
* dhconvalidator_paperProviderClass=`org.adho.dhconvalidator.demo.DemoPaperProvider`
* dhconvalidator_performSchemaValidation=`true`
* dhconvalidator_publicationStmt=`'<publicationStmt xmlns="http://www.tei-c.org/ns/1.0"><publisher>Name, Institution</publisher><address><addrLine>Street</addrLine><addrLine>City</addrLine><addrLine>Country</addrLine><addrLine>Name</addrLine></address></publicationStmt>'`
* dhconvalidator_showOnlyAcceptedPapers=`false`
* dhconvalidator_showOnlyAcceptedUsers=`false`
* dhconvalidator_tei_image_location=`/Pictures`
* dhconvalidator_templateFileDE=`template/DH_template_DH2018_de`
* dhconvalidator_templateFileEN=`template/DH_template_DH2018_en`
* dhconvalidator_templateFileES=`template/DH_template_DH2018_es`
* dhconvalidator_userProviderClass=`org.adho.dhconvalidator.demo.DemoUserProvider`

You can overwrite the default settings via environment variables, e.g. 
```bash
docker run -d --rm -p8080:8080 --name dhconvalidator -e dhconvalidator_base_url=http://my.dhconvalidator.base.url dhconvalidator 
```
will set the base url to `http://my.dhconvalidator.base.url`. 

### Docker-compose with TEIGarage sidekick

`docker-compose` allows you to easily run your own dedicated TEIGarage 
instance as a sidekick to the DHConvalidator.

```
version: '3'

services:
  dhconvalidator:
    image: dhconvalidator
    restart: always
    ports:
      - "8080:8080"
    environment:
      dhconvalidator_base_ur: http://localhost:8080
      dhconvalidator_oxgarage_url: http://teigarage:8080/ege-webservice/
  teigarage:
    image: teic/teigarage
    restart: always
    environment:
      WEBSERVICE_URL: http://localhost:8080/ege-webservice/ 
    volumes:
      - /your/path/to/TEI/P5:/usr/share/xml/tei/odd:ro
      - /your/path/to/Stylesheets:/usr/share/xml/tei/stylesheet:ro
``` 
NB: You may provide your own copy of the TEI sources and Stylesheets! (see
https://github.com/TEIC/teigarage for more details on how to run that container)
