package dev.hashnode.rubenscheedler.axon.shipping.command;

import dev.hashnode.rubenscheedler.axon.shipping.coreapi.command.CancelShipmentCommand;
import dev.hashnode.rubenscheedler.axon.shipping.coreapi.command.CreateShipmentCommand;
import dev.hashnode.rubenscheedler.axon.shipping.coreapi.command.UpdateDeliveryMomentCommand;
import dev.hashnode.rubenscheedler.axon.shipping.coreapi.event.DeliveryMomentUpdatedEvent;
import dev.hashnode.rubenscheedler.axon.shipping.coreapi.event.ShipmentCanceledEvent;
import dev.hashnode.rubenscheedler.axon.shipping.coreapi.event.ShipmentCreatedEvent;
import dev.hashnode.rubenscheedler.axon.shipping.coreapi.model.value.Address;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.axonframework.test.matchers.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ShipmentTest {
    private FixtureConfiguration<Shipment> fixture;
    private final Address deliveryAddress = Address.builder().zipCode("1234AB").houseNumber(3).build();

    @BeforeEach
    public void setUp() {
        fixture = new AggregateTestFixture<>(Shipment.class);
    }

    @Test
    void createShipmentCommand_createsEvent() {
        CreateShipmentCommand command = CreateShipmentCommand.builder()
                .deliveryAddress(deliveryAddress)
                .deliveryMoment(Instant.now())
                .productIds(List.of(UUID.randomUUID()))
                .recipientName("John Diamond")
                .build();
        fixture
                .given()
                .when(command)
                .expectEventsMatching(predicate(events -> events.size() == 1))
                .expectEventsMatching(exactSequenceOf(messageWithPayload(matches(e ->
                        ((ShipmentCreatedEvent)e).getDeliveryAddress().equals(command.getDeliveryAddress())
                        && ((ShipmentCreatedEvent)e).getDeliveryMoment().equals(command.getDeliveryMoment())
                        && ((ShipmentCreatedEvent)e).getRecipientName().equals(command.getRecipientName())
                        && ((ShipmentCreatedEvent)e).getProductIds().equals(command.getProductIds())))));
    }

    @Test
    void updateDeliveryMomentCommand_createsEvent() {
        UpdateDeliveryMomentCommand command = UpdateDeliveryMomentCommand.builder()
                .shipmentId(UUID.randomUUID())
                .deliveryMoment(Instant.now())
                .build();

        fixture
                .given(ShipmentCreatedEvent.builder()
                        .shipmentId(command.getShipmentId())
                        .deliveryAddress(deliveryAddress)
                        .deliveryMoment(Instant.now().minus(1, ChronoUnit.DAYS))
                        .productIds(List.of(UUID.randomUUID()))
                        .recipientName("John Diamond")
                        .build())
                .when(command)
                .expectEvents(DeliveryMomentUpdatedEvent.builder()
                        .shipmentId(command.getShipmentId())
                        .deliveryMoment(command.getDeliveryMoment())
                        .build()
                );
    }


    @Test
    void cancelShipmentCommand_createsEvent() {
        CancelShipmentCommand command = CancelShipmentCommand.builder()
                .shipmentId(UUID.randomUUID())
                .build();

        fixture
                .given(ShipmentCreatedEvent.builder()
                        .shipmentId(command.getShipmentId())
                        .deliveryAddress(deliveryAddress)
                        .deliveryMoment(Instant.now().minus(1, ChronoUnit.DAYS))
                        .productIds(List.of(UUID.randomUUID()))
                        .recipientName("John Diamond")
                        .build())
                .when(command)
                .expectEvents(ShipmentCanceledEvent.builder()
                        .shipmentId(command.getShipmentId())
                        .build()
                );
    }

    @Test
    void shipmentCreatedEvent_setsShipmentDetailsFromEvent() {
        // GIVEN an aggregate and a shipmentCreatedEvent
        ShipmentCreatedEvent event = ShipmentCreatedEvent.builder()
                .shipmentId(UUID.randomUUID())
                .deliveryAddress(deliveryAddress)
                .deliveryMoment(Instant.now().minus(1, ChronoUnit.DAYS))
                .productIds(List.of(UUID.randomUUID()))
                .recipientName("John Diamond")
                .build();
        Shipment aggregate = new Shipment();

        // WHEN the aggregate is asked to handle that event
        aggregate.on(event);

        // THEN I expect that the state of the aggregate matches the fields of the event
        assertThat(aggregate.getId(), is(event.getShipmentId()));
        assertThat(aggregate.getDeliveryAddress(), is(event.getDeliveryAddress()));
        assertThat(aggregate.getDeliveryMoment(), is(event.getDeliveryMoment()));
        assertThat(aggregate.getRecipientName(), is(event.getRecipientName()));
        assertThat(aggregate.getProductIds(), is(event.getProductIds()));
        // Newly created shipment should not immediately be canceled:
        assertThat(aggregate.isCanceled(), is(false));

    }

    @Test
    void deliveryMomentUpdatedEvent_updatesDeliveryMoment() {
        // GIVEN an aggregate and a deliveryMomentUpdatedEvent
        ShipmentCreatedEvent createEvent = ShipmentCreatedEvent.builder()
                .shipmentId(UUID.randomUUID())
                .deliveryAddress(deliveryAddress)
                .deliveryMoment(Instant.now().minus(1, ChronoUnit.DAYS))
                .productIds(List.of(UUID.randomUUID()))
                .recipientName("John Diamond")
                .build();
        Shipment aggregate = new Shipment();
        aggregate.on(createEvent);

        DeliveryMomentUpdatedEvent event = DeliveryMomentUpdatedEvent.builder()
                .shipmentId(createEvent.getShipmentId())
                .deliveryMoment(Instant.now())
                .build();

        // WHEN the aggregate is asked to handle that event
        aggregate.on(event);

        // THEN I expect that only the delivery moment is updated
        assertThat(aggregate.getId(), is(event.getShipmentId()));
        assertThat(aggregate.getDeliveryAddress(), is(createEvent.getDeliveryAddress()));
        assertThat(aggregate.getDeliveryMoment(), is(event.getDeliveryMoment()));
        assertThat(aggregate.getRecipientName(), is(createEvent.getRecipientName()));
        assertThat(aggregate.getProductIds(), is(createEvent.getProductIds()));
        assertThat(aggregate.isCanceled(), is(false));
    }


    @Test
    void shipmentCanceledEvent_cancelsShipment() {
        // GIVEN an aggregate and a shipmentCanceledEvent
        ShipmentCreatedEvent createEvent = ShipmentCreatedEvent.builder()
                .shipmentId(UUID.randomUUID())
                .deliveryAddress(deliveryAddress)
                .deliveryMoment(Instant.now().minus(1, ChronoUnit.DAYS))
                .productIds(List.of(UUID.randomUUID()))
                .recipientName("John Diamond")
                .build();
        Shipment aggregate = new Shipment();
        aggregate.on(createEvent);

        ShipmentCanceledEvent event = ShipmentCanceledEvent.builder()
                .shipmentId(createEvent.getShipmentId())
                .build();

        // WHEN the aggregate is asked to handle that event
        aggregate.on(event);

        // THEN I expect that the aggregate is canceled
        assertThat(aggregate.getId(), is(event.getShipmentId()));
        assertThat(aggregate.getDeliveryAddress(), is(createEvent.getDeliveryAddress()));
        assertThat(aggregate.getDeliveryMoment(), is(createEvent.getDeliveryMoment()));
        assertThat(aggregate.getRecipientName(), is(createEvent.getRecipientName()));
        assertThat(aggregate.getProductIds(), is(createEvent.getProductIds()));
        assertThat(aggregate.isCanceled(), is(true));
    }
}
