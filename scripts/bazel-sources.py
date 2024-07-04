import sys
import os
import zipfile
import glob

def process_jar_files(directory):
    # Find all .jar files in the given directory
    jar_files = glob.glob(os.path.join(directory, '*.jar'))
    
    # Filter jar files containing '.source_' in their name
    source_jars = [jar for jar in jar_files if '.source_' in os.path.basename(jar)]
    
    # Create a new sources.jar file as a sibling to the directory
    output_jar_path = os.path.join(os.path.dirname(directory), os.path.basename(directory) + '-sources.jar')
    with zipfile.ZipFile(output_jar_path, 'w') as output_jar:
        for source_jar in source_jars:
            with zipfile.ZipFile(source_jar, 'r') as jar:
                # Iterate through all entries in the jar
                for entry in jar.infolist():
                    # Check if the entry is a .java file
                    if entry.filename.endswith('.java'):
                        # Read the content of the .java file
                        content = jar.read(entry.filename)
                        # Write the content to the new sources.jar
                        if entry.filename not in output_jar.namelist():
                            output_jar.writestr(entry.filename, content)
    print(f"Created {output_jar_path}")

if __name__ == "__main__":
    
    base_directory = os.getcwd()
    eclipse_platforms_dir = os.path.join(base_directory, "eclipse-platforms")
    
    if not os.path.isdir(eclipse_platforms_dir):
        print(f"Error: {eclipse_platforms_dir} is not a directory")
        sys.exit(1)
    
    subdirectories = [d for d in os.listdir(eclipse_platforms_dir) if os.path.isdir(os.path.join(eclipse_platforms_dir, d))]
    
    for subdir in subdirectories:
        full_path = os.path.join(eclipse_platforms_dir, subdir)
        print(f"Processing directory: {full_path}")
        process_jar_files(full_path)
