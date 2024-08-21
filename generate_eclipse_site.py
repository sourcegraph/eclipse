import os
import sys
import xml.etree.ElementTree as ET

def generate_site_structure(site_xml_path, output_dir):
    tree = ET.parse(site_xml_path)
    root = tree.getroot()

    for feature in root.findall('feature'):
        url = feature.get('url')
        if url:
            file_path = os.path.join(output_dir, url)
            os.makedirs(os.path.dirname(file_path), exist_ok=True)
            with open(file_path, 'w') as f:
                f.write(f"Placeholder for {url}")

    # Copy site.xml to the output directory
    with open(site_xml_path, 'r') as src, open(os.path.join(output_dir, 'site.xml'), 'w') as dst:
        dst.write(src.read())

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python generate_eclipse_site.py <site_xml_path> <output_dir>")
        sys.exit(1)

    site_xml_path = sys.argv[1]
    output_dir = sys.argv[2]
    generate_site_structure(site_xml_path, output_dir)
