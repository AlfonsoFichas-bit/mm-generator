#!/usr/bin/env python3
import re

with open('C:\\Users\\alfon\\IdeaProjects\\mm-generator\\src\\main\\kotlin\\com\\buga\\mmgenerator\\MainController.kt', 'r') as f:
    content = f.read()

# Find and replace the card click handler
old_text = """// Card Click Selection
        cardStack.setOnMouseClicked {
            selectedTemplate = template
            renderGallery()
        }"""

new_text = """// Card Click Selection
        cardStack.setOnMouseClicked {
            selectedTemplate = template
            openMemeEditor(template)
        }"""

if old_text in content:
    content = content.replace(old_text, new_text)
    with open('C:\\Users\\alfon\\IdeaProjects\\mm-generator\\src\\main\\kotlin\\com\\buga\\mmgenerator\\MainController.kt', 'w') as f:
        f.write(content)
    print('Replacement successful')
else:
    print('Old text not found, trying alternative...')
    # Try to find the pattern
    if 'cardStack.setOnMouseClicked' in content:
        print('Found cardStack.setOnMouseClicked in file')