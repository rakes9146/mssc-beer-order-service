package guru.sfg.beer.order.service.sfg.brewery.model.events;

import guru.sfg.beer.order.service.sfg.brewery.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocateOrderRequest {

    private BeerOrderDto beerOrderDto;
    private boolean pendingInventory;
    private boolean allocateError;

}
