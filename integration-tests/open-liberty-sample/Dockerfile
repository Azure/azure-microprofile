FROM icr.io/appcafe/open-liberty:kernel-slim-java17-openj9-ubi

# Add config
COPY --chown=1001:0 src/main/liberty/config/server.xml /config/server.xml

# This script will add the requested XML snippets to enable Liberty features and grow image to be fit-for-purpose using featureUtility.
# Only available in 'kernel-slim'. The 'full' tag already includes all features for convenience.
RUN features.sh

# Add app
COPY --chown=1001:0 target/azure-microprofile-integration-tests-open-liberty-sample.war /config/apps/

# This script will add the requested XML snippets, grow image to be fit-for-purpose and apply interim fixes
RUN configure.sh
