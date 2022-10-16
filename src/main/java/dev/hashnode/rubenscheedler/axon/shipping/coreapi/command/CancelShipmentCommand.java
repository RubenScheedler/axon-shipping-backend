package dev.hashnode.rubenscheedler.axon.shipping.coreapi.command;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Value
@Builder
public class CancelShipmentCommand {
    @NonNull
    @TargetAggregateIdentifier
    UUID shipmentId;
}
