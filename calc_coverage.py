import xml.etree.ElementTree as ET
import sys

xml_file = sys.argv[1]
tree = ET.parse(xml_file)
root = tree.getroot()

# Get the last LINE counter (overall coverage)
counters = [c for c in root.iter('counter') if c.get('type') == 'LINE']
if counters:
    last = counters[-1]
    missed = int(last.get('missed'))
    covered = int(last.get('covered'))
    total = missed + covered
    if total > 0:
        percent = (covered * 100) // total
        print(f"{percent}%: {covered}/{total} lines")
