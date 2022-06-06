from dataclasses import dataclass
from enum import Enum
import logging
import dotenv
import os


class ConfigKeys(str, Enum):
    TRAINING_DATASET_DIR = "TRAINING_DATASET_DIR"
    TRAINING_BACKUP_DIR = "TRAINING_BACKUP_DIR"
    SECRET_KEY = "SECRET_KEY"
    DB_NAME = "DB_NAME"
    DB_HOST = "DB_HOST"
    ML_MODELS_DIR = "ML_MODELS_DIR"
    LOGGING_LEVEL = "LOGGING_LEVEL"


@dataclass
class Config:

    logging_level = logging.INFO

    def __init__(self, config_file=".env") -> None:
        cfg = dotenv.dotenv_values(config_file)
        self.training_dataset = TrainingDatasetConfig()
        self.auth_config = AuthConfig()
        self.db_config = DBConfig()
        self.ml_config = MLConfig()
        if ConfigKeys.TRAINING_DATASET_DIR in cfg:
            self.training_dataset.dataset_dir = cfg["TRAINING_DATASET_DIR"]
        if ConfigKeys.TRAINING_BACKUP_DIR in cfg:
            self.training_dataset.backup_dir = cfg["TRAINING_BACKUP_DIR"]
        if ConfigKeys.SECRET_KEY in cfg:
            self.auth_config.secret_key = cfg["SECRET_KEY"]
        if ConfigKeys.DB_NAME in cfg:
            self.db_config.db = cfg[ConfigKeys.DB_NAME]
        if ConfigKeys.DB_HOST in cfg:
            self.db_config.host = cfg[ConfigKeys.DB_HOST]
        if ConfigKeys.ML_MODELS_DIR in cfg:
            self.ml_config.models_dir = cfg[ConfigKeys.ML_MODELS_DIR]
        if ConfigKeys.LOGGING_LEVEL in cfg:
            self.logging_level = logging.getLevelName(
                cfg[ConfigKeys.LOGGING_LEVEL])


@dataclass
class TrainingDatasetConfig:

    dataset_dir: str = "./train/dataset"
    backup_dir: str = "./train/backup"


@dataclass
class AuthConfig:

    secret_key: str = ""


@dataclass
class DBConfig:

    db: str = "ppiwd"
    host: str = "localhost"


@dataclass
class MLConfig:

    models_dir: str = "./ml_models"


# Default configuration, it loads configuration from .env file
config = Config()

# Some other configuration
# otherConfiguration = Config(".env.other")
