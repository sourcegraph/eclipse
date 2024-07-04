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
                        output_jar.writestr(entry.filename, content)

    print(f"Created sources.jar in {directory}")

if __name__ == "__main__":
    import sys
    if len(sys.argv) != 2:
        print("Usage: python bazel-sources.py <directory>")
        sys.exit(1)
    
    directory = sys.argv[1]
    process_jar_files(directory)
