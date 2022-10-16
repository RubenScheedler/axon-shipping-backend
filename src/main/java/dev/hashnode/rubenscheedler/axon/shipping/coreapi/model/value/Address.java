package dev.hashnode.rubenscheedler.axon.shipping.coreapi.model.value;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Address {
    @NonNull
    String zipCode;
    int houseNumber;
    String houseNumberAddition;
}
