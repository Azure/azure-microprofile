#!/usr/bin/env bash
################################################
# This script is invoked by a human who:
# - can create environment and variables in the github repo from which this file was cloned.
# - has the gh client >= 2.32.1 installed.
#
# This script initializes the repo from which this file was cloned
# with the necessary environment and variables to run the workflows.
# 
# This script should be invoked in the root directory of the github repo that was cloned, e.g.:
# ```
# cd <path-to-local-clone-of-the-github-repo>
# ./.github/setup-env.sh
# ``` 
#
# Script design taken from https://github.com/microsoft/NubesGen.
#
################################################

################################################
# Set environment variables - the main variables you might want to configure.
#
# Owner/reponame, e.g., <USER_NAME>/azure-microprofile
OWNER_REPONAME=
# Azure client ID for a user-assigned managed identity configured with federated identity credentials for authentication
AZURE_CLIENT_ID=
# Azure subscription ID of the user-assigned managed identity
AZURE_SUBSCRIPTION_ID=
# Azure tenant ID of the of the user-assigned managed identity
AZURE_TENANT_ID=

# End set environment variables
################################################


set -Eeuo pipefail
trap cleanup SIGINT SIGTERM ERR EXIT

cleanup() {
  trap - SIGINT SIGTERM ERR EXIT
  # script cleanup here
}

setup_colors() {
  if [[ -t 2 ]] && [[ -z "${NO_COLOR-}" ]] && [[ "${TERM-}" != "dumb" ]]; then
    NOFORMAT='\033[0m' RED='\033[0;31m' GREEN='\033[0;32m' ORANGE='\033[0;33m' BLUE='\033[0;34m' PURPLE='\033[0;35m' CYAN='\033[0;36m' YELLOW='\033[1;33m'
  else
    NOFORMAT='' RED='' GREEN='' ORANGE='' BLUE='' PURPLE='' CYAN='' YELLOW=''
  fi
}

msg() {
  echo >&2 -e "${1-}"
}

setup_colors

# get OWNER_REPONAME if not set at the beginning of this file
if [ "$OWNER_REPONAME" == '' ] ; then
    read -r -p "Enter owner/reponame: " OWNER_REPONAME
fi
GH_FLAGS="--repo ${OWNER_REPONAME}"

# get AZURE_CLIENT_ID if not set at the beginning of this file
if [ "$AZURE_CLIENT_ID" == '' ] ; then
    read -r -p "Enter client ID for a user-assigned managed identity configured with federated identity credentials for authentication: " AZURE_CLIENT_ID
fi

# get AZURE_SUBSCRIPTION_ID if not set at the beginning of this file
if [ "$AZURE_SUBSCRIPTION_ID" == '' ] ; then
    read -r -p "Enter the subscription ID of the user-assigned managed identity you just provided: " AZURE_SUBSCRIPTION_ID
fi

# get AZURE_TENANT_ID if not set at the beginning of this file
if [ "$AZURE_TENANT_ID" == '' ] ; then
    read -r -p "Enter the tenant ID of the of the user-assigned managed identity you just provided: " AZURE_TENANT_ID
fi

# Check GitHub CLI status
msg "${GREEN}(1/2) Checking GitHub CLI status...${NOFORMAT}"
USE_GITHUB_CLI=false
{
  gh auth status && USE_GITHUB_CLI=true && msg "${YELLOW}GitHub CLI is installed and configured!"
} || {
  msg "${YELLOW}Cannot use the GitHub CLI. ${GREEN}No worries! ${YELLOW}We'll set up the GitHub variables manually."
  USE_GITHUB_CLI=false
}

# Create environment and set GitHub action variables
ENVIRONMENT_NAME="ci"
msg "${GREEN}(2/2) Create environment ${ENVIRONMENT_NAME} and set variables in GitHub"
if $USE_GITHUB_CLI; then
  {
    msg "${GREEN}Using the GitHub CLI to create environment \"${ENVIRONMENT_NAME}\".${NOFORMAT}"
    gh api -X PUT repos/${OWNER_REPONAME}/environments/${ENVIRONMENT_NAME}
    msg "${GREEN}Environment \"${ENVIRONMENT_NAME}\" created"
    msg "${GREEN}Using the GitHub CLI to set variables.${NOFORMAT}"
    gh ${GH_FLAGS} variable set AZURE_CLIENT_ID -b"${AZURE_CLIENT_ID}" -e ${ENVIRONMENT_NAME}
    gh ${GH_FLAGS} variable set AZURE_SUBSCRIPTION_ID -b"${AZURE_SUBSCRIPTION_ID}" -e ${ENVIRONMENT_NAME}
    gh ${GH_FLAGS} variable set AZURE_TENANT_ID -b"${AZURE_TENANT_ID}" -e ${ENVIRONMENT_NAME}
    msg "${GREEN}Variables configured"
  } || {
    USE_GITHUB_CLI=false
  }
fi
if [ $USE_GITHUB_CLI == false ]; then
  msg "${NOFORMAT}======================MANUAL SETUP======================================"
  msg "${GREEN}Using your Web browser to set up variables..."
  msg "${NOFORMAT}Go to the GitHub repository you want to configure."
  msg "${NOFORMAT}In the \"settings\", go to the \"Environments\" tab and select \"New environment\" to add an environment named \"${ENVIRONMENT_NAME}\"."
  msg "${NOFORMAT}In the section \"Environment variables\" of new added environment \"${ENVIRONMENT_NAME}\", add the following variables:"
  msg "(in ${YELLOW}yellow the variable name and${NOFORMAT} in ${GREEN}green the variable value)"
  msg "${YELLOW}\"AZURE_CLIENT_ID\""
  msg "${GREEN}${AZURE_CLIENT_ID}"
  msg "${YELLOW}\"AZURE_SUBSCRIPTION_ID\""
  msg "${GREEN}${AZURE_SUBSCRIPTION_ID}"
  msg "${YELLOW}\"AZURE_TENANT_ID\""
  msg "${GREEN}${AZURE_TENANT_ID}"
  msg "${NOFORMAT}========================================================================"
fi
