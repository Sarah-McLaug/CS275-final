from django.views.generic import TemplateView

from db.models import Conversation


class ConversationsView(TemplateView):
    template_name = "frontend/index.html"

    def get_context_data(self, **kwargs):
        context = super().get_context_data(**kwargs)
        context.update({
            "conversations": Conversation.objects.all()
        })

        return context
