package com.zerocode.tuningnew;

/**
 * Created by milorad on 15.10.2015.
 */
public enum FrequencyOfNotesFor432 {
    /*C0 ("C", 16.05),
    C0D0 ("C#", 17.01),
    D0 ("D", 18.02),
    D0E0 ("D#", 19.09),
    E0 ("E", 20.23),
    F0 ("F", 21.43),
    F0G0 ("F#", 22.70),
    G0 ("G", 24.05),
    G0A0 ("G#", 25.48),*/

    A0 ("A", 27.00),
    A0B0 ("A#", 28.61),
    B0 ("B", 30.31),
    C1 ("C", 32.11),
    C1D1 ("C#", 34.02),
    D1 ("D", 36.04),
    D1E1 ("D#", 38.18),
    E1 ("E", 40.45),
    F1 ("F", 42.86),
    F1G1 ("F#", 45.41),
    G1 ("G", 48.11),
    G1A1 ("G#", 50.97),
    A1 ("A", 54.00),
    A1B1 ("A#", 57.21),
    B1 ("B", 60.61),
    C2 ("C", 64.22),
    C2D2 ("C#", 68.04),
    D2 ("D", 72.08),
    D2E2 ("D#", 76.37),
    E2 ("E", 80.91),
    F2 ("F", 85.72),
    F2G2 ("F#", 90.82),
    G2 ("G", 96.22),
    G2A2 ("G#", 101.94),
    A2 ("A", 108.00),
    A2B2 ("A#", 114.16),
    B2 ("B", 121.23),
    C3 ("C", 128.43),
    C3D3 ("C#", 136.07),
    D3 ("D", 144.16),
    D3E3 ("D#", 152.74),
    E3 ("E", 161.82),
    F3 ("F", 171.44),
    F3G3 ("F#", 181.63),
    G3 ("G", 192.43),
    G3A3 ("G#", 203.88),

//Old code
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
    A5 ("A", 864.00),
//End of old code

    A5B5 ("A#", 915.38),
    B5 ("B", 969.81),
    C6 ("C", 1027.47),
    C6D6 ("C#", 1088.57),
    D6 ("D", 1153.30),
    D6E6 ("D#", 1221.88),
    E6 ("E", 1294.54),
    F6 ("F", 1371.51),
    F6G6 ("F#", 1453.07),
    G6 ("G", 1539.47),
    G6A6 ("G#", 1631.01),
    A6 ("A", 1728.00),
    A6B6 ("A#", 1830.75),
    B6 ("B", 1939.61),
    C7 ("C", 2054.95),
    C7D7 ("C#", 2177.14),
    D7 ("D", 2306.60),
    D7E7 ("D#", 2443.76),
    E7 ("E", 2589.07),
    F7 ("F", 2743.03),
    F7G7 ("F#", 2906.14),
    G7 ("G", 3078.95),
    G7A7 ("G#", 3262.03),
    A7 ("A", 3456.00),
    A7B7 ("A#", 3661.50),
    B7 ("B", 3879.23),
    C8 ("C", 4109.90),
    C8D8 ("C#", 4354.29),
    D8 ("D", 4613.21),
    D8E8 ("D#", 4887.52),
    E8 ("E", 5178.15),
    F8 ("F", 5486.06),
    F8G8 ("F#", 5812.28),
    G8 ("G", 6157.89),
    G8A8 ("G#", 6524.06),
    A8 ("A", 6912.00);

           /*A8B8 ("A#", 7323.01),
           B8 ("B", 7758.46);*/






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
