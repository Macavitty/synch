#!/usr/bin/env bash
ffmpeg -i "$1" -vf "pad=iw:2*ih [top]; movie="$2" [bottom]; [top][bottom] overlay=0:main_h/2\ " "$3"