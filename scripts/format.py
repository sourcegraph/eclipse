import os
import subprocess


def format_java_files():
    # Run git ls-files to get a list of Java files
    java_files = (
        subprocess.check_output(["git", "ls-files", "*.java"])
        .decode()
        .strip()
        .split("\n")
    )

    java_files_path = "java_files.txt"
    # Write the list of Java files to a file
    with open(java_files_path, "w") as f:
        f.write("\n".join(java_files))

    subprocess.run(
        [
            "java",
            "-jar",
            os.path.join(os.path.dirname(__file__), "coursier"),
            "launch",
            "com.google.googlejavaformat:google-java-format:1.22.0",
            "--",
            "--replace",
            "@" + java_files_path,
        ]
    )

    os.remove(java_files_path)


if __name__ == "__main__":
    format_java_files()
