package com.space.controller;

import com.space.model.Ship;
import com.space.service.ShipService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.space.util.ShipUtil.*;


@Controller
@RequestMapping(value = "/")
public class ShipController {
    @PersistenceContext
    private EntityManager em;
    @Autowired
    ShipService shipService;
    @Autowired
    Logger logger;


    private Ship findShip(long id) throws IllegalArgumentException, NotFoundException {
        if (id <= 0) throw new IllegalArgumentException("Invalid ship id: " + id);
        Ship ship = shipService.findById(id);
        if (Objects.isNull(ship)) throw new NotFoundException("Ship not found (id: " + id + ")");
        return ship;
    }

    private Ship findShip(String ids) throws IllegalArgumentException {
        long id;
        try {
            id = Long.parseLong(ids);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ship id: " + ids, e);
        }
        return findShip(id);
    }


    @RequestMapping(value = "/rest/ships/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Integer> deleteShip(@PathVariable(value = "id") Long id)
    {
        logger.trace("Execute ShipController.deleteShip(id: " + id + ")");
        Ship ship;
        try {
            ship = findShip(id);
        } catch (IllegalArgumentException e) {
            logger.warn("Error deleting ship. Invalid id: " + id);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            logger.warn("Error deleting ship with id: " + id + " - not found.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        shipService.delete(ship);
        logger.info("Ship deleted successfully with id: " + id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/rest/ships/{id}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<ShipUI> updateShip(@PathVariable(value = "id") String id,
                                             @RequestBody ShipUI shipUI) {
        if (logger.isTraceEnabled()) {
            logger.trace("Execute ShipController.updateShip(id: " + id + ", data: " + shipUI + ")");
        }

        Ship ship;
        try {
            ship = findShip(id);
        } catch (IllegalArgumentException e) {
            logger.error("Error updating ship. Invalid id: " + id);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            logger.error("Error updating ship with id: " + id + " - not found.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (Objects.isNull(shipUI)) {
            shipUI = new ShipUI(ship);
            return new ResponseEntity<>(shipUI, HttpStatus.OK);
        }

        try {
            checkInputData(shipUI, false);
        }
        catch (EmptyFieldsException e) {
            logger.info("Ship info is not updated (all fields are null)");
            shipUI = new ShipUI(ship);
            return new ResponseEntity<>(shipUI, HttpStatus.OK);
        }
        catch (IllegalArgumentException e) {
            logger.error("Error updating ship - invalid data: " + e.getMessage() +
                    " .Request body data: " + shipUI);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!Objects.isNull(shipUI.name)) ship.setName(shipUI.name);
        if (!Objects.isNull(shipUI.planet)) ship.setPlanet(shipUI.planet);
        if (!Objects.isNull(shipUI.shipType)) ship.setShipType(shipUI.shipType);
        if (!Objects.isNull(shipUI.prodDate)) ship.setProdDate(getFirstDayOfYear(shipUI.prodDate));
        if (!Objects.isNull(shipUI.isUsed)) ship.setIsUsed(shipUI.isUsed);
        if (!Objects.isNull(shipUI.speed)) ship.setSpeed(shipUI.speed);
        if (!Objects.isNull(shipUI.crewSize)) ship.setCrewSize(shipUI.crewSize);
        ship.setRating(calculateRating(ship.getSpeed(), ship.getProdDate(), ship.getIsUsed()));
        shipService.save(ship);
        shipUI = new ShipUI(ship);
        logger.info("Ship updated successfully with info: " + shipUI + " (id: " + id + ")");
        return new ResponseEntity<>(shipUI, HttpStatus.OK);
    }


    @RequestMapping(value = "/rest/ships/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<ShipUI> getShip(@PathVariable(value = "id") String id) {
        if (logger.isTraceEnabled()) {
            logger.trace("Execute ShipController.getShip(id: " + id + ")");
        }
        Ship ship;
        try {
            ship = findShip(id);
        } catch (IllegalArgumentException e) {
            logger.error("Error getting ship. Invalid id: " + id);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            logger.info("Unable to get ship with id: " + id + " - not found.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ShipUI shipUI = new ShipUI(ship);
        logger.info("Ship retrieved successfully with info: " + shipUI + " (id: " + id + ")");
        return new ResponseEntity<>(shipUI, HttpStatus.OK);
    }

    private void checkInputData(ShipUI ship, boolean createMode) throws EmptyFieldsException, IllegalArgumentException {
        final int MIN_PRODDATE_YEAR = 2800;
        final int MAX_PRODDATE_YEAR = 3019;
        final double MIN_SPEED = 0.01;
        final double MAX_SPEED = 0.99;
        final int MIN_CREW_SIZE = 1;
        final int MAX_CREW_SIZE = 9999;
        if (Objects.isNull(ship)) throw new IllegalArgumentException("Incorrect value for field");
        if ((Objects.isNull(ship.name) || Objects.isNull(ship.planet) ||
                Objects.isNull(ship.shipType) || Objects.isNull(ship.speed) ||
                Objects.isNull(ship.crewSize) || (Objects.isNull(ship.prodDate)))
                && createMode) {
            throw new EmptyFieldsException("At least one of the field is null");
        }

        if (Objects.isNull(ship.name) && Objects.isNull(ship.planet) &&
                Objects.isNull(ship.shipType) && Objects.isNull(ship.speed) &&
                Objects.isNull(ship.crewSize) && (Objects.isNull(ship.prodDate))) {
            throw new EmptyFieldsException("All fields are null");
        }

        if (!Objects.isNull(ship.name)) {
            if (ship.name.length() > 50 || ship.name.isEmpty())
                throw new IllegalArgumentException("Incorrect value for field 'name': \"" + ship.name + "\"");
        }

        if (!Objects.isNull(ship.planet)) {
            if (ship.planet.length() > 50 || ship.planet.isEmpty())
                throw new IllegalArgumentException("Incorrect value for field 'planet': \"" + ship.planet + "\"");
        }

        if (!Objects.isNull(ship.prodDate)) {
            if (ship.prodDate < 0)
                throw new IllegalArgumentException("Incorrect value for field 'prodDate': less then 0");
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(ship.prodDate);
            cal.get(Calendar.YEAR);
            if (cal.get(Calendar.YEAR) < MIN_PRODDATE_YEAR || cal.get(Calendar.YEAR) > MAX_PRODDATE_YEAR)
                throw new IllegalArgumentException("Incorrect value for field 'prodDate': " + cal);
        }

        if (!Objects.isNull(ship.speed)) {
            ship.speed = Math.round(ship.speed * 100) / 100.0;
            if (ship.speed < MIN_SPEED || ship.speed > MAX_SPEED)
                throw new IllegalArgumentException("Incorrect value for field 'speed': " + ship.speed);
        }

        if (!Objects.isNull(ship.crewSize)) {
            if (ship.crewSize < MIN_CREW_SIZE || ship.crewSize > MAX_CREW_SIZE)
                throw new IllegalArgumentException("Incorrect value for field 'crewSize': " + ship.crewSize);
        }
    }


    @RequestMapping(value = "/rest/ships", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<ShipUI> createShip(@RequestBody ShipUI shipUI) {
        if (logger.isTraceEnabled()) {
            logger.trace("Execute ShipController.create(" + shipUI + ")");
        }
        try {
            checkInputData(shipUI, true);
        } catch(EmptyFieldsException | IllegalArgumentException e) {
            logger.error("Error creating new ship - invalid data: " + e.getMessage() +
                    " .Request body data: " + shipUI);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (shipUI.isUsed == null) shipUI.isUsed = Boolean.FALSE;

        Ship ship = new Ship();
        ship.setName(shipUI.name);
        ship.setPlanet(shipUI.planet);
        ship.setShipType(shipUI.shipType);
        ship.setProdDate(getFirstDayOfYear(shipUI.prodDate));
        ship.setIsUsed(shipUI.isUsed);
        ship.setSpeed(shipUI.speed);
        ship.setCrewSize(shipUI.crewSize);
        ship.setRating(calculateRating(ship.getSpeed(), ship.getProdDate(), ship.getIsUsed()));
        ship = shipService.save(ship);
        shipUI = new ShipUI(ship);
        logger.info("Ship created successfully with info: " + ship);
        return new ResponseEntity<>(shipUI, HttpStatus.OK);
    }

    private List<Predicate> getPredicates(CriteriaBuilder cb, Root<Ship> root,
            String name, String planet, String shipType, Long after, Long before,
                                  Boolean isUsed, Double minSpeed, Double maxSpeed,
                                  Integer minCrewSize, Integer maxCrewSize,
                                  Double minRating, Double maxRating) {

        List<Predicate> prs = new LinkedList<>();

        if (!Objects.isNull(name)) {
            prs.add(cb.like(root.get("name"), "%" + name + "%"));
        }

        if (!Objects.isNull(planet)) {
            prs.add(cb.like(root.get("planet"), "%" + planet + "%"));
        }

        if (!Objects.isNull(shipType)) {
            prs.add(cb.like(root.get("shipType").as(String.class), shipType));
        }

        if (!Objects.isNull(after)) {
            Date d = new Date();
            d.setTime(after);
            prs.add(cb.greaterThanOrEqualTo(root.get("prodDate"), d));
        }
        if (!Objects.isNull(before)) {
            Date d = new Date();
            d.setTime(before);
            prs.add(cb.lessThanOrEqualTo(root.get("prodDate"), d));
//            prs.add(cb.lessThanOrEqualTo(cb.function("year", Integer.class, root.get("prodDate")),
//                    getYearOfMillis(before)));
        }


        if (!Objects.isNull(minSpeed)) {
            prs.add(cb.greaterThanOrEqualTo(root.get("speed"), minSpeed));
        }
        if (!Objects.isNull(maxSpeed)) {
            prs.add(cb.lessThanOrEqualTo(root.get("speed"), maxSpeed));
        }

        if (!Objects.isNull(minCrewSize)) {
            prs.add(cb.greaterThanOrEqualTo(root.get("crewSize"), minCrewSize));
        }
        if (!Objects.isNull(maxCrewSize)) {
            prs.add(cb.lessThanOrEqualTo(root.get("crewSize"), maxCrewSize));
        }

        if (!Objects.isNull(minRating)) {
            prs.add(cb.greaterThanOrEqualTo(root.get("rating"), minRating));
        }
        if (!Objects.isNull(maxRating)) {
            prs.add(cb.lessThanOrEqualTo(root.get("rating"), maxRating));
        }

        if (!Objects.isNull(isUsed)) {
            if (isUsed) prs.add(cb.isTrue(root.get("isUsed")));
            else prs.add(cb.isFalse(root.get("isUsed")));
        }

        return prs;
    }


    @RequestMapping(value = "/rest/ships", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Ship> listShip(@RequestParam(value = "name", required = false) String name,
                               @RequestParam(value = "planet", required = false) String planet,
                               @RequestParam(value = "shipType", required = false) String shipType,
                               @RequestParam(value = "after", required = false) Long after,
                               @RequestParam(value = "before", required = false) Long before,
                               @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                               @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                               @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                               @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                               @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                               @RequestParam(value = "minRating", required = false) Double minRating,
                               @RequestParam(value = "maxRating", required = false) Double maxRating,
                               @RequestParam(value = "order", required = false) String order,
                               @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                               @RequestParam(value = "pageSize", required = false) Integer pageSize,
                               HttpServletRequest request) {


        if (logger.isTraceEnabled()) {
            logger.trace("Execute ShipController.listShip(" + request.getQueryString() + ")");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Ship list URL params: " + request.getQueryString());
        }

        if (Objects.isNull(pageNumber)) pageNumber = 0;
        if (Objects.isNull(pageSize) || pageSize == 0) pageSize = 3;

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Ship> listCriteriaQuery = cb.createQuery(Ship.class);
        Root<Ship> root = listCriteriaQuery.from(Ship.class);
        root.alias("ship_");
        List<Predicate> prs = getPredicates(cb, root, name, planet, shipType, after, before,
                isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);

        if(prs.size() > 0) {
             listCriteriaQuery.where(cb.and(prs.toArray(new Predicate[0])));
        }

        if (!Objects.isNull(order)) {
            try {
                listCriteriaQuery.orderBy(cb.asc(root.get(ShipOrder.valueOf(order).getFieldName())));
            } catch (IllegalArgumentException e) {
                listCriteriaQuery.orderBy(cb.asc(root.get("id")));
            }
        }
        else listCriteriaQuery.orderBy(cb.asc(root.get("id")));

        TypedQuery<Ship> listQuery = em.createQuery(listCriteriaQuery);
        listQuery.setFirstResult(pageNumber * pageSize).setMaxResults(pageSize);
        return listQuery.getResultList();
}


    @RequestMapping(value = "/rest/ships/count", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public long getCount(@RequestParam(value = "name", required = false) String name,
                         @RequestParam(value = "planet", required = false) String planet,
                         @RequestParam(value = "shipType", required = false) String shipType,
                         @RequestParam(value = "after", required = false) Long after,
                         @RequestParam(value = "before", required = false) Long before,
                         @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                         @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                         @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                         @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                         @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                         @RequestParam(value = "minRating", required = false) Double minRating,
                         @RequestParam(value = "maxRating", required = false) Double maxRating) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countCriteriaQuery = cb.createQuery(Long.class);
        Root<Ship> root = countCriteriaQuery.from(Ship.class);
        root.alias("ship_");
        List<Predicate> prs = getPredicates(cb, root, name, planet, shipType, after, before,
                isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);
        countCriteriaQuery.select(cb.count(root.get("id")).alias("id"));

        if(prs.size() > 0) {
            countCriteriaQuery.where(cb.and(prs.toArray(new Predicate[0])));
        }

        TypedQuery<Long> countQuery = em.createQuery(countCriteriaQuery);
        return countQuery.getSingleResult();
    }

}

