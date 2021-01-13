#!/bin/bash
find . -type d -name target -exec rm -rf {} \;
find . -type d -name node_modules -exec rm -rf {} \;
