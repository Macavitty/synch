#!/usr/bin/env bash
ffmpeg -i "$1" -vf "pad=iw:2*ih [top]; movie="$2" [bottom]; [top][bottom] overlay=0:main_h/2\ " "$5"
ffmpeg -i "$5" -vf drawtext="fontfile=/path/to/font.ttf: text='"$3"': fontcolor=white: fontsize=24: box=1: boxcolor=black@0.5: boxborderw=5: x=(w-text_w)/2: y=(h-text_h)/2" -codec:a copy "$4"