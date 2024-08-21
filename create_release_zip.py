import os
import shutil
import tempfile
import zipfile

def create_release_zip(output_path, site_xml_path, feature_xml_path, plugin_jar_path, version_path):
    with open(version_path, 'r') as file:
        version = file.read().strip()

    with tempfile.TemporaryDirectory() as tmp_dir:
        os.makedirs(os.path.join(tmp_dir, "features"))
        os.makedirs(os.path.join(tmp_dir, "plugins"))
        
        with open(site_xml_path, 'r') as file:
            site_xml_content = file.read()
            site_xml_content = site_xml_content.replace("VERSION", version)
        with open(os.path.join(tmp_dir, "site.xml"), 'w') as file:
            file.write(site_xml_content)        

        with open(feature_xml_path, 'r') as file:
            feature_xml_content = file.read()
            feature_xml_content = feature_xml_content.replace("VERSION", version)
        with zipfile.ZipFile(os.path.join(tmp_dir, f"features/cody_feature_{version}.jar"), "w") as feature_jar:
            feature_jar.writestr("feature.xml", feature_xml_content)

        shutil.copy(plugin_jar_path, os.path.join(tmp_dir, f"plugins/cody-chat_{version}.jar"))
        
        os.makedirs(os.path.dirname(output_path), exist_ok=True)
        with zipfile.ZipFile(output_path, "w", zipfile.ZIP_DEFLATED) as release_zip:
            for root, _, files in os.walk(tmp_dir):
                for file in files:
                    file_path = os.path.join(root, file)
                    arcname = os.path.relpath(file_path, tmp_dir)
                    with open(file_path, 'rb') as f:
                        release_zip.writestr(arcname, f.read())

if __name__ == "__main__":
    import sys
    create_release_zip(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5])
