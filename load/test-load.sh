#!/bin/sh

SECONDS_ARG=$1

WORKERS=100
RATE=0

if ! command -v vegeta &> /dev/null
then
    echo "Install vegeta first: https://github.com/tsenart/vegeta"
    exit 1
fi

if [ -z "$SECONDS_ARG" ]; then
  echo "Usage:"
  echo "     test-load.sh SECONDS"
  exit 1
fi

echo "GET http://localhost:8080/anything" | vegeta attack -duration="$SECONDS_ARG"s -rate "$RATE" -max-workers "$WORKERS" | tee results.bin | vegeta report

vegeta report -type=json results.bin > metrics.json
cat results.bin | vegeta plot > plot.html

echo "open metrics.json and plot.html to see the results"