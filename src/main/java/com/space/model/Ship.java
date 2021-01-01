package com.space.model;


import com.fasterxml.jackson.annotation.JsonAutoDetect;

import javax.persistence.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;


@Entity
@Table(name = "ship")
public class Ship implements Serializable {
    private Long id;
    private String name;
    private String planet;
    private ShipType shipType;
    private Date prodDate;

    public Boolean isUsed;

    private Double speed;
    private Integer crewSize;
    private Double rating;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    public Long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "planet")
    public String getPlanet() {
        return planet;
    }

    public void setPlanet(String planet) {
        this.planet = planet;
    }

    @Column(name = "shipType")
    @Enumerated(EnumType.STRING)
    public ShipType getShipType() {
        return shipType;
    }
    public void setShipType(ShipType shipType) {
        this.shipType = shipType;
    }

    @Column(name = "prodDate")
    @Temporal(TemporalType.DATE)
    public Date getProdDate() {
        return prodDate;
    }
    public void setProdDate(Date prodDate) {
        this.prodDate = prodDate;
    }
    public void setProdDateInMillis(long millis) {
        this.prodDate.setTime(millis);
    }

    @Column(name = "isUsed")
    public Boolean getIsUsed() {
        return isUsed;
    }
    public void setIsUsed(boolean isUsed) {
        this.isUsed = isUsed;
    }

    @Column(name = "speed")
    public Double getSpeed() {
        return speed;
    }
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Column(name = "crewSize")
    public Integer getCrewSize() {
        return crewSize;
    }
    public void setCrewSize(int crewSize) {
        this.crewSize = crewSize;
    }

    @Column(name = "rating")
    public Double getRating() {
        return rating;
    }
    public void setRating(double rating) {
        this.rating = rating;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ship ship = (Ship) o;
        return Objects.equals(id, ship.id) &&
                name.equals(ship.name) &&
                planet.equals(ship.planet) &&
                shipType == ship.shipType &&
                prodDate.getYear() == ship.prodDate.getYear() &&
                Objects.equals(isUsed, ship.isUsed) &&
                Objects.equals(speed, ship.speed) &&
                Objects.equals(crewSize, ship.crewSize) &&
                Objects.equals(rating, ship.rating);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, name, planet, shipType, prodDate.getYear(), isUsed, speed, crewSize, rating);
    }

    @Override
    public String toString() {
        return "Ship{" +
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
