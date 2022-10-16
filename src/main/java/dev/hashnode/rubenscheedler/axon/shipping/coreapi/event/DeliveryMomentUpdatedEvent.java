package dev.hashnode.rubenscheedler.axon.shipping.coreapi.event;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class DeliveryMomentUpdatedEvent {
    @NonNull
    @TargetAggregateIdentifier
    UUID shipmentId;
    @NonNull
    Instant deliveryMoment;
}
