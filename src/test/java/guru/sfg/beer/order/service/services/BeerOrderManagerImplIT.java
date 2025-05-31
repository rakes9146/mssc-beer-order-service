package guru.sfg.beer.order.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import guru.sfg.beer.order.service.services.beer.BeerServiceImpl;
import guru.sfg.beer.order.service.sfg.brewery.model.BeerDto;
import guru.sfg.beer.order.service.sfg.brewery.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@ExtendWith(WireMockExtension.class)
@SpringBootTest
public class BeerOrderManagerImplIT {

    @Autowired
    BeerOrderManager beerOrderManager;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    CustomerRepository customerRepository;

    Customer testCustomer;

    UUID beerId = UUID.randomUUID();

    @Autowired
    WireMockServer wireMockServer;

    @Autowired
    ObjectMapper objectMapper;

    @TestConfiguration
    static class  RestTemplateBuilderProvider{
        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer(){
            WireMockServer server = with(wireMockConfig().port(8086));
            server.start();
            return server;
        }
    }
    @BeforeEach
    void setUp(){
        testCustomer = customerRepository.save(
                Customer.builder()
                        .customerName("Test Customer")
                        .build());
    }

    @Test
    void testNewToAllocate() throws JsonProcessingException, InterruptedException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("0631234200036").build();
        BeerPagedList list = new BeerPagedList(Arrays.asList(beerDto));
        wireMockServer.stubFor(get(BeerServiceImpl.BEER_PATH_V1+"0631234200036")
                .willReturn(okJson(objectMapper.writeValueAsString(list))));
        BeerOrder beerOrder = createBeerOrder();
        BeerOrder saveNewBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(()->{
            BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            //todo - allocated status
            assertEquals(BeerOrderStatusEnum.ALLOCATED , foundOrder.getOrderStatus());
         });

        await().untilAsserted(()->{

            BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            BeerOrderLine lines = foundOrder.getBeerOrderLines().iterator().next();
            assertEquals(lines.getOrderQuantity(), lines.getQuantityAllocated());

        });


        BeerOrder savedBeerOrder2 = beerOrderRepository.findById(saveNewBeerOrder.getId()).get();
        assertNotNull(saveNewBeerOrder);
        assertEquals(BeerOrderStatusEnum.ALLOCATED, saveNewBeerOrder.getOrderStatus());

    }

    public BeerOrder createBeerOrder(){

        BeerOrder beerOrder = BeerOrder.builder()
                .customer(testCustomer)
                .build();

        Set<BeerOrderLine> lines = new HashSet<>();
        lines.add(BeerOrderLine.builder()
                .beerId(beerId)
                .orderQuantity(1)
                        .upc("12345")
                .beerOrder(beerOrder)
                .build());
        beerOrder.setBeerOrderLines(lines);
        return beerOrder;
    }

}