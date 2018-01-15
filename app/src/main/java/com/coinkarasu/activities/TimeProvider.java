package com.coinkarasu.activities;

import com.coinkarasu.activities.etc.Section;

public interface TimeProvider {
    long getLastUpdated(Section section);
}
