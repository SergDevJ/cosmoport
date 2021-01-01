package com.space.service;

import com.space.model.Ship;


public interface ShipService {
    Ship findById(Long id);
    Ship save(Ship ship);
    void delete(Ship ship);
}
