#!/usr/bin/env bash
curl -X POST -i -F video_file=@"$1"  "$2"
