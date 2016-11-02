#!/bin/bash
if [ ! -d vk-java-sdk ]; then
  git clone https://github.com/VKCOM/vk-java-sdk.git
  # apply bugfix-patch to vk api schema
  cp vk-sdk-patch.patch vk-java-sdk
  cd vk-java-sdk
  # checkout exact revision so that patch could be applied correctly
  git reset --hard bf2a45b831de05a6a9b867045d3381877933bde8
  git apply vk-sdk-patch.patch
  cd ..
fi
# build and package vk-java-sdk
cd vk-java-sdk
gradle build
mkdir -p ../project/libs
mv sdk/build/libs/sdk* ../project/libs
# build and package project
cd ../project
gradle build
