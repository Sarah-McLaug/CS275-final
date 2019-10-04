from rest_framework import serializers

from db.models import Conversation, Device


class DeviceSerializer(serializers.ModelSerializer):
    class Meta:
        model = Device
        exclude = ()


class ConversationSerializer(serializers.ModelSerializer):
    device = serializers.PrimaryKeyRelatedField(many=False, read_only=False, queryset=Device.objects.all())

    class Meta:
        model = Conversation
        fields = ('device', 'uuid', 'date', 'gammatone')
