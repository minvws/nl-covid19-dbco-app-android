#!/bin/sh

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NO_COLOR='\033[0m'
CLEAR_LINE='\r\033[K'

# Exit if any subcommand fails
set -e

# Check install script dependencies

if ! command -v bundle > /dev/null; then
  printf "${CLEAR_LINE}❌${RED} You must install bundler on your system before setup can continue${NO_COLOR}\n"
  printf "${YELLOW}You could try running 'gem install bundler'${NO_COLOR}\n"
  exit -1
fi

# Configure bundler
printf "Configuring bundler to use vendor/bundle… "
if bundle config set path 'vendor/bundle'; then
  printf "${GREEN}[ok]${NO_COLOR}\n"
else
  exit -1
fi

# Install ruby dependencies
printf "Installing gems via bundler… "
if bundle install --quiet; then
  printf "${GREEN}[ok]${NO_COLOR}\n"
else
  exit -1
fi

printf "\n${CLEAR_LINE}${GREEN}All done! Everything seems a-okay 👌${NO_COLOR}\n"