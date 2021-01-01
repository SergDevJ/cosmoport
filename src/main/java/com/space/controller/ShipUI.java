package com.space.controller;


import com.space.model.Ship;
import com.space.model.ShipType;

import java.util.Objects;

class ShipUI {
    public Long id;
    public String name;
    public String planet;
    public ShipType shipType;
    public Long prodDate;
    public Boolean isUsed;
    public Double speed;
    public Integer crewSize;
    public Double rating;

    public ShipUI() {}

    public ShipUI(Ship ship) {
        Objects.requireNonNull(ship);
        this.name = ship.getName();
        this.planet = ship.getPlanet();
        this.shipType = ship.getShipType();
        this.prodDate = ship.getProdDate().getTime();
        this.isUsed = ship.getIsUsed();
        this.speed = ship.getSpeed();
        this.crewSize = ship.getCrewSize();
        this.rating = ship.getRating();
        this.id = ship.getId();
    }

    @Override
    public String toString() {
        return "ShipUI{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", planet='" + planet + '\'' +
                ", shipType=" + shipType +
                ", prodDate=" + prodDate +
                ", isUsed=" + isUsed +
                ", speed=" + speed +
                ", crewSize=" + crewSize +
                ", rating=" + rating +
                '}';
    }
}
