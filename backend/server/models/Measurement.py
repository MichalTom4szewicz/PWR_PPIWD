import datetime

from mongoengine.fields import StringField, DateTimeField, ReferenceField, IntField, EmbeddedDocumentListField
from mongoengine import Document, EmbeddedDocument

from server.models.User import User


class MeasurementClassification(EmbeddedDocument):

    start = DateTimeField(required=True)
    end = DateTimeField(required=True)
    activity_name = StringField(required=True)
    count = IntField(null=True)


class Measurement(Document):

    data = StringField(required=True)
    user = ReferenceField(User)
    classifications = EmbeddedDocumentListField(MeasurementClassification)
    sent_at = DateTimeField(default=datetime.datetime.now)
    processed_at = DateTimeField(null=True)
