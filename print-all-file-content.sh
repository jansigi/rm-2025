#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 FolderPath Extension"
    exit 1
fi

folder="$1"
ext="$2"
[ -d "$folder" ] || { echo "Folder '$folder' does not exist."; exit 1; }
[[ "$ext" != .* ]] && ext=".$ext"

find "$folder" -type f -name "*$ext" | while read file; do
    echo "File Name: $file"
    echo "-----------------------------------"
    if ! cat "$file"; then
        echo "Warning: Could not read file: $file"
    fi
    echo
    echo "===================================="
done
