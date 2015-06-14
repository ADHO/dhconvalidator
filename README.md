# DHConvalidator

The goal of the DHConvalidator is a simplification of the creation of a consistent [TEI] (http://www.tei-c.org) text base of Digital Humanities (DH) conference abstracts for further processing like the generation of the book of abstracts. The idea was to offer the user the possibility to edit the final version of the submission with her tool of choice starting with two of the most popular systems Libre Office and Microsoft Word. 

The DHConvalidator works together with the conference management tool [ConfTool] (http://www.conftool.net/) and uses [OxGarage] (http://www.tei-c.org/oxgarage/) to do the bulk conversion.

The submission of the final version is composed of four steps after logging in:

1. Generates a template for each of his submissions for one of the supported editing systems via the Template Generation Service.
2. Adding subtitle (optional), content, notes (optional) and references (optional) to the template.
3. Converting the template to a .dhc package via the Conversion and Validation Service
4. Uploading the .dhc package to ConfTool

The .dhc package contains the TEI version, integrated media files and a HTML version of the submission.

## Installation
Take the latest [WAR file release] (https://github.com/mpetris/dhconvalidator/blob/master/release/) or create a WAR file yourself and extract it to a Servlet container that supports websockets (Jetty 7 or higher, Tomcat 8 or higher) and JNDI. 

Copy the dhconvalidator.properties.example to dhconvalidator.properties and edit it as needed:
- Set the ConfTool REST interface settings like URL and shared password. 
- Set the OxGarage webservice URL (Note that the dhconvalidator TEI/Stylesheets profile must be installed for the OxGarage webservice)
- Edit settings for validation, min resolution, publication statement and so on.

If you change the context path you need to change it in WEB-INF/lib/jetty-web.xml as well!

Integration of the DHConvalidator is done via integrating an IFRAME into the host site:

```
<iframe src="http://mydomain/dhconvalidator/" height="230" width="250" frameborder="0">
</iframe>`
```

To avoid cross site scripting issues the domain should be the same as the domain of the host site. 

If you want to use a proxy to forward the dhconvalidator requests keep in mind that the proxy must support websockets as well.
This is a working [nginx] (http://nginx.org/) setting for nginx version 1.6.2:

	map $http_upgrade $connection_upgrade {
        default Upgrade;
        ''      close;
	}

	location /dhconvalidator/ {
		proxy_pass http://myforwarddomain:8090/dhconvalidator/;

		keepalive_timeout 60;
		proxy_redirect off;
		proxy_set_header Host $host;
		proxy_set_header   X-Real-IP        $remote_addr;
		proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
		proxy_max_temp_file_size 0;
		proxy_connect_timeout      5m;
		proxy_send_timeout         20;
		proxy_read_timeout         5m;
		proxy_buffer_size          128k;
		proxy_buffers              16 64k;
		proxy_busy_buffers_size    128k;
		proxy_temp_file_write_size 128k;
		proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
		proxy_http_version 1.1;
		proxy_set_header Upgrade $http_upgrade;
		proxy_set_header Connection $connection_upgrade;
		proxy_buffering off;
		proxy_ignore_client_abort off;
		break;
	}




