import datetime

from mongoengine.fields import StringField, DateTimeField, ReferenceField, IntField, EmbeddedDocumentListField, FloatField
from mongoengine import Document, EmbeddedDocument

from server.models.User import User


class MeasurementClassification(EmbeddedDocument):

    start = FloatField(required=True)
    end = FloatField(required=True)
    activity_name = StringField(required=True)
    count = IntField(null=True)


class Measurement(Document):

    data = StringField(required=True)
    user = ReferenceField(User)
    classifications = EmbeddedDocumentListField(MeasurementClassification)
    sent_at = DateTimeField(default=datetime.datetime.now)
    processed_at = DateTimeField(null=True)

    def to_dict(self):
        return {
            'id': str(self.id),
            'user': str(self.user.id),
            'classifications': self.classifications,
            'sent_at': str(self.sent_at),
            'processed_at': str(self.processed_at)
        }
