from datetime import datetime
import unittest
import tempfile
import shutil
import os
import tarfile

from server.services.DatasetService import DatasetService
from server.config import config


class DatasetService_test(unittest.TestCase):

    def setUp(self):
        self.tmpDir = tempfile.mkdtemp()
        self.tmpBackupDir = tempfile.mkdtemp()
        self.datasetService = DatasetService(self.tmpDir, self.tmpBackupDir)
        self.tmpExtractDir = None

    def tearDown(self):
        shutil.rmtree(self.tmpDir)
        shutil.rmtree(self.tmpBackupDir)

        if self.tmpExtractDir:
            shutil.rmtree(self.tmpExtractDir)

    def test_savesFileToANewDirectory(self):
        data = "some\ttsv\tdata"

        self.datasetService.saveMeasurement("activity_one", data)

        self.assertTrue(os.path.isfile(os.path.join(
            self.tmpDir, "activity_one", "1.csv")))

        with open(os.path.join(self.tmpDir, "activity_one", "1.csv"), 'r') as f:
            self.assertEqual(f.read(), data)

    def test_savesFilesToSeparateDirectories(self):
        data1 = "some\ttsv\tdata1"
        data2 = "some\ttsv\tdata2"

        self.datasetService.saveMeasurement("activity_one", data1)
        self.datasetService.saveMeasurement("activity_two", data2)

        with open(os.path.join(self.tmpDir, "activity_one", "1.csv"), 'r') as f:
            self.assertEqual(f.read(), data1)

        with open(os.path.join(self.tmpDir, "activity_two", "1.csv"), 'r') as f:
            self.assertEqual(f.read(), data2)

    def test_savesMultipleFilesToTheSameActivity(self):
        data1 = "some\ttsv\tdata1"
        data2 = "some\ttsv\tdata2"

        self.datasetService.saveMeasurement("activity_one", data1)
        self.datasetService.saveMeasurement("activity_one", data2)

        with open(os.path.join(self.tmpDir, "activity_one", "1.csv"), 'r') as f:
            self.assertEqual(f.read(), data1)

        with open(os.path.join(self.tmpDir, "activity_one", "2.csv"), 'r') as f:
            self.assertEqual(f.read(), data2)

    def test_exportsTarGZFile(self):

        os.makedirs(os.path.join(self.tmpDir, "activity_one"))
        os.makedirs(os.path.join(self.tmpDir, "activity_two"))

        with open(os.path.join(self.tmpDir, "activity_one", "1.csv"), "w") as f:
            f.write("some\ttsv\tdata1\n")

        with open(os.path.join(self.tmpDir, "activity_two", "1.csv"), "w") as f:
            f.write("some\ttsv\tdata2\n")

        now = datetime.now()
        time_str = now.strftime("%Y-%m-%d-%H-%M-%S")
        backup_archive = self.datasetService.exportTarGZ()

        self.assertEqual(backup_archive.split(
            "/")[-1], f"train_{time_str}.tar.gz")

        self.tmpExtractDir = tempfile.mkdtemp()

        with tarfile.open(backup_archive, "r:gz") as t:
            t.extractall(self.tmpExtractDir)

        with open(os.path.join(self.tmpExtractDir, "activity_one", "1.csv"), 'r') as f:
            self.assertEqual(f.read(), "some\ttsv\tdata1\n")

        with open(os.path.join(self.tmpExtractDir, "activity_two", "1.csv"), 'r') as f:
            self.assertEqual(f.read(), "some\ttsv\tdata2\n")

    def test_defaultInstanceConfiguration(self):
        defaultInstance = DatasetService.getDefaultInstance()

        self.assertEqual(defaultInstance.dataset_dir,
                         config.training_dataset.dataset_dir)
        self.assertEqual(defaultInstance.backup_dir,
                         config.training_dataset.backup_dir)


if __name__ == "__main__":
    unittest.main()
