import sys
import os

def convert_dot_file(folder, dot_file, out_dir, extension):
    os.system(f"cat {os.path.join(folder, dot_file)} | dot -T{extension} > {os.path.join(out_dir,dot_file[:-4])}.{extension}")

def create_folder(folder):
    os.makedirs(folder,exist_ok=True)

def convert_folder(folder_name, extension="eps"):
    out_dir = os.path.join(folder_name,extension);
    create_folder(out_dir)
    dir_list = os.listdir(folder_name)
    for dot_file in dir_list:
        if dot_file[-4:] == ".dot":
            convert_dot_file(folder_name, dot_file, out_dir, extension)

if __name__ == "__main__":
    convert_folder(sys.argv[1])
