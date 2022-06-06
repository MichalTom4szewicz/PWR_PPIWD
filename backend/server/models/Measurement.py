import datetime

from mongoengine.fields import StringField, DateTimeField, ReferenceField, IntField, EmbeddedDocumentListField, FloatField, BooleanField
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
    invalid = BooleanField(default=False)

    def to_dict(self):
        representation = {
            'id': str(self.id),
            'user': str(self.user.id),
            'classifications': self.classifications,
            'sent_at': str(self.sent_at),
            'processed_at': str(self.processed_at) if self.processed_at else None
        }
        if self.invalid:
            representation['invalid'] = True

        return representation
