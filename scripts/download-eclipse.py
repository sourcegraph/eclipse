import os
import requests
from zipfile import ZipFile
from io import BytesIO

def download_and_extract_eclipse():
    # We always download the distribution for Windows even on macOS because we are only using Bazel to build the plugin for now, not actually run tests.
    # This file is a manually created mirror of the official Eclipse distribution. The GitHub URL is faster to download from,
    url = "https://github.com/sourcegraph/eclipse/releases/download/v0.3.0/eclipse-committers-2024-03-R-win32-x86_64.zip"
    output_dir = "eclipse-platforms/eclipse-2024-03-jars"
    eclipse_zip_file = os.environ.get('ECLIPSE_ZIP_FILE')
    if eclipse_zip_file:
        print(f"Using Eclipse ZIP file from environment variable: {eclipse_zip_file}")
        with open(eclipse_zip_file, 'rb') as f:
            zip_bytes = BytesIO(f.read())
    else:
        print(f"Downloading Eclipse from {url}")
        response = requests.get(url)
        response.raise_for_status()
        zip_bytes = BytesIO(response.content)
    print("Extracting plugins directory")
    with ZipFile(zip_bytes) as zip_file:
        for file in zip_file.namelist():
            if file.startswith("eclipse/plugins/") and not file.endswith('/'):
                target_path = os.path.join(output_dir, os.path.relpath(file, "eclipse/plugins/"))
                zip_file.extract(file, output_dir)
                os.makedirs(os.path.dirname(target_path), exist_ok=True)
                os.rename(os.path.join(output_dir, file), target_path)
    print(f"Plugins extracted to {output_dir}")

if __name__ == "__main__":
    download_and_extract_eclipse()
