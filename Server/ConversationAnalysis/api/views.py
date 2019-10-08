# Create your views here.
from django.shortcuts import get_object_or_404
from rest_framework import viewsets, mixins
from rest_framework.decorators import action
from rest_framework.response import Response

from api.serializers import ConversationSerializer
from db.models import Conversation, Device


class ConversationViewSet(mixins.CreateModelMixin, mixins.RetrieveModelMixin, viewsets.GenericViewSet):
    queryset = Conversation.objects.all()
    serializer_class = ConversationSerializer

    @action(detail=False, methods=['get'],
            url_path='device/(?P<device>[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})')
    def list_action(self, request, device, *args, **kwargs):
        device = get_object_or_404(Device, uuid=device)
        qs = self.queryset.filter(device=device)
        serializer = ConversationSerializer(qs, many=True)
        return Response(serializer.data)

    def create(self, request, *args, **kwargs):
        return super().create(request, *args, **kwargs)
