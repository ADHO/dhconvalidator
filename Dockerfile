# First step: build the war file
FROM gradle:5.4 as builder

WORKDIR /home/gradle/dhconvalidator
USER root:root
COPY . . 
RUN gradle war

# step 2: run the application server
FROM jetty:alpine

#base_url=http://adho.org/dhconvalidator-2018
#paperProviderClass=org.adho.dhconvalidator.conftool.ConfToolClient
#userProviderClass=org.adho.dhconvalidator.conftool.ConfToolClient
ENV dhconvalidator_base_url=http://localhost:8080/dhconv, \
    dhconvalidator_conftool_login_url=https://www.conftool.pro/dh2018/ \
    dhconvalidator_conftool_rest_url=https://www.conftool.pro/dh2018/rest.php \
    dhconvalidator_conftool_shared_pass=quae4EeCh8iepaisoay2gei9wai3Eiz5uyaig1Daisie5ho4caxab8ahraeChaiz \
    dhconvalidator_defaultSubmissionLanguage=ENGLISH \
    dhconvalidator_encodingDesc='<encodingDesc xmlns="http://www.tei-c.org/ns/1.0"><appInfo><application ident="DHCONVALIDATOR" version="{VERSION}"><label>DHConvalidator</label></application></appInfo></encodingDesc>' \
    dhconvalidator_html_address_generation=true \
    dhconvalidator_html_to_xml_link=true \
    dhconvalidator_image_min_resolution_height=50 \
    dhconvalidator_image_min_resolution_width=800 \
    dhconvalidator_logConversionStepOutput=false \
    dhconvalidator_oxgarage_url=http://www.tei-c.org/ege-webservice/ \
    dhconvalidator_paperProviderClass=org.adho.dhconvalidator.demo.DemoPaperProvider \
    dhconvalidator_performSchemaValidation=true \
    dhconvalidator_publicationStmt='<publicationStmt xmlns="http://www.tei-c.org/ns/1.0"><publisher>Name, Institution</publisher><address><addrLine>Street</addrLine><addrLine>City</addrLine><addrLine>Country</addrLine><addrLine>Name</addrLine></address></publicationStmt>' \
    dhconvalidator_showOnlyAcceptedPapers=false \
    dhconvalidator_showOnlyAcceptedUsers=false \
    dhconvalidator_tei_image_location=/Pictures \
    dhconvalidator_templateFileDE=template/DH_template_DH2018_en \
    dhconvalidator_templateFileEN=template/DH_template_DH2018_en \
    dhconvalidator_templateFileES=template/DH_template_DH2018_es \
    dhconvalidator_userProviderClass=org.adho.dhconvalidator.demo.DemoUserProvider

COPY --from=builder /home/gradle/dhconvalidator/build/libs/*.war /tmp/
COPY entrypoint.sh /entrypoint.sh

USER root:root
RUN mkdir -p ${JETTY_BASE}/webapps/ROOT \
    && unzip /tmp/*.war -d ${JETTY_BASE}/webapps/ROOT \
    && chown -R jetty:jetty ${JETTY_BASE}/webapps/ROOT 

USER jetty:jetty

ENTRYPOINT ["/entrypoint.sh"]
CMD ["java","-jar","/usr/local/jetty/start.jar"]