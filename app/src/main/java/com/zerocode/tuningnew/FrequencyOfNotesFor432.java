package com.zerocode.tuningnew;

/**
 * Created by milorad on 15.10.2015.
 */
public enum FrequencyOfNotesFor432 {
    A3 ("A", 216.00),
    A3B3 ("A#", 228.84),
    B3 ("B", 242.45),
    C4 ("C", 256.87),
    C4D4 ("C#", 272.14),
    D4 ("D", 288.33),
    D4E4 ("D#", 305.47),
    E4 ("E", 323.63),
    F4 ("F", 342.88),
    F4G4 ("F#", 363.27),
    G4 ("G", 384.87),
    G4A4 ("G#", 407.75),
    A4 ("A", 432.00),
    A4B4 ("A#", 457.69),
    B4 ("B", 484.90),
    C5 ("C", 513.74),
    C5D5 ("C#", 544.29),
    D5 ("D", 576.65),
    D5E5 ("D#", 610.94),
    E5 ("E", 647.27),
    F5 ("F", 685.76),
    F5G5 ("F#", 726.53),
    G5 ("G", 769.74),
    G5A5 ("G#", 815.51),
    A5 ("A", 869.81);

    private String mName;
    private double mFrequency;

    FrequencyOfNotesFor432(String name, double frequency) {
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
