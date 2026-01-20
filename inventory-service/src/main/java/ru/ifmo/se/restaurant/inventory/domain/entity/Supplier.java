package ru.ifmo.se.restaurant.inventory.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Supplier {
    private final Long id;
    private final String name;
    private final String contactPerson;
    private final String phone;
    private final String email;
    private final String address;

    public Supplier updateInfo(String newName, String newContactPerson, String newPhone,
                                String newEmail, String newAddress) {
        return new Supplier(
            id,
            newName != null ? newName : name,
            newContactPerson != null ? newContactPerson : contactPerson,
            newPhone != null ? newPhone : phone,
            newEmail != null ? newEmail : email,
            newAddress != null ? newAddress : address
        );
    }
}
