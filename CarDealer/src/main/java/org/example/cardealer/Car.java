package org.example.cardealer;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

public class Car {
    private final StringProperty make;
    private final StringProperty model;
    private final StringProperty vinNumber;  // This line was updated
    private final StringProperty problem;
    private final StringProperty status;

    public Car(String make, String model, String vinNumber, String problem, String status) {
        this.make = new SimpleStringProperty(make);
        this.model = new SimpleStringProperty(model);
        this.vinNumber = new SimpleStringProperty(vinNumber);
        this.problem = new SimpleStringProperty(problem);
        this.status = new SimpleStringProperty(status);
    }

    public String getMake() {
        return make.get();
    }

    public StringProperty makeProperty() {
        return make;
    }

    public String getModel() {
        return model.get();
    }

    public StringProperty modelProperty() {
        return model;
    }

    public String getVinNumber() {
        return vinNumber.get();
    }

    public StringProperty vinNumberProperty() {
        return vinNumber;
    }

    public String getProblem() {
        return problem.get();
    }

    public StringProperty problemProperty() {
        return problem;
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public ObservableValue<String> vinProperty() {
        return vinNumber;
    }
}
