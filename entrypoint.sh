# this is a Docker entrypoint script that will be executed
# for each start of a container based on that image.
#
# the purpose here is simply to overwrite the settings in 
# `dhconvalidator.properties` with parameters provided
# on the command line, e.g. 
# `docker run -e dhconvalidator_base_url=http://my.dhconvalidator.base.url …`

#!/bin/sh

cat <<EOF > ${JETTY_BASE}/webapps/ROOT/dhconvalidator.properties
#base_url=http://adho.org/dhconvalidator-2018
base_url=${dhconvalidator_base_url}
conftool_login_url=${dhconvalidator_conftool_login_url}
conftool_rest_url=${dhconvalidator_conftool_rest_url}
conftool_shared_pass=${dhconvalidator_conftool_shared_pass}
defaultSubmissionLanguage=${dhconvalidator_defaultSubmissionLanguage}
encodingDesc=${dhconvalidator_encodingDesc}
html_address_generation=${dhconvalidator_html_address_generation}
html_to_xml_link=${dhconvalidator_html_to_xml_link}
image_min_resolution_height=${dhconvalidator_image_min_resolution_height}
image_min_resolution_width=${dhconvalidator_image_min_resolution_width}
logConversionStepOutput=${dhconvalidator_logConversionStepOutput}
oxgarage_url=${dhconvalidator_oxgarage_url}
#paperProviderClass=org.adho.dhconvalidator.conftool.ConfToolClient
paperProviderClass=${dhconvalidator_paperProviderClass}
performSchemaValidation=${dhconvalidator_performSchemaValidation}
publicationStmt=${dhconvalidator_publicationStmt}
showOnlyAcceptedPapers=${dhconvalidator_showOnlyAcceptedPapers}
showOnlyAcceptedUsers=${dhconvalidator_showOnlyAcceptedUsers}
tei_image_location=${dhconvalidator_tei_image_location}
templateFileDE=${dhconvalidator_templateFileDE}
templateFileEN=${dhconvalidator_templateFileEN}
templateFileES=${dhconvalidator_templateFileES}
#userProviderClass=org.adho.dhconvalidator.conftool.ConfToolClient
userProviderClass=${dhconvalidator_userProviderClass}
version=1.22
EOF

# run the original jetty entrypoint script (from the jetty base image) 
exec /docker-entrypoint.sh
