# First step: build the war file
FROM gradle:5.4 as builder

WORKDIR /home/gradle/dhconvalidator
USER root:root
COPY . .
RUN gradle war

# step 2: run the application server
FROM jetty:alpine

COPY --from=builder /home/gradle/dhconvalidator/build/libs/*.war /tmp/
COPY entrypoint.sh /entrypoint.sh

USER root:root
RUN mkdir -p ${JETTY_BASE}/webapps/ROOT \
    && unzip /tmp/*.war -d ${JETTY_BASE}/webapps/ROOT \
    && chown -R jetty:jetty ${JETTY_BASE}/webapps/ROOT

USER jetty:jetty

ENTRYPOINT ["/entrypoint.sh"]
CMD ["java","-jar","/usr/local/jetty/start.jar"]
