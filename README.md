# DHConvalidator

The goal of the DHConvalidator is a simplification of the creation of a consistent [TEI] (http://www.tei-c.org) text base of Digital Humanities (DH) conference abstracts for further processing like the generation of the book of abstracts. The idea was to offer the user the possibility to edit the final version of the submission with her tool of choice starting with two of the most popular systems Libre Office and Microsoft Word. 

The DHConvalidator works together with the conference management tool [ConfTool] (http://www.conftool.net/) and uses [OxGarage] (http://www.tei-c.org/oxgarage/) to do the bulk conversion.

The submission of the final version is composed of four steps after logging in:

1. Generates a template for each of his submissions for one of the supported editing systems via the Template Generation Service.
2. Adding subtitle (optional), content, notes (optional) and references (optional) to the template.
3. Converting the template to a .dhc package via the Conversion and Validation Service
4. Uploading the .dhc package to ConfTool

The .dhc package contains the original edited template file (docx or odt), the TEI version, integrated media files and a HTML version of the submission.

## Installation
Take the latest [WAR file release] (https://github.com/mpetris/dhconvalidator/blob/master/release/) or create a WAR file yourself and extract it to a Servlet container (Jetty 9 or higher, Tomcat 8 or higher). 

Copy the dhconvalidator.properties.example to dhconvalidator.properties and edit it as needed:
- Set the ConfTool REST interface settings like URL and shared password. 
- Set the OxGarage webservice URL (Note that the dhconvalidator TEI/Stylesheets profile must be installed for the OxGarage webservice)
- Edit settings for validation, min resolution, publication statement and so on.

## Optional integration into a conference host page
Integration of the DHConvalidator is done via integrating an IFRAME into the host site:

```
<iframe src="http://mydomain/dhconvalidator/" height="230" width="250" frameborder="0">
</iframe>`
```

To avoid cross site scripting issues the domain should be the same as the domain of the host site. 

