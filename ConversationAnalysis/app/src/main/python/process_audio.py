import matplotlib
import scipy
from gammatone import fftweight
from gammatone import plot


def main(inpath, outpath):
    render_audio_from_file(inpath, 0, fftweight.fft_gtgram, outpath)


# copied from gammatone repo, modified to save to file
# https://github.com/detly/gammatone/blob/master/gammatone/plot.py
def render_audio_from_file(path, duration, function, outpath):
    """
    Renders the given ``duration`` of audio from the audio file at ``path``
    using the gammatone spectrogram function ``function``
    saves graph to file ``outpath``.
    """
    samplerate, data = scipy.io.wavfile.read(path)

    # Average the stereo signal
    if duration:
        nframes = duration * samplerate
        data = data[0: nframes, :]

    signal = data.mean(1)

    # Default gammatone-based spectrogram parameters
    twin = 0.08
    thop = twin / 2
    channels = 1024
    fmin = 20

    # Set up the plot
    fig = matplotlib.pyplot.figure()
    axes = fig.add_axes([0.1, 0.1, 0.8, 0.8])

    plot.gtgram_plot(
        function,
        axes,
        signal,
        samplerate,
        twin, thop, channels, fmin)

    # axes.set_title(os.path.basename(path))
    axes.set_xlabel("Time (s)")
    axes.set_ylabel("Frequency")

    # matplotlib.pyplot.show()
    matplotlib.pyplot.savefig(outpath)
