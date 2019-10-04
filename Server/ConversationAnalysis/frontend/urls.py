from django.conf.urls import url
from django.conf.urls.static import static
from django.contrib import admin
from django.urls import path

from ca import settings
from frontend import views


urlpatterns = [
    path('', views.ConversationsView.as_view()),
]