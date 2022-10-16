package dev.hashnode.rubenscheedler.axon.shipping.command;

import dev.hashnode.rubenscheedler.axon.shipping.coreapi.command.CancelShipmentCommand;
import dev.hashnode.rubenscheedler.axon.shipping.coreapi.command.CreateShipmentCommand;
import dev.hashnode.rubenscheedler.axon.shipping.coreapi.command.UpdateDeliveryMomentCommand;
import dev.hashnode.rubenscheedler.axon.shipping.coreapi.event.DeliveryMomentUpdatedEvent;
import dev.hashnode.rubenscheedler.axon.shipping.coreapi.event.ShipmentCanceledEvent;
import dev.hashnode.rubenscheedler.axon.shipping.coreapi.event.ShipmentCreatedEvent;
import dev.hashnode.rubenscheedler.axon.shipping.coreapi.model.value.Address;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Aggregate
@Data
@NoArgsConstructor // Required by Axon
public class Shipment {

    @AggregateIdentifier
    private UUID id;
    private Address deliveryAddress;
    private Instant deliveryMoment;
    private String recipientName;
    private List<UUID> productIds;
    private boolean canceled;

    /**
     * Executes the create command and publishes an event about it.
     */
    @CommandHandler
    public Shipment(CreateShipmentCommand command) {
        UUID shipmentId = UUID.randomUUID();
        // Register a create event on this new aggregate
        AggregateLifecycle.apply(ShipmentCreatedEvent.builder()
                .shipmentId(shipmentId)
                .deliveryAddress(command.getDeliveryAddress())
                .deliveryMoment(command.getDeliveryMoment())
                .recipientName(command.getRecipientName())
                .productIds(command.getProductIds())
                .build()
        );
    }

    /**
     * Executes the update delivery moment command and publishes an event about it.
     */
    @CommandHandler
    public void handle(UpdateDeliveryMomentCommand command) {
        AggregateLifecycle.apply(
                DeliveryMomentUpdatedEvent.builder()
                        .shipmentId(command.getShipmentId())
                        .deliveryMoment(command.getDeliveryMoment())
                        .build()
        );
    }

    /**
     * Executes the cancel shipment and publishes an event about it.
     */
    @CommandHandler
    public void handle(CancelShipmentCommand command) {
        AggregateLifecycle.apply(
                ShipmentCanceledEvent.builder()
                        .shipmentId(command.getShipmentId())
                        .build()
        );
    }

    /**
     * Updates the aggregate state, by setting the aggregate's ID.
     */
    @EventSourcingHandler
    public void on(ShipmentCreatedEvent event) {
        id = event.getShipmentId();
        deliveryAddress = event.getDeliveryAddress();
        deliveryMoment = event.getDeliveryMoment();
        recipientName = event.getRecipientName();
        productIds = event.getProductIds();
    }

    /**
     * Updates the moment on which the shipment should be delivered.
     */
    @EventSourcingHandler
    public void on(DeliveryMomentUpdatedEvent event) {
        deliveryMoment = event.getDeliveryMoment();
    }

    /**
     * Cancels the shipment, without changing other parts of its state.
     */
    @EventSourcingHandler
    public void on(ShipmentCanceledEvent event) {
        canceled = true;
    }
}
