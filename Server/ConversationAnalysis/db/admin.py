from django.contrib import admin

# Register your models here.
from db.models import Conversation, Device


@admin.register(Conversation)
class ConversationAdmin(admin.ModelAdmin):
    list_filter = ('device', 'date')
    list_display = ('device', 'date', 'uuid')
    fields = ('device', 'uuid', 'date', 'date_uploaded', 'gammatone')
    readonly_fields = fields

    def has_add_permission(self, request):
        return False


@admin.register(Device)
class DeviceAdmin(admin.ModelAdmin):
    list_display = ('uuid',)
    fields = ('uuid',)
