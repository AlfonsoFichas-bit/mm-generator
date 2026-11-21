#!/usr/bin/env python3
# This script fixes the MainController.kt click handler to call openMemeEditor

filepath = r'C:\Users\alfon\IdeaProjects\mm-generator\src\main\kotlin\com\buga\mmgenerator\MainController.kt'

with open(filepath, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Find and replace the click handler
# We need to find the line with "renderGallery()" that's in the card click handler context
# and replace it with "openMemeEditor(template)"

new_lines = []
i = 0
while i < len(lines):
    line = lines[i]
    # Check if this is the renderGallery() call in the click handler
    # Pattern: "renderGallery()" preceded by being inside the cardStack.setOnMouseClicked block
    if i > 0 and 'renderGallery()' in line and i > 0 and 'selectedTemplate = template' in ''.join(lines[max(0,i-5):i]):
        # Replace this renderGallery() with openMemeEditor(template)
        line = line.replace('renderGallery()', 'openMemeEditor(template)')
        new_lines.append(line)
        i += 1
    else:
        new_lines.append(line)
        i += 1

with open(filepath, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

print("File updated successfully")