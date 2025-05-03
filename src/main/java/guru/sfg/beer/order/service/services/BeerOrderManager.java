package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.sfg.brewery.model.BeerOrderDto;

import java.util.UUID;

public interface BeerOrderManager {

    public BeerOrder newBeerOrder(BeerOrder beerOrder);

    void processValidationResult(UUID beerOrderId, Boolean isValid);

    void beerOrderAllocationPassed(BeerOrderDto beerOrder);

    void beerOrderAllocationPendingInventory(BeerOrderDto beerOrder);

    void beerOrderAllocationFailed(BeerOrderDto beerOrder);

}
