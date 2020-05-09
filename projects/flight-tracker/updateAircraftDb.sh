#!/usr/bin/env bash

aircraft_db_url="https://opensky-network.org/datasets/metadata/aircraftDatabase.csv"
destination_file="src/main/resources/aircraftDatabase.csv"

echo "Saving ${aircraft_db_url} to ${destination_file}"
curl --progress-bar ${aircraft_db_url} --output ${destination_file}
