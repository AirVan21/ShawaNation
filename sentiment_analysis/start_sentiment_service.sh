#!/usr/bin/env bash

sudo dockebuild . -t sentiment
sudo docker run -p 5001:5000 -v $(pwd)/Docker/Volume:/root/Volume sentiment "/root/main.py"