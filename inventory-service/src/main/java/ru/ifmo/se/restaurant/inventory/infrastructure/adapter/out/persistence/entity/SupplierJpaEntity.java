package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.ifmo.se.restaurant.inventory.domain.entity.Supplier;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 100)
    private String contactPerson;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 200)
    private String address;

    public static SupplierJpaEntity fromDomain(Supplier domain) {
        return SupplierJpaEntity.builder()
            .id(domain.getId())
            .name(domain.getName())
            .contactPerson(domain.getContactPerson())
            .phone(domain.getPhone())
            .email(domain.getEmail())
            .address(domain.getAddress())
            .build();
    }

    public Supplier toDomain() {
        return Supplier.builder()
            .id(id)
            .name(name)
            .contactPerson(contactPerson)
            .phone(phone)
            .email(email)
            .address(address)
            .build();
    }
}
