import os


def path_and_rename(instance, filename):
    upload_to = 'gammatones'
    ext = filename.split('.')[-1]
    # get filename
    if instance.pk:
        filename = '{}.{}'.format(instance.pk, ext)
    # return the whole path to the file
    return os.path.join(upload_to, filename)
