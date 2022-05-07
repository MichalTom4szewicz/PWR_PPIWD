import datetime

from mongoengine.fields import StringField, DateTimeField
from mongoengine import Document


class User(Document):

    email = StringField(required=True, unique=True)
    password = StringField(required=True)
    firstName = StringField(required=True)
    lastName = StringField(required=True)
    createdAt = DateTimeField()
    updatedAt = DateTimeField(default=datetime.datetime.now)

    def save(self, *args, **kwargs):
        if not self.createdAt:
            self.createdAt = datetime.datetime.now()
        self.modifiedAt = datetime.datetime.now()
        return super(User, self).save(*args, **kwargs)

    def to_dict(self):
        return {
            'id': str(self.id),
            'email': self.email,
            'firstName': self.firstName,
            'lastName': self.lastName,
            'createdAt': str(self.createdAt),
            'updatedAt': str(self.updatedAt)
        }
