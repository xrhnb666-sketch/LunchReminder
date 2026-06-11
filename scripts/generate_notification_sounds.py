from __future__ import annotations

"""Generate original bundled notification sounds for res/raw."""

import math
import wave
from pathlib import Path


SAMPLE_RATE = 44_100
OUTPUT_DIR = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res" / "raw"


def empty(duration: float) -> list[float]:
    return [0.0] * int(SAMPLE_RATE * duration)


def add_at(target: list[float], source: list[float], start: float) -> None:
    offset = int(start * SAMPLE_RATE)
    for index, value in enumerate(source):
        position = offset + index
        if 0 <= position < len(target):
            target[position] += value


def adsr(
    index: int,
    total: int,
    attack: float,
    decay: float,
    sustain: float,
    release: float,
) -> float:
    time = index / SAMPLE_RATE
    duration = total / SAMPLE_RATE
    release_start = max(0.0, duration - release)

    if attack > 0 and time < attack:
        return time / attack
    if decay > 0 and time < attack + decay:
        progress = (time - attack) / decay
        return 1.0 + (sustain - 1.0) * progress
    if release > 0 and time > release_start:
        progress = (time - release_start) / release
        return max(0.0, sustain * (1.0 - progress))
    return sustain


def tone(
    frequency: float,
    duration: float,
    volume: float = 0.45,
    attack: float = 0.01,
    decay: float = 0.1,
    sustain: float = 0.45,
    release: float = 0.12,
    harmonics: tuple[tuple[float, float], ...] = (),
    vibrato: float = 0.0,
) -> list[float]:
    total = int(SAMPLE_RATE * duration)
    data: list[float] = []
    phase = 0.0
    for index in range(total):
        time = index / SAMPLE_RATE
        modulation = math.sin(2.0 * math.pi * 5.0 * time) * vibrato
        phase += 2.0 * math.pi * (frequency + modulation) / SAMPLE_RATE
        sample = math.sin(phase)
        for multiplier, level in harmonics:
            sample += math.sin(phase * multiplier) * level
        data.append(sample * adsr(index, total, attack, decay, sustain, release) * volume)
    return data


def bell(
    frequency: float,
    duration: float,
    volume: float = 0.5,
    brightness: float = 1.0,
) -> list[float]:
    total = int(SAMPLE_RATE * duration)
    data: list[float] = []
    for index in range(total):
        time = index / SAMPLE_RATE
        decay = math.exp(-time * 4.0)
        strike = min(1.0, time / 0.008)
        sample = (
            math.sin(2.0 * math.pi * frequency * time)
            + math.sin(2.0 * math.pi * frequency * 2.01 * time) * 0.30 * brightness
            + math.sin(2.0 * math.pi * frequency * 3.02 * time) * 0.12 * brightness
        )
        data.append(sample * decay * strike * volume)
    return data


def soft_echo(data: list[float], delays: tuple[float, ...], gains: tuple[float, ...]) -> list[float]:
    result = data[:]
    for delay, gain in zip(delays, gains):
        offset = int(delay * SAMPLE_RATE)
        for index in range(len(data) - offset):
            result[index + offset] += data[index] * gain
    return result


def normalize(data: list[float], peak: float = 0.88) -> list[float]:
    maximum = max((abs(value) for value in data), default=0.0)
    if maximum == 0:
        return data
    scale = peak / maximum
    return [value * scale for value in data]


def write_wav(path: Path, data: list[float]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    data = normalize(data)
    with wave.open(str(path), "wb") as output:
        output.setnchannels(1)
        output.setsampwidth(2)
        output.setframerate(SAMPLE_RATE)
        frames = bytearray()
        for value in data:
            clipped = max(-1.0, min(1.0, value))
            frames.extend(int(clipped * 32767).to_bytes(2, byteorder="little", signed=True))
        output.writeframes(frames)


def default_sound() -> list[float]:
    data = empty(1.0)
    add_at(data, tone(880.0, 0.16, volume=0.55, attack=0.004, decay=0.04, sustain=0.65, release=0.05), 0.08)
    add_at(data, tone(1174.66, 0.22, volume=0.48, attack=0.004, decay=0.05, sustain=0.55, release=0.07), 0.28)
    return soft_echo(data, delays=(0.12,), gains=(0.16,))


def gentle_sound() -> list[float]:
    data = empty(1.5)
    notes = ((523.25, 0.05), (659.25, 0.28), (783.99, 0.54), (1046.50, 0.82))
    for frequency, start in notes:
        add_at(data, bell(frequency, 0.58, volume=0.34, brightness=0.7), start)
    return soft_echo(data, delays=(0.14, 0.26), gains=(0.18, 0.08))


def bear_sound() -> list[float]:
    data = empty(1.5)
    notes = ((659.25, 0.04), (783.99, 0.22), (987.77, 0.42), (783.99, 0.68), (1174.66, 0.88))
    for frequency, start in notes:
        add_at(
            data,
            tone(
                frequency,
                0.22,
                volume=0.36,
                attack=0.006,
                decay=0.05,
                sustain=0.50,
                release=0.08,
                harmonics=((2.0, 0.18),),
                vibrato=2.2,
            ),
            start,
        )
    return soft_echo(data, delays=(0.10, 0.20), gains=(0.12, 0.06))


def music_sound() -> list[float]:
    data = empty(2.7)
    chord_notes = (
        (392.00, 0.05),
        (493.88, 0.05),
        (587.33, 0.05),
        (440.00, 0.82),
        (554.37, 0.82),
        (659.25, 0.82),
        (523.25, 1.58),
        (659.25, 1.58),
        (783.99, 1.58),
    )
    for frequency, start in chord_notes:
        add_at(
            data,
            tone(
                frequency,
                0.95,
                volume=0.20,
                attack=0.035,
                decay=0.16,
                sustain=0.42,
                release=0.34,
                harmonics=((2.0, 0.08),),
                vibrato=0.8,
            ),
            start,
        )
    add_at(data, bell(987.77, 0.7, volume=0.18, brightness=0.45), 2.0)
    return soft_echo(data, delays=(0.18, 0.36), gains=(0.16, 0.07))


def main() -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    sounds = {
        "default_sound.wav": default_sound(),
        "gentle_sound.wav": gentle_sound(),
        "bear_sound.wav": bear_sound(),
        "music_sound.wav": music_sound(),
    }

    for file_name, data in sounds.items():
        wav_path = OUTPUT_DIR / file_name
        mp3_path = wav_path.with_suffix(".mp3")
        if mp3_path.exists():
            mp3_path.unlink()
        write_wav(wav_path, data)
        print(f"generated {wav_path}")


if __name__ == "__main__":
    main()
