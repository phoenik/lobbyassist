package com.lobbyassist.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;

public class User {

    private IntegerProperty id = new SimpleIntegerProperty(0);
    private LongProperty ping = new SimpleLongProperty(0);

}
