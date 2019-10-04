from django.db import models

from db import utils


class Device(models.Model):
    uuid = models.UUIDField(primary_key=True, editable=True,
                            help_text="UUID of the device (as displayed)")


class Conversation(models.Model):
    uuid = models.UUIDField(primary_key=True, editable=True, help_text="UIUD of the recording")
    device = models.ForeignKey(Device, on_delete=models.PROTECT, help_text="Device that recorded the conversation")
    date = models.DateTimeField(help_text="Date the conversation took place")
    date_uploaded = models.DateTimeField(auto_now_add=True, help_text="Date the conversation was uploaded")
    gammatone = models.ImageField(upload_to=utils.path_and_rename, unique=True,
                                  help_text="Media path of the saved gammatone")

    class Meta:
        unique_together = ('device', 'date')
