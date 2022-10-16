package dev.hashnode.rubenscheedler.axon.shipping.coreapi.command;

import dev.hashnode.rubenscheedler.axon.shipping.coreapi.model.value.Address;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class CreateShipmentCommand {
    @NonNull
    Address deliveryAddress;
    @NonNull
    String recipientName;
    @NonNull
    Instant deliveryMoment;
    @NonNull
    List<UUID> productIds;
}
