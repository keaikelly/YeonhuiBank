package com.db.bank.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "user",
        indexes = {
                @Index(name = "idx_user_userId", columnList = "userId")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_userId", columnNames = "userId")
        }
)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String userId;

    @Column(nullable = false, length = 50)
    private String password;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // User : Account = 1 : N
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Account> accounts;




}
