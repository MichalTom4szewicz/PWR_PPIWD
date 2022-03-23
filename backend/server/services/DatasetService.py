import logging
import os
import tarfile
from server.config import config

from datetime import datetime


class DatasetService:

    __defaultInstance = None

    @staticmethod
    def getDefaultInstance() -> 'DatasetService':
        if not DatasetService.__defaultInstance:
            DatasetService.__defaultInstance = DatasetService(
                config.training_dataset.dataset_dir, config.training_dataset.backup_dir)
        return DatasetService.__defaultInstance

    def __init__(self, dataset_dir: str, backup_dir: str):
        self.dataset_dir = dataset_dir
        self.backup_dir = backup_dir

    def saveMeasurement(self, activity_name: str, data: str):
        """Zapisuje nowy plik do klasy <activity_name> o treści <data>, w katalogu określonym przez <dataset_dir>"""
        directory_path = os.path.join(self.dataset_dir, activity_name.lower())
        self.__createDirectoryIfNotExists(directory_path)
        files_count = self.__countFilesInDirectory(directory_path)
        filename = f"{files_count + 1}.csv"

        self.__saveFile(os.path.join(directory_path, filename), data)

    def exportTarGZ(self):
        """Kompresuje zawartość folderu <dataset_dir> i zapisuje archiwum w folderze <backup_dir>. Zwraca ścieżkę do nowego archiwum"""
        time_str = self.__getCurrentTimeString()
        tar_gz_path = os.path.join(self.backup_dir, f"train_{time_str}.tar.gz")

        self.__createDirectoryIfNotExists(self.backup_dir)

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

    def __getCurrentTimeString(self):
        now = datetime.now()
        return now.strftime("%Y-%m-%d-%H-%M-%S")

    def __saveFile(self, destination_path: str, data: str):
        with open(destination_path, 'w') as f:
            f.write(data)

    def __countFilesInDirectory(self, directory_path: str):
        for _, _, filenames in os.walk(directory_path):
            return len(filenames)

    def __createDirectoryIfNotExists(self, directory_path: str):
        if not self.__checkIfDirectoryExists(directory_path):
            os.makedirs(directory_path)

    def __checkIfDirectoryExists(self, directory_path: str):
        return os.path.isdir(directory_path)
