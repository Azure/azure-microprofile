#!/usr/bin/env bash
################################################
# This script is invoked by a human who:
# - has invoked the setup-env.sh script
#
# This script removes the environment and variables created in setup-env.sh.
#
# This script should be invoked in the root directory of the github repo that was cloned, e.g.:
# ```
# cd <path-to-local-clone-of-the-github-repo>
# ./.github/tear-down-env.sh
# ``` 
#
# Script design taken from https://github.com/microsoft/NubesGen.
#
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

read -r -p "Enter owner/reponame: " OWNER_REPONAME
GH_FLAGS="--repo ${OWNER_REPONAME}"

# Check GitHub CLI status
msg "${GREEN}(1/2) Checking GitHub CLI status...${NOFORMAT}"
USE_GITHUB_CLI=false
{
  gh auth status && USE_GITHUB_CLI=true && msg "${YELLOW}GitHub CLI is installed and configured!"
} || {
  msg "${YELLOW}Cannot use the GitHub CLI. ${GREEN}No worries! ${YELLOW}We'll remove the GitHub variables and environment manually."
  USE_GITHUB_CLI=false
}

msg "${GREEN}(2/2) Removing the variables and environment...${NOFORMAT}"
ENVIRONMENT_NAME="ci"
if $USE_GITHUB_CLI; then
  {
    msg "${GREEN}Using the GitHub CLI to remove variables.${NOFORMAT}"
    gh ${GH_FLAGS} variable remove AZURE_CLIENT_ID -e ${ENVIRONMENT_NAME}
    gh ${GH_FLAGS} variable remove AZURE_SUBSCRIPTION_ID -e ${ENVIRONMENT_NAME}
    gh ${GH_FLAGS} variable remove AZURE_TENANT_ID -e ${ENVIRONMENT_NAME}
    msg "${GREEN}Variables removed"
    msg "${GREEN}Using the GitHub CLI to remove the environment \"${ENVIRONMENT_NAME}\".${NOFORMAT}"
    gh api -X DELETE repos/${OWNER_REPONAME}/environments/${ENVIRONMENT_NAME}
    msg "${GREEN}Environment \"${ENVIRONMENT_NAME}\" removed"
  } || {
    USE_GITHUB_CLI=false
  }
fi
if [ $USE_GITHUB_CLI == false ]; then
  msg "${NOFORMAT}======================MANUAL REMOVAL======================================"
  msg "${GREEN}Using your Web browser to remove variables and environment..."
  msg "${NOFORMAT}Go to the GitHub repository you want to configure."
  msg "${NOFORMAT}In the \"settings\", go to the \"Environments\" tab and select environment \"${ENVIRONMENT_NAME}\"."
  msg "${NOFORMAT}In the section \"Environment variables\" of environment \"${ENVIRONMENT_NAME}\", remove the following variables:"
  msg "(in ${YELLOW}yellow the variable name)"
  msg "${YELLOW}\"AZURE_CLIENT_ID\""
  msg "${YELLOW}\"AZURE_SUBSCRIPTION_ID\""
  msg "${YELLOW}\"AZURE_TENANT_ID\""
  msg "${NOFORMAT}========================================================================"
fi
