from dataclasses import dataclass
from enum import Enum
import dotenv
import os


class ConfigKeys(str, Enum):
    TRAINING_DATASET_DIR = "TRAINING_DATASET_DIR"
    TRAINING_BACKUP_DIR = "TRAINING_BACKUP_DIR"
    SECRET_KEY = "SECRET_KEY"


class Config:

    def __init__(self, config_file=".env") -> None:
        cfg = dotenv.dotenv_values(config_file)
        self.training_dataset = TrainingDatasetConfig()
        self.auth_config = AuthConfig()
        if ConfigKeys.TRAINING_DATASET_DIR in cfg:
            self.training_dataset.dataset_dir = cfg["TRAINING_DATASET_DIR"]
        if ConfigKeys.TRAINING_BACKUP_DIR in cfg:
            self.training_dataset.backup_dir = cfg["TRAINING_BACKUP_DIR"]
        if ConfigKeys.SECRET_KEY in cfg:
            self.auth_config.secret_key = cfg["SECRET_KEY"]


@dataclass
class TrainingDatasetConfig:

    dataset_dir: str = "./train/dataset"
    backup_dir: str = "./train/backup"

@dataclass
class AuthConfig:

    secret_key: str = ""


# Default configuration, it loads configuration from .env file
config = Config()

# Some other configuration
# otherConfiguration = Config(".env.other")
