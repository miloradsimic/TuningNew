package com.zerocode.tuningnew;

/**
 * Created by tbojan on 31.8.2015.
 */
public enum FrequencyOfNotes {
    A3 ("A", 220.00),
    A3B3 ("A#", 233.08),
    B3 ("B", 246.94),
    C4 ("C", 261.63),
    C4D4 ("C#", 277.18),
    D4 ("D", 293.66),
    D4E4 ("D#", 311.13),
    E4 ("E", 329.63),
    F4 ("F", 349.23),
    F4G4 ("F#", 369.99),
    G4 ("G", 392.00),
    G4A4 ("G#", 415.30),
    A4 ("A", 440.00),
    A4B4 ("A#", 466.16),
    B4 ("B", 493.88),
    C5 ("C", 523.25),
    C5D5 ("C#", 554.37),
    D5 ("D", 587.33),
    D5E5 ("D#", 622.25),
    E5 ("E", 659.25),
    F5 ("F", 698.46),
    F5G5 ("F#", 739.99),
    G5 ("G", 783.99),
    G5A5 ("G#", 830.61),
    A5 ("A", 880.00);

    private String mName;
    private double mFrequency;

    FrequencyOfNotes(String name, double frequency) {
        this.mName = name;
        this.mFrequency = frequency;
    }

    public String getName() {
        return mName;
    }

    public double getFrequency() {
        return mFrequency;
    }
}
