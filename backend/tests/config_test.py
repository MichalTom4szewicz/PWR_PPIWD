import os
import unittest
import tempfile
import shutil
from server.config import Config, TrainingDatasetConfig


class ConfigTest(unittest.TestCase):

    def setUp(self):
        self.tempPaths = []

    def tearDown(self) -> None:
        for path in self.tempPaths:
            shutil.rmtree(path)

    def test_keepsDefaultOptionsWhenNoConfigAvailable(self):
        tmpDir = tempfile.mkdtemp()
        self.tempPaths.append(tmpDir)

        with open(os.path.join(tmpDir, ".env"), 'w') as f:
            f.write("")

        config = Config(os.path.join(tmpDir, ".env"))

        self.assertEqual(config.training_dataset.dataset_dir,
                         "./train/dataset")
        self.assertEqual(config.training_dataset.backup_dir, "./train/backup")

    def test_readsConfigFileCorrectly(self):
        tmpDir = tempfile.mkdtemp()
        self.tempPaths.append(tmpDir)

        with open(os.path.join(tmpDir, ".env"), 'w') as f:
            f.write('TRAINING_DATASET_DIR="/training/"\n')
            f.write('TRAINING_BACKUP_DIR="/backup/"\n')

        config = Config(os.path.join(tmpDir, ".env"))

        self.assertEqual(config.training_dataset.dataset_dir,
                         "/training/")
        self.assertEqual(config.training_dataset.backup_dir, "/backup/")


if __name__ == "__main__":
    unittest.main()
