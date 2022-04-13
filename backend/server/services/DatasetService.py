import logging
import os
import tarfile
from server.config import config

from datetime import datetime


class DatasetService:

    __defaultInstance = None

    @staticmethod
    def get_default_instance() -> 'DatasetService':
        if not DatasetService.__defaultInstance:
            DatasetService.__defaultInstance = DatasetService(
                config.training_dataset.dataset_dir, config.training_dataset.backup_dir)
        return DatasetService.__defaultInstance

    def __init__(self, dataset_dir: str, backup_dir: str):
        self.dataset_dir = dataset_dir
        self.backup_dir = backup_dir

    def save_measurement(self, activity_name: str, repeat_count: int, data: str):
        """Zapisuje nowy plik do klasy <activity_name>/<repeat_count> o treści <data>, w katalogu określonym przez <dataset_dir>"""
        directory_path = os.path.join(
            self.dataset_dir, activity_name.lower(), str(repeat_count))
        self.__create_directory_if_doesnt_exist(directory_path)
        files_count = self.__count_files_in_directory(directory_path)
        filename = f"{files_count + 1}.csv"

        self.__save_file(os.path.join(directory_path, filename), data)

    def export_tar_gz(self):
        """Kompresuje zawartość folderu <dataset_dir> i zapisuje archiwum w folderze <backup_dir>. Zwraca ścieżkę do nowego archiwum"""
        time_str = self.__get_current_time_string()
        tar_gz_path = os.path.join(self.backup_dir, f"train_{time_str}.tar.gz")

        self.__create_directory_if_doesnt_exist(self.backup_dir)

        with tarfile.open(tar_gz_path, "w:gz") as t:
            for child in os.listdir(self.dataset_dir):
                child_path = os.path.join(self.dataset_dir, child)

                if os.path.isdir(child_path):
                    t.add(
                        child_path,
                        arcname=child_path.split(self.dataset_dir)[1]
                        # this rewrites the root inside tar file, for example /tmp/dataset/activity/1.csv -> /activity/1.csv
                    )

        return tar_gz_path

    def __get_current_time_string(self):
        now = datetime.now()
        return now.strftime("%Y-%m-%d-%H-%M-%S")

    def __save_file(self, destination_path: str, data: str):
        with open(destination_path, 'w') as f:
            f.write(data)

    def __count_files_in_directory(self, directory_path: str):
        for _, _, filenames in os.walk(directory_path):
            return len(filenames)

    def __create_directory_if_doesnt_exist(self, directory_path: str):
        if not self.__check_if_directory_exists(directory_path):
            os.makedirs(directory_path)

    def __check_if_directory_exists(self, directory_path: str):
        return os.path.isdir(directory_path)
